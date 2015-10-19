package org.jiffy.models;

import java.util.Date;

import org.jiffy.server.db.DB;
import org.jiffy.server.db.annotations.DBColumn;
import org.jiffy.server.db.annotations.DBTable;
import org.jiffy.util.PasswordUtil;

@DBTable(table="user")
public class JiffyUser extends JiffyModel 
{
	@DBColumn
	public String userName;
	@DBColumn
	public String password;
	@DBColumn
	public String role;
	@DBColumn
	public String email;
	@DBColumn
	public int failedAttempts;
	@DBColumn
	public boolean forcePwChange;
	@DBColumn
	public boolean isFrozen = false;
	@DBColumn
	public Date lastLogonTs;
	

	public static JiffyUser getByUsername(String username) throws Exception
	{
		return DB.selectOne(JiffyUser.class, "WHERE @userName@=?", username);
	}
	
	public static JiffyUser getById(long userId) throws Exception
	{
		return DB.selectOne(JiffyUser.class, "WHERE id=?", userId);
	}
	
	public static void markUserAsLoggedIn(String username) throws Exception
	{
		DB.update(JiffyUser.class, "UPDATE @table@ SET @lastLogonTs@=NOW(), @failedAttempts@=0 WHERE @userName@=?", username);
	}
	public static void incrementFailedAttempts(String username, int failedAttempts) throws Exception
	{
        String updateSql = "UPDATE @table@ SET @failedAttempts@=? WHERE @userName@=?";
        DB.update(JiffyUser.class, updateSql, failedAttempts, username);
	}
	
	public static void freezeUser(String username) throws Exception
	{
		String failSql = "UPDATE @table@ SET @isFrozen@=true, @failedAttempts@=0 WHERE @userName@=?";
        DB.update(JiffyUser.class, failSql, username);
	}
	
	public static void setNewPassword(long userId, String password) throws Exception
	{
		String sql = "UPDATE @table@ SET @password@=?, @forcePwChange@=? WHERE @id@=?";
		DB.update(JiffyUser.class, sql, PasswordUtil.encrypt(password), true, userId);
	}
}
