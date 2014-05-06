
package org.jiffy.server;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jiffy.models.UserSession;
import org.jiffy.server.cache.Cache;
import org.jiffy.server.db.DB;
import org.jiffy.server.security.Security;
import org.jiffy.server.services.Service;
import org.jiffy.server.services.ServiceRequest;
import org.jiffy.server.services.ServiceResponse;
import org.jiffy.server.services.responses.HttpResponse;
import org.jiffy.server.services.responses.JsonResponse;
import org.jiffy.server.threads.UserSessionUpdaterThread;
import org.jiffy.util.Constants;
import org.jiffy.util.JSPUtil;
import org.jiffy.util.Jiffy;
import org.jiffy.util.Util;

@WebServlet(name="JiffyHTTP", 
            displayName="JiffyHTTP", 
            urlPatterns={ "*.serv" }, 
            loadOnStartup=2,
            initParams={ @WebInitParam(name = "nocache", value = "true")})
public class JiffyHttpServlet extends HttpServlet
{	
	private static Logger logger = LogManager.getLogger();

	// @X-JVM-safe this member is only used in 1-server deploys
	// @Thread-safe this member is inherently thread-safe
	private ConcurrentHashMap<String, Object> cache;
	
	@Override
	public void init() throws ServletException
	{
		System.out.println("*****************************");
		System.out.println("*** Jiffy Servlet Started ***");
		System.out.println("*****************************");
		logger.info("******************************************");
		logger.info("     Jiffy Servlet Started.");
		logger.info("******************************************");
		
		try
		{
			////////////////
			// Init the Settings
			///////////////
			Jiffy.configure();
			System.out.println("***** Jiffy Configured");
			logger.info("***** Jiffy Configured");
			
			////////////////
			// Init the DB
			////////////////
			DB.init();
			System.out.println("***** DB Initialized");
			logger.info("***** DB Initialized");
			
			////////////////
			// Init the Cache
			// Save the return value, so the JVM cache stays in memory and doesn't get GC'ed
			////////////////
			cache = Cache.init();
			System.out.println("***** Cache Initialized");
			logger.info("***** Cache Initialized");
			
			////////////////
			// Init the Sessions
			////////////////
			Sessions.init();
			System.out.println("***** Sessions Initialized");
			logger.info("***** Sessions Initialized");
			
			///////////////
			// Start the ScheduleThread & UserSessionThread if and only if it's the "admin" server
			///////////////
			boolean isAdminServer = Jiffy.getBool("isAdminServer");
			System.out.println("***** IsAdminServer => " + isAdminServer);
			logger.info("***** IsAdminServer => " + isAdminServer);
			
			if (isAdminServer)
			{
				// if the sessions are jvm based, we need to delete the sessions stored in the DB
				// if the sessions are db based, we don't need to do this
				if (Jiffy.getBool("isSingleServerDeploy"))
				{
					UserSession.deleteSessions();
				}
				
				// start the session updater thread
		        UserSessionUpdaterThread sessionUpdater = new UserSessionUpdaterThread();
		        Thread t2 = new Thread(sessionUpdater);
		        t2.start();
			}
			
			////////////////
			// Preload the caches if and only if it's the "admin" server
			////////////////
			if (isAdminServer)
			{
				
			}
			
			//////////////////
			// Log other settings about the server
			//////////////////
			logger.info("***** " + Jiffy.getValue("version"));
			System.out.println("***** " + Jiffy.getValue("version"));
			logger.info("***** Running on IP Address " + InetAddress.getLocalHost());
			System.out.println("***** Running on IP Address " + InetAddress.getLocalHost());
		}
		catch (Exception ex)
		{
			System.out.println("^^^^  ERROR STARTING JIFFY SERVLET   ^^^^");
			logger.info("^^^^  ERROR STARTING JIFFY SERVLET   ^^^^");
			System.out.println(ex.getMessage());
			Util.printErrorDetails(logger, ex);
            throw new ServletException(ex.getMessage());
		}
	}
	
	private void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{	
    	// This line ensures all characters are accepted when typed into text fields, including euro, pound, yen, etc.
    	req.setCharacterEncoding("UTF-8");
    	    	    	
		// parse in the incoming request
		String requestURI = req.getRequestURI().substring(1, req.getRequestURI().length() - 5);
		String controller = StringUtils.capitalize(Util.underscoreToCamel(requestURI.substring(0, requestURI.indexOf("/"))));
		String method = StringUtils.lowerCase(Util.underscoreToCamel(requestURI.substring(requestURI.indexOf("/") + 1)));
		
		try
		{			
	    	// get the session
			UserSession appSess = Sessions.getSession(req);
			
			// build the input object
			ServiceRequest input = new ServiceRequest();
			input.appSess = appSess;
			input.req = req;
			input.requestType = method;
			input.resp = resp;
			
			// get the controller
			Class c = Class.forName("org.jiffy.controllers." + controller + "Controller");
			
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
				Util.printErrorDetails(logger, e, "JiffyHttpServlet Exception");
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
		handleRequest(req, resp);
	}
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		handleRequest(req, resp);
	}
}
