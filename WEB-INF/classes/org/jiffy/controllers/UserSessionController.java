package org.jiffy.controllers;

import java.util.concurrent.FutureTask;

import org.apache.commons.lang3.StringUtils;
import org.jiffy.models.User;
import org.jiffy.models.UserSession;
import org.jiffy.server.Flash;
import org.jiffy.server.Sessions;
import org.jiffy.server.db.DB;
import org.jiffy.server.security.PasswordService;
import org.jiffy.server.services.Service;
import org.jiffy.server.services.ServiceRequest;
import org.jiffy.server.services.ServiceResponse;
import org.jiffy.server.services.ServiceTask;
import org.jiffy.server.services.responses.HttpResponse;
import org.jiffy.util.Constants;
import org.jiffy.util.Jiffy;
import org.jiffy.util.Util;

public class UserSessionController extends AppController
{		
	@Service(access=User.ALL)
	public static FutureTask<ServiceResponse> login(final ServiceRequest input)
	{
		return new FutureTask<ServiceResponse>(new ServiceTask(){
			
			@Override
			public ServiceResponse call() throws Exception
			{
				try
				{			
					String username = input.req.getParameter("username");
					String password = input.req.getParameter("password");
					
			        String sql = "SELECT * FROM user WHERE user_name=? AND is_frozen=false";
			        			        
			        User user = DB.selectOne(sql, User.class, username);
					        
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
			            long userId = user.id;
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
			                DB.update("UPDATE user SET last_logon_ts=NOW(), failed_attempts=0 WHERE user_name=?", username);
					        Sessions.addSession(input.req, input.resp, uSess);
			            } 
			            // they entered a valid user name, but incorrect password
			            else 
			            {
			                failedAttempts++;
					                    
			                String updateSql = "UPDATE user SET failed_attempts=? WHERE user_name=?";
			                DB.update(updateSql, failedAttempts, username);
					                    
			                if (failedAttempts > Jiffy.getInt("numberAllowedFailedLogons")) 
			                {
			                	String failSql = "UPDATE user SET is_frozen=true,failed_attempts=0 WHERE id=?";
			                    DB.update(failSql, userId);
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
					Util.printErrorDetails(logger, e);
					Flash.set(input.req, Constants.ERROR_MESSAGE, e.getMessage());
				}
						
				HttpResponse response = new HttpResponse();
				response.nextPage = "/index.jsp";
				return response;		
			};
		});
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
			Util.printErrorDetails(logger, e);
			Flash.set(input.req, Constants.ERROR_MESSAGE, e.getMessage());
		}
		
		HttpResponse response = new HttpResponse();
		response.nextPage = "/index.jsp";
		return response;
	}
}
