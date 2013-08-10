package com.activeandroid.util;

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

import android.database.Cursor;
import android.os.Build;
import android.text.TextUtils;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.TableInfo;
import com.activeandroid.annotation.Column;
import com.activeandroid.serializer.TypeSerializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class SQLiteUtils {
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
			put(byte[].class, SQLiteType.BLOB);
			put(Byte.class, SQLiteType.INTEGER);
			put(Short.class, SQLiteType.INTEGER);
			put(Integer.class, SQLiteType.INTEGER);
			put(Long.class, SQLiteType.INTEGER);
			put(Float.class, SQLiteType.REAL);
			put(Double.class, SQLiteType.REAL);
			put(Boolean.class, SQLiteType.INTEGER);
			put(Character.class, SQLiteType.TEXT);
			put(String.class, SQLiteType.TEXT);
			put(Byte[].class, SQLiteType.BLOB);
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public static void execSql(String sql) {
		Cache.openDatabase().execSQL(sql);
	}

	public static void execSql(String sql, Object[] bindArgs) {
		Cache.openDatabase().execSQL(sql, bindArgs);
	}

	public static <T extends Model> List<T> rawQuery(Class<? extends Model> type, String sql, String[] selectionArgs) {
		Cursor cursor = Cache.openDatabase().rawQuery(sql, selectionArgs);
		List<T> entities = processCursor(type, cursor);
		cursor.close();

		return entities;
	}

	public static <T extends Model> T rawQuerySingle(Class<? extends Model> type, String sql, String[] selectionArgs) {
		List<T> entities = rawQuery(type, sql, selectionArgs);

		if (entities.size() > 0) {
			return entities.get(0);
		}

		return null;
	}

	// Database creation

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

	@SuppressWarnings("unchecked")
	public static String createColumnDefinition(TableInfo tableInfo, Field field) {
		StringBuilder definition = new StringBuilder();

		Class<?> type = field.getType();
		final String name = tableInfo.getColumnName(field);
		final TypeSerializer typeSerializer = Cache.getParserForType(field.getType());
		final Column column = field.getAnnotation(Column.class);

		if (typeSerializer != null) {
			type = typeSerializer.getSerializedType();
		}

		if (TYPE_MAP.containsKey(type)) {
			definition.append(name);
			definition.append(" ");
			definition.append(TYPE_MAP.get(type).toString());
		}
		else if (ReflectionUtils.isModel(type)) {
			definition.append(name);
			definition.append(" ");
			definition.append(SQLiteType.INTEGER.toString());
		}
		else if (ReflectionUtils.isSubclassOf(type, Enum.class)) {
			definition.append(name);
			definition.append(" ");
			definition.append(SQLiteType.TEXT.toString());
		}

		if (!TextUtils.isEmpty(definition)) {
			if (column.length() > -1) {
				definition.append("(");
				definition.append(column.length());
				definition.append(")");
			}

			if (name.equals("Id")) {
				definition.append(" PRIMARY KEY AUTOINCREMENT");
			}

			if (column.notNull()) {
				definition.append(" NOT NULL ON CONFLICT ");
				definition.append(column.onNullConflict().toString());
			}

			if (column.unique()) {
				definition.append(" UNIQUE ON CONFLICT ");
				definition.append(column.onUniqueConflict().toString());
			}

			if (FOREIGN_KEYS_SUPPORTED && ReflectionUtils.isModel(type)) {
				definition.append(" REFERENCES ");
				definition.append(Cache.getTableInfo((Class<? extends Model>) type).getTableName());
				definition.append("(Id)");
				definition.append(" ON DELETE ");
				definition.append(column.onDelete().toString().replace("_", " "));
				definition.append(" ON UPDATE ");
				definition.append(column.onUpdate().toString().replace("_", " "));
			}
		}
		else {
			Log.e("No type mapping for: " + type.toString());
		}

		return definition.toString();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Model> List<T> processCursor(Class<? extends Model> type, Cursor cursor) {
		final List<T> entities = new ArrayList<T>();

		try {
			Constructor<?> entityConstructor = type.getConstructor();

			if (cursor.moveToFirst()) {
				do {
					Model entity = Cache.getEntity(type, cursor.getLong(cursor.getColumnIndex("Id")));
					if (entity == null) {
						entity = (T) entityConstructor.newInstance();
					}

					entity.loadFromCursor(cursor);
					entities.add((T) entity);
				}
				while (cursor.moveToNext());
			}

		}
		catch (Exception e) {
			Log.e("Failed to process cursor.", e);
		}

		return entities;
	}
}
