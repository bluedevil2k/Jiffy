package org.jiffy.controllers;

import org.jiffy.models.User;
import org.jiffy.models.UserList;
import org.jiffy.server.db.DB;
import org.jiffy.server.services.Service;
import org.jiffy.server.services.ServiceRequest;
import org.jiffy.server.services.ServiceResponse;
import org.jiffy.server.services.responses.HttpResponse;
import org.jiffy.server.services.responses.JsonResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserController 
{
	@Service(access=User.ADMIN)
	public static ServiceResponse index(ServiceRequest input) throws Exception
	{
		UserList s = User.lookup();
		
		if (input.wantsJson)
		{
			JsonResponse response = new JsonResponse();
			response.text = new JSONArray(s).toString();
			return response;
		}
		else
		{
			HttpResponse response = new HttpResponse();
			response.nextPage = "/index.jsp";
			return response;
		}
	}
	
	@Service(access=User.ADMIN)
	public static ServiceResponse show(ServiceRequest input) throws Exception
	{
		User u = User.lookup(input.id);

		JsonResponse response = new JsonResponse();
		response.text = new JSONObject(u).toString();
		return response;
	}
	
	@Service(access=User.ADMIN)
	public static ServiceResponse destroy(ServiceRequest input) throws Exception
	{
		String sql = "DELETE FROM users WHERE id=?";
		DB.update(sql, input.id);
		
		JsonResponse response = new JsonResponse();
		response.text = "SUCCESS";
		return response;
	}
}
