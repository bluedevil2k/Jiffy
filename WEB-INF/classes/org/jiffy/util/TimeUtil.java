package org.jiffy.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimeUtil
{
	public static Logger logger = LogManager.getLogger();
	
	public static String formatTime(long millis, String format, String timeZoneId)
	{
		return DateFormatUtils.format(millis,  format, TimeZone.getTimeZone(timeZoneId));
	}

	public static String formatTime(Date date, String format, String timeZoneId)
	{
		return DateFormatUtils.format(date, format, TimeZone.getTimeZone(timeZoneId));
	}

	public static long parseTime(String time, String format, String timeZoneId) throws Exception
	{
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		
		if (timeZoneId != null)
		{
			formatter.setTimeZone(TimeZone.getTimeZone(timeZoneId));
		}
		
		Date date;
		try
		{
			date = formatter.parse(time);
		}
		catch (java.text.ParseException pe)
		{
			throw new Exception("error.invalidTimeFormat");
		}
		
		// time formatter allows things like 09:65:04, which we don't want so I add additional checking
		// determine number resolution of time format based upon number of ':' separators
		StringTokenizer st = new StringTokenizer(time, ":");
		int numTokens = st.countTokens();
		if (numTokens > 0)
		{
			String hours = getEndingDigits(st.nextToken());
			if (Integer.parseInt(hours) > 23)
			{
				throw new Exception("error.invalidTimeFormat");
			}
		}
		if (numTokens > 1)
		{
			String mins = getStartingDigits(st.nextToken());
			if (Integer.parseInt(mins) > 59)
			{
				throw new Exception("error.invalidTimeFormat");
			}
		}
		if (numTokens > 2)
		{
			String secs = getStartingDigits(st.nextToken());
			if (Integer.parseInt(secs) > 59)
			{
				throw new Exception("error.invalidTimeFormat");
			}
		}
		return date.getTime();
	}

	private static String getEndingDigits(String str)
	{
		int pos = -1;
		int startPos = str.length() - 1;
		for (int ct = startPos; ct >= 0; ct--)
		{
			if (!Character.isDigit(str.charAt(ct)))
			{
				pos = ct + 1;
				break;
			}
		}
		return (pos == -1 ? str : str.substring(pos));
	}

	private static String getStartingDigits(String str)
	{
		int pos = -1;
		for (int ct = 0; ct < str.length(); ct++)
		{
			if (!Character.isDigit(str.charAt(ct)))
			{
				pos = ct;
				break;
			}
		}
		return (pos == -1 ? str : str.substring(0, pos));
	}
	
	public static String formatDaysHoursMins(long time)
	{
		String formattedTime = "";
		
		// time is in ms, so we must convert it to sec first
		time = time / 1000;
		
		// since we don't allow seconds for phase times, convert it to minutes
		time = time / 60;
		
		// get the minutes by finding the mod
		String min = (time % 60) + "";
		
		// convert it to hours
		time = time / 60;
		
		// format depending on hours, return if there's no hours
		if (time == 0)
		{
			return min;
		}
		else
		{
			if (min.length() == 1)
			{
				min = "0" + min;
			}
			formattedTime = ":" + min;
		}
		
		// get the hours by finding the mod
		String hour = (time % 24) + "";
		
		// convert it to days
		time = time / 24;
		
		// format depending on days, return if there's no days
		if (time == 0)
		{
			return hour + formattedTime;
		}
		else
		{
			if (hour.length() == 1)
			{
				hour = "0" + hour;
			}
			formattedTime = ":" + hour + formattedTime;
		}
		
		// attach the days and return it
		return time + formattedTime;
	}
	
	public static long parseDaysHoursMin(String formattedTime) throws Exception
	{
		long SEC = 60 * 1000;
		
		try
		{
			String[] t = StringUtils.split(formattedTime, ':');
			
			if (t.length == 3)
			{
				long days = Long.parseLong(t[0]);
				long hours = Long.parseLong(t[1]);
				long minutes = Long.parseLong(t[2]);
				
				if (days < 0) throw new Exception("error.invalidTimeFormat");
				if (hours < 0 || hours > 23) throw new Exception("error.invalidTimeFormat");
				if (minutes < 0 || minutes > 59) throw new Exception("error.invalidTimeFormat");
				
				return (days * 24 * 60 * SEC) + (hours * 60 * SEC) + (minutes * SEC);
			}
			if (t.length == 2)
			{
				long hours = Long.parseLong(t[0]);
				long minutes = Long.parseLong(t[1]);

				if (hours < 0 || hours > 23) throw new Exception("error.invalidTimeFormat");
				if (minutes < 0 || minutes > 59) throw new Exception("error.invalidTimeFormat");
				
				return (hours * 60 * SEC) + (minutes * SEC);
			}
			if (t.length == 1)
			{
				long minutes = Long.parseLong(t[0]);

				if (minutes < 0 || minutes > 59) throw new Exception("error.invalidTimeFormat");
				
				return (minutes * SEC);
			}
		}
		catch (Exception ex)
		{
			LogUtil.printErrorDetails(logger, ex);
			throw new Exception("error.invalidTimeFormat");
		}
		
		return 0;
	}

	/**
	 * this method rounds the specified time to the nearest interval passed. It is useful in the schedule to round times to the nearest 5 minutes for example
	 */
	public static long roundTimeUp(long time, long toNearest)
	{
		long extraTime = time % toNearest;
		if (extraTime == 0)
		{
			return time;
		}
		else
		{
			return time - (time % toNearest) + toNearest;

		}
	}

}