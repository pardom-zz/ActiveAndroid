package com.activeandroid.test.automigration;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.activeandroid.Model;

import android.app.Application;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.test.ApplicationTestCase;

public abstract class AutoMigrationTest extends ApplicationTestCase<Application> {

	protected static final String DATABASE = "auto_migration.db";

	private String mTable;

	public AutoMigrationTest(String table) {
		super(Application.class);
		this.mTable = table;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createApplication();
	}

	protected void initializeActiveAndroid(Class<? extends Model> clazz) {
		ActiveAndroid.initialize(new Configuration.Builder(getApplication()).addModelClass(clazz).setDatabaseName(DATABASE).setDatabaseVersion(3).create(), true);
	}

	protected void createOldDatabase() {
		ActiveAndroid.dispose();
		getApplication().deleteDatabase(DATABASE);
		SQLiteDatabase db = getApplication().openOrCreateDatabase(DATABASE, 0, null);
		db.execSQL("PRAGMA user_version = 2");
		db.execSQL("CREATE TABLE " + mTable + " (Id INTEGER PRIMARY KEY AUTOINCREMENT, textValue TEXT, boolValue INTEGER, floatValue REAL, unusedColumn INTEGER);");
		db.beginTransaction();
		try {
			for (int i = 0; i < 10; ++i) {
				db.insert(mTable, null, getContentValues("textValue", "Text " + i, "boolValue", i % 2 == 0, "floatValue", (float) i, "unusedColumn", i * 100));
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	private ContentValues getContentValues(Object... args) {
		assertTrue(args.length % 2 == 0);
		ContentValues contentValues = new ContentValues();
		for (int i = 0; i < args.length / 2; ++i) {
			String key = (String) args[i * 2];
			Object value = args[(i * 2) + 1];
			if (value instanceof Float)
				contentValues.put(key, (Float) value);
			else if (value instanceof String)
				contentValues.put(key, (String) value);
			else if (value instanceof Integer)
				contentValues.put(key, (Integer) value);
			else if (value instanceof Long)
				contentValues.put(key, (Long) value);
			else if (value instanceof Double)
				contentValues.put(key, (Double) value);
			else if (value instanceof Boolean)
				contentValues.put(key, (Boolean) value);
		}
		return contentValues;
	}

}
