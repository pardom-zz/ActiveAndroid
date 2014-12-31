package com.activeandroid;

/*
 * Copyright (C) 2010 Michael Pardo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Collection;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.LruCache;

import com.activeandroid.serializer.TypeSerializer;
import com.activeandroid.util.Log;

public final class Cache {
	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	public static final int DEFAULT_CACHE_SIZE = 1024;

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private static Context sContext;

	private static ModelInfo sModelInfo;
	private static DatabaseHelper sDatabaseHelper;

	private static LruCache<String, Model> sEntities;

	private static boolean sIsInitialized = false;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	private Cache() {
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public static synchronized void initialize(Configuration configuration) {
		if (sIsInitialized) {
			Log.v("ActiveAndroid already initialized.");
			return;
		}

		sContext = configuration.getContext();
		sModelInfo = new ModelInfo(configuration);
		sDatabaseHelper = new DatabaseHelper(configuration);

		checkDbReset(configuration);

		// TODO: It would be nice to override sizeOf here and calculate the memory
		// actually used, however at this point it seems like the reflection
		// required would be too costly to be of any benefit. We'll just set a max
		// object size instead.
		sEntities = new LruCache<String, Model>(configuration.getCacheSize());

		openDatabase();

		sIsInitialized = true;

		Log.v("ActiveAndroid initialized successfully.");
	}

	public static synchronized void clear() {
		sEntities.evictAll();
		Log.v("Cache cleared.");
	}

	public static synchronized void dispose() {
		closeDatabase();

		sEntities = null;
		sModelInfo = null;
		sDatabaseHelper = null;

		sIsInitialized = false;

		Log.v("ActiveAndroid disposed. Call initialize to use library.");
	}

	// Database access
	
	public static boolean isInitialized() {
		return sIsInitialized;
	}

	public static synchronized SQLiteDatabase openDatabase() {
		return sDatabaseHelper.getWritableDatabase();
	}

	public static synchronized void closeDatabase() {
		sDatabaseHelper.close();
	}

	// Context access

	public static Context getContext() {
		return sContext;
	}

	// Entity cache

	public static String getIdentifier(Class<? extends Model> type, Long id) {
		return getTableName(type) + "@" + id;
	}

	public static String getIdentifier(Model entity) {
		return getIdentifier(entity.getClass(), entity.getId());
	}

	public static synchronized void addEntity(Model entity) {
		sEntities.put(getIdentifier(entity), entity);
	}

	public static synchronized Model getEntity(Class<? extends Model> type, long id) {
		return sEntities.get(getIdentifier(type, id));
	}

	public static synchronized void removeEntity(Model entity) {
		sEntities.remove(getIdentifier(entity));
	}

	// Model cache

	public static synchronized Collection<TableInfo> getTableInfos() {
		return sModelInfo.getTableInfos();
	}

	public static synchronized TableInfo getTableInfo(Class<? extends Model> type) {
		return sModelInfo.getTableInfo(type);
	}

	public static synchronized TypeSerializer getParserForType(Class<?> type) {
		return sModelInfo.getTypeSerializer(type);
	}

	public static synchronized String getTableName(Class<? extends Model> type) {
		return sModelInfo.getTableInfo(type).getTableName();
	}

    /**
     * Checks if the db should be reset on startup. It can be enabled
     * by putting the following lines in your application's manifest:
     *
     * ```
     * <meta-data
     *    android:name="AA_DB_RESET"
     *    android:value="true" />
     * ```
     *
     * It will reset the database only once, if and only the `AA_DB_VERSION`
     * parameter is increased between app releases.
     *
     * @param configuration The ActiveAndroid configuration.
     */
    public static synchronized void checkDbReset(Configuration configuration) {
        // Check if the reset flag is set
        if (configuration != null && configuration.getResetDatabase()) {
            Log.w("Checking db reset");

            // Get the current db
            SQLiteDatabase db = sContext.openOrCreateDatabase(
                    configuration.getDatabaseName(),
                    Context.MODE_PRIVATE,
                    null);

            if (db == null) {
                return;
            }

            // New dbs always have version == 0
            if (db.getVersion() == 0) {
                Log.w("New db, not resetting");
                return;
            }

            // Only update if the config version is higher. This
            // prevents repeated resets
            if (db.getVersion() >= configuration.getDatabaseVersion()) {
                Log.w(String.format("Current db version >= config, not resetting (%d >= %d)",
                        db.getVersion(),
                        configuration.getDatabaseVersion()));
                return;
            }

            // Finally, reset the db. We first delete it
            // and then we create a new one with the same
            // name.
            Log.w("Resetting database");
            sContext.deleteDatabase(configuration.getDatabaseName());
            sContext.openOrCreateDatabase(
                    configuration.getDatabaseName(),
                    Context.MODE_PRIVATE,
                    null).close();
        }
    }
}
