package org.jiffy.models;

import org.jiffy.server.db.annotations.DBColumn;
import org.json.JSONObject;

public class JiffyModel
{
	@DBColumn
	public int id;

	public long getId() 
	{
		return id;
	}

	public void setId(int id) 
	{
		this.id = id;
	}
	
	public String toJSON()
	{
		return new JSONObject(this).toString();
	}
}
