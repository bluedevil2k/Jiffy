package org.jiffy.server.db;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.jiffy.server.db.annotations.DBColumn;
import org.jiffy.server.db.annotations.DBTable;
import org.jiffy.server.db.handlers.AnnotatedDataRowProcessor;
import org.jiffy.util.Constants;
import org.jiffy.util.Jiffy;
import org.jiffy.util.Util;

public class DB
{
	protected static Logger logger = LogManager.getLogger();

	// @X-JVM-safe this member is read-only after init()
	// @Thread-safe this member is read-only after init()
    private static DataSource dataSource = null;

    public static final String MYSQL = "mysql";
    public static final String MARIADB = "mariadb";
    public static final String POSTGRESQL = "postgresql";

    private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    private static final String MARIADB_DRIVER = "org.mariadb.jdbc.Driver";
    private static final String POSTGRESQL_DRIVER = "org.postgresql.Driver";
    
    private static volatile boolean isInitialized = false;
    
	// @X-JVM-safe this member is read-only after init()
	// @Thread-safe this member is read-only after init()
    private static String jdbcDriver = MYSQL_DRIVER;

	// @X-JVM-Synchronized only called by HtmlServlet.init()
	// @Synchronized
	public synchronized static void init() throws Exception
	{
		if (isInitialized)
			return;
		
		// Get all these values from the settings object
		String dbEngine = Jiffy.getValue("dbEngine");
		
		if (StringUtils.equals(dbEngine, MYSQL))
		{
			jdbcDriver = MYSQL_DRIVER; 
		}
		else if (StringUtils.equals(dbEngine, MARIADB))
		{
			jdbcDriver = MARIADB_DRIVER;
		}
		else if (StringUtils.equals(dbEngine, POSTGRESQL))
		{
			jdbcDriver = POSTGRESQL_DRIVER;
		}
		else if (StringUtils.equals(dbEngine, Constants.NONE))
		{
			return;
		}

		// db parameters
		String server = Jiffy.getValue("dbServer");
		String port = Jiffy.getValue("dbPort");
		String database = Jiffy.getValue("dbDatabase");
		String userName = Jiffy.getValue("dbUser");
		String password = Jiffy.getValue("dbPassword");
		
		String connectURI = "jdbc:" + dbEngine + "://" + server + ":" + port + "/" +  database;
		
		// create the pool properties to create the db pool
		PoolProperties p = new PoolProperties();
		
		// connection parameters
        p.setUrl(connectURI);
        p.setDriverClassName(jdbcDriver);
        p.setUsername(userName);
        p.setPassword(password);
        
        // pooling parameters
        p.setJmxEnabled(Jiffy.getBool("dbPoolJmxEnabled"));
        p.setTestWhileIdle(Jiffy.getBool("dbPoolTestWhileIdle"));
        p.setTestOnBorrow(Jiffy.getBool("dbPoolTestOnBorrow"));
        p.setValidationQuery(Jiffy.getValue("dbPoolValidationQuery"));
        p.setTestOnReturn(Jiffy.getBool("dbPoolTestOnReturn"));
        p.setValidationInterval(Jiffy.getInt("dbPoolValidationInterval"));
        p.setTimeBetweenEvictionRunsMillis(Jiffy.getInt("dbPoolTimeBetweenEvictionRunsMillis"));
        p.setMaxActive(Jiffy.getInt("dbPoolMaxActive"));
        p.setInitialSize(Jiffy.getInt("dbPoolInitialSize"));
        p.setMaxWait(Jiffy.getInt("dbPoolMaxWait"));
        p.setRemoveAbandonedTimeout(Jiffy.getInt("dbPoolRemoveAbandonedTimeout"));
        p.setMinEvictableIdleTimeMillis(Jiffy.getInt("dbPoolMinEvictableIdleTimeMillis"));
        p.setMinIdle(Jiffy.getInt("dbPoolMinIdle"));
        p.setLogAbandoned(Jiffy.getBool("dbPoolLogAbandoned"));
        p.setRemoveAbandoned(Jiffy.getBool("dbPoolRemoveAbandoned"));
        
        // Create the pooled DataSource
        dataSource = new DataSource();
        dataSource.setPoolProperties(p);

        isInitialized = true;
        
		logger.debug("***** Database Initialized");
	}
	
    public static Connection getConnection() throws SQLException 
    {
    	return dataSource.getConnection();
    }
        
    public static void close(Connection conn) throws Exception
    {
    	DbUtils.close(conn);
    }
    
    public static void openTransaction(Connection conn) throws Exception
    {
        conn.setAutoCommit(false);
    }
    
    public static void committTransaction(Connection conn) throws Exception
    {
    	if (conn != null)
    	{
    		conn.commit();
    		conn.setAutoCommit(true);
    		DbUtils.close(conn);
    	}
    }
    
    public static void rollbackTransaction(Connection conn) throws Exception
    {
    	if (conn != null)
    	{
    		conn.rollback();
    		conn.setAutoCommit(true);
    		DbUtils.close(conn);
            logger.debug("*** Transaction rolled back *** ");
    	}
    }
        
    public static DBResult select(String sql, Object...args) throws Exception
    {
    	QueryRunner run = new QueryRunner();
		MapListHandler h = new MapListHandler();
		
		Connection conn = DB.getConnection();
		try
		{
			return new DBResult(run.query(conn, sql, h, args));
		}
		finally
		{
			DbUtils.close(conn);
		}
    }
    	
//	public static <T> T selectOne(String sql, Class<T> type, Object...args) throws Exception
//	{
//		QueryRunner run = new QueryRunner();
//		ResultSetHandler<T> h = new BeanHandler<T>(type, new AnnotatedDataRowProcessor());
//
//		Connection conn = DB.getConnection();
//		try
//		{
//			T result = run.query(conn, sql, h, args);
//			return result;		        
//		} 
//		finally 
//		{
//			DbUtils.close(conn);  
//		}
//	}
	
	public static <T> T selectOne(Class<T> type) throws Exception
	{
		return selectOne(type, "");
	}
    
	public static <T> T selectOne(Class<T> type, String clause, Object...args) throws Exception
	{
		QueryRunner run = new QueryRunner();
		ResultSetHandler<T> h = new BeanHandler<T>(type, new AnnotatedDataRowProcessor());
		
		Connection conn = DB.getConnection();
		try
		{
			if (!StringUtils.isEmpty(clause))
			{
				clause = " " + clause;
			}
			
			T result = run.query(conn, "SELECT * FROM " + getTableName(type) + convertFieldsToColumns(type, clause), h, args);
			return result;		        
		} 
		finally 
		{
			DbUtils.close(conn);  
		}
	}
	
//	public static <T, S extends ArrayList<T>> S selectAll(String sql, Class<S> listType, Object...args) throws Exception
//	{
//		Class<T> t = (Class<T>)((ParameterizedType)listType.getGenericSuperclass()).getActualTypeArguments()[0];
//
//		QueryRunner run = new QueryRunner();
//		ResultSetHandler<List<T>> h = new BeanListHandler<T>(t, new AnnotatedDataRowProcessor(listType));
//
//		Connection conn = DB.getConnection();
//		try
//		{
//		    return (S)run.query(conn, sql, h, args);
//		} 
//		finally 
//		{
//		    DbUtils.close(conn);  
//		}
//	}
	
	public static <T, S extends ArrayList<T>> S selectAll(Class<S> listType) throws Exception
	{
		return selectAll(listType, "");
	}
	
