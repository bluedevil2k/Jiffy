package org.jiffy.server.services;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class ServiceResponse {

	public static final String HTTP = "http";
	public static final String JSON = "json";
	public static final String NO = "no";
	
	public String responseType;
	
	public abstract void respond(HttpServletRequest req, HttpServletResponse resp) throws Exception;
}
