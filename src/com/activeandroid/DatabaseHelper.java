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
import android.os.Build;
import android.text.TextUtils;

import com.activeandroid.annotation.Column;
import com.activeandroid.serializer.TypeSerializer;

class DatabaseHelper extends SQLiteOpenHelper {
	private final static String AA_DB_NAME = "AA_DB_NAME";
	private final static String AA_DB_VERSION = "AA_DB_VERSION";
	private final static String MIGRATION_PATH = "migrations";

	private final static boolean FOREIGN_KEYS_SUPPORTED = Integer.parseInt(Build.VERSION.SDK) >= 8;

	private Context mContext;

	//////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS

	public DatabaseHelper(Context context) {
		super(context, getDBName(), null, getDBVersion());
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		if (FOREIGN_KEYS_SUPPORTED) {
			db.execSQL("PRAGMA foreign_keys=ON;");
			Log.i("Foreign Keys supported. Enabling foreign key features.");
		}

		final ArrayList<Class<? extends Model>> tables = ReflectionUtils.getModelClasses();

		Log.i("Creating " + tables.size() + " tables");

		for (Class<? extends Model> table : tables) {
			createTable(db, table);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (FOREIGN_KEYS_SUPPORTED) {
			db.execSQL("PRAGMA foreign_keys=ON;");
			Log.i("Foreign Keys supported. Enabling foreign key features.");
		}

		if (!executeMigrations(db, oldVersion, newVersion)) {
			Log.i("No migrations found. Calling onCreate");
			onCreate(db);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS

	private boolean executeMigrations(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i("Checking for migration scripts");

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
					Log.w("Skipping invalidly named file: " + file);
				}
			}
		}
		catch (IOException e) {
			Log.e(e.getMessage());
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
			Log.e(e.getMessage());
		}

		db.execSQL(text.toString());
	}

	private void createTable(SQLiteDatabase db, Class<? extends Model> table) {
		ArrayList<Field> fields = ReflectionUtils.getTableFields(table);
		ArrayList<String> definitions = new ArrayList<String>();

		for (Field field : fields) {
			String definition = createColumnDefinition(field);

			if (definition != null) {
				definitions.add(definition);
			}
		}

		String sql = String.format("CREATE TABLE IF NOT EXISTS %s (%s);", ReflectionUtils.getTableName(table),
				TextUtils.join(", ", definitions));

		Log.i(sql);

		db.execSQL(sql);
	}

	private String createColumnDefinition(Field field) {
		String definition = null;

		final Class<?> type = field.getType();
		final String name = ReflectionUtils.getColumnName(field);
		final TypeSerializer typeSerializer = Registry.getInstance().getParserForType(type);

		// Column definition
		final Column column = field.getAnnotation(Column.class);
		final Integer length = column.length();

		if (typeSerializer != null) {
			definition = name + " " + typeSerializer.getSerializedType().toString();
		}
		else if (ReflectionUtils.typeIsSQLiteReal(type)) {
			definition = name + " REAL";

		}
		else if (ReflectionUtils.typeIsSQLiteInteger(type)) {
			definition = name + " INTEGER";

		}
		else if (ReflectionUtils.typeIsSQLiteText(type)) {
			definition = name + " TEXT";
		}

		if (definition != null) {
			//////////////////////////////////
			// LENGTH

			if (length > -1) {
				definition += "(" + length + ")";
			}

			//////////////////////////////////
			// PRIMARY KEY

			if (name.equals("Id")) {
				definition += " PRIMARY KEY AUTOINCREMENT";
			}

			//////////////////////////////////
			// NOT NULL

			if (column.notNull()) {
				definition += " NOT NULL ON CONFLICT " + column.onNullConflict().toString();
			}

			//////////////////////////////////
			// FOREIGN KEY

			if (FOREIGN_KEYS_SUPPORTED && !type.isPrimitive() && type.getSuperclass() != null
					&& type.getSuperclass().equals(Model.class)) {

				definition += " REFERENCES " + ReflectionUtils.getTableName(type) + "(Id)";
				definition += " ON DELETE " + column.onDelete().toString().replace("_", " ");
				definition += " ON UPDATE " + column.onUpdate().toString().replace("_", " ");
			}
		}

		return definition;
	}

	private static String getDBName() {
		String aaName = ReflectionUtils.getMetaDataString(AA_DB_NAME);

		if (aaName == null) {
			aaName = "Application.db";
		}

		return aaName;
	}

	private static int getDBVersion() {
		Integer aaVersion = ReflectionUtils.getMetaDataInteger(AA_DB_VERSION);

		if (aaVersion == null || aaVersion == 0) {
			aaVersion = 1;
		}

		return aaVersion;
	}
}