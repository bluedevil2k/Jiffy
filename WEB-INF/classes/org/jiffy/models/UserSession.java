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
public class UserSession implements Serializable
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
	
	public static long getLoggedInUserCount() throws Exception
	{
	  	return DB.countDistinct("user_session", "user_id", "WHERE role!='admin'");
	}

	public static UserSessionList lookup() throws Exception 
	{
	  	return DB.selectAll("SELECT * FROM user_session", UserSessionList.class);
	}
	        
	public static UserSession lookup(String sessionId) throws Exception 
	{
	  	return DB.selectOne("SELECT * FROM user_session WHERE session_id=?", UserSession.class, sessionId);
	}

	public static void deleteSessions() throws Exception 
	{
	   	DB.update("DELETE FROM user_session");
	}    
	    
	public static void deleteSessions(long userId) throws Exception 
	{
	   	DB.update("DELETE FROM user_session WHERE user_id=?", userId);
	}    
	    
	public static void updateSessionActivity(UserSession session, String requestType) throws Exception 
	{
	    if (session == null)
	        return;
	      
	    DB.update("UPDATE user_session SET last_user_activity=NOW() WHERE session_id=?", session.sessionId);
	}
	
	public static void removeInactiveSessions() throws Exception 
	{
	 	long currTime = System.currentTimeMillis();
	  	long sessionUserActivityTimeout = Jiffy.getInt("sessionUserActivityTimeout");
	    java.util.Date date = new java.util.Date(currTime - sessionUserActivityTimeout);
	               
	    String qSql = "SELECT * FROM user_session WHERE last_user_activity < ?";
	    UserSessionList sessions = DB.selectAll(qSql, UserSessionList.class, date);
	        
	    String sql = "DELETE FROM user_session WHERE last_user_activity < ?";
	    DB.update(sql, date);
	        
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
