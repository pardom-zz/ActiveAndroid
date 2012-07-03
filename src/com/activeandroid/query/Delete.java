package com.activeandroid.query;

import com.activeandroid.Model;

final public class Delete implements Sqlable {

	public Delete() {
	}

	public From from(Class<? extends Model> table) {
		return new From(table, this);
	}

	@Override
	public String toSql() {
		return "DELETE ";
	}
}