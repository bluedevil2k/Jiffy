package org.jiffy.server;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jiffy.models.UserSession;
import org.jiffy.server.cache.Cache;
import org.jiffy.server.db.DB;
import org.jiffy.util.Constants;
import org.jiffy.util.Jiffy;
import org.jiffy.util.PasswordUtil;

public class Sessions
{
	public static final String JVM = "jvm";
	public static final String DATABASE = "db";
	
	private static final String SESSION = "session.";

	// @X-JVM-safe this member is read-only after init()
	// @Thread-safe this member is read-only after init()
	private static String sessionType = JVM;

	// @X-JVM-Synchronized only called by HtmlServlet.init()
	// @Synchronized only called by HtmlServlet.init()
	public static void init() throws Exception
	{
		if (Jiffy.getBool("isSingleServerDeploy"))
		{
			sessionType = JVM;
		}
		else
		{
			sessionType = DATABASE;
		}
	}

	public static void addSession(HttpServletRequest req, HttpServletResponse res, UserSession appSession) throws Exception
	{
		if (StringUtils.equals(sessionType, DATABASE))
		{
			// create a random session ID, independent of the JVM's session ID
			appSession.sessionId = PasswordUtil.autogeneratePassword(40, PasswordUtil.ALPHA_NUMERIC);

			// write that sessionID to the app ID in the cookie
			Cookies.addCookieExpiresWithBrowser(res, Jiffy.getValue("sessionAppID"), appSession.sessionId);
			
			// create a Map in the Cache to store session variables
			Map<String, Object> map = new HashMap<String, Object>();
			Cache.set(SESSION + appSession.sessionId, map);
		}
		else
		{
			// Just use the normal JVM session here
			req.getSession().setAttribute(Constants.SESSION, appSession);
			appSession.sessionId = req.getSession().getId();
		}

		// Store it in the database
		String sql = "INSERT INTO @table@ (@sessionId@, @userId@, @userName@, @role@, @ipAddress@, @logonTime@, @lastUserActivity@) VALUES (?,?,?,?,?,?,?)";

		DB.update(UserSession.class, sql, appSession.sessionId, appSession.userId, appSession.userName, appSession.role, appSession.ipAddress, appSession.logonTime, appSession.lastUserActivity);

	}

	public static void removeSession(HttpServletRequest req, HttpServletResponse res, UserSession appSession) throws Exception
	{
		if (StringUtils.equals(sessionType, DATABASE))
		{
			// Remove the cookie
			Cookies.removeCookie(res, Jiffy.getValue("sessionAppID"));
			
			// Remove the Map from the Cache with this session ID
			if (appSession != null)
			{
				Cache.delete(SESSION + appSession.sessionId);
			}
		}
		else
		{
			// Just use the normal JVM session here
			req.getSession().removeAttribute(Constants.SESSION);
		}

		// always invalidate the session so a new ID is generated
		req.getSession().invalidate();

		// Delete the session from the DB
		String sql = "DELETE FROM @table@ WHERE @sessionId@=?";
		if (appSession != null)
		{
			DB.update(UserSession.class, sql, appSession.sessionId);
		}
	}

	public static UserSession getSession(HttpServletRequest req)
	{
		if (StringUtils.equals(sessionType, DATABASE))
		{
			String sessionID = Cookies.getCookieValue(req, Jiffy.getValue("sessionAppID"));

			if (StringUtils.isEmpty(sessionID))
			{
				return null;
			}

			try
			{
				UserSession session = DB.selectOne(UserSession.class, "WHERE @sessionId@=?", sessionID);
				return session;
			}
			catch (Exception ex)
			{
				return null;
			}
		}
		else
		{
			return (UserSession) req.getSession().getAttribute(Constants.SESSION);
		}
	}

	public static Object get(HttpServletRequest req, String key) throws Exception
	{
		if (StringUtils.equals(sessionType, DATABASE))
		{
			Map<String, Object> map =  (Map)Cache.get(SESSION + Cookies.getCookieValue(req, Jiffy.getValue("sessionAppID")));

			if (map == null)
			{
				return null;
			}
			
			return map.get(key);
		}
		else
		{
			return req.getSession().getAttribute(key);
		}
	}

	public static void set(HttpServletRequest req, String key, Object value) throws Exception
	{
		if (StringUtils.equals(sessionType, DATABASE))
		{
			String sessionID = SESSION + Cookies.getCookieValue(req, Jiffy.getValue("sessionAppID"));
			Map<String, Object> map =  (Map)Cache.get(sessionID);
			
			if (map == null)
			{
				return;
			}
			
			map.put(key, value);
			Cache.set(sessionID, map);
		}
		else
		{
			req.getSession().setAttribute(key, value);
		}
	}

	public static void remove(HttpServletRequest req, String key) throws Exception
	{
		if (StringUtils.equals(sessionType, DATABASE))
		{
			String sessionID = SESSION + Cookies.getCookieValue(req, Jiffy.getValue("sessionAppID"));
			Map<String, Object> map =  (Map)Cache.get(sessionID);
			
			if (map == null)
			{
				return;
			}
			
			map.remove(key);
			Cache.set(sessionID, map);
		}
		else
		{
			req.getSession().removeAttribute(key);
		}
	}
}
