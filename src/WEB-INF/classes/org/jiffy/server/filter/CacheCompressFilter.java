package org.jiffy.server.filter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

@WebFilter(filterName = "CompressCacheFilter", displayName = "CompressCacheFilter", urlPatterns = { "*.html", "*.js", "*.css" })
public class CacheCompressFilter extends CacheFilter
{
	Map<String, Object> fileWriteLocks = new HashMap<String, Object>();
	/**
	 * Indicates whether to use Dojo ShrinkSafe JS compression
	 */
	boolean shrinkJS = false;
	/**
	 * Indicates whether to keep a local copy of the compressed data
	 */
	boolean cacheCompressedData = true;
	public final static String DEFAULT_CACHE_FOLDER = "cache";
	/**
	 * Name of the directory to use for cached compressed data
	 */
	String cacheFolderName;

	/**
	 * Filter handling mechanism to intercept requests and see if they have new content and do the compression
	 */
	@Override
	public void doFilter(ServletRequest genericRequest, ServletResponse genericResponse, FilterChain chain) throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest) genericRequest;
		HttpServletResponse response = (HttpServletResponse) genericResponse;
		setCacheHeader(response);
		String acceptEncoding = request.getHeader("Accept-Encoding");
		if (acceptEncoding != null && acceptEncoding.indexOf("gzip") != -1)
		{ // make sure the browser can handle gzip
			String filepath = getCachedGzipPath(request);
			File gzip = new File(filepath);
			long lastCompressed = -1;
			if (cacheCompressedData && gzip.exists())
				lastCompressed = gzip.lastModified();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			long ifModifiedSince = request.getDateHeader("If-Modified-Since");
			OutputStream responseOutput = new GZIPOutputStream(baos);
			// intercept the response so we can handle it
			StatusMonitorResponseWrapper responseWrapper = new StatusMonitorResponseWrapper(response, responseOutput);
			// let the other filters and servlet do it's thing
			chain.doFilter(new RequestModifiedSinceWrapper(request, lastCompressed), responseWrapper);
			int status = responseWrapper.getStatus();
			if (!cacheCompressedData || (status < HttpServletResponse.SC_MULTIPLE_CHOICES) || (status >= HttpServletResponse.SC_BAD_REQUEST))
			{
				// allow anything but exclude 300 codes
				// This is indicates fresh/new content
				response.setHeader("Content-Encoding", "gzip");
				// this needs to be set to work righ twith proxies (and apache)
				response.setHeader("Vary", "Accept-Encoding");
				responseWrapper.flush();
				responseOutput.close();
				byte[] bytes = baos.toByteArray();
				if (cacheCompressedData)
				{
					File cacheFolder = new File(request.getRealPath(cacheFolderName));
					if (!cacheFolder.exists())
						cacheFolder.mkdir();
					boolean hasLock = false;
					while (!hasLock)
					{
						// blocking mechanism to prevent multiple writes at the same time
						synchronized (fileWriteLocks)
						{
							if (fileWriteLocks.get(filepath) == null)
							{
								hasLock = true;
								fileWriteLocks.put(filepath, new Object());
							}
						}
						if (!hasLock)
							try
							{
								Thread.sleep(100);
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
							}

					}
					FileOutputStream gzipOut = new FileOutputStream(gzip);
					try
					{
						gzipOut.write(bytes); // write out the compressed gzip file
					}
					finally
					{
						synchronized (fileWriteLocks)
						{
							fileWriteLocks.remove(filepath);
						}
						gzipOut.close();
					}
				}
				String mimeType = servletContext.getMimeType(request.getRequestURI());
				if (mimeType != null)
					response.setContentType(mimeType);
				response.setContentLength(bytes.length);
				OutputStream os = response.getOutputStream();
				os.write(bytes);
				os.flush();
				os.close();
			}
			else if (status == 304)
			{
				// this indicates that the cached gzip file is the latest
				// version and we can send the our compressed copy
				if (ifModifiedSince < (lastCompressed / 1000L) * 1000L)
				{
					response.setHeader("Content-Encoding", "gzip");
					// this needs to be set to work right with proxies (and apache)
					response.setHeader("Vary", "Accept-Encoding");
					if (!response.containsHeader("Last-Modified") && lastCompressed >= 0L)
						response.setDateHeader("Last-Modified", lastCompressed);
					response.setStatus(200);
					boolean hasClearance = false;
					while (!hasClearance)
					{
						// don't read the file if it is being written right now
						synchronized (fileWriteLocks)
						{
							if (fileWriteLocks.get(filepath) == null)
							{
								hasClearance = true;
							}
						}
						if (!hasClearance)
							try
							{
								Thread.sleep(100);
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
							}

					}
					String mimeType = servletContext.getMimeType(request.getRequestURI());
					if (mimeType != null)
						response.setContentType(mimeType);
					response.setContentLength((int) gzip.length());
					FileInputStream gzipInputStream = new FileInputStream(gzip);
					// send our compressed copy
					OutputStream os = response.getOutputStream();
					byte[] b = new byte[4096];
					for (int n; (n = gzipInputStream.read(b)) != -1;)
					{
						os.write(b, 0, n);
					}
					os.flush();
					gzipInputStream.close();
				}
			}

		}
		else
			chain.doFilter(request, response);

	}

	private ServletContext servletContext;

	/**
	 * Read the configuration parameters
	 */
	@Override
	public void init(FilterConfig config) throws ServletException
	{
		servletContext = config.getServletContext();
		shrinkJS = "true".equals(config.getInitParameter("shrink-js"));
		cacheCompressedData = !"false".equals(config.getInitParameter("cached-compressed-data"));
		cacheFolderName = config.getInitParameter("cache-folder");
		if (cacheFolderName == null)
			cacheFolderName = DEFAULT_CACHE_FOLDER;
		super.init(config);
	}

	/**
	 * Calculates the file path of the cached gzip file
	 * 
	 * @param httpRequest
	 * @return the file path of the gzipped file
	 */
	private String getCachedGzipPath(HttpServletRequest httpRequest)
	{
		String sourcePath = URLDecoder.decode(httpRequest.getRequestURI()).substring(httpRequest.getContextPath().length());
		return httpRequest.getRealPath(cacheFolderName + "/" + sourcePath.replaceAll("[\\/\\*]", "_")) + ".gz";
	}

	/**
	 * This class changes the request to have an if-modified-since header to see if the content has changed
	 */
	public static class RequestModifiedSinceWrapper extends HttpServletRequestWrapper
	{
		long lastModified;

		public RequestModifiedSinceWrapper(HttpServletRequest request, long lastModified)
		{
			super(request);
			this.lastModified = lastModified;
		}

		@Override
		public long getDateHeader(String name)
		{
			if ("If-Modified-Since".equals(name))
				return lastModified;
			return super.getDateHeader(name);
		}

	}

	/**
	 * 
	 * This class utilizes code from ehcache: http://ehcache.sourceforge.net/ This class monitors the status code and redirects the output stream
	 */
	public static class StatusMonitorResponseWrapper extends HttpServletResponseWrapper implements Serializable
	{

		public StatusMonitorResponseWrapper(HttpServletResponse response, OutputStream outstr)
		{
			super(response);
			statusCode = 200;
			this.outputStream = new RedirectedOutputStream(outstr);
		}

		@Override
		public ServletOutputStream getOutputStream()
		{
			return outputStream;
		}

		@Override
		public void setStatus(int code)
		{
			statusCode = code;
			super.setStatus(code);
		}

		@Override
		public void setStatus(int code, String msg)
		{
			statusCode = code;
			super.setStatus(code);
		}

		public int getStatus()
		{
			return statusCode;
		}

		@Override
		public PrintWriter getWriter()
		{
			if (writer == null)
				writer = new PrintWriter(outputStream, true);
			return writer;
		}

		@Override
		public void flushBuffer() throws IOException
		{
			flush();
			super.flushBuffer();
		}

		@Override
		public void reset()
		{
			super.reset();
			statusCode = 200;
		}

		public void flush() throws IOException
		{
			if (writer != null)
				writer.flush();
			outputStream.flush();
		}

		private int statusCode;

		private ServletOutputStream outputStream;

		private PrintWriter writer;

	}

	public static class RedirectedOutputStream extends ServletOutputStream
	{

		public RedirectedOutputStream(OutputStream stream)
		{
			this.stream = stream;
		}

		@Override
		public void write(byte b[]) throws IOException
		{
			stream.write(b);
		}

		@Override
		public void write(byte b[], int off, int len) throws IOException
		{
			stream.write(b, off, len);
		}

		@Override
		public void write(int b) throws IOException
		{
			stream.write(b);
		}

		private OutputStream stream;
	}

}
