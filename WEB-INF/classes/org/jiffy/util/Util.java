package org.jiffy.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

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

	public static void addToZipFile(String zipFile, File[] filesToZip) throws Exception
	{
		String[] files = new String[filesToZip.length];
		for (int i = 0; i < filesToZip.length; i++)
			files[i] = filesToZip[i].getAbsolutePath();
		addToZipFile(zipFile, files);
	}

	public static void addToZipFile(String zipFile, String[] filesToZip) throws Exception
	{
		// Create a buffer for reading the files
		byte[] buf = new byte[1024];

		// Create the ZIP file
		String outFilename = zipFile;
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));

		// Compress the files
		for (int i = 0; i < filesToZip.length; i++)
		{
			FileInputStream in = new FileInputStream(filesToZip[i]);

			// Add ZIP entry to output stream.
			out.putNextEntry(new ZipEntry(FilenameUtils.getName(filesToZip[i])));

			// Transfer bytes from the file to the ZIP file
			int len;
			while ((len = in.read(buf)) > 0)
			{
				out.write(buf, 0, len);
			}

			// Complete the entry
			out.closeEntry();
			in.close();
		}

		// Complete the ZIP file
		out.close();

	}

}
