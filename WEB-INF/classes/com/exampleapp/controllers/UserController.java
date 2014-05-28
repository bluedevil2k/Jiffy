package com.exampleapp.controllers;

import org.apache.commons.lang3.StringUtils;
import org.jiffy.controllers.AppController;
import org.jiffy.models.UserSession;
import org.jiffy.server.Flash;
import org.jiffy.server.Sessions;
import org.jiffy.server.security.Roles;
import org.jiffy.server.services.Service;
import org.jiffy.server.services.ServiceRequest;
import org.jiffy.server.services.ServiceResponse;
import org.jiffy.server.services.responses.HttpResponse;
import org.jiffy.server.services.responses.JsonResponse;
import org.jiffy.util.Constants;
import org.jiffy.util.Jiffy;
import org.jiffy.util.LogUtil;
import org.jiffy.util.PasswordUtil;

import com.exampleapp.models.User;
import com.exampleapp.models.UserList;

public class UserController extends AppController
{
	@Service(access=Roles.ADMIN)
	public static ServiceResponse index(ServiceRequest input) throws Exception
	{		
		UserList s = User.lookup();
		
		if (input.wantsJson)
		{
			JsonResponse response = new JsonResponse();
			response.jsonArray = s.toArray();
			return response;
		}
		else
		{
			HttpResponse response = new HttpResponse();
			response.nextPage = "/index.jsp";
			return response;
		}
	}
	
	@Service(access=Roles.ADMIN)
	public static ServiceResponse show(ServiceRequest input) throws Exception
	{
		User u = User.lookup(input.id);

		JsonResponse response = new JsonResponse();
		response.jsonObject = u;
		return response;
	}
	
	@Service(access=Roles.ADMIN)
	public static ServiceResponse destroy(ServiceRequest input) throws Exception
	{
		int id = input.id;
		User.delete(id);
		
		JsonResponse response = new JsonResponse();
		response.jsonObject = "SUCCESS";
		return response;
	}
	
	@Service(access=Roles.ALL)
	public static ServiceResponse login(ServiceRequest input)
	{
		try
		{			
			String username = input.req.getParameter("username");
			String password = input.req.getParameter("password");
						
		    User user = User.lookupForLogin(username);
					        
		    UserSession uSess = null;
					        
		    // there's at least 1 user in the system with this username
		    if (user != null)
		    {
		        // check that the case of the username is the same
		        if (!StringUtils.equals(user.userName, username))
		        {
		            throw new Exception("logon.error.invalidLogon");
		        }

		        // now move onto password    
		        String attemptPassword = PasswordUtil.encrypt(password);
			        	
		        int failedAttempts = user.failedAttempts;
					        	
		        boolean isPasswordValid = StringUtils.equals(user.password, attemptPassword);
		        boolean logonSuccessful = false;    
					            
		        if (isPasswordValid) 
		        {			            	
		          	// normal logon
		           	uSess = new UserSession();
		           	uSess.sessionId = "";
		           	uSess.userId = user.id;
		           	uSess.role = user.role;
		           	uSess.userName = user.userName;
		           	uSess.ipAddress = input.req.getRemoteAddr();
		           	uSess.logonTime = new java.util.Date();
		           	uSess.lastUserActivity = new java.util.Date();
		            logonSuccessful = true;
		        }

		        // they logged in successfully
		        if (logonSuccessful) 
		        {
		            User.markUserAsLoggedIn(username);
		            Sessions.addSession(input.req, input.resp, uSess);
			    } 
			    // they entered a valid user name, but incorrect password
			    else 
			    {
			          failedAttempts++;
				                    
			          User.incrementFailedAttempts(username, failedAttempts);
					                    
			          if (failedAttempts > Jiffy.getInt("numberAllowedFailedLogons")) 
			          {
			        	  User.freezeUser(username);
			          }
			                
			          throw new Exception("logon.error.invalidLogon");
					                 
			     }
			} 
			// they entered an invalid user name, or are frozen out
		    else 
			{
			   throw new Exception("logon.error.invalidLogon");
			}

		}
		catch (Exception e)
		{
			LogUtil.printErrorDetails(logger, e);
			Flash.set(input.req, Constants.ERROR_MESSAGE, e.getMessage());
		}
						
		HttpResponse response = new HttpResponse();
		response.nextPage = "/index.jsp";
		return response;
	}
	
	@Service(access=Roles.ALL_ROLES)
	public static ServiceResponse logout(ServiceRequest input)
	{
		try
		{		
			Sessions.removeSession(input.req, input.resp, input.appSess);
		}
		catch (Exception e)
		{
			LogUtil.printErrorDetails(logger, e);
			Flash.set(input.req, Constants.ERROR_MESSAGE, e.getMessage());
		}
		
		HttpResponse response = new HttpResponse();
		response.nextPage = "/index.jsp";
		return response;
	}
}
