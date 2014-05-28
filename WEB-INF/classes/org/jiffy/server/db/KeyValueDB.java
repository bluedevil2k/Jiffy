package org.jiffy.server.db;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jiffy.util.Constants;
import org.jiffy.util.Jiffy;

import com.couchbase.client.CouchbaseClient;

public class KeyValueDB 
{
		// @X-JVM-safe this member is read-only after init()
		// @Thread-safe this member is read-only after init()
		private static CouchbaseClient cacheClient;

		// @X-JVM-Synchronized only called by HtmlServlet.init()
		// @Synchronized only called by HtmlServlet.init()
		public static void init() throws Exception
		{
			if (!StringUtils.equals(Jiffy.getValue("keyValueDB"), Constants.NONE))
			{
				List<URI> nodes = Arrays.asList( new URI("http://" + Jiffy.getValue("keyValueDBServer") + ":" + Jiffy.getValue("keyValueDBPort") + "/pools"));

				// connect to the CouchbaseClient
				cacheClient = new CouchbaseClient(nodes, Jiffy.getValue("keyValueDBBucket"), Jiffy.getValue("keyValueDBUsername"), Jiffy.getValue("keyValueDBPassword"));	
			}
		}

		public static Object get(String key)
		{
			return cacheClient.get(key);
		}

		public static Object set(String key, Object value)
		{
			return cacheClient.set(key, 0, value);
		}

		public static Object delete(String key)
		{
			return cacheClient.delete(key);
		}
}
