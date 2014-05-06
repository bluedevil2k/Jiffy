
package org.jiffy.server;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jiffy.models.UserSession;
import org.jiffy.server.security.Security;
import org.jiffy.server.services.Service;
import org.jiffy.server.services.ServiceRequest;
import org.jiffy.server.services.ServiceResponse;
import org.jiffy.server.services.responses.HttpResponse;
import org.jiffy.server.services.responses.JsonResponse;
import org.jiffy.util.Constants;
import org.jiffy.util.JSPUtil;
import org.jiffy.util.Util;

@WebServlet(name="JiffyREST", 
            displayName="JiffyREST", 
            urlPatterns={ "/rest/*" }, 
            loadOnStartup=1,
            initParams={ @WebInitParam(name = "nocache", value = "true")})
public class JiffyRestServlet extends HttpServlet
{	
	private static Logger logger = LogManager.getLogger();
	
	@Override
	public void init() throws ServletException
	{
		System.out.println("*****************************");
		System.out.println("***   Jiffy REST Started   ***");
		System.out.println("*****************************");
		logger.info("******************************************");
		logger.info("     Jiffy REST Started.");
		logger.info("******************************************");
	}
	
	private void handleRequest(HttpServletRequest req, HttpServletResponse resp, String restType) throws ServletException, IOException
	{	
    	// This line ensures all characters are accepted when typed into text fields, including euro, pound, yen, etc.
    	req.setCharacterEncoding("UTF-8");
    	    	    	
		// parse in the incoming request
		String requestURI = req.getRequestURI().substring(6);
		String controller = StringUtils.capitalize(Util.underscoreToCamel(requestURI.substring(0, requestURI.indexOf("/"))));
		String IDString = "";
		if (requestURI.indexOf("/") < requestURI.length()-1)
		{
			IDString = Util.underscoreToCamel(requestURI.substring(requestURI.indexOf("/") + 1));
		}
		int ID = 0;
		if (NumberUtils.isNumber(IDString))
		{
			ID = Integer.parseInt(IDString);
		}
		
		try
		{			
	    	// get the session
			UserSession appSess = Sessions.getSession(req);
			
			// build the input object
			ServiceRequest input = new ServiceRequest();
			input.appSess = appSess;
			input.req = req;
			input.requestType = restType;
			input.resp = resp;
			input.id = ID;
			input.wantsJson = req.getHeader("accept").indexOf("json") > -1;
			
			// get the controller
			Class c = Class.forName("org.jiffy.controllers." + controller + "Controller");
					
			// do the REST analysis
			String method = "";
			if (ID == 0)
			{
				if (StringUtils.equals(restType, "GET"))
				{
					method = "index";
				}
				else if (StringUtils.equals(restType, "POST"))
				{
					method = "create";
				}
			}
			else if (ID > 0)
			{
				if (StringUtils.equals(restType, "GET"))
				{
					method = "show";
				}
				else if (StringUtils.equals(restType, "POST"))
				{
					method = "update";
				}
				else if (StringUtils.equals(restType, "PUT"))
				{
					method = "update";
				}
				else if (StringUtils.equals(restType, "DELETE"))
				{
					method = "destroy";
				}
			}
			
			// get the method that will be invoked
			Method m = c.getMethod(method, ServiceRequest.class);
			
			// if there's no annotation, it's an invalid access
			if (!m.isAnnotationPresent(Service.class))
			{
				throw new Exception(Constants.INVALID_ACCESS);
			}
			
			// read all the roles allowed on the service method
			String role = m.getAnnotation(Service.class).access();
			
			
			// if there's no role defined, or the NO_USERS is specifically defined, it's an invalid access
			if (StringUtils.isEmpty(role) || StringUtils.equals(role, UserSession.NO_USERS))
			{
				throw new Exception(Constants.INVALID_ACCESS);
			}
				
			// a group of roles is defined, access check them
			if (StringUtils.equals(role, UserSession.ANY_USER))
			{
				Security.validateAccess(appSess, UserSession.ALL_ROLES, method);
			}
			// do no access checks
			else if (StringUtils.equals(role, UserSession.ANYONE))
			{
				
			}
			// a specific role is defined, access check it
			else
			{
				Security.validateAccess(appSess, new String[]{role}, method);
			}
			
			// invoke the service method
			ServiceResponse response = (ServiceResponse)m.invoke(null, input);
			
			// get the response and check the output
			boolean doRedirect = false;
			String forwardTo = null;
			
			switch (response.responseType)
			{
				case ServiceResponse.NO:
				{
					return;
				}
						
				case ServiceResponse.JSON:
				{
					JsonResponse r = (JsonResponse)response;
			    	JSPUtil.setJSPHeader(resp);
					resp.setCharacterEncoding("UTF-8");
					resp.getWriter().print(r.ajaxResponse);
					resp.getWriter().flush();
					return;
				}
					
				case ServiceResponse.HTTP:
				{
					HttpResponse r = (HttpResponse)response;
					doRedirect = r.doRedirect;
					forwardTo = r.forwardTo;
					break;
				}
			}	
			
			// now forward to the appropriate page
			if (doRedirect)
			{
				resp.sendRedirect(forwardTo);
			}
			else
			{
				req.getRequestDispatcher(forwardTo).forward(req, resp);
			}

		}
		catch (Exception e)
		{
			if (StringUtils.equals(Constants.INVALID_ACCESS, e.getMessage()))
			{
				resp.sendError(500);
			}
			else
			{
				Util.printErrorDetails(logger, e, "JiffyRESTServlet Exception");
				req.setAttribute("javax.servlet.jsp.jspException", e);
				req.setAttribute(Constants.EXCEPTION, e);
				req.setAttribute(Constants.ERROR, e);
				req.getRequestDispatcher("/error.jsp").forward(req, resp);
			}
		}
	}
	
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		handleRequest(req, resp, "POST");
	}

	@Override
	public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		handleRequest(req, resp, "DELETE");
	}
	
	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		handleRequest(req, resp, "PUT");
	}
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		handleRequest(req, resp, "GET");
	}

}
