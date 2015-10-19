package org.jiffy.server.services;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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
	
	public int getIntParameter(String param)
	{
		return Integer.parseInt(request.getParameter(param));
	}
	
	public double getDoubleParameter(String param)
	{
		return Double.parseDouble(request.getParameter(param));
	}
	
	public boolean getBooleanParameter(String param)
	{
		return StringUtils.equals(request.getParameter(param), "checked");
	}
	
}
