package org.jiffy.server.services.responses;

import org.jiffy.server.services.ServiceResponse;

public class NoResponse extends ServiceResponse
{
	public NoResponse()
	{
		super();
		responseType = ServiceResponse.NO;
	}
}
