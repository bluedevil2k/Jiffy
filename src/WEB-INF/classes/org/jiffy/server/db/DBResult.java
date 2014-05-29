package org.jiffy.server.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DBResult extends ArrayList<Map<String, Object>>
{
	public DBResult(List<Map<String, Object>> rs)
	{
		addAll(rs);
	}

	public Object get(String column)
	{
		if (!isEmpty())
			return get(0).get(column);

		return null;
	}

	/**
	 * Convenience methods to get a double, since DBUtils defaults it to BigDecimal
	 */
	public double getDouble(String column)
	{
		if (!isEmpty())
			return ((java.math.BigDecimal) get(0).get(column)).doubleValue();

		return 0.0;
	}

	public double getDouble(int index, String column)
	{
		if (isEmpty())
			return 0.0;
		if (index >= size())
			return 0.0;
		
		return ((java.math.BigDecimal) get(index).get(column)).doubleValue();
	}

	public <T> List<T> getAllRows(String column)
	{
		List<T> list = new ArrayList<T>();

		if (!isEmpty())
		{
			for (int i = 0; i < size(); i++)
			{
				list.add((T) get(i).get(column));
			}
		}

		return list;
	}
}
