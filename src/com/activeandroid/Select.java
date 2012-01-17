package com.activeandroid;


import android.text.TextUtils;

public class Select extends QueryBase {
	private String[] mColumns;
	private boolean mDistinct = false;
	private boolean mAll = false;

	public Select() {
	}

	public Select(String... columns) {
		mColumns = columns;
	}

	public Select(Column... columns) {
		final int size = columns.length;
		mColumns = new String[size];
		for (int i = 0; i < size; i++) {
			mColumns[i] = columns[i].name + " AS " + columns[i].alias;
		}
	}

	public Select distinct() {
		mDistinct = true;
		mAll = false;

		return this;
	}

	public Select all() {
		mDistinct = false;
		mAll = true;

		return this;
	}

	public From from(Class<? extends Model> table) {
		mFrom = new From(table, this);
		return mFrom;
	}

	public static class Column {
		String name;
		String alias;

		public Column(String name, String alias) {
			this.name = name;
			this.alias = alias;
		}
	}

	public String toSql() {
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT ");

		if (mDistinct) {
			sql.append("DISTINCT ");
		}
		else if (mAll) {
			sql.append("ALL ");
		}

		if (mColumns != null && mColumns.length > 0) {
			sql.append(TextUtils.join(", ", mColumns) + " ");
		}
		else {
			sql.append("* ");
		}

		return sql.toString();
	}
}