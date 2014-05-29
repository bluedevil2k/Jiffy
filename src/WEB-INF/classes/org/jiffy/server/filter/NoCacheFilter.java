package org.jiffy.server.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletResponse;

@WebFilter(filterName="NoCacheFilter",
		   displayName="NoCacheFilter",
           initParams={@WebInitParam(name="no-cache", value="no-cache,max-age=0,no-store,must-revalidate")}         
		  )
public class NoCacheFilter implements Filter
{
	private FilterConfig config;

	public void destroy()
	{
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		HttpServletResponse httpResp = (HttpServletResponse) response;
		String noCache = config.getInitParameter("no-cache");
		httpResp.setHeader("Cache-Control", noCache);
		httpResp.setHeader("Pragma", "no-cache");
		httpResp.setDateHeader("Expires", 0);

		chain.doFilter(request, response);
	}

	public void init(FilterConfig config) throws ServletException
	{
		this.config = config;
	}

}
