package com.activeandroid;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.activeandroid.serializer.TypeSerializer;

final class Registry {
	private Context mContext;
	private DatabaseHelper mDatabaseHelper;
	private SQLiteDatabase mDatabase;

	private boolean mIsInitialized = false;

	private Set<Model> mEntities;

	private HashMap<Class<?>, TypeSerializer> mParsers;
	private HashMap<Class<?>, String> mTableNames;
	private HashMap<Class<?>, ArrayList<Field>> mClassFields;
	private HashMap<Field, String> mColumnNames;

	// Hide constructor. Must use getInstance()
	private Registry() {
	}

	private static class InstanceHolder {
		public static final Registry instance = new Registry();
	}

	// Public methods

	public static Registry getInstance() {
		return InstanceHolder.instance;
	}

	public synchronized void initialize(Context context) {
		if (mIsInitialized) {
			return;
		}

		mContext = context.getApplicationContext();

		if (Params.IS_TRIAL) {
			final boolean isEmulator = isEmulator();
			final int icon = android.R.drawable.stat_notify_error;
			String tickerText;
			String contentTitle;
			String contentText;
			String appName;

			try {
				PackageManager pm = mContext.getPackageManager();
				appName = pm.getApplicationInfo(mContext.getPackageName(), 0).loadLabel(pm).toString();
			}
			catch (NameNotFoundException e) {
				appName = "This application";
			}

			if (isEmulator) {
				tickerText = appName + " uses ActiveAndroid Trial";
				contentTitle = appName + " uses ActiveAndroid Trial";
				contentText = "Purchase ActiveAndroid before use on devices.";
			}
			else {
				tickerText = appName + " has been shut down";
				contentTitle = appName + " uses ActiveAndroid Trial";
				contentText = "ActiveAndroid Trial only works on the emulator.";
			}

			Intent contentIntent = new Intent(Intent.ACTION_VIEW);
			contentIntent.setData(Uri.parse("https://www.activeandroid.com/"));
			PendingIntent contentPendingIntent = PendingIntent.getActivity(mContext, 0, contentIntent, 0);

			Notification notification = new Notification(icon, tickerText, 0);
			notification.setLatestEventInfo(mContext, contentTitle, contentText, contentPendingIntent);

			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(-6532, notification);

			if (!isEmulator) {
				System.exit(0);
			}
		}

		mDatabaseHelper = new DatabaseHelper(mContext);
		mParsers = ReflectionUtils.getParsers();

		mEntities = new HashSet<Model>();
		mTableNames = new HashMap<Class<?>, String>();
		mClassFields = new HashMap<Class<?>, ArrayList<Field>>();
		mColumnNames = new HashMap<Field, String>();

		openDatabase();

		mIsInitialized = true;

		Log.v("ActiveAndroid initialized succesfully");
	}

	public synchronized void clearCache() {
		mEntities = new HashSet<Model>();
		mTableNames = new HashMap<Class<?>, String>();
		mClassFields = new HashMap<Class<?>, ArrayList<Field>>();
		mColumnNames = new HashMap<Field, String>();

		Log.v("Cache cleared");
	}

	public synchronized void dispose() {
		mDatabaseHelper = null;
		mParsers = null;

		mEntities = null;
		mTableNames = null;
		mClassFields = null;
		mColumnNames = null;

		closeDatabase();

		mIsInitialized = false;

		Log.v("ActiveAndroid disposed. Call initialize to use library.");
	}

	// Open/close database

	public synchronized SQLiteDatabase openDatabase() {
		if (mDatabase != null) {
			Log.v("Returning opened database.");
			return mDatabase;
		}

		Log.v("Opening database");

		mDatabase = mDatabaseHelper.getWritableDatabase();

		return mDatabase;
	}

	public synchronized void closeDatabase() {
		if (mDatabase != null) {
			mDatabase.close();
			mDatabase = null;

			Log.v("Database closed and set to null");
		}
	}

	// Non-public methods

	public Context getContext() {
		return mContext;
	}

	// Cache methods

	public synchronized void addClassFields(Class<?> type, ArrayList<Field> fields) {
		mClassFields.put(type, fields);
	}

	public synchronized void addColumnName(Field field, String columnName) {
		mColumnNames.put(field, columnName);
	}

	public synchronized void addEntities(Set<Model> entities) {
		mEntities.addAll(entities);
	}

	public synchronized void addEntity(Model entity) {
		mEntities.add(entity);
	}

	public synchronized void addTableName(Class<?> type, String tableName) {
		mTableNames.put(type, tableName);
	}

	public synchronized ArrayList<Field> getClassFields(Class<?> type) {
		return mClassFields.get(type);
	}

	public synchronized String getColumnName(Field field) {
		return mColumnNames.get(field);
	}

	public synchronized Model getEntity(Class<? extends Model> entityType, long id) {
		for (Model entity : mEntities) {
			if (entity != null) {
				if (entity.getClass() != null && entity.getClass() == entityType) {
					if (entity.getId() != null && entity.getId() == id) {
						return entity;
					}
				}
			}
		}

		return null;
	}

	public synchronized TypeSerializer getParserForType(Class<?> fieldType) {
		return mParsers.get(fieldType);
	}

	public synchronized String getTableName(Class<?> type) {
		return mTableNames.get(type);
	}

	public synchronized void removeEntity(Model entity) {
		mEntities.remove(entity);
	}

	// Private methods

	private boolean isEmulator() {
		return android.os.Build.MODEL.equals("sdk") || android.os.Build.MODEL.equals("google_sdk");
	}
}