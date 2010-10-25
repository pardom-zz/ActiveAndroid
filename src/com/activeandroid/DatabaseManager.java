package com.activeandroid;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

//import android.util.Log;

class DatabaseManager {
	private DatabaseHelper mDatabaseHelper;
	private SQLiteDatabase mDB;

	public SQLiteDatabase getDB() {
		return mDB;
	}

	public DatabaseManager(Context context) {
		mDatabaseHelper = new DatabaseHelper(context);
	}

	public SQLiteDatabase openDB() {
		mDB = mDatabaseHelper.getWritableDatabase();

		return mDB;
	}

	public void closeDB() {
		if (mDB != null) {
			mDB.close();
			mDB = null;
		}
	}
}
