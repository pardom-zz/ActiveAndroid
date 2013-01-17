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

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.activeandroid.serializer.TypeSerializer;
import com.activeandroid.util.Log;
import com.activeandroid.util.ReflectionUtils;

public final class Cache {
    //////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    //////////////////////////////////////////////////////////////////////////////////////

    private static Context sContext;

    private static ModelInfo sModelInfo;

    private static Set<SoftReference<Model>> sEntities;

    private static Hashtable<Class<? extends DbMetaData>, DatabaseHelper> sDatabaseHelper;

    //////////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    //////////////////////////////////////////////////////////////////////////////////////

    private Cache() {
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    public static synchronized void initialize(Application application) {
        initialize(application, null);
    }

    public static synchronized void initialize(Application application, DbMetaData metaData) {
        DbMetaData meta = metaData;
        if (meta == null) meta = DefaultMetaData.getInstanse(application);

        // initialize static variables
        if (sContext == null) sContext = application;
        if (sModelInfo == null) sModelInfo = new ModelInfo(application);
        if (sDatabaseHelper == null)
            sDatabaseHelper = new Hashtable<Class<? extends DbMetaData>, DatabaseHelper>();
        if (sEntities == null)
            sEntities = new HashSet<SoftReference<Model>>();

        // if database
        if (sDatabaseHelper.get(meta.getClass()) != null && !meta.isResettable()) {
            Log.v("ActiveAndroid already initialized.");
            return;

        } else if (sDatabaseHelper.get(meta.getClass()) != null) {
            Log.v("ActiveAndroid "+meta.getClass().getSimpleName()+" already initialized. Reset by new meta data");
            closeDatabase(meta.getClass());
        }

        // initialize DatabaseHelper
        sDatabaseHelper.put(meta.getClass(), new DatabaseHelper(sContext, meta));

        openDatabase(meta.getClass());

        Log.v("ActiveAndroid "+ meta.getClass().getSimpleName()+" initialized succesfully.");
    }

    public static synchronized void clearCache() {
        sEntities.clear();
        Log.v("Cache cleared.");
    }

    public static synchronized void dispose() {
        closeAllDatabase();

        sEntities = null;
        sModelInfo = null;
        sDatabaseHelper = null;

        Log.v("ActiveAndroid disposed. Call initialize to use library.");
    }

    // Database access

    public static synchronized SQLiteDatabase openDatabase(Class<?> type) {
        Class<? extends DbMetaData> metaDataType = ReflectionUtils.getDbMetaDataClass(type);
        DatabaseHelper databaseHelper = sDatabaseHelper.get(metaDataType);
        if (databaseHelper != null) return databaseHelper.getWritableDatabase();
        throw new IllegalArgumentException("db meta" + metaDataType.getClass().getSimpleName() +" not found!");
    }

    public static synchronized void closeDatabase(Class<?> type) {
        Class<? extends DbMetaData> metaDataType = ReflectionUtils.getDbMetaDataClass(type);
        DatabaseHelper databaseHelper = sDatabaseHelper.get(metaDataType);
        if (databaseHelper != null) databaseHelper.close();
    }

    public static synchronized void closeAllDatabase() {
        for (DatabaseHelper databaseHelper : sDatabaseHelper.values()) {
            databaseHelper.close();
        }
    }

    // Context access

    public static Context getContext() {
        return sContext;
    }

    // Entity cache

    public static synchronized void addEntity(Model entity) {
        sEntities.add(new SoftReference<Model>(entity));
    }

    public static synchronized Model getEntity(Class<? extends Model> type, long id) {
        for (SoftReference<Model> ref: sEntities) {
            Model entity = ref.get();
            if (entity != null && entity.getClass() != null && entity.getClass() == type && entity.getId() != null
                    && entity.getId() == id) {

                return entity;
            }
        }

        return null;
    }

    public static synchronized void removeEntity(Model entity) {
        SoftReference<Model> removedRef = null;
        for (SoftReference<Model> ref: sEntities) {
            Model model = ref.get();
            if (entity == model) {
                removedRef = ref;
                break;
            }
        }

        if (removedRef != null) sEntities.remove(removedRef);
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
}