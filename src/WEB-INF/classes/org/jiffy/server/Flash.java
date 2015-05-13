package org.jiffy.server;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jiffy.util.LogUtil;

public class Flash extends HashMap<String, Object>
{
	private static Logger logger = LogManager.getLogger();
	
	private static final String FLASH = "FLASH";
		
	public static void set(HttpServletRequest req, String key, Object value)
	{
		try
		{
			Flash flash = (Flash)Sessions.get(req, FLASH);
			
			if (flash == null)
			{
				flash = new Flash();
			}
			
			flash.put(key, value);
			
			Sessions.set(req, FLASH, flash);
		}
		catch (Exception ex)
		{
			LogUtil.printErrorDetails(logger, ex);
		}
	}
	
	public static Flash retrieve(HttpServletRequest req) throws Exception
	{
		Flash flash = (Flash)Sessions.get(req, FLASH);
		Sessions.remove(req, FLASH);
		return flash;
	}
	
	public static void delete(HttpServletRequest req) throws Exception
	{
		Sessions.remove(req, FLASH);
	}
}
