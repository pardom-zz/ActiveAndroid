package com.activeandroid;

import java.util.ArrayList;

final public class Select extends QueryBase {
	private String[] mColumns = null;
	private String mSelection = null;
	private String[] mSelectionArgs = null;
	private String mGroupBy = null;
	private String mHaving = null;
	private String mOrderBy = null;
	private String mLimit = null;

	public Select() {
	}

	public Select(String... columns) {
		mColumns = columns;
	}

	public Select from(Class<? extends ActiveRecordBase<?>> table) {
		mTable = table;
		return this;
	}

	public Select where(String where) {
		mSelection = where;
		return this;
	}

	public Select where(String where, Object... whereArgs) {
		final int size = whereArgs.length;

		mSelection = where;
		mSelectionArgs = new String[whereArgs.length];

		for (int i = 0; i < size; i++) {
			mSelectionArgs[i] = String.valueOf(whereArgs[i]);
		}

		return this;
	}

	public Select groupBy(String groupBy) {
		mGroupBy = groupBy;
		return this;
	}

	public Select having(String having) {
		mHaving = having;
		return this;
	}

	public Select orderBy(String orderBy) {
		mOrderBy = orderBy;
		return this;
	}

	public Select limit(String limit) {
		mLimit = limit;
		return this;
	}

	public <T> ArrayList<T> execute() {
		ensureTableDeclared();
		return ActiveRecordBase
				.query(mTable, mColumns, mSelection, mSelectionArgs, mGroupBy, mHaving, mOrderBy, mLimit);
	}

	public <T> T executeSingle() {
		ensureTableDeclared();
		return ActiveRecordBase.querySingle(mTable, mColumns, mSelection, mSelectionArgs, mGroupBy, mHaving, mOrderBy);
	}
}