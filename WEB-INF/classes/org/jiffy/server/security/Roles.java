package org.jiffy.server.security;

public class Roles 
{
	//////////
	// Outline all the roles available in the software
	//////////
	public static final String ADMIN = "admin";
	public static final String USER = "user";

	public static final String[] ALL_SYSTEM_ROLES = new String[]{ADMIN, USER};
	
	// a shortcut to ALL_ROLES, since we can't use [] with Services
	public static final String ALL_ROLES = "any_role";
	// no access for anyone
	public static final String NO_ACCESS = "no_one";
	// everyone is allowed, even those that aren't logged in (e.g. index.jsp)
	public static final String ALL = "all";	
}
