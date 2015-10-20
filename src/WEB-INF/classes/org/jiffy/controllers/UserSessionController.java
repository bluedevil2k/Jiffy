package org.jiffy.controllers;

import org.apache.commons.lang3.StringUtils;
import org.jiffy.models.JiffyUser;
import org.jiffy.models.UserSession;
import org.jiffy.server.Flash;
import org.jiffy.server.Sessions;
import org.jiffy.server.security.Roles;
import org.jiffy.server.services.Service;
import org.jiffy.server.services.ServiceRequest;
import org.jiffy.server.services.ServiceResponse;
import org.jiffy.server.services.responses.HtmlResponse;
import org.jiffy.util.Constants;
import org.jiffy.util.Jiffy;
import org.jiffy.util.LogUtil;
import org.jiffy.util.PasswordUtil;
import org.jiffy.util.Text;

public class UserSessionController extends JiffyController
{		
	@Service(access=Roles.ALL)
	public static ServiceResponse login(ServiceRequest input)
	{
		String onFail = "";
		
		try
		{			
			String username = input.getParameter("username");
			String password = input.getParameter("password");
			String onSuccess = input.getParameter("successPage");
			onFail = input.getParameter("errorPage");
						
		    JiffyUser user = JiffyUser.getByUsername(username);
					        
		    // there's at least 1 user in the system with this username
		    if (user != null)
		    {
		    	// if their account is frozen
		    	if (user.isFrozen)
		    	{
		    		throw new Exception(Text.get("login.locked"));
		    	}
		    	
		        // check that the case of the username is the same
		        if (!StringUtils.equals(user.userName, username))
		        {
		            throw new Exception(Text.get("login.error"));
		        }

		        // now move onto password    
		        String attemptPassword = PasswordUtil.encrypt(password);
			        	
					        	
		        boolean isPasswordValid = StringUtils.equals(user.password, attemptPassword);
					            
		        if (isPasswordValid) 
		        {			         
					UserSession session = UserSession.createUserSession(user, input.request.getRemoteAddr());
			        Sessions.addSession(input.request, input.response, session);
		        }
			    // they entered a valid user name, but incorrect password
			    else 
			    {
			        int failedAttempts = user.failedAttempts;		          
			        failedAttempts++;
				                    
			        JiffyUser.incrementFailedAttempts(username, failedAttempts);
					                    
			        if (failedAttempts > Jiffy.getInt("numberAllowedFailedLogons")) 
			        {
			        	JiffyUser.freezeUser(username);
			        }
			                
			        throw new Exception(Text.get("login.error"));					                 
			     }
			} 
			// they entered an invalid user name
		    else 
			{
			   throw new Exception(Text.get("login.error"));
			}

			HtmlResponse response = new HtmlResponse();
			response.nextPage = onSuccess;
			return response;

		}
		catch (Exception e)
		{
			LogUtil.printErrorDetails(logger, e);
			Flash.set(input.request, Constants.ERROR_MESSAGE, e.getMessage());
			
			HtmlResponse response = new HtmlResponse();
			response.nextPage = onFail;
			return response;
		}						
	}
	
	@Service(access=Roles.ALL_ROLES)
	public static ServiceResponse logout(ServiceRequest input)
	{
		String nextPage = "";
		
		try
		{		
			nextPage = input.getParameter("nextPage");
			Sessions.removeSession(input.request, input.response, input.appSession);
		}
		catch (Exception e)
		{
			LogUtil.printErrorDetails(logger, e);
			Flash.set(input.request, Constants.ERROR_MESSAGE, e.getMessage());
		}
		
		HtmlResponse response = new HtmlResponse();
		response.nextPage = nextPage;
		return response;
	}
}
