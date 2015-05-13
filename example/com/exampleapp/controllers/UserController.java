package com.exampleapp.controllers;

import org.apache.commons.lang3.StringUtils;
import org.jiffy.controllers.JiffyController;
import org.jiffy.models.UserSession;
import org.jiffy.server.Flash;
import org.jiffy.server.Sessions;
import org.jiffy.server.security.Roles;
import org.jiffy.server.services.Service;
import org.jiffy.server.services.ServiceRequest;
import org.jiffy.server.services.ServiceResponse;
import org.jiffy.server.services.responses.HtmlResponse;
import org.jiffy.server.services.responses.JsonResponse;
import org.jiffy.server.services.responses.NoResponse;
import org.jiffy.util.Constants;
import org.jiffy.util.Jiffy;
import org.jiffy.util.LogUtil;
import org.jiffy.util.PasswordUtil;

import com.exampleapp.models.User;
import com.exampleapp.models.UserList;

public class UserController extends JiffyController
{
	@Service(access=Roles.ADMIN)
	public static ServiceResponse index(ServiceRequest input) throws Exception
	{		
		UserList s = User.getAll();
		
		if (input.shouldReturnJson)
		{
			JsonResponse response = new JsonResponse();
			response.jsonArray = s.toArray();
			return response;
		}
		else
		{
			HtmlResponse response = new HtmlResponse();
			response.nextPage = "/index.jsp";
			return response;
		}
	}
	
	@Service(access=Roles.ADMIN)
	public static ServiceResponse show(ServiceRequest input) throws Exception
	{
		User u = User.get(Integer.parseInt(input.restID));

		if (input.shouldReturnJson)
		{
			JsonResponse response = new JsonResponse();
			response.jsonObject = u;
			return response;
		}
		else
		{
			HtmlResponse response = new HtmlResponse();
			response.nextPage = "/index.jsp";
			return response;
		}
	}
	
	@Service(access=Roles.ADMIN)
	public static ServiceResponse destroy(ServiceRequest input) throws Exception
	{
		int id = Integer.parseInt(input.restID);
		User.delete(id);

		if (input.shouldReturnJson)
		{
			JsonResponse response = new JsonResponse();
			response.jsonObject = "SUCCESS";
			return response;
		}
		else
		{
			return new NoResponse();
		}
	}
	
	@Service(access=Roles.ALL)
	public static ServiceResponse login(ServiceRequest input)
	{
		try
		{			
			String username = input.getParameter("username");
			String password = input.getParameter("password");
						
		    User user = User.getByLogin(username);
					        
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
		           	uSess.ipAddress = input.request.getRemoteAddr();
		           	uSess.logonTime = new java.util.Date();
		           	uSess.lastUserActivity = new java.util.Date();
		            logonSuccessful = true;
		        }

		        // they logged in successfully
		        if (logonSuccessful) 
		        {
		            User.markUserAsLoggedIn(username);
		            Sessions.addSession(input.request, input.response, uSess);
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
			Flash.set(input.request, Constants.ERROR_MESSAGE, e.getMessage());
		}
						
		HtmlResponse response = new HtmlResponse();
		response.nextPage = "/index.jsp";
		return response;
	}
	
	@Service(access=Roles.ALL_ROLES)
	public static ServiceResponse logout(ServiceRequest input)
	{
		try
		{		
			Sessions.removeSession(input.request, input.response, input.appSession);
		}
		catch (Exception e)
		{
			LogUtil.printErrorDetails(logger, e);
			Flash.set(input.request, Constants.ERROR_MESSAGE, e.getMessage());
		}
		
		HtmlResponse response = new HtmlResponse();
		response.nextPage = "/index.jsp";
		return response;
	}
}
