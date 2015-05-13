package org.jiffy.server.services;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jiffy.models.UserSession;

public class ServiceRequest
{
	public HttpServletRequest request;
	public HttpServletResponse response;
	public UserSession appSession;
	public String requestType;
	public String restID;
	public boolean shouldReturnJson;
	
	public String getParameter(String param)
	{
		return request.getParameter(param);
	}
	
	
}
