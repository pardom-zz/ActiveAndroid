package com.activeandroid.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import android.os.Build;
import android.text.TextUtils;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.serializer.TypeSerializer;

public class SQLiteUtils {
	//////////////////////////////////////////////////////////////////////////////////////
	// ENUMERATIONS
	//////////////////////////////////////////////////////////////////////////////////////

	public enum SQLiteType {
		INTEGER, REAL, TEXT, BLOB
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	public static final boolean FOREIGN_KEYS_SUPPORTED = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONTSANTS
	//////////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("serial")
	private static final HashMap<Class<?>, SQLiteType> TYPE_MAP = new HashMap<Class<?>, SQLiteType>() {
		{
			put(byte.class, SQLiteType.INTEGER);
			put(short.class, SQLiteType.INTEGER);
			put(int.class, SQLiteType.INTEGER);
			put(long.class, SQLiteType.INTEGER);
			put(float.class, SQLiteType.REAL);
			put(double.class, SQLiteType.REAL);
			put(boolean.class, SQLiteType.INTEGER);
			put(char.class, SQLiteType.TEXT);
			put(Byte.class, SQLiteType.INTEGER);
			put(Short.class, SQLiteType.INTEGER);
			put(Integer.class, SQLiteType.INTEGER);
			put(Long.class, SQLiteType.INTEGER);
			put(Float.class, SQLiteType.REAL);
			put(Double.class, SQLiteType.REAL);
			put(Boolean.class, SQLiteType.INTEGER);
			put(Character.class, SQLiteType.TEXT);
			put(String.class, SQLiteType.TEXT);
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public static String createTableDefinition(Class<? extends Model> type) {
		final ArrayList<Field> fields = ReflectionUtils.getColumnFields(type);
		final ArrayList<String> definitions = new ArrayList<String>();

		for (Field field : fields) {
			String definition = createColumnDefinition(field);
			if (!TextUtils.isEmpty(definition)) {
				definitions.add(definition);
			}
		}

		return String.format("CREATE TABLE IF NOT EXISTS %s (%s);", ReflectionUtils.getTableName(type),
				TextUtils.join(", ", definitions));
	}

	public static String createColumnDefinition(Field field) {
		String definition = null;

		final Class<?> type = field.getType();
		final String name = ReflectionUtils.getColumnName(field);
		final TypeSerializer typeSerializer = Cache.getParserForType(type);
		final Column column = field.getAnnotation(Column.class);

		if (typeSerializer != null) {
			definition = name + " " + typeSerializer.getSerializedType().toString();
		}
		else if (TYPE_MAP.containsKey(type)) {
			definition = name + " " + TYPE_MAP.get(type).toString();
		}
		else if (ReflectionUtils.isModelSubclass(type)) {
			definition = name + " " + SQLiteType.INTEGER.toString();
		}

		if (definition != null) {
			if (column.length() > -1) {
				definition += "(" + column.length() + ")";
			}

			if (name.equals("Id")) {
				definition += " PRIMARY KEY AUTOINCREMENT";
			}

			if (column.notNull()) {
				definition += " NOT NULL ON CONFLICT " + column.onNullConflict().toString();
			}

			if (FOREIGN_KEYS_SUPPORTED && ReflectionUtils.isModelSubclass(type)) {
				definition += " REFERENCES " + ReflectionUtils.getTableName(type) + "(Id)";
				definition += " ON DELETE " + column.onDelete().toString().replace("_", " ");
				definition += " ON UPDATE " + column.onUpdate().toString().replace("_", " ");
			}
		}
		else {
			Log.e("No type mapping for: " + type.toString());
		}

		return definition;
	}
}