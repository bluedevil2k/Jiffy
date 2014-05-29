package org.jiffy.server.cache;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.jiffy.util.Jiffy;

import com.couchbase.client.CouchbaseClient;

public class Cache
{
	public static final String JVM = "jvm";
	public static final String MEMCACHED = "memcached";

	// @X-JVM-safe this member is read-only after init()
	// @Thread-safe this member is read-only after init()
	private static String cacheType = JVM;
	// @X-JVM-safe this member is read-only after init()
	// @Thread-safe this member is read-only after init()
	private static CouchbaseClient cacheClient;
	// @X-JVM-safe this member is only used one 1-server deploys
	// @Thread-safe this member is inherently thread-safe
	private static ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<String, Object>();

	// @X-JVM-Synchronized only called by HtmlServlet.init()
	// @Synchronized only called by HtmlServlet.init()
	public static ConcurrentHashMap<String, Object> init() throws Exception
	{
		if (Jiffy.getBool("isSingleServerDeploy"))
		{
			cacheType = JVM;
		}
		else
		{
			cacheType = MEMCACHED;
		}
		
		if (StringUtils.equals(cacheType, MEMCACHED))
		{
			List<URI> nodes = Arrays.asList( new URI("http://" + Jiffy.getValue("cacheServer") + ":" + Jiffy.getValue("cachePort") + "/pools"));

			// connect to the CouchbaseClient (memcached)
			cacheClient = new CouchbaseClient(nodes, Jiffy.getValue("cacheBucket"), Jiffy.getValue("cacheUsername"), Jiffy.getValue("cachePassword"));	
		}
		else
		{

		}
		
		return cache;
	}

	public static Object get(String key)
	{
		if (StringUtils.equals(cacheType, MEMCACHED))
		{
			return cacheClient.get(key);
		}
		else
		{
			return cache.get(key);
		}
	}

	public static Object set(String key, Object value)
	{
		if (StringUtils.equals(cacheType, MEMCACHED))
		{
			return cacheClient.set(key, 0, value);
		}
		else
		{
			return cache.put(key, value);
		}
	}

	public static Object delete(String key)
	{
		if (StringUtils.equals(cacheType, MEMCACHED))
		{
			return cacheClient.delete(key);
		}
		else
		{
			return cache.remove(key);
		}
	}
}
