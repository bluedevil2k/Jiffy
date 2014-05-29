package org.jiffy.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

public class LogUtil 
{
	public static String tabs(int count, int tabSize)
	{
		StringBuffer buf = new StringBuffer();
		for (int ct = 0; ct < (count * tabSize); ct++)
			buf.append(' ');
		return buf.toString();
	}

	public static String tabs(int count)
	{
		return tabs(count, 4);
	}
	
	public static String getStackTrace(Throwable t)
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(bytes);
		t.printStackTrace(pw);
		pw.close();
		return bytes.toString();
	}

	public static void printErrorDetails(Logger logger, Exception ex)
	{
		printErrorDetails(logger, ex, "");
	}

	public static void printErrorDetails(Logger logger, Exception ex, String customMessage)
	{
		if (!StringUtils.isEmpty(customMessage))
			logger.error(customMessage);
		
		logger.error(ex.getMessage());
		int size = Math.min(10, ex.getStackTrace().length);
		for (int i = 0; i < size; i++)
			logger.error("\t" + ex.getStackTrace()[i]);
	}

}
