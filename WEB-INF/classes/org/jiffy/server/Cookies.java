package org.jiffy.server;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class Cookies
{
	public static void addCookie(HttpServletResponse res, String key, String value, int expires)
	{
		Cookie c = new Cookie(key, value);
		c.setMaxAge(expires);
		c.setPath("/");
		c.setHttpOnly(true);
		res.addCookie(c);
	}
	
	public static void addCookieExpiresWithBrowser(HttpServletResponse res, String key, String value)
	{
		addCookie(res, key, value, -1);
	}	
	
	public static String getCookieValue(HttpServletRequest req, String key)
	{
		Cookie[] cookies = req.getCookies();
		for (int i = 0; i < cookies.length; i++)
		{
			Cookie c = cookies[i];
			if (StringUtils.equals(c.getName(), key))
			{
				return c.getValue();
			}
		}
		return "";
	}
	
	public static void removeCookie(HttpServletResponse res, String key)
	{
		Cookie c = new Cookie(key, "");
		c.setMaxAge(0);
		c.setValue("");
		c.setPath("/");
		c.setHttpOnly(true);
		res.addCookie(c);
	}
	
}
