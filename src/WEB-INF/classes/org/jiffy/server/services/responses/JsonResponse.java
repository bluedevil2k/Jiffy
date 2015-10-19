package org.jiffy.server.services.responses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jiffy.server.services.ServiceResponse;
import org.jiffy.util.JSPUtil;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonResponse extends ServiceResponse
{
	public Object jsonObject;
	public Collection<?> jsonArray;
	public boolean success;
	
	@Override
	public void respond(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		// must set not to cache for iOS devices
    	JSPUtil.noCache(resp);
    	    	
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json");
		
		if (jsonObject != null)
		{
			if (jsonObject instanceof Map<?, ?>)
			{
				resp.getWriter().print(new JSONObject((Map)jsonObject).toString());
			}
			else if (jsonObject instanceof String)
			{
				resp.getWriter().print(new JSONObject((String)jsonObject).toString());
			}
			else
			{
				resp.getWriter().print(new JSONObject(jsonObject).toString());
			}
		}
		else if (jsonArray != null)
		{
			List json = new ArrayList();
			for (Iterator iter=jsonArray.iterator(); iter.hasNext();)
			{
				json.add(new JSONObject(iter.next()));
			}
			resp.getWriter().print(new JSONArray(json).toString());
		}
		resp.getWriter().flush();
	}
}
