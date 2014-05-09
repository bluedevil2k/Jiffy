package org.jiffy.server.services.responses;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jiffy.server.services.ServiceResponse;
import org.jiffy.util.JSPUtil;

public class JsonResponse extends ServiceResponse
{
	public String text;
	
	@Override
	public void respond(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		// must set not to cache for iOS devices
    	JSPUtil.noCache(resp);
    	    	
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json");
		resp.getWriter().print(text);
		resp.getWriter().flush();
	}
}
