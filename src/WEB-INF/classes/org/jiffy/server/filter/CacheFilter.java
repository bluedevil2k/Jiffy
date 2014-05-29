package org.jiffy.server.filter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

import org.jiffy.util.Jiffy;

@WebFilter(filterName = "CacheFilter", displayName = "CacheFilter", urlPatterns = { "*.jpg", "*.png", "*.gif" })
public class CacheFilter implements Filter
{
	public void destroy()
	{
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		setCacheHeader((HttpServletResponse) response);
		chain.doFilter(request, response);
	}

	/**
	 * Sets the caching header directives (if enabled) on the given response
	 * 
	 * @param response
	 */
	public void setCacheHeader(HttpServletResponse response)
	{
		if (cachingHeadersEnabled)
			setCacheHeader(response, expirationTime);
	}

	/**
	 * Sets the caching header directives on the given response with the given expiration
	 * 
	 * @param response
	 * @param expirationTime
	 */
	public static void setCacheHeader(HttpServletResponse response, long expirationTime)
	{
		// expiration from properties file instead
		expirationTime = Jiffy.getInt("webCacheExpirationTime") * 1000;
		
		response.setHeader("Pragma", "");
		response.setHeader("Cache-Control", "public");
		Date expirationDate = new Date(new Date().getTime() + expirationTime);
		DateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		response.setHeader("Expires", formatter.format(expirationDate));
	}

	/**
	 * expiration time in the future to use for the caching directives
	 */
	long expirationTime = 86400000;
	/**
	 * indicates whether or not to use caching header directives
	 */
	boolean cachingHeadersEnabled = true;

	/**
	 * Read the configuration parameters
	 */
	public void init(FilterConfig config) throws ServletException
	{
		if (config != null)
		{
			String expirationTimeParameter = config.getInitParameter("expiration-time");
			if (expirationTimeParameter != null)
			{
				this.expirationTime = Long.parseLong(expirationTimeParameter);
			}
			cachingHeadersEnabled = !"false".equals(config.getInitParameter("caching-headers-enabled"));
		}
	}

}
