package com.activeandroid;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.activeandroid.serializer.TypeSerializer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class ApplicationCache {
	private Context mContext;
	private DatabaseHelper mDatabaseHelper;
	private SQLiteDatabase mDatabase;

	private boolean mIsInitialized = false;

	private Set<ActiveRecordBase<?>> mEntities;

	private HashMap<Class<?>, TypeSerializer> mParsers;
	private HashMap<Class<?>, String> mTableNames;
	private HashMap<Class<?>, ArrayList<Field>> mClassFields;
	private HashMap<Field, String> mColumnNames;
	private HashMap<Field, Integer> mColumnLengths;

	// Hide constructor. Must use getInstance()
	private ApplicationCache() {
	}

	private static class InstanceHolder {
		public static final ApplicationCache instance = new ApplicationCache();
	}

	public static ApplicationCache getInstance() {
		return InstanceHolder.instance;
	}

	final void initialize(Context context) {
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

		mEntities = new HashSet<ActiveRecordBase<?>>();
		mTableNames = new HashMap<Class<?>, String>();
		mClassFields = new HashMap<Class<?>, ArrayList<Field>>();
		mColumnNames = new HashMap<Field, String>();
		mColumnLengths = new HashMap<Field, Integer>();

		openDatabase();

		mIsInitialized = true;
	}

	public void dispose() {
		mDatabaseHelper = null;
		mParsers = null;

		mEntities = null;
		mTableNames = null;
		mClassFields = null;
		mColumnNames = null;
		mColumnLengths = null;

		closeDatabase();

		mIsInitialized = false;
	}

	// Open/close database

	final SQLiteDatabase openDatabase() {
		if (mDatabase != null) {
			return mDatabase;
		}

		mDatabase = mDatabaseHelper.getWritableDatabase();

		return mDatabase;
	}

	final void closeDatabase() {
		if (mDatabase != null) {
			mDatabase.close();
			mDatabase = null;
		}
	}

	// Non-public methods

	final Context getContext() {
		return mContext;
	}

	//

	final void addClassFields(Class<?> type, ArrayList<Field> fields) {
		mClassFields.put(type, fields);
	}

	final void addColumnName(Field field, String columnName) {
		mColumnNames.put(field, columnName);
	}

	final void addColumnLength(Field field, Integer columnLength) {
		mColumnLengths.put(field, columnLength);
	}

	final void addEntities(Set<ActiveRecordBase<?>> entities) {
		mEntities.addAll(entities);
	}

	final void addEntity(ActiveRecordBase<?> entity) {
		mEntities.add(entity);
	}

	final void addTableName(Class<?> type, String tableName) {
		mTableNames.put(type, tableName);
	}

	final ArrayList<Field> getClassFields(Class<?> type) {
		return mClassFields.get(type);
	}

	final String getColumnName(Field field) {
		return mColumnNames.get(field);
	}

	final Integer getColumnInteger(Field field) {
		return mColumnLengths.get(field);
	}

	final ActiveRecordBase<?> getEntity(Class<? extends ActiveRecordBase<?>> entityType, long id) {
		for (ActiveRecordBase<?> entity : mEntities) {
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

	final TypeSerializer getParserForType(Class<?> fieldType) {
		return mParsers.get(fieldType);
	}

	final String getTableName(Class<?> type) {
		return mTableNames.get(type);
	}

	private boolean isEmulator() {
		return android.os.Build.MODEL.equals("sdk") || android.os.Build.MODEL.equals("google_sdk");
	}

	final void removeEntity(ActiveRecordBase<?> entity) {
		mEntities.remove(entity);
	}
}