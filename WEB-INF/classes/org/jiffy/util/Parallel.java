package org.jiffy.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Parallel
{

	//	Parallel.peach(elems, 
	//		 // The operation to perform with each item
	//		 new Parallel.Operation<Integer>() {
	//		    public void perform(Integer param, int counter) {
	//		        System.out.println(param);
	//		    };
	//		});	
	
	private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

	private static final ExecutorService forPool = Executors.newFixedThreadPool(NUM_CORES + 1);

	public static <T> void peach(final Iterable<T> elements, final Operation<T> operation)
	{
		try
		{
			forPool.invokeAll(createCallables(elements, operation));
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	public static <T> void pfor(int start, int end, final Operation<T> operation)
	{
		try
		{
			forPool.invokeAll(createCallables(start, end, operation));
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	private static <T> Collection<Callable<Void>> createCallables(final Iterable<T> elements, final Operation<T> operation)
	{
		List<Callable<Void>> callables = new LinkedList<Callable<Void>>();
		for (final T elem : elements)
		{
			callables.add(new Callable<Void>()
			{
				@Override
				public Void call()
				{
					operation.perform(elem, 0);
					return null;
				}
			});
		}

		return callables;
	}
	
	private static <T> Collection<Callable<Void>> createCallables(final int start, final int stop, final Operation<T> operation)
	{
		List<Callable<Void>> callables = new LinkedList<Callable<Void>>();
		for (int i=start; i<stop; i++)
		{
			final int counter = i;
			callables.add(new Callable<Void>()
			{
				@Override
				public Void call()
				{
					operation.perform(null, counter);
					return null;
				}
			});
		}

		return callables;
	}

	public static interface Operation<T>
	{
		public void perform(T pParameter, int counter);
	}
}
