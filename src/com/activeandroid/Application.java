package com.activeandroid;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.database.sqlite.SQLiteDatabase;

@SuppressWarnings("unused")
public class Application extends android.app.Application {
	private DatabaseHelper mDatabaseHelper;
	private SQLiteDatabase mDatabase;
	private Set<ActiveRecordBase<?>> mEntities;
	private Map<Class<?>, TypeSerializer<?>> mParsers;
	private Map<Class<?>, String> mTableNames;
	private Map<Class<?>, ArrayList<Field>> mClassFields;

	@Override
	public void onCreate() {
		super.onCreate();

		if (Params.IS_TRIAL && !isEmulator()) {
			throw new TrialVersionException();
		}

		mDatabaseHelper = new DatabaseHelper(this);
		mEntities = new HashSet<ActiveRecordBase<?>>();
		mParsers = ReflectionUtils.getParsers(this);
		mTableNames = new HashMap<Class<?>, String>();
		mClassFields = new HashMap<Class<?>, ArrayList<Field>>();
	}

	@Override
	public void onTerminate() {
		closeDatabase();

		super.onTerminate();
	}

	public SQLiteDatabase openDatabase() {
		if (mDatabase != null) {
			return mDatabase;
		}

		mDatabase = mDatabaseHelper.getWritableDatabase();

		return mDatabase;
	}

	public void closeDatabase() {
		if (mDatabase != null) {
			mDatabase.close();
			mDatabase = null;
		}
	}

	final TypeSerializer<?> getParserForType(Class<?> fieldType) {
		if (mParsers.containsKey(fieldType)) {
			return mParsers.get(fieldType);
		}

		return null;
	}
	
	final void addClassFields(Class<?> type, ArrayList<Field> fields) {
		mClassFields.put(type, fields);
	}
	
	final ArrayList<Field> getClassFields(Class<?> type) {
		if(mClassFields.containsKey(type)) {
			return mClassFields.get(type);
		}
		
		return null;
	}

	final void addEntity(ActiveRecordBase<?> entity) {
		mEntities.add(entity);
	}

	final void addEntities(Set<ActiveRecordBase<?>> entities) {
		mEntities.addAll(entities);
	}

	final void removeEntity(ActiveRecordBase<?> entity) {
		mEntities.remove(entity);
	}
	
	final void addTableName(Class<?> type, String tableName) {
		mTableNames.put(type, tableName);
	}
	
	final String getTableName(Class<?> type) {
		if(mTableNames.containsKey(type)) {
			return mTableNames.get(type);
		}
		
		return null;
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

	private boolean isEmulator() {
		return android.os.Build.MODEL.equals("sdk") || android.os.Build.MODEL.equals("google_sdk");
	}
}
