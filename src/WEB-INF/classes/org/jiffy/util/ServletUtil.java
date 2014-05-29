package org.jiffy.util;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class ServletUtil
{
	public static InputStream getUploadedFileStream(HttpServletRequest request) throws Exception
	{
		ServletFileUpload upload = new ServletFileUpload();
		FileItemIterator iter = upload.getItemIterator(request);
		while (iter.hasNext())
		{
			FileItemStream item = iter.next();
			InputStream stream = item.openStream();
			if (item.isFormField())
				continue;
			else
			{
				return stream;
			}
		}
		return null;
	}
}
