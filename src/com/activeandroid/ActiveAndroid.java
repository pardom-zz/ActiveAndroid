package com.activeandroid;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public final class ActiveAndroid {
	public static void initialize(Context context) {
		Registry.getInstance().initialize(context);
	}

	public static void clearCache() {
		Registry.getInstance().clearCache();
	}

	public static void dispose() {
		Registry.getInstance().dispose();
	}

	public static String getVersion() {
		return Params.VERSION;
	}

	public static void setLoggingEnabled(boolean enabled) {
		Log.configure(enabled);
	}

	// Expose Application database

	public static SQLiteDatabase getDatabase() {
		return Registry.getInstance().openDatabase();
	}

	// Convenience wrappers

	public static void beginTransaction() {
		Registry.getInstance().openDatabase().beginTransaction();
	}

	public static void endTransaction() {
		Registry.getInstance().openDatabase().endTransaction();
	}

	public static void execSQL(String sql) {
		Registry.getInstance().openDatabase().execSQL(sql);
	}

	public static void execSQL(String sql, Object[] bindArgs) {
		Registry.getInstance().openDatabase().execSQL(sql, bindArgs);
	}

	public static boolean inTransaction() {
		return Registry.getInstance().openDatabase().inTransaction();
	}

	public static void setTransactionSuccessful() {
		Registry.getInstance().openDatabase().setTransactionSuccessful();
	}
}