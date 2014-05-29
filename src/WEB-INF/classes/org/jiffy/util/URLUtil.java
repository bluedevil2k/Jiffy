package org.jiffy.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class URLUtil 
{
	public static String getURLContents(String website) throws Exception
	{
		URL url = new URL(website);
        URLConnection urlC = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(urlC.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuilder a = new StringBuilder();
        while ((inputLine = in.readLine()) != null)
            a.append(inputLine);
        in.close();

        return a.toString();
	}
}
