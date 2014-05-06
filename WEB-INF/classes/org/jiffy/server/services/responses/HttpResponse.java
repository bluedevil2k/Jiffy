package org.jiffy.server.services.responses;

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

}
