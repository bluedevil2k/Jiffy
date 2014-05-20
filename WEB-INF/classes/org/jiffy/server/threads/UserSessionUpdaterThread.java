package org.jiffy.server.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jiffy.models.UserSession;
import org.jiffy.util.LogUtil;

public class UserSessionUpdaterThread implements Runnable
{
	private static Logger logger = LogManager.getLogger();

	private volatile boolean running = false;
	private long SESSION_UPDATE_INTERVAL = 5 * 1000;

	public UserSessionUpdaterThread()
	{
	}

	@Override
	public void run()
	{
		System.out.println("***** Session Updater Thread Started");
		logger.info("***** Session Updater Thread Started");
		
		running = true;
		try
		{
			while (running)
			{
				UserSession.removeInactiveSessions();
				Thread.sleep(SESSION_UPDATE_INTERVAL);
			}
		}
		catch (Exception ex)
		{
			System.out.println("***** Session Updater Threw an Error");
			logger.error("***** Session Updater Threw an Error");
			LogUtil.printErrorDetails(logger, ex);
		}
	}

	public void stop()
	{
		running = false;
	}
}
