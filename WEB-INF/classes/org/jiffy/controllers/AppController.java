package org.jiffy.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jiffy.models.User;
import org.jiffy.server.services.Service;
import org.jiffy.server.services.ServiceRequest;
import org.jiffy.server.services.ServiceResponse;
import org.jiffy.server.services.responses.NoResponse;

public class AppController 
{
	protected static Logger logger = LogManager.getLogger();

	@Service(access=User.NO_ACCESS)
	public static ServiceResponse index(ServiceRequest input) throws Exception
	{
		return new NoResponse();
	}

	@Service(access=User.NO_ACCESS)
	public static ServiceResponse show(ServiceRequest input) throws Exception
	{
		return new NoResponse();
	}

	@Service(access=User.NO_ACCESS)
	public static ServiceResponse destroy(ServiceRequest input) throws Exception
	{
		return new NoResponse();
	}

	@Service(access=User.NO_ACCESS)
	public static ServiceResponse create(ServiceRequest input) throws Exception
	{
		return new NoResponse();
	}

	@Service(access=User.NO_ACCESS)
	public static ServiceResponse update(ServiceRequest input) throws Exception
	{
		return new NoResponse();
	}
}
