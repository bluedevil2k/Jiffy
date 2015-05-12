package org.jiffy.server.services.responses;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jiffy.server.Flash;
import org.jiffy.server.services.ServiceResponse;

public class HtmlResponse extends ServiceResponse
{
	public String nextPage;
	
	@Override
	public void respond(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		// Set all responses to redirects for proper routing in the browser
		
		// As a result, we must map the attributes to the Flash object
		Enumeration<String> e = req.getAttributeNames();
		while (e.hasMoreElements())
		{
			String key = e.nextElement();
			Object value = req.getAttribute(key);
			Flash.set(req, key, value);
		}
		
		resp.sendRedirect(nextPage);
	}

}
