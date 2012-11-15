package com.activeandroid;

import com.activeandroid.util.Log;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public final class ActiveAndroid {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////
	
	private static Object sLock = new Object();

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////
	
	public static void initialize(Context context) {
		initialize(context, false);
	}

	public static void initialize(Context context, boolean loggingEnabled) {
		setLoggingEnabled(loggingEnabled);

		synchronized (sLock) {
			Cache.initialize(context);
		}
	}

	public static void clearCache() {
		Cache.clear();
	}

	public static void dispose() {
		Cache.dispose();
	}

	public static void setLoggingEnabled(boolean enabled) {
		Log.setEnabled(enabled);
	}

	public static SQLiteDatabase getDatabase() {
		synchronized (sLock) {
			return Cache.openDatabase();
		}
	}

	public static void beginTransaction() {
		Cache.openDatabase().beginTransaction();
	}

	public static void endTransaction() {
		Cache.openDatabase().endTransaction();
	}

	public static void setTransactionSuccessful() {
		Cache.openDatabase().setTransactionSuccessful();
	}

	public static boolean inTransaction() {
		return Cache.openDatabase().inTransaction();
	}

	public static void execSQL(String sql) {
		Cache.openDatabase().execSQL(sql);
	}

	public static void execSQL(String sql, Object[] bindArgs) {
		Cache.openDatabase().execSQL(sql, bindArgs);
	}
}