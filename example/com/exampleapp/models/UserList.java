package com.exampleapp.models;

import java.io.Serializable;
import java.util.ArrayList;

public class UserList extends ArrayList<User> implements Serializable
{
	public User getById(long id)
	{
		for (int ct = 0; ct < size(); ct++)
		{
			if (get(ct).id == id)
				return get(ct);
		}
		return null;
	}
}