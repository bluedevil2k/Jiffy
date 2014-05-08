package org.jiffy.server.services.responses;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jiffy.server.services.ServiceResponse;

public class HttpResponse extends ServiceResponse
{
	public String forwardTo;
	public boolean doRedirect;
	
	public HttpResponse()
	{
		super();
		responseType = ServiceResponse.HTTP;
	}

	@Override
	public void respond(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		if (doRedirect)
		{
			resp.sendRedirect(forwardTo);
		}
		else
		{
			req.getRequestDispatcher(forwardTo).forward(req, resp);
		}
	}

}
