package org.jiffy.util;

import java.math.BigDecimal;

public class MathUtil
{
	public static double round(double unrounded)
	{
		return round(unrounded, 2, BigDecimal.ROUND_HALF_UP);
	}

	public static double round(double unrounded, int precision, int roundingMode)
	{
		BigDecimal bd = new BigDecimal(unrounded);
		BigDecimal rounded = bd.setScale(precision, roundingMode);
		return rounded.doubleValue();
	}

	public static double round(String unrounded, int precision, int roundingMode)
	{
		BigDecimal bd = new BigDecimal(unrounded);
		BigDecimal rounded = bd.setScale(precision, roundingMode);
		return rounded.doubleValue();
	}

	public static String roundToString(double unrounded, int precision, int roundingMode)
	{
		BigDecimal bd = new BigDecimal(unrounded);
		BigDecimal rounded = bd.setScale(precision, roundingMode);
		return rounded.toString();
	}

	public static String roundToString(String unrounded, int precision, int roundingMode)
	{
		BigDecimal bd = new BigDecimal(unrounded);
		BigDecimal rounded = bd.setScale(precision, roundingMode);
		return rounded.toString();
	}

	public static double roundToNearest(double beforeRounding, double roundToNearest)
	{
		String d = "" + roundToNearest;
		int decimal = d.indexOf(".");
		if (decimal == -1)
			decimal = 0;
		else
			decimal = d.length() - decimal - 1;
		return roundToNearest(beforeRounding, roundToNearest, decimal, BigDecimal.ROUND_HALF_UP);
	}

	public static double roundToNearest(double beforeRounding, double roundToNearest, int precision, int defaultRoundingMode)
	{
		BigDecimal bdPrice = new BigDecimal(beforeRounding);
		bdPrice = bdPrice.divide(new BigDecimal(roundToNearest), defaultRoundingMode);
		bdPrice = bdPrice.setScale(precision, defaultRoundingMode);
		bdPrice = bdPrice.setScale(0, defaultRoundingMode);
		bdPrice = bdPrice.multiply(new BigDecimal(roundToNearest));
		return round(bdPrice.doubleValue(), precision, defaultRoundingMode);
	}

	public static double roundToSigDigits(double beforeRounding, int defaultSigDigits, int defaultRoundingMode)
	{
		int sign = 1;
		if (beforeRounding < 0.0)
		{
			sign = -1;
			beforeRounding = beforeRounding * -1;
		}
		String s = roundToString(beforeRounding, 10, defaultRoundingMode);
		int digits = s.length();
		if (s.indexOf(".") > -1)
			digits = s.substring(0, s.indexOf(".")).length();
		if (beforeRounding < 1.0)
		{
			digits--;
			String dec = s.substring(s.indexOf(".") + 1);
			int i = 0;
			while (dec.length() > i && dec.charAt(i) == '0')
			{
				i++;
				digits--;
			}
		}
		double diff = Math.pow(10, (digits - defaultSigDigits));
		int scale = Math.abs(digits - defaultSigDigits);
		return round(sign * roundToNearest(beforeRounding, diff, scale, defaultRoundingMode), scale, BigDecimal.ROUND_HALF_UP);
	}

	public static int compareWithPrecision(double value1, double value2, int precision, int roundingMode)
	{
		BigDecimal d1 = new BigDecimal(value1).setScale(precision, roundingMode);
		BigDecimal d2 = new BigDecimal(value2).setScale(precision, roundingMode);
		return d1.compareTo(d2);
	}

	public static boolean lessThan(double value1, double value2, int precision, int roundingMode)
	{
		BigDecimal d1 = new BigDecimal(value1).setScale(precision, roundingMode);
		BigDecimal d2 = new BigDecimal(value2).setScale(precision, roundingMode);
		return d1.compareTo(d2) == -1;
	}
	
	public static boolean lessThanEqualTo(double value1, double value2, int precision, int roundingMode)
	{
		return lessThan(value1, value2, precision, roundingMode) || equalsTo(value1, value2, precision, roundingMode);
	}

	public static boolean greaterThan(double value1, double value2, int precision, int roundingMode)
	{
		BigDecimal d1 = new BigDecimal(value1).setScale(precision, roundingMode);
		BigDecimal d2 = new BigDecimal(value2).setScale(precision, roundingMode);
		return d1.compareTo(d2) == 1;
	}
	
	public static boolean greaterThanEqualTo(double value1, double value2, int precision, int roundingMode)
	{
		return greaterThan(value1, value2, precision, roundingMode) || equalsTo(value1, value2, precision, roundingMode);
	}

	public static boolean equalsTo(double value1, double value2, int precision, int roundingMode)
	{
		BigDecimal d1 = new BigDecimal(value1).setScale(precision, roundingMode);
		BigDecimal d2 = new BigDecimal(value2).setScale(precision, roundingMode);
		return d1.compareTo(d2) == 0;
	}

	public static double getPercentChange(double newValue, double oldValue)
	{
		double change = 0;
		if (oldValue != 0)
			change = (newValue - oldValue) / oldValue;
		return change;
	}
}
