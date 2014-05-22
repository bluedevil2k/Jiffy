package org.jiffy.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jiffy.server.db.DB;
import org.jiffy.server.db.DBResult;
import org.jiffy.server.db.annotations.DBColumn;
import org.jiffy.server.db.annotations.DBTable;

@DBTable
public class User implements Serializable
{	
	//////////
	// Outline all the roles available in the software
	//////////
	public static final String ADMIN = "admin";
	public static final String USER = "user";

	public static final String[] ALL_ROLES = new String[]{ADMIN, USER};
	
	// a shortcut to ALL_ROLES, since we can't use [] with Services
	public static final String ANY_ROLE = "any_role";
	// no access for anyone
	public static final String NO_ACCESS = "no_one";
	// everyone is allowed, even those that aren't logged in (e.g. index.jsp)
	public static final String ALL = "all";	
	
	@DBColumn
	public long id;
	@DBColumn
	public String userName;
	@DBColumn
	public String role;
	@DBColumn
	public String firstName;
	@DBColumn
	public String lastName;
	@DBColumn
	public String email;
	@DBColumn
	public String company;
	@DBColumn
	public String address1;
	@DBColumn
	public String address2;
	@DBColumn
	public String city;
	@DBColumn
	public String state;
	@DBColumn
	public String postalCode;
	@DBColumn
	public String phonePrimary;
	@DBColumn
	public String phoneCell;
	@DBColumn
	public String phoneFax;
	@DBColumn
	public int failedAttempts;
	@DBColumn
	public String password;
	@DBColumn
	public boolean forcePwChange;
	@DBColumn
	public boolean isFrozen = false;
	@DBColumn
	public boolean isDisabled = false;
	@DBColumn
	public String custom1;
	@DBColumn
	public String custom2;
	@DBColumn
	public String custom3;
	@DBColumn
	public String custom4;
	@DBColumn
	public String custom5;
	
	/* An example of a 1-to-many database relation
	@DBHasMany
	public ItemList items;
	*/
	
	/* An example of a 1-to-1 database relation	
	@DBHasOne
	public Item item;
	*/
			
	public static User lookup(long id) throws Exception
	{
		return DB.selectOne(User.class, "WHERE @id@=?", id);
	}

	public static UserList lookup(String userRole) throws Exception
	{
		return DB.selectAll(UserList.class, "WHERE @role@=? ORDER BY @userName@", userRole);
	}

	public static UserList lookup() throws Exception
	{
		return DB.selectAll(UserList.class, "ORDER BY @userName@");
	}
	
	public static User lookupForLogin(String username) throws Exception
	{
		return DB.selectOne(User.class, "WHERE @userName@=? AND @isFrozen@=false", username);
	}
	
	public static void markUserAsLoggedIn(String username) throws Exception
	{
		DB.update(User.class, "UPDATE @table@ SET @lastLogonTs@=NOW(), @failedAttempts@=0 WHERE @userName@=?", username);
	}
	
	public static void incrementFailedAttempts(String username, int failedAttempts) throws Exception
	{
        String updateSql = "UPDATE @table@ SET @failedAttempts@=? WHERE @userName@=?";
        DB.update(User.class, updateSql, failedAttempts, username);
	}
	
	public static void freezeUser(String username) throws Exception
	{
		String failSql = "UPDATE @table@ SET @isFrozen@=true, @failedAttempts@=0 WHERE @userName@=?";
        DB.update(User.class, failSql, username);
	}
	
	public static List<Long> getAllUserIDs(String userRole) throws Exception
	{
		String sql = "SELECT DISTINCT id FROM user WHERE role=? ORDER BY user_name ASC";
		DBResult rs = DB.select(sql, userRole);
		return rs.getAllRows("id");
	}

	public static List<Long> getAllUserIDs(List<String> userRoles) throws Exception
	{
		List<Long> toReturn = new ArrayList<Long>();
		for (Iterator<String> iter = userRoles.iterator(); iter.hasNext();)
		{
			toReturn.addAll(getAllUserIDs(iter.next()));
		}
		return toReturn;
	}
	
	public static void delete(int id) throws Exception
	{
		DB.update(User.class, "DELETE FROM @table@ WHERE @id@=?", id);
	}
}
