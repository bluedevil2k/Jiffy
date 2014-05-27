package org.jiffy.util;

import java.util.Properties;

public class Jiffy  extends Properties
{
	// @X-JVM-safe this member is ready only
	// @Thread-safe this member is read only
	private static Jiffy _jiffy;
	
	private Jiffy() 
	{
	}	
		
	public static synchronized void configure() throws Exception
	{
		if (_jiffy == null)
		{
			_jiffy = new Jiffy();
		}
		init("jiffy.properties");
		init("environment.properties");
		init(Jiffy.getValue("environment") + ".properties");
	}
	
	private static void init(String propName) throws Exception 
	{
		_jiffy.load(Jiffy.class.getResourceAsStream(propName));
	}
	
	public static String getValue(String key)
	{
		return _jiffy.getProperty(key);
	}
	
	public static int getInt(String key)
	{
		return Integer.parseInt(_jiffy.getProperty(key));
	}
	
	public static boolean getBool(String key)
	{
		return Boolean.parseBoolean(_jiffy.getProperty(key));
	}
}