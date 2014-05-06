package org.jiffy.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PusherUtil
{	
	private static Logger logger = LogManager.getLogger();
	
	// Pusher Host name
	private final static String pusherHost = "api.pusherapp.com";
			
	public static void trigger(final String message, final String pusherChannel, final String event)
	{
		// do it on a separate thread so that we don't block the application, and that errors are swallowed up
		Thread thread = new Thread(){
			
			@Override
			public void run(){
				try
				{	
					PusherUtil.triggerPush(Jiffy.getValue("pusherAppID"), Jiffy.getValue("pusherAppKey"), Jiffy.getValue("pusherAppSecret"), pusherChannel, event, message);
				}
				catch (Exception ex) { Util.printErrorDetails(logger, ex); }
			}
		};
		
		thread.start();
	}
	
	/**
	 * Delivers a message to the Pusher API
	 * 
	 */
	public static boolean triggerPush(String api_id, String key, String secret, String channel, String event, String jsonData) throws Exception
	{
		return triggerPush(api_id, key, secret, channel, event, jsonData, "");
	}
	
	private static boolean triggerPush(String api_id, String key, String secret, String channel, String event, String jsonData, String socketId)
	{
		// Build URI path
		String uriPath = buildURIPath(api_id, channel);
		// Build query
		String query = buildQuery(key, event, jsonData, socketId);
		// Generate signature
		String signature = buildAuthenticationSignature(secret, uriPath, query);
		// Build URI
		URL url = buildURI(uriPath, query, signature);
		
		return httpPOST(url, jsonData);
	}

	/**
	 * Converts a byte array to a string representation
	 * 
	 * @param data
	 * @return
	 */
	private static String byteArrayToString(byte[] data)
	{
		BigInteger bigInteger = new BigInteger(1, data);
		String hash = bigInteger.toString(16);
		// Zero pad it
		while (hash.length() < 32)
		{
			hash = "0" + hash;
		}
		return hash;
	}

	/**
	 * Returns a md5 representation of the given string
	 * 
	 * @param data
	 * @return
	 */
	private static String md5Representation(String data)
	{
		try
		{
			// Get MD5 MessageDigest
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			byte[] digest = messageDigest.digest(data.getBytes("US-ASCII"));
			return byteArrayToString(digest);
		}
		catch (NoSuchAlgorithmException nsae)
		{
			// We should never come here, because GAE has a MD5 algorithm
			throw new RuntimeException("No MD5 algorithm");
		}
		catch (UnsupportedEncodingException e)
		{
			// We should never come here, because UTF-8 should be available
			throw new RuntimeException("No UTF-8");
		}
	}

	/**
	 * Returns a HMAC/SHA256 representation of the given string
	 * 
	 * @param data
	 * @return
	 */
	private static String hmacsha256Representation(String secret, String data)
	{
		try
		{
			// Create the HMAC/SHA256 key from application secret
			final SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");

			// Create the message authentication code (MAC)
			final Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(signingKey);

			// Process and return data
			byte[] digest = mac.doFinal(data.getBytes("UTF-8"));
			digest = mac.doFinal(data.getBytes());
			// Convert to string
			BigInteger bigInteger = new BigInteger(1, digest);
			return String.format("%0" + (digest.length << 1) + "x", bigInteger);
		}
		catch (NoSuchAlgorithmException nsae)
		{
			// We should never come here, because GAE has HMac SHA256
			throw new RuntimeException("No HMac SHA256 algorithm");
		}
		catch (UnsupportedEncodingException e)
		{
			// We should never come here, because UTF-8 should be available
			throw new RuntimeException("No UTF-8");
		}
		catch (InvalidKeyException e)
		{
			throw new RuntimeException("Invalid key exception while converting to HMac SHA256");
		}
	}

	/**
	 * Build query string that will be appended to the URI and HMAC/SHA256
	 * encoded
	 * 
	 * @param eventName
	 * @param jsonData
	 * @return
	 */
	private static String buildQuery(String key, String eventName, String jsonData, String socketID)
	{
		StringBuffer buffer = new StringBuffer();
		// Auth_Key
		buffer.append("auth_key=");
		buffer.append(key);
		// Timestamp
		buffer.append("&auth_timestamp=");
		buffer.append(System.currentTimeMillis() / 1000);
		// Auth_version
		buffer.append("&auth_version=1.0");
		// MD5 body
		buffer.append("&body_md5=");
		buffer.append(md5Representation(jsonData));
		// Event Name
		buffer.append("&name=");
		buffer.append(eventName);
		// Append socket id if set
		if (!socketID.isEmpty())
		{
			buffer.append("&socket_id=");
			buffer.append(socketID);
		}
		// Return content of buffer
		return buffer.toString();
	}

	/**
	 * Build path of the URI that is also required for Authentication
	 * 
	 * @return
	 */
	private static String buildURIPath(String api_id, String channelName)
	{
		StringBuffer buffer = new StringBuffer();
		// Application ID
		buffer.append("/apps/");
		buffer.append(api_id);
		// Channel name
		buffer.append("/channels/");
		buffer.append(channelName);
		// Event
		buffer.append("/events");
		// Return content of buffer
		return buffer.toString();
	}

	/**
	 * Build authentication signature to assure that our event is recognized by
	 * Pusher
	 * 
	 * @param uriPath
	 * @param query
	 * @return
	 */
	private static String buildAuthenticationSignature(String secret, String uriPath, String query)
	{
		StringBuffer buffer = new StringBuffer();
		// request method
		buffer.append("POST\n");
		// URI Path
		buffer.append(uriPath);
		buffer.append("\n");
		// Query string
		buffer.append(query);
		// Encode data
		String h = buffer.toString();
		return hmacsha256Representation(secret, h);
	}

	/**
	 * Build URI where request is send to
	 * 
	 * @param uriPath
	 * @param query
	 * @param signature
	 * @return
	 */
	private static URL buildURI(String uriPath, String query, String signature)
	{
		StringBuffer buffer = new StringBuffer();
		// Protocol
		buffer.append("http://");
		// Host
		buffer.append(pusherHost);
		// URI Path
		buffer.append(uriPath);
		// Query string
		buffer.append("?");
		buffer.append(query);
		// Authentication signature
		buffer.append("&auth_signature=");
		buffer.append(signature);
		// Build URI
		try
		{
			return new URL(buffer.toString());
		}
		catch (MalformedURLException e)
		{
			throw new RuntimeException("Malformed URI");
		}
	}

	/**
	 * Delivers a message to the Pusher API
	 * 
	 * @param channel
	 * @param event
	 * @param jsonData
	 * @param socketId
	 * @return
	 * 
	 */
	
	private static boolean httpPOST(URL url, String data)
	{
		HttpURLConnection connection = null;
		try
		{
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			 
			connection.setUseCaches (false);
		    connection.setDoInput(true);
		    connection.setDoOutput(true);
	
		    //Send request
		    DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
		    wr.writeBytes(data);
		    wr.flush();
		    wr.close();
	
		    //Get Response	
		    InputStream is = connection.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    String line;
		    StringBuffer response = new StringBuffer(); 
		    while((line = rd.readLine()) != null) {
		      response.append(line);
		      response.append('\r');
		    }
		    rd.close();
		    		 	
		   // logger.debug(response.toString());
		    
		    if (StringUtils.contains(response.toString(), "202"))
		    	return true;
		    
		    return false;
		    			
		}
		catch (Exception ex)
		{
			Util.printErrorDetails(logger, ex);
			return false;
		}
		finally
		{
		    if (connection != null) 
		    {
		    	connection.disconnect(); 
		    }
		}
	}
	
}
