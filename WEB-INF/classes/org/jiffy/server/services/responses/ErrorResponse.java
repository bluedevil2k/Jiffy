package org.jiffy.server.services.responses;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jiffy.server.services.ServiceResponse;

public class ErrorResponse extends ServiceResponse 
{
	public int errorCode;
	
	@Override
	public void respond(HttpServletRequest req, HttpServletResponse resp) throws Exception 
	{
		resp.sendError(errorCode);
	}

}