	public static <T, S extends ArrayList<T>> S selectAll(Class<S> listType, String clause, Object...args) throws Exception
	{
		Class<T> t = (Class<T>)((ParameterizedType)listType.getGenericSuperclass()).getActualTypeArguments()[0];

		QueryRunner run = new QueryRunner();
		ResultSetHandler<List<T>> h = new BeanListHandler<T>(t, new AnnotatedDataRowProcessor(listType));

		Connection conn = DB.getConnection();
		try
		{			
			if (!StringUtils.isEmpty(clause))
			{
				clause = " " + clause;
			}
			
		    return (S)run.query(conn, "SELECT * FROM " + getTableName(t) + convertFieldsToColumns(t, clause), h, args);
		} 
		finally 
		{
		    DbUtils.close(conn);  
		}
	}
		
	// Note - this doesn't return the new key generated on an insert
	public static <T> int update(Class<T> type, String sql, Object...args) throws Exception
	{
		QueryRunner run = new QueryRunner();
		
		Connection conn = DB.getConnection();
		
		try
		{
			String table = type.newInstance().getClass().getAnnotation(DBTable.class).table();
			if (StringUtils.isEmpty(table))
			{
				table = type.getSimpleName().toLowerCase();
			}
			table = Util.camelToUnderscore(table);

			sql = StringUtils.replace(sql, "@table@", table);
			
		    return run.update(conn, convertFieldsToColumns(type, sql), args);
		}
		finally 
		{		
			DbUtils.close(conn);  
		}
	}
	
	public static <T> int updateWithConn(Class<T> type, String sql, Connection conn, Object...args) throws Exception
	{
		QueryRunner run = new QueryRunner();
		
		String table = type.newInstance().getClass().getAnnotation(DBTable.class).table();
		if (StringUtils.isEmpty(table))
		{
			table = type.getSimpleName().toLowerCase();
		}
		table = Util.camelToUnderscore(table);

		sql = StringUtils.replace(sql, "@table@", table);
		
	    return run.update(conn, convertFieldsToColumns(type, sql), args);
	}
		
	public static <T> int count(Class<T> type, String clause, Object... args) throws Exception
	{
		QueryRunner run = new QueryRunner();
		ResultSetHandler<Integer> h = new ResultSetHandler<Integer>() 
		{
		    public Integer handle(ResultSet rs) throws SQLException 
		    {
		    	if (rs != null && rs.next())
		    	{
		    		return rs.getInt("COUNT");
		    	}
		    	return 0;
		    }
		};
		
		Connection conn = DB.getConnection();
		try
		{
			if (!StringUtils.isEmpty(clause))
			{
				clause = " " + clause;
			}
			
		    return run.query(conn, new StringBuilder("SELECT COUNT(*) AS COUNT FROM ").append(getTableName(type)).append(convertFieldsToColumns(type, clause)).toString(), h, args);
		} 
		finally 
		{
		    DbUtils.close(conn);  
		}
	}
	
	public static <T> int countDistinct(Class<T> type, String fieldName, String clause, Object... args) throws Exception
	{
		QueryRunner run = new QueryRunner();
		ResultSetHandler<Integer> h = new ResultSetHandler<Integer>() 
		{
		    public Integer handle(ResultSet rs) throws SQLException 
		    {
		    	if (rs != null && rs.next())
		    	{
		    		return rs.getInt("COUNT");
		    	}
		    	return 0;
		    }
		};
		
		Connection conn = DB.getConnection();
		try
		{
			if (!StringUtils.isEmpty(clause))
			{
				clause = " " + clause;
			}
			
		    return run.query(conn, new StringBuilder("SELECT COUNT(DISTINCT(").append(convertFieldsToColumns(type, fieldName)).append(")) AS COUNT FROM ").append(getTableName(type)).append(convertFieldsToColumns(type, clause)).toString(), h, args);
		} 
		finally 
		{
		    DbUtils.close(conn);  
		}
	}
	
