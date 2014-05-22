package org.jiffy.controllers;

import org.apache.commons.lang3.StringUtils;
import org.jiffy.models.User;
import org.jiffy.models.UserSession;
import org.jiffy.server.Flash;
import org.jiffy.server.Sessions;
import org.jiffy.server.security.PasswordService;
import org.jiffy.server.services.Service;
import org.jiffy.server.services.ServiceRequest;
import org.jiffy.server.services.ServiceResponse;
import org.jiffy.server.services.responses.HttpResponse;
import org.jiffy.util.Constants;
import org.jiffy.util.Jiffy;
import org.jiffy.util.LogUtil;

public class UserSessionController extends AppController
{		
	@Service(access=User.ALL)
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
		        String attemptPassword = PasswordService.encrypt(password);
			        	
		        String attemptRole = user.role;
		        int failedAttempts = user.failedAttempts;
					        	
		        boolean isPasswordValid = StringUtils.equals(user.password, attemptPassword);
		        boolean logonSuccessful = false;    
					            
		        if (isPasswordValid) 
		        {			            	
		          	// normal logon
		           	uSess = new UserSession();
		           	uSess.sessionId = "";
		           	uSess.userId = user.id;
		           	uSess.role = attemptRole;
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
	
	@Service(access=User.ANY_ROLE)
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
