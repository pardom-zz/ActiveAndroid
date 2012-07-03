package com.activeandroid.query;

import com.activeandroid.Model;
import com.activeandroid.QueryUtils;

final public class Update implements Sqlable {
	private Class<? extends Model> mType;

	public Update(Class<? extends Model> table) {
		mType = table;
	}

	public Set set(String set) {
		return new Set(this, set);
	}

	public Set set(String set, Object... args) {
		return new Set(this, set, args);
	}

	Class<? extends Model> getType() {
		return mType;
	}

	@Override
	public String toSql() {
		return "UPDATE " + QueryUtils.getTableName(mType) + " ";
	}
}