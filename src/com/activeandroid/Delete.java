package com.activeandroid;


final public class Delete extends QueryBase {

	public Delete() {
	}

	public From from(Class<? extends Model> table) {
		mFrom = new From(table, this);
		return mFrom;
	}

	@Override
	public String toSql() {
		return "DELETE ";
	}
}