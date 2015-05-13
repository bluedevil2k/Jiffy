package org.jiffy.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.jiffy.util.URLUtil;

public class PayPalPDT 
{
		public static final String RESULT = "result";
		public static final String FIRST_NAME = "first_name";
		public static final String LAST_NAME = "last_name";
		public static final String PAYMENT_STATUS = "payment_status";
		public static final String EMAIL = "payer_email";
		public static final String AMOUNT = "payment_gross";
		public static final String CUSTOM = "custom";
		public static final String DATE = "payment_date";
		public static final String COUNTRY = "residence_country";
		public static final String TXN_ID = "txn_id";
		public static final String INVOICE = "invoice";
		public static final String ITEM_NUMBER = "item_number";
		
		public static Map<String, String> getPaymentDetails(HttpServletRequest req) throws Exception
		{
			return getPaymentDetails(req.getParameter("tx"));
		}
		
		public static Map<String, String> getPaymentDetails(String txn) throws Exception
		{
			String at = "at=" + Jiffy.getValue("paypalPDT_ID");
			String cmd = "cmd=_notify-synch";
			   
			String query = "https://www.paypal.com/cgi-bin/webscr?tx=" + txn + "&" + at + "&" + cmd; 
			System.out.println(query);
			String response = URLUtil.getURLContents(query);
			System.out.println(response);
			
			String[] results = StringUtils.split(response, "\n");
			
			System.out.println(results.length);
			
			Map<String, String> resultMap = new HashMap<String, String>();
			resultMap.put(RESULT, results[0]);
			for (int i=1; i<results.length; i++)
			{
				resultMap.put(results[i].substring(0, results[i].indexOf("=")), convertQueryToText(results[i].substring(results[i].indexOf("=")+1)));
			}
			
			return resultMap;
		}
		

		public static String convertQueryToText(String query)
		{
			query = StringUtils.replace(query, "+", " ");
			query = StringUtils.replace(query, "%40", "@");
			query = StringUtils.replace(query, "%3A", ":");
			query = StringUtils.replace(query, "%2C", ",");
			
			return query;
		}
		
		public static String convertTextToQuery(String preQuery)
		{
			preQuery = StringUtils.replace(preQuery, " ", "+");
			preQuery = StringUtils.replace(preQuery, "@", "%40");
			preQuery = StringUtils.replace(preQuery, ":", "%3A");
			preQuery = StringUtils.replace(preQuery, ",", "%2C");
			
			return preQuery;
		}
		
	}