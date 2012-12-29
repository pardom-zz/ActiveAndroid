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
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.activeandroid.migration.Migration;
import com.activeandroid.migration.MigrationOperation;
import com.activeandroid.util.*;

public final class DatabaseHelper extends SQLiteOpenHelper {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private final static String AA_DB_NAME = "AA_DB_NAME";
	private final static String AA_DB_VERSION = "AA_DB_VERSION";

	private final static String MIGRATION_PATH = "migrations";

    private Context mContext;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public DatabaseHelper(Context context) {
		super(context, getDbName(context), null, getDbVersion(context));
        mContext = context;
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
		String dbName = getDbName(context);
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

    private boolean executeNamedMigrations(SQLiteDatabase db, int oldVersion, int newVersion) {
        boolean migrationExecuted = false;

        List<Migration> migrations = MigrationUtils.getMigrations(mContext, oldVersion, newVersion);
        Collections.sort(migrations, new Comparator<Migration>() {
            @Override
            public int compare(Migration migration, Migration migration2) {
                return migration.databaseVersion() - migration2.databaseVersion();
            }
        });

        db.beginTransaction();

        for (Migration migration : migrations) {
            String []sqlStatements = migration.executeSql();

            if (sqlStatements == null) {
                sqlStatements = MigrationUtils.migrationOperationsToSql(migration.change());
            }

            if (sqlStatements == null) break;

            for (String statement : sqlStatements) {
                db.execSQL(statement);
            }

            migrationExecuted = true;
        }

        db.setTransactionSuccessful();
        db.endTransaction();

        return migrationExecuted;
    }

	private boolean executeMigrations(SQLiteDatabase db, int oldVersion, int newVersion) {
		boolean migrationExecuted = false;
		try {
			final List<String> files = Arrays.asList(Cache.getContext().getAssets().list(MIGRATION_PATH));
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
			final InputStream input = Cache.getContext().getAssets().open(MIGRATION_PATH + "/" + file);
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

	// Meta-data methods

	private static String getDbName(Context context) {
		String aaName = ReflectionUtils.getMetaData(context, AA_DB_NAME);

		if (aaName == null) {
			aaName = "Application.db";
		}

		return aaName;
	}

	private static int getDbVersion(Context context) {
		Integer aaVersion = ReflectionUtils.getMetaData(context, AA_DB_VERSION);

		if (aaVersion == null || aaVersion == 0) {
			aaVersion = 1;
		}

		return aaVersion;
	}
}