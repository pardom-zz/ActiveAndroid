package com.activeandroid;

import java.util.HashSet;
import java.util.Set;

import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("unused")
public class Application extends android.app.Application {
	private DatabaseManager mDatabaseManager;
	private Set<ActiveRecordBase<?>> mEntities;

	@Override
	public void onCreate() {
		super.onCreate();

		if (Params.IS_TRIAL && !isEmulator()) {
			if (Params.LOGGING_ENABLED) {
				Log.e(Params.LOGGING_TAG, "ActiveAndroid trial only works on emulator. Shutting down.");
			}

			System.exit(0);
		}

		mDatabaseManager = new DatabaseManager(this);
		mEntities = new HashSet<ActiveRecordBase<?>>();
	}

	@Override
	public void onTerminate() {
		if (mDatabaseManager != null) {
			mDatabaseManager.closeDB();
		}

		super.onTerminate();
	}

	final DatabaseManager getDatabaseManager() {
		return mDatabaseManager;
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
			if (entity.getClass() == entityType && entity.getId() == id) {
				return entity;
			}
		}

		return null;
	}

	private boolean isEmulator() {
		return android.os.Build.MODEL.equals("sdk") || android.os.Build.MODEL.equals("google_sdk");
	}
}
