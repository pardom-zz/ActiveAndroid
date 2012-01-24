package com.activeandroid.query;

import com.activeandroid.Model;



final public class Delete extends QueryBase {

	public Delete() {
	}

	public From from(Class<? extends Model> table) {
		mFrom = new From(table, this);
		return mFrom;
	}

	@Override
	String toSql() {
		return "DELETE ";
	}
}