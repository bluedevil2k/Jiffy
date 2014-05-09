package org.jiffy.server.services;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class ServiceResponse 
{
	public abstract void respond(HttpServletRequest req, HttpServletResponse resp) throws Exception;
}
