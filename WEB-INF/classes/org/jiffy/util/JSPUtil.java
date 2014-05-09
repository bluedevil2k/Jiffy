package org.jiffy.util;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class JSPUtil
{	
	public static void noCache(HttpServletResponse res) throws Exception
	{
		// for HTTP 1.1
		res.setHeader("Cache-Control", "no-cache,max-age=0,no-store");
		// for HTTP 1.0
		res.setHeader("Pragma", "no-cache");
		res.setDateHeader("Expires", 0);
	}
	
	public static String displaySafe(String s)
	{
		if (s == null || s.equals("null"))
		{
			return "&nbsp;";
		}
		else
		{
			s = StringUtils.replace(s, "\n", "<br>");
			return s;
		}
	}
}
