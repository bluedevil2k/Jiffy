package org.jiffy.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A utility class that replicates the Stream's parallel() functionality found in Java 8, for projects that can't upgrade to Java 8.
 * 
 * I found some of this code a year ago on StackOverflow, but have changed the class and function names so much, I couldn't find the original post and give
 * the author his credit.  So, to that author, thanks.
 * 
 * <p><b>Example Usage</b>:
 * <code>
 * <p>Parallel.forEach(elems, 
 * <br>  new Parallel.Function<Integer>() {
 * <br>    public void execute(Integer param) {
 * <br>       System.out.println(param);
 * <br>    };
 * <br>  });	
 * </code>
 * 
 * @author bluedevil2k
 *
 */
public class Parallel
{
	private static Logger logger = LogManager.getLogger();
		
	private static final int CORES = Runtime.getRuntime().availableProcessors();
	private static final ExecutorService executor = Executors.newFixedThreadPool(CORES / 2);

	public static <T> void forEach(final Iterable<T> elements, final Function<T> function)
	{
		try
		{
			executor.invokeAll(tasks(elements, function));
		}
		catch (InterruptedException ex)
		{
			LogUtil.printErrorDetails(logger, ex);
		}
	}

	private static <T> Collection<Callable<Void>> tasks(final Iterable<T> elements, final Function<T> function)
	{
		List<Callable<Void>> callables = new ArrayList<Callable<Void>>();
		for (final T elem : elements)
		{
			callables.add(new Callable<Void>()
			{
				@Override
				public Void call()
				{
					function.execute(elem);
					return null;
				}
			});
		}

		return callables;
	}

	public static interface Function<T>
	{
		public void execute(T param);
	}
}
