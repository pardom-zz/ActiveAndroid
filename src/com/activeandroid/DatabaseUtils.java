package com.activeandroid;

import android.database.sqlite.SQLiteDatabase;

public class DatabaseUtils {
	// Expose Application database

	public static SQLiteDatabase getDatabase() {
		return ApplicationCache.getInstance().openDatabase();
	}

	// Convenience wrappers

	public static void beginTransaction() {
		ApplicationCache.getInstance().openDatabase().beginTransaction();
	}

	public static void endTransaction() {
		ApplicationCache.getInstance().openDatabase().endTransaction();
	}

	public static void execSQL(String sql) {
		ApplicationCache.getInstance().openDatabase().execSQL(sql);
	}

	public static void execSQL(String sql, Object[] bindArgs) {
		ApplicationCache.getInstance().openDatabase().execSQL(sql, bindArgs);
	}

	public static boolean inTransaction() {
		return ApplicationCache.getInstance().openDatabase().inTransaction();
	}

	public static void setTransactionSuccessful() {
		ApplicationCache.getInstance().openDatabase().setTransactionSuccessful();
	}
}