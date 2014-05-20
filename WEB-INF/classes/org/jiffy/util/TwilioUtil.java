package org.jiffy.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestResponse;

public class TwilioUtil {

	private static Logger logger = LogManager.getLogger();
	
	public static void sendSMS(final String api, final String api_key, final String to, final String from, final String message)
	{
		// do it on a separate thread so that we don't block the application, 
		// and that errors are swallowed up
		Thread thread = new Thread(){
			
			@Override
			public void run(){
				try
				{
					TwilioRestClient t = new TwilioRestClient(api, api_key, null);
					Map<String,String> map = new HashMap<String,String>();
					map.put("To", TwilioUtil.sanitizeInternationalNumber(to));
					map.put("From", TwilioUtil.sanitizeInternationalNumber(from));
					map.put("Body", message);
											
					TwilioRestResponse response = t.request("/2010-04-01/Accounts/" + t.getAccountSid() + "/SMS/Messages", "POST", map);
				}
				catch (Exception ex) { LogUtil.printErrorDetails(logger, ex); }
			}
		};
		
		thread.start();
	}
		
	public static String sanitizeUSNumber(String number) throws Exception
	{
		number = StringUtils.remove(number, " ");
		number = StringUtils.remove(number, ".");
		number = StringUtils.remove(number, "-");
		number = StringUtils.remove(number, "(");
		number = StringUtils.remove(number, ")");
		number = StringUtils.remove(number, "+");
		
		if (!StringUtils.isNumeric(number))
			throw new Exception("Only numbers are allowed in a phone number, no letters");
		
		if (number.charAt(0) != '1')
			number = "1" + number;
		
		number = "+" + number;
		
		if (number.length() != 12)
			throw new Exception("Invalid phone number length");		
		
		return number;
	}
	
	public static String sanitizeInternationalNumber(String number) throws Exception
	{
		number = StringUtils.remove(number, " ");
		number = StringUtils.remove(number, ".");
		number = StringUtils.remove(number, "-");
		number = StringUtils.remove(number, "(");
		number = StringUtils.remove(number, ")");
		number = StringUtils.remove(number, "+");
		
		if (!StringUtils.isNumeric(number))
			throw new Exception("Only numbers are allowed in a phone number, no letters");
		
		number = "+" + number;
				
		return number;
	}
}
