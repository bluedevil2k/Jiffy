package org.jiffy.server;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jiffy.util.LogUtil;

public class Flash 
{
	private static Logger logger = LogManager.getLogger();
	
	private static final String FLASH = "FLASH";
	
	public static synchronized void set(HttpServletRequest req, String key, Object value)
	{
		try
		{
			Map<String, Object> flash = (Map<String, Object>)Sessions.get(req, FLASH);
			
			if (flash == null)
			{
				flash = new HashMap<String, Object>();
			}
			
			flash.put(key, value);
			
			Sessions.set(req, FLASH, flash);
		}
		catch (Exception ex)
		{
			LogUtil.printErrorDetails(logger, ex);
		}
	}
	
	public static Map<String, Object> retrieve(HttpServletRequest req) throws Exception
	{
		Map<String, Object> flash = (Map<String, Object>)Sessions.get(req, FLASH);
		Sessions.remove(req, FLASH);
		return flash;
	}
}
