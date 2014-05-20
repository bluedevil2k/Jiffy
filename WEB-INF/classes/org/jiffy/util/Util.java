package org.jiffy.util;

public class Util 
{
	public static String camelToUnderscore(String camelCase)
	{
		StringBuffer b = new StringBuffer("");
		for (int i=0; i<camelCase.length(); i++)
		{
			char c = camelCase.charAt(i);
			if (Character.isUpperCase(c))
			{
				b.append("_" + Character.toLowerCase(c));
			}
			else
			{
				b.append(c);
			}
		}
		return b.toString();
	}
	
	public static String underscoreToCamel(String underscore)
	{
		StringBuffer b = new StringBuffer("");
		for (int i=0; i<underscore.length(); i++)
		{
			char c = underscore.charAt(i);
			if (c == '_')
			{
				b.append(Character.toUpperCase(underscore.charAt(i+1)));
				i++;
			}
			else
			{
				b.append(c);
			}
		}
		return b.toString();
	}
	
	public static Object stringToValue(Class cls, String value) throws Exception
	{
		if (cls == String.class)
		{
			return value.toString().trim();
		}
		else if (cls == boolean.class)
		{
			return Boolean.parseBoolean(value);
		}
		else if (cls == double.class)
		{
			return Double.parseDouble(value);
		}
		else if (cls == int.class)
		{
			return Integer.parseInt(value);
		}
		else if (cls == long.class)
		{
			return Long.parseLong(value);
		}
		else if (cls == float.class)
		{
			return Float.parseFloat(value);
		}
		else if (cls == char.class)
		{
			if (value.length() > 0)
				return new Character(value.charAt(0));
			else
				return new Character(' ');
		}
		return value;
	}
}
