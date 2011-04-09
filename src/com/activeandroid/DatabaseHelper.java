package com.activeandroid;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import dalvik.system.DexFile;

class DatabaseHelper extends SQLiteOpenHelper {
	private final static String AA_DB_NAME = "AA_DB_NAME";
	private final static String AA_DB_VERSION = "AA_DB_VERSION";
	private final static String MIGRATION_PATH = "migrations";

	private Context mContext;

	//////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS

	public DatabaseHelper(Context context) {
		super(context, getDBName(context), null, getDBVersion(context));
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		ArrayList<Class<? extends ActiveRecordBase<?>>> tables = getEntityClasses(mContext);

		if (Params.LOGGING_ENABLED) {
			Log.i(Params.LOGGING_TAG, "Creating " + tables.size() + " tables");
		}

		for (Class<? extends ActiveRecordBase<?>> table : tables) {
			createTable(db, table);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
		executeMigrations(db, oldVersion, newVersion);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS

	private void executeMigrations(SQLiteDatabase db, int oldVersion, int newVersion) {
		try {
			List<String> files = Arrays.asList(mContext.getAssets().list(MIGRATION_PATH));
			Collections.sort(files, new AlphanumComparator());

			for (String file : files) {
				try {
					int version = Integer.valueOf(file.replace(".sql", ""));

					if (version > oldVersion && version <= newVersion) {
						executeSqlScript(db, file);
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
	}

	private void executeSqlScript(SQLiteDatabase db, String file) {
		Log.d(Params.LOGGING_TAG, file);
	}

	private static void createTable(SQLiteDatabase db, Class<? extends ActiveRecordBase<?>> table) {
		ArrayList<Field> fields = ReflectionUtils.getTableFields(table);
		ArrayList<String> definitions = new ArrayList<String>();

		for (Field field : fields) {
			Class<?> fieldType = field.getType();
			String fieldName = ReflectionUtils.getColumnName(field);
			Integer fieldLength = ReflectionUtils.getColumnLength(field);
			String definition = null;

			if (ReflectionUtils.typeIsSQLiteFloat(fieldType)) {
				definition = fieldName + " FLOAT";

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

		String sql = StringUtils.format("CREATE TABLE IF NOT EXISTS {0} ({1});", ReflectionUtils.getTableName(table),
				StringUtils.join(definitions, ", "));

		if (Params.LOGGING_ENABLED) {
			Log.i(Params.LOGGING_TAG, sql);
		}

		db.execSQL(sql);
	}

	@SuppressWarnings("unchecked")
	private static ArrayList<Class<? extends ActiveRecordBase<?>>> getEntityClasses(Context context) {
		ArrayList<Class<? extends ActiveRecordBase<?>>> entityClasses = new ArrayList<Class<? extends ActiveRecordBase<?>>>();

		try {
			String path = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).sourceDir;
			DexFile dexfile = new DexFile(path);
			Enumeration<String> entries = dexfile.entries();

			while (entries.hasMoreElements()) {
				String name = entries.nextElement();
				Class<?> discoveredClass = null;
				Class<?> superClass = null;

				try {
					discoveredClass = Class.forName(name, true, context.getClass().getClassLoader());
					superClass = discoveredClass.getSuperclass();
				}
				catch (ClassNotFoundException e) {
					Log.e(Params.LOGGING_TAG, e.getMessage());
				}

				if (discoveredClass != null && superClass != null) {
					if (discoveredClass.getSuperclass().equals(ActiveRecordBase.class)) {
						entityClasses.add((Class<? extends ActiveRecordBase<?>>) discoveredClass);
					}
				}
			}

		}
		catch (IOException e) {
			Log.e(Params.LOGGING_TAG, e.getMessage());
		}
		catch (NameNotFoundException e) {
			Log.e(Params.LOGGING_TAG, e.getMessage());
		}

		return entityClasses;
	}

	private static String getDBName(Context context) {
		String aaName = getMetaDataString(context, AA_DB_NAME);

		if (aaName == null) {
			aaName = "Application.db";
		}

		return aaName;
	}

	private static int getDBVersion(Context context) {
		Integer aaVersion = getMetaDataInteger(context, AA_DB_VERSION);

		if (aaVersion == null || aaVersion == 0) {
			aaVersion = 1;
		}

		return aaVersion;
	}

	private static String getMetaDataString(Context context, String name) {
		String value = null;
		PackageManager pm;
		ApplicationInfo ai;

		pm = context.getPackageManager();

		try {

			ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			value = ai.metaData.getString(name);

		}
		catch (Exception e) {
			Log.w(Params.LOGGING_TAG, "Couldn't find meta data string: " + name);
		}

		return value;
	}

	private static Integer getMetaDataInteger(Context context, String name) {
		Integer value = null;
		PackageManager pm;
		ApplicationInfo ai;

		pm = context.getPackageManager();

		try {

			ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			value = ai.metaData.getInt(name);

		}
		catch (Exception e) {
			Log.w(Params.LOGGING_TAG, "Couldn't find meta data string: " + name);
		}

		return value;
	}
}
