package org.jiffy.server.services.responses;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jiffy.server.services.ServiceResponse;

public class NoResponse extends ServiceResponse
{
	public NoResponse()
	{
		super();
		responseType = ServiceResponse.NO;
	}

	@Override
	public void respond(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		
	}
}
