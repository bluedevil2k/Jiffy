package org.jiffy.server.services.responses;

import org.jiffy.server.services.ServiceResponse;

public class JsonResponse extends ServiceResponse
{
	public boolean isAjax;
	public String ajaxResponse;
	
	public JsonResponse()
	{
		super();
		responseType = ServiceResponse.JSON;
	}
}
