package org.jiffy.server.services.responses;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jiffy.server.services.ServiceResponse;
import org.jiffy.util.JSPUtil;

public class JsonResponse extends ServiceResponse
{
	public boolean isAjax;
	public String ajaxResponse;
	
	public JsonResponse()
	{
		super();
		responseType = ServiceResponse.JSON;
	}

	@Override
	public void respond(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		// must set cache to empty for iOS devices
    	JSPUtil.setJSPHeader(resp);
		resp.setCharacterEncoding("UTF-8");
		resp.getWriter().print(ajaxResponse);
		resp.getWriter().flush();
	}
}
