package com.activeandroid;

public class QueryBase {
	protected Class<? extends ActiveRecordBase<?>> mTable = null;

	protected void ensureTableDeclared() {
		if (mTable == null) {
			throw new NullPointerException(
					"Must declare table name using from(Class<?> extends ActiveRecordBase<?>> table)");
		}
	}
}