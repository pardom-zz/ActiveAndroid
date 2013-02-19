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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.activeandroid.util.Log;
import com.activeandroid.util.NaturalOrderComparator;
import com.activeandroid.util.SQLiteUtils;

public final class DatabaseHelper extends SQLiteOpenHelper {

    private DbMetaData mDbMetaData;

    //////////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    //////////////////////////////////////////////////////////////////////////////////////

    public DatabaseHelper(Context context, DbMetaData metaData) {
        super(context, metaData.getDatabaseName(), null, metaData.getDatabaseVersion());
        mDbMetaData = metaData;
        copyAttachedDatabase(context);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // OVERRIDEN METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (SQLiteUtils.FOREIGN_KEYS_SUPPORTED) {
            db.execSQL("PRAGMA foreign_keys=ON;");
            Log.i("Foreign Keys supported. Enabling foreign key features.");
        }

        db.beginTransaction();

        for (TableInfo tableInfo : Cache.getTableInfos()) {
            db.execSQL(SQLiteUtils.createTableDefinition(tableInfo));
            Log.d("generate index:" + SQLiteUtils.createIndexDefinition(tableInfo));
        }



        db.setTransactionSuccessful();
        db.endTransaction();

        executeMigrations(db, -1, db.getVersion());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (SQLiteUtils.FOREIGN_KEYS_SUPPORTED) {
            db.execSQL("PRAGMA foreign_keys=ON;");
            Log.i("Foreign Keys supported. Enabling foreign key features.");
        }

        if (!executeMigrations(db, oldVersion, newVersion)) {
            Log.i("No migrations found. Calling onCreate.");
            onCreate(db);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    public void copyAttachedDatabase(Context context) {
        String dbName = mDbMetaData.getDatabaseName();
        final File dbPath = context.getDatabasePath(dbName);

        // If the database already exists, return
        if (dbPath.exists()) {
            return;
        }

        // Make sure we have a path to the file
        dbPath.getParentFile().mkdirs();

        // Try to copy database file
        try {
            final InputStream inputStream = context.getAssets().open(dbName);
            final OutputStream output = new FileOutputStream(dbPath);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            output.flush();
            output.close();
            inputStream.close();
        }
        catch (IOException e) {
            Log.e("Failed to open file", e);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    private boolean executeMigrations(SQLiteDatabase db, int oldVersion, int newVersion) {
        boolean migrationExecuted = false;
        try {
            final List<String> files = Arrays.asList(Cache.getContext().getAssets().list(mDbMetaData.getMigrationPath()));
            Collections.sort(files, new NaturalOrderComparator());

            db.beginTransaction();

            for (String file : files) {
                try {
                    final int version = Integer.valueOf(file.replace(".sql", ""));

                    if (version > oldVersion && version <= newVersion) {
                        executeSqlScript(db, file);
                        migrationExecuted = true;

                        Log.i(file + " executed succesfully.");
                    }
                }
                catch (NumberFormatException e) {
                    Log.w("Skipping invalidly named file: " + file, e);
                }
            }

            db.setTransactionSuccessful();
            db.endTransaction();
        }
        catch (IOException e) {
            Log.e("Failed to execute migrations.", e);
        }

        return migrationExecuted;
    }

    private void executeSqlScript(SQLiteDatabase db, String file) {
        try {
            final InputStream input = Cache.getContext().getAssets().open(mDbMetaData.getMigrationPath() + "/" + file);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = null;

            while ((line = reader.readLine()) != null) {
                db.execSQL(line.replace(";", ""));
            }
        }
        catch (IOException e) {
            Log.e("Failed to execute " + file, e);
        }
    }

}