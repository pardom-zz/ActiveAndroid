package com.activeandroid;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.database.sqlite.SQLiteDatabase;

@SuppressWarnings("unused")
public class Application extends android.app.Application {
	private DatabaseHelper mDatabaseHelper;
	private SQLiteDatabase mDatabase;
	private Set<ActiveRecordBase<?>> mEntities;
	private Map<Class<?>, TypeParser<?>> mParsers;

	@Override
	public void onCreate() {
		super.onCreate();

		if (Params.IS_TRIAL && !isEmulator()) {
			throw new TrialVersionException();
		}

		mDatabaseHelper = new DatabaseHelper(this);
		mParsers = ReflectionUtils.getParsers(this);
		mEntities = new HashSet<ActiveRecordBase<?>>();
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

	final void addEntity(ActiveRecordBase<?> entity) {
		mEntities.add(entity);
	}

	final void addEntities(Set<ActiveRecordBase<?>> entities) {
		mEntities.addAll(entities);
	}

	final void removeEntity(ActiveRecordBase<?> entity) {
		mEntities.remove(entity);
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
