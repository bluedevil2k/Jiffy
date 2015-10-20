package org.jiffy.util;

import java.util.ResourceBundle;

public class Text
{
	private static ResourceBundle text;
	
	private Text() {}
	
	public static synchronized ResourceBundle init()
	{
		if (text == null)
		{
			text = ResourceBundle.getBundle(Jiffy.getValue("textResources"));
		}
		return text;
	}
	
	public static String get(String key)
	{
		return text.getString(key);
	}

}
