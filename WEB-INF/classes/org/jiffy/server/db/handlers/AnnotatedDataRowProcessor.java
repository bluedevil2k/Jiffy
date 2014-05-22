package org.jiffy.server.db.handlers;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.lang3.StringUtils;
import org.jiffy.server.db.DB;
import org.jiffy.server.db.annotations.DBColumn;
import org.jiffy.server.db.annotations.DBHasMany;
import org.jiffy.server.db.annotations.DBHasOne;
import org.jiffy.server.db.annotations.DBTable;
import org.jiffy.util.Util;

public class AnnotatedDataRowProcessor extends BasicRowProcessor
{			
	private Class<?> type;
	
	public AnnotatedDataRowProcessor()
	{
		super();
	}
	
	public <T> AnnotatedDataRowProcessor(Class<T> type)
	{
		this.type = type;
	}
	
	@Override
	public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException
	{
		try
		{
			T data = type.newInstance();
						
			Field[] f = type.getFields();
		
			for (int i=0; i<f.length; i++)
			{
				Field field = f[i];
				
				if (field.isAnnotationPresent(DBColumn.class))
				{
					String columnName = field.getAnnotation(DBColumn.class).name();
					boolean convertName = field.getAnnotation(DBColumn.class).convertName();
										
					// allow the argument to be blank, in which case we'll just use the field name as the column name
					// replacing any upper case letters with a _<lower> to guess-map it to the db column
					if (StringUtils.isEmpty(columnName) && convertName)
					{
						String fieldName = field.getName();
						columnName = Util.camelToUnderscore(fieldName);
					}
					
					if (field.getType().getName().equals("int"))
					{
						field.setInt(data, rs.getInt(columnName));
					}
					else if (field.getType().getName().equals("long"))
					{
						field.setLong(data, rs.getLong(columnName));
					}
					else if (field.getType().getName().equals("double"))
					{
						field.setDouble(data, rs.getDouble(columnName));
					}
					else if (field.getType().getName().equals("java.lang.String"))
					{
						field.set(data, rs.getString(columnName));
					}
					else if (field.getType().getName().equals("boolean"))
					{
						field.setBoolean(data, rs.getBoolean(columnName));
					}
					else if (field.getType().getName().equals("float"))
					{
						field.setFloat(data, rs.getFloat(columnName));
					}
					else if (field.getType().getName().equals("byte"))
					{
						field.setByte(data, rs.getByte(columnName));
					}
					else if (field.getType().getName().equals("char"))
					{
						field.setChar(data, rs.getString(columnName).charAt(0));
					}
					else if (field.getType().getName().equals("short"))
					{
						field.setShort(data, rs.getShort(columnName));
					}
					else if (field.getType().getName().equals("java.util.Date"))
					{
						try
						{
							java.sql.Timestamp t = rs.getTimestamp(columnName);
							field.set(data, t);
						}
						catch (SQLException e)
						{
							// just ignore blank dates and leave them null
						}
					}
				}
				else if (field.isAnnotationPresent(DBHasOne.class))
				{
					String reference = data.getClass().getAnnotation(DBTable.class).table();
					if (StringUtils.isEmpty(reference))
					{
						reference = data.getClass().getSimpleName().toLowerCase();
					}
					Class<?> clazz = field.getType();
					String table = clazz.newInstance().getClass().getAnnotation(DBTable.class).table();
					if (StringUtils.isEmpty(table))
					{
						table = clazz.getSimpleName().toLowerCase();
					}
					field.set(data, DB.selectOne(clazz, new StringBuilder("WHERE ").append(reference).append("_id=?").toString(), clazz, data.getClass().getField("id").getLong(data)));
				}
				else if (field.isAnnotationPresent(DBHasMany.class))
				{
					String reference = data.getClass().getAnnotation(DBTable.class).table();
					if (StringUtils.isEmpty(reference))
					{
						reference = data.getClass().getSimpleName().toLowerCase();
					}
					Class clazz = field.getType();
					String table = ((Class<?>)((ParameterizedType)clazz.getGenericSuperclass()).getActualTypeArguments()[0]).getAnnotation(DBTable.class).table();
					if (StringUtils.isEmpty(table))
					{
						table = ((Class<?>)((ParameterizedType)clazz.getGenericSuperclass()).getActualTypeArguments()[0]).getSimpleName().toLowerCase();
					}
					field.set(data, DB.selectAll(clazz, new StringBuilder("WHERE ").append(reference).append("_id=?").toString(), clazz, data.getClass().getField("id").getLong(data)));
				}
			}
			return data;
		}
		catch (Exception ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}

	@Override
	public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException
	{
		try
		{
			List<T> list = (List<T>)this.type.newInstance();
			
			while (rs.next())
			{
				list.add(toBean(rs,type));
			}
			
			return list;
		}
		catch (IllegalAccessException ex)
		{
			throw new SQLException(ex.getMessage());
		}
		catch (InstantiationException ex)
		{
			throw new SQLException(ex.getMessage());
		}
	}
}
