package com.activeandroid;

final public class Delete extends QueryBase {
	private String mSelection = null;
	private String[] mSelectionArgs = null;

	public Delete() {
	}

	public Delete from(Class<? extends ActiveRecordBase<?>> table) {
		mTable = table;
		return this;
	}

	public Delete where(String where) {
		mSelection = where;
		return this;
	}

	public Delete where(String where, Object... whereArgs) {
		final int size = whereArgs.length;

		mSelection = where;
		mSelectionArgs = new String[whereArgs.length];

		for (int i = 0; i < size; i++) {
			mSelectionArgs[i] = String.valueOf(whereArgs[i]);
		}

		return this;
	}

	public int execute() {
		ensureTableDeclared();
		return ActiveRecordBase.delete(mTable, mSelection, mSelectionArgs);
	}
}