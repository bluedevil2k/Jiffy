package org.jiffy.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;

public class ZipUtil 
{
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