	public static <T> double sum(Class<T> type, String fieldName, String clause, Object...args) throws Exception
	{
		QueryRunner run = new QueryRunner();
		ResultSetHandler<Double> h = new ResultSetHandler<Double>() 
		{
		    public Double handle(ResultSet rs) throws SQLException 
		    {
		    	if (rs != null && rs.next())
		    	{
		    		return rs.getDouble("SUM");
		    	}
		    	return 0.0;
		    }
		};
		
		Connection conn = DB.getConnection();
		try
		{
			if (!StringUtils.isEmpty(clause))
			{
				clause = " " + clause;
			}
			
		    return run.query(conn, new StringBuilder("SELECT SUM(").append(convertFieldsToColumns(type, fieldName)).append(") AS SUM FROM ").append(getTableName(type)).append(convertFieldsToColumns(type, clause)).toString(), h, args);
		} 
		finally 
		{
		    DbUtils.close(conn);  
		}
	}
	
	public static <T> double max(Class<T> type, String fieldName, String clause, Object...args) throws Exception
	{
		QueryRunner run = new QueryRunner();
		ResultSetHandler<Double> h = new ResultSetHandler<Double>() 
		{
		    public Double handle(ResultSet rs) throws SQLException 
		    {
		    	if (rs != null && rs.next())
		    	{
		    		return rs.getDouble("MAX");
		    	}
		    	return 0.0;
		    }
		};
		
		Connection conn = DB.getConnection();
		try
		{
			if (!StringUtils.isEmpty(clause))
			{
				clause = " " + clause;
			}
			
		    return run.query(conn, new StringBuilder("SELECT MAX(").append(convertFieldsToColumns(type, fieldName)).append(") AS MAX FROM ").append(getTableName(type)).append(convertFieldsToColumns(type, clause)).toString(), h, args);
		} 
		finally 
		{
		    DbUtils.close(conn);  
		}
	}
	
	public static <T> double min(Class<T> type, String fieldName, String clause, Object...args) throws Exception
	{
		QueryRunner run = new QueryRunner();
		ResultSetHandler<Double> h = new ResultSetHandler<Double>() 
		{
		    public Double handle(ResultSet rs) throws SQLException 
		    {
		    	if (rs != null && rs.next())
		    	{
		    		return rs.getDouble("MIN");
		    	}
		    	return 0.0;
		    }
		};
		
		Connection conn = DB.getConnection();
		try
		{
			if (!StringUtils.isEmpty(clause))
			{
				clause = " " + clause;
			}

		    return run.query(conn, new StringBuilder("SELECT MIN(").append(convertFieldsToColumns(type, fieldName)).append(") AS MIN FROM ").append(getTableName(type)).append(convertFieldsToColumns(type, clause)).toString(), h, args);
		} 
		finally 
		{
		    DbUtils.close(conn);  
		}
	}
	
	private static <T> String getTableName(Class<T> type) throws Exception
	{
		String table = type.newInstance().getClass().getAnnotation(DBTable.class).table();
		
		if (StringUtils.isEmpty(table))
		{
			table = type.getSimpleName().toLowerCase();
		}
		table = Util.camelToUnderscore(table);
		
		return table;
	}
	
	private static <T> String convertFieldsToColumns(Class<T> type, String sql) throws Exception
	{
		while (StringUtils.contains(sql, "@"))
		{
			String fieldName = StringUtils.substringBetween(sql, "@");
			
			Field f = type.getField(fieldName);
			String columnName = f.getAnnotation(DBColumn.class).name();
			boolean convertName = f.getAnnotation(DBColumn.class).convertName();
								
			// allow the argument to be blank, in which case we'll just use the field name as the column name
			// replacing any upper case letters with a _<lower> to guess-map it to the db column
			if (StringUtils.isEmpty(columnName) && convertName)
			{
				String field = f.getName();
				columnName = Util.camelToUnderscore(field);
			}
			
			sql = StringUtils.replace(sql, "@" + fieldName + "@", columnName);
		}
		return sql;
	}
}
