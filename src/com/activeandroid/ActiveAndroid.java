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

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.activeandroid.util.Log;

public final class ActiveAndroid {
    //////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    public synchronized static void initialize(Application application) {
        initialize(application, false);
    }

    public synchronized static void initialize(Application application, boolean loggingEnabled) {
        setLoggingEnabled(loggingEnabled);
        Cache.initialize(application);
    }

    public synchronized static void registerDbMetaData(Application application, DbMetaData metaData) {
        Cache.initialize(application, metaData);
    }

    public static void clearCache() {
        Cache.clearCache();
    }

    public static void dispose() {
        Cache.dispose();
    }

    public static void setLoggingEnabled(boolean enabled) {
        Log.setEnabled(enabled);
    }

    public synchronized static SQLiteDatabase getDatabase(Class<?> type) {
        return Cache.openDatabase(type);
    }

    public static void beginTransaction(Class<?> type) {
        Cache.openDatabase(type).beginTransaction();
    }

    public static void endTransaction(Class<?> type) {
        Cache.openDatabase(type).endTransaction();
    }

    public static void setTransactionSuccessful(Class<?> type) {
        Cache.openDatabase(type).setTransactionSuccessful();
    }

    public static boolean inTransaction(Class<?> type) {
        return Cache.openDatabase(type).inTransaction();
    }

    public static void execSQL(Class<?> type, String sql) {
        Cache.openDatabase(type).execSQL(sql);
    }

    public static void execSQL(Class<?> type, String sql, Object[] bindArgs) {
        Cache.openDatabase(type).execSQL(sql, bindArgs);
    }
}