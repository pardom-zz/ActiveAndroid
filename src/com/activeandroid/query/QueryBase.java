package com.activeandroid.query;

import com.activeandroid.Model;

public abstract class QueryBase {
	protected Class<? extends Model> mTable = null;

	protected void ensureTableDeclared() {
		if (mTable == null) {
			throw new NullPointerException(
					"Must declare table name using from(Class<?> extends ActiveRecordBase<?>> table)");
		}
	}
}