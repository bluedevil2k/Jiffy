package org.jiffy.server.security;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jiffy.models.UserSession;
import org.jiffy.server.Sessions;
import org.jiffy.util.Constants;

public class Security
{
	protected static Logger logger = LogManager.getLogger();
	
	public static void validateAccess(UserSession appSession, String[] allowedRoles, String method) throws Exception
	{	 
		// if the appSession can't be found, throw an invalid access error (500) which will force them to log in again
	    if (appSession == null)
	    {
	    	throw new Exception(Constants.INVALID_ACCESS);	    	
	    }   
	    	    
	    // if their appSession from their session doesn't match one in the DB, they've been kicked-off but still have a valid browser session
	    if (UserSession.lookup(appSession.sessionId) == null)
	    {
	    	throw new Exception(Constants.INVALID_ACCESS);	    	
	    }   
	    	    
	    // log the request in the Access logs
	    StringBuffer message = new StringBuffer("ACCESS=(" + method + ") ALLOWED=(" + ArrayUtils.toString(allowedRoles) + ") " +
	    		       "ROLE=(" + appSession.role + ") USERNAME=(" + appSession.userName + ") " +
	    			   "USERID=(" + appSession.userId + ") IPADDRESS=(" + appSession.ipAddress + ") " +
	    			   "SESSIONID=(" + appSession.sessionId + ") RESULT=");
		try
		{		    
		    
		    // if there's no role in the appSession, throw an error
		    if (StringUtils.isEmpty(appSession.role))
		    {
		    	message.append("ERROR");
		    	throw new Exception(Constants.INVALID_ACCESS);	    	
		    }
		    
	        // if there's no allowed role passed in, throw an error
	        if (allowedRoles == null || allowedRoles.length == 0)
		    {
		    	message.append("ERROR");
		    	throw new Exception(Constants.INVALID_ACCESS);	    	
		    }
	        
	        // check the role of the appSession vs. what's allowed
	        for (int i=0; i<allowedRoles.length; i++)
	        {
	        	// if the appSession's role is in the allowed list, return safely from this function
	        	if (StringUtils.equals(appSession.role, allowedRoles[i]))
	        	{
	    	    	message.append("SUCCESS");
	    	    	UserSession.updateSessionActivity(appSession, method);
	        		return;
	        	}
	        }

	        // if there was no match, throw an exception
	    	message.append("ERROR");
	    	throw new Exception(Constants.INVALID_ACCESS);	
		}
		finally
		{
			logger.info(message.toString());
		}
	}
	
	public static void validateAccess(UserSession appSession, long userID) throws Exception
	{
		// if the appSession can't be found, throw an invalid access error (500) which will force them to log in again
	    if (appSession == null)
	    {
	    	throw new Exception(Constants.INVALID_ACCESS);	    	
	    } 
	    
	    // if their appSession from their session doesn't match one in the DB, they've been kicked-off but still have a valid browser session
	    if (UserSession.lookup(appSession.sessionId) == null)
	    {
	    	throw new Exception(Constants.INVALID_ACCESS);	    	
	    }  
	    
	    // check that the appSession's ID matches the passed in userID
	    if (appSession.userId != userID)
	    {
	    	throw new Exception(Constants.INVALID_ACCESS);	    	
	    } 
	}
	
	public static void validateCSRF(HttpServletRequest request, UserSession appSession) throws Exception
	{
		// if the appSession can't be found, throw an invalid access error (500) which will force them to log in again
	    if (appSession == null)
	    {
	    	throw new Exception(Constants.INVALID_ACCESS);	    	
	    } 
	    
        // check the CSRF token in the form being submitted against the expected token
        String csrf = request.getParameter("csrf");
        String csrf_check = PasswordService.encrypt(appSession.sessionId);
        if (!StringUtils.equals(csrf, csrf_check))
        {
	    	throw new Exception(Constants.INVALID_ACCESS);
        }
        	
	}
	
	public static void validateAccess(HttpServletRequest request, long userID) throws Exception
	{
		validateAccess(Sessions.getSession(request), userID);
	}
	
	public static void validateAccess(HttpServletRequest request, String[] allowedRoles) throws Exception
	{
		validateAccess(Sessions.getSession(request), allowedRoles, request.getRequestURI());
	}
	
	public static void validateAccess(HttpServletRequest request, String allowedRole) throws Exception
	{
		validateAccess(Sessions.getSession(request), new String[]{allowedRole}, request.getRequestURI());
	}

	public static void validateAccess(UserSession appSession, String allowedRole, String method) throws Exception
	{
		validateAccess(appSession, new String[]{allowedRole}, method);
	}

	public static void logActivity(UserSession appSession, String method)
	{
		// log the request in the Access logs
	    StringBuffer message = new StringBuffer("ACCESS=(" + method + ") ALLOWED=(ALL) " +
	    		       "ROLE=(" + appSession.role + ") USERNAME=(" + appSession.userName + ") " +
	    			   "USERID=(" + appSession.userId + ") IPADDRESS=(" + appSession.ipAddress + ") " +
	    			   "SESSIONID=(" + appSession.sessionId + ") RESULT=SUCCESS");
		logger.info(message.toString());
	
	}
	
	public static void logActivity(String userName, String method)
	{
		// log the request in the Access logs
	    StringBuffer message = new StringBuffer("ACCESS=(" + method + ") ALLOWED=(ALL) " +
	    		       "ROLE=() USERNAME=(" + userName + ") " +
	    			   "USERID=() IPADDRESS=() " +
	    			   "SESSIONID=() ISCODEKEY=() RESULT=SUCCESS");
		logger.info(message.toString());
	
	}

	public static void logActivityFail(String userName, String method)
	{
		// log the request in the Access logs
	    StringBuffer message = new StringBuffer("ACCESS=(" + method + ") ALLOWED=(ALL) " +
	    		       "ROLE=() USERNAME=(" + userName + ") " +
	    			   "USERID=() IPADDRESS=() " +
	    			   "SESSIONID=() ISCODEKEY=() RESULT=FAILED");
		logger.info(message.toString());
	
	}
}
