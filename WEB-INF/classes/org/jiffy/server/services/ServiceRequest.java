package org.jiffy.server.services;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jiffy.models.UserSession;

public class ServiceRequest
{
	public HttpServletRequest req;
	public HttpServletResponse resp;
	public UserSession appSess;
	public String requestType;
	public int id;
	public boolean wantsJson;
}
