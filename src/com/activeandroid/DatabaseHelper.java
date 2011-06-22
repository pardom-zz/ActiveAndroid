package com.activeandroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DatabaseHelper extends SQLiteOpenHelper {
	private final static String AA_DB_NAME = "AA_DB_NAME";
	private final static String AA_DB_VERSION = "AA_DB_VERSION";
	private final static String MIGRATION_PATH = "migrations";

	private Context mContext;

	//////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS

	public DatabaseHelper(Context context) {
		super(context, getDBName(context), null, getDBVersion(context));
		mContext = context.getApplicationContext();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		final ArrayList<Class<? extends ActiveRecordBase<?>>> tables = ReflectionUtils.getEntityClasses(mContext);

		if (Params.LOGGING_ENABLED) {
			Log.v(Params.LOGGING_TAG, "Creating " + tables.size() + " tables");
		}

		for (Class<? extends ActiveRecordBase<?>> table : tables) {
			createTable(db, table);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (!executeMigrations(db, oldVersion, newVersion)) {
			onCreate(db);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS

	private boolean executeMigrations(SQLiteDatabase db, int oldVersion, int newVersion) {
		boolean migrationExecuted = false;
		try {
			final List<String> files = Arrays.asList(mContext.getAssets().list(MIGRATION_PATH));
			Collections.sort(files, new NaturalOrderComparator());

			for (String file : files) {
				try {
					final int version = Integer.valueOf(file.replace(".sql", ""));

					if (version > oldVersion && version <= newVersion) {
						executeSqlScript(db, file);
						migrationExecuted = true;
					}
				}
				catch (NumberFormatException e) {
					Log.w(Params.LOGGING_TAG, "Skipping invalidly named file: " + file);
				}
			}
		}
		catch (IOException e) {
			Log.e(Params.LOGGING_TAG, e.getMessage());
		}

		return migrationExecuted;
	}

	private void executeSqlScript(SQLiteDatabase db, String file) {
		final StringBuilder text = new StringBuilder();

		try {
			final InputStream is = mContext.getAssets().open(MIGRATION_PATH + "/" + file);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;

			while ((line = reader.readLine()) != null) {
				text.append(line);
				text.append("\n");
			}

		}
		catch (IOException e) {
			Log.e(Params.LOGGING_TAG, e.getMessage());
		}

		db.execSQL(text.toString());
	}

	private void createTable(SQLiteDatabase db, Class<? extends ActiveRecordBase<?>> table) {
		ArrayList<Field> fields = ReflectionUtils.getTableFields(mContext, table);
		ArrayList<String> definitions = new ArrayList<String>();

		for (Field field : fields) {
			Class<?> fieldType = field.getType();
			final String fieldName = ReflectionUtils.getColumnName(mContext, field);
			final Integer fieldLength = ReflectionUtils.getColumnLength(mContext, field);
			String definition = null;

			TypeSerializer typeSerializer = ((Application) mContext).getParserForType(fieldType);
			if (typeSerializer != null) {
				definition = fieldName + " " + typeSerializer.getSerializedType().toString();
			}
			else if (ReflectionUtils.typeIsSQLiteReal(fieldType)) {
				definition = fieldName + " REAL";

			}
			else if (ReflectionUtils.typeIsSQLiteInteger(fieldType)) {
				definition = fieldName + " INTEGER";

			}
			else if (ReflectionUtils.typeIsSQLiteString(fieldType)) {
				definition = fieldName + " TEXT";
			}

			if (definition != null) {
				if (fieldLength != null && fieldLength > 0) {
					definition += "(" + fieldLength + ")";
				}

				if (fieldName.equals("Id")) {
					definition += " PRIMARY KEY AUTOINCREMENT";
				}

				definitions.add(definition);
			}
		}

		String sql = StringUtils.format("CREATE TABLE IF NOT EXISTS {0} ({1});",
				ReflectionUtils.getTableName(mContext, table), StringUtils.join(definitions, ", "));

		if (Params.LOGGING_ENABLED) {
			Log.v(Params.LOGGING_TAG, sql);
		}

		db.execSQL(sql);
	}

	private static String getDBName(Context context) {
		String aaName = ReflectionUtils.getMetaDataString(context, AA_DB_NAME);

		if (aaName == null) {
			aaName = "Application.db";
		}

		return aaName;
	}

	private static int getDBVersion(Context context) {
		Integer aaVersion = ReflectionUtils.getMetaDataInteger(context, AA_DB_VERSION);

		if (aaVersion == null || aaVersion == 0) {
			aaVersion = 1;
		}

		return aaVersion;
	}
}
