package com.activeandroid;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseUtils {
	// Expose Application database
	
	public static SQLiteDatabase getDatabase(Context context) {
		return ((Application) context.getApplicationContext()).openDatabase();
	}
	
	// Convenience wrappers
	
	public static void beginTransaction(Context context) {
		((Application) context.getApplicationContext()).openDatabase().beginTransaction();
	}

	public static void endTransaction(Context context) {
		((Application) context.getApplicationContext()).openDatabase().endTransaction();
	}

	public static void execSQL(Context context, String sql) {
		((Application) context.getApplicationContext()).openDatabase().execSQL(sql);
	}

	public static void execSQL(Context context, String sql, Object[] bindArgs) {
		((Application) context.getApplicationContext()).openDatabase().execSQL(sql, bindArgs);
	}

	public static boolean inTransaction(Context context) {
		return ((Application) context.getApplicationContext()).openDatabase().inTransaction();
	}

	public static void setTransactionSuccessful(Context context) {
		((Application) context.getApplicationContext()).openDatabase().setTransactionSuccessful();
	}
}
