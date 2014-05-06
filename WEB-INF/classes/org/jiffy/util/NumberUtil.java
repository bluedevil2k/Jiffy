package org.jiffy.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class NumberUtil
{	
	public static DecimalFormat getSystemPriceFormatter()
	{
		DecimalFormat formatter = (DecimalFormat)NumberFormat.getInstance(Locale.getDefault());
		formatter.applyPattern(Jiffy.getValue("priceFormat"));
		return formatter;
	}
	
	public static DecimalFormat getSystemFormatter()
	{
		NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault()); 
		return (DecimalFormat)formatter;
	}
	
	public static double parsePrice(String text) throws Exception
	{		
		    if (text == null || text.trim() == null || text.trim().equals(""))
				return 0.0;
		    
			double unrounded = 0.0;
			// the price one isn't very tolerant when the currency is excluded, so for this we try it with the currency
			// one in case they did type it, and then fail to a normal one if they didn't type it, and then fail
			// to a thrown exception
			try
			{
				DecimalFormat formatter = getSystemPriceFormatter();
				unrounded = formatter.parse(text.trim()).doubleValue();
			}
			catch (Exception ex)
			{
				DecimalFormat formatter = getSystemFormatter();
				try
				{
					unrounded = formatter.parse(text.trim()).doubleValue();
				}
				catch (Exception e) 
				{ 
					throw new Exception("error.invalidPriceFormat");
				}
			}
			
			return unrounded;
	}
	
	public static double parseDouble(String text, String format) throws Exception
	{		
		try
		{
			DecimalFormat formatter = getSystemFormatter();
			formatter.applyPattern(format);
			return formatter.parse(text).doubleValue();
		}
		catch (ParseException pe)
		{
			// now remove all the thousands delimiters and currency symbols
			// and just keep the negative sign and decimal point
			String work = "";
			for (int i=0; i<text.length(); i++)
			{
				char c = text.charAt(i);
				if (Character.isDigit(c) || c == '-')
					work += c;
			}
			return Double.parseDouble(work);
		}
	}
}
