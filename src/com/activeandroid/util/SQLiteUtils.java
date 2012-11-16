package com.activeandroid.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import android.os.Build;
import android.text.TextUtils;

import com.activeandroid.Cache;
import com.activeandroid.TableInfo;
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

	public static String createTableDefinition(TableInfo tableInfo) {
		final ArrayList<String> definitions = new ArrayList<String>();

		for (Field field : tableInfo.getFields()) {
			String definition = createColumnDefinition(tableInfo, field);
			if (!TextUtils.isEmpty(definition)) {
				definitions.add(definition);
			}
		}

		return String.format("CREATE TABLE IF NOT EXISTS %s (%s);", tableInfo.getTableName(),
				TextUtils.join(", ", definitions));
	}

	public static String createColumnDefinition(TableInfo tableInfo, Field field) {
		String definition = null;

		final Class<?> type = field.getType();
		final String name = tableInfo.getColumnName(field);
		final TypeSerializer typeSerializer = Cache.getParserForType(tableInfo.getType());
		final Column column = field.getAnnotation(Column.class);

		if (typeSerializer != null) {
			definition = name + " " + typeSerializer.getSerializedType().toString();
		}
		else if (TYPE_MAP.containsKey(type)) {
			definition = name + " " + TYPE_MAP.get(type).toString();
		}
		else if (ReflectionUtils.isModel(type)) {
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

			if (FOREIGN_KEYS_SUPPORTED && ReflectionUtils.isModel(type)) {
				definition += " REFERENCES " + tableInfo.getTableName() + "(Id)";
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