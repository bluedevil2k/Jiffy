package org.jiffy.server.services.responses;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jiffy.server.services.ServiceResponse;
import org.jiffy.util.JSPUtil;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonResponse extends ServiceResponse
{
	public Object jsonObject;
	public Object[] jsonArray;
	
	@Override
	public void respond(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		// must set not to cache for iOS devices
    	JSPUtil.noCache(resp);
    	    	
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json");
		if (jsonObject != null)
		{
			resp.getWriter().print(new JSONObject(jsonObject).toString());
		}
		else if (jsonObject != null)
		{
			resp.getWriter().print(new JSONArray(jsonArray).toString());
		}
		resp.getWriter().flush();
	}
}
