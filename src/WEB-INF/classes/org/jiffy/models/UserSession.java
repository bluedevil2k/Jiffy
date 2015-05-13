package org.jiffy.models;

import java.io.Serializable;
import java.util.List;

import org.jiffy.server.db.DB;
import org.jiffy.server.db.DBResult;
import org.jiffy.server.db.annotations.DBColumn;
import org.jiffy.server.db.annotations.DBTable;
import org.jiffy.server.security.Security;
import org.jiffy.util.Jiffy;

@DBTable
public class UserSession extends JiffyModel implements Serializable
{	
	@DBColumn
	public String sessionId;
	@DBColumn
	public long userId;
	@DBColumn
	public String userName;
	@DBColumn
	public String role;
	@DBColumn
	public String ipAddress;
	@DBColumn
	public java.util.Date logonTime;
	@DBColumn
	public java.util.Date lastUserActivity;

	
	public static UserSessionList getAll() throws Exception 
	{
	  	return DB.selectAll(UserSessionList.class);
	}   
	
	public static UserSession get(String sessionId) throws Exception 
	{
	  	return DB.selectOne(UserSession.class, "WHERE @sessionId@=?", sessionId);
	}

	public static void deleteAll() throws Exception 
	{
	   	DB.update(UserSession.class, "DELETE FROM @table@");
	}    
	    
	public static void delete(long userId) throws Exception 
	{
	   	DB.update(UserSession.class, "DELETE FROM @table@ WHERE @userId@=?", userId);
	}    
	
	
	
	public static long getLoggedInUserCount() throws Exception
	{
	  	return DB.countDistinct(UserSession.class, "@userId@", "WHERE @role@!='admin'");
	}	   
	    
	public static void updateSessionActivity(UserSession session, String requestType) throws Exception 
	{
	    if (session == null)
	        return;
	      
	    DB.update(UserSession.class, "UPDATE @table@ SET @lastUserActivity@=NOW() WHERE @sessionId@=?", session.sessionId);
	}
	
	public static void removeInactiveSessions() throws Exception 
	{
	 	long currTime = System.currentTimeMillis();
	  	long sessionUserActivityTimeout = Jiffy.getInt("sessionUserActivityTimeout");
	    java.util.Date date = new java.util.Date(currTime - sessionUserActivityTimeout);
	               
	    UserSessionList sessions = DB.selectAll(UserSessionList.class, "WHERE @lastUserActivity@ < ?", date);
	        
	    String sql = "DELETE FROM @table@ WHERE @lastUserActivity@ < ?";
	    DB.update(UserSession.class, sql, date);
	        
	    for (int i=0; i<sessions.size(); i++)
	    {
	      	Security.logActivity(sessions.get(i), "Forced Logout");
	    }
	}
	    
	public static List<String> getActiveUsers() throws Exception
	{
	    java.util.Date date = new java.util.Date(System.currentTimeMillis() - (Jiffy.getInt("heartbeatEvictionTime") * 1000));
	    String sql = "SELECT user_name FROM user_session WHERE last_user_activity > ?";
	    DBResult rs2 = DB.select(sql, date);
	    return rs2.getAllRows("user_name");
	}
}
