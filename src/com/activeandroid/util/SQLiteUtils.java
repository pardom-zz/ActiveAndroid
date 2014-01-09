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
import android.database.DatabaseUtils;
import android.os.Build;
import android.text.TextUtils;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.TableInfo;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.ForeignKey;
import com.activeandroid.annotation.PrimaryKey;
import com.activeandroid.exception.PrimaryKeyCannotBeNullException;
import com.activeandroid.serializer.TypeSerializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

	public static String createIndexDefinition(TableInfo tableInfo) {
		final ArrayList<String> definitions = new ArrayList<String>();

		for (Field field : tableInfo.getFields()) {
			String definition = createIndexColumnDefinition(tableInfo, field);
			if (!TextUtils.isEmpty(definition)) {
				definitions.add(definition);
			}
		}
		if (definitions.isEmpty()) return null;

		return String.format("CREATE INDEX IF NOT EXISTS %s on %s(%s);",
				"index_" + tableInfo.getTableName(),
				tableInfo.getTableName(),
				TextUtils.join(",", definitions));
	}

	@SuppressWarnings("unchecked")
	public static String createIndexColumnDefinition(TableInfo tableInfo, Field field) {
		StringBuilder definition = new StringBuilder();

		Class<?> type = field.getType();
		final String name = tableInfo.getColumnName(field);
		final Column column = field.getAnnotation(Column.class);

		if (column.index()) {
			definition.append(name);
		}

		return definition.toString();
	}

	public static String createTableDefinition(TableInfo tableInfo) {
		final ArrayList<String> definitions = new ArrayList<String>();

		for (Field field : tableInfo.getFields()) {
			String definition = createColumnDefinition(tableInfo, field);
			if (!TextUtils.isEmpty(definition)) {
				definitions.add(definition);
			}
		}

        List<Field> primaryColumns = tableInfo.getPrimaryKeys();
        List<Field> foreignColumns = tableInfo.getForeignKeys();
        if(!primaryColumns.isEmpty()){
            StringBuilder builder = new StringBuilder("PRIMARY KEY(");


            for(int i  =0 ; i< primaryColumns.size(); i++){
                builder.append(tableInfo.getColumnName(primaryColumns.get(i)));
                if(i< primaryColumns.size()-1){
                    builder.append(", ");
                }
            }

            builder.append(")");

            definitions.add(builder.toString());
        }

        for(int i = 0; i < foreignColumns.size(); i++){
            final Field column = foreignColumns.get(i);
            ForeignKey foreignKey = column.getAnnotation(ForeignKey.class);

            StringBuilder forDef = new StringBuilder("FOREIGN KEY(");
            forDef.append(tableInfo.getColumnName(column)).append(") REFERENCES ")
                    .append(Cache.getTableName((Class<? extends Model>) column.getType()))
                    .append("(").append(foreignKey.foreignColumn()).append(")");

            definitions.add(forDef.toString());
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

			if (field.isAnnotationPresent(PrimaryKey.class)) {
                PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
                if(primaryKey.type().equals(PrimaryKey.Type.AUTO_INCREMENT)){
				    definition.append(" PRIMARY KEY AUTOINCREMENT");
                }
			}

			if (column.notNull()) {
				definition.append(" NOT NULL ON CONFLICT ");
				definition.append(column.onNullConflict().toString());
			}

			if (column.unique()) {
				definition.append(" UNIQUE ON CONFLICT ");
				definition.append(column.onUniqueConflict().toString());
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
					Model entity = (T) entityConstructor.newInstance();
					entity.loadFromCursor(cursor);
					entities.add((T) entity);
				}
				while (cursor.moveToNext());
			}

		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(
                "Your model " + type.getName() + " does not define a default " +
                "constructor. The default constructor is required for " +
                "now in ActiveAndroid models, as the process to " +
                "populate the ORM model is : " +
                "1. instantiate default model " +
                "2. populate fields"
            );
		}
		catch (Exception e) {
			Log.e("Failed to process cursor.", e);
		}

		return entities;
	}

    /**
     * Returns the where statement with primary keys with no values
     * @param tableInfo
     * @return
     */
    public static String getWhereStatement(Class<? extends Model> modelClass, TableInfo tableInfo){
        List<Field> fields = new ArrayList<Field>();
        ArrayList<Field> primaryColumn = new ArrayList<Field>();
        fields = ReflectionUtils.getAllFields(fields, modelClass);

        for(Field field : fields){
            if(field.isAnnotationPresent(PrimaryKey.class)){
                primaryColumn.add(field);
            }
        }

        final StringBuilder where = new StringBuilder();
        for(int i = 0 ; i < primaryColumn.size(); i++){
            final Field field = primaryColumn.get(i);
            where.append(tableInfo.getColumnName(field));
            where.append("=?");

            if(i < primaryColumn.size()-1){
                where.append(" AND ");
            }
        }

        String sql = where.toString();

        return sql;
    }

    /**
     * Returns the where statement with primary keys and values filled in
     * @param model
     * @param tableInfo
     * @return
     */
    public static String getWhereStatement(Model model, TableInfo tableInfo){
        List<Field> fields = new ArrayList<Field>();
        ArrayList<Field> primaryColumn = new ArrayList<Field>();
        fields = ReflectionUtils.getAllFields(fields, model.getClass());

        for(Field field : fields){
            if(field.isAnnotationPresent(PrimaryKey.class)){
                primaryColumn.add(field);
            }
        }

        final StringBuilder where = new StringBuilder();
        for(int i = 0 ; i < primaryColumn.size(); i++){
            final Field field = primaryColumn.get(i);
            where.append(tableInfo.getColumnName(field));
            where.append("=?");

            if(i < primaryColumn.size()-1){
                where.append(" AND ");
            }
        }

        String sql = where.toString();

        for(int i = 0; i < primaryColumn.size(); i++){
            final Field field = primaryColumn.get(i);
            field.setAccessible(true);
            try {
                Object object = field.get(model);
                if(object==null){
                    throw new PrimaryKeyCannotBeNullException("The primary key: " + field.getName() + "from " + tableInfo.getTableName() + " cannot be null.");
                } else if(object instanceof Number){
                    sql = sql.replaceFirst("\\?", object.toString());
                } else {
                    String escaped = DatabaseUtils.sqlEscapeString(object.toString());

                    sql = sql.replaceFirst("\\?", escaped);
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return sql;
    }

    public static String getWhereFromEntityId(Class<? extends Model> model, String entityId){
        String[] primaries = entityId.split(",");
        String whereString = getWhereStatement(model, Cache.getTableInfo(model));

        List<Field> fields = new ArrayList<Field>();
        fields = ReflectionUtils.getAllFields(fields, model);

        ArrayList<Field> primaryColumn = new ArrayList<Field>();
        for(Field field : fields){
            if(field.isAnnotationPresent(PrimaryKey.class)){
                primaryColumn.add(field);
            }
        }

        for(int i = 0; i < primaries.length; i++){
            final Field field = primaryColumn.get(i);
            field.setAccessible(true);
            try {
                if(field.getType().isAssignableFrom(String.class)){
                    String escaped = DatabaseUtils.sqlEscapeString(primaries[i]);
                    whereString = whereString.replaceFirst("\\?", escaped);
                } else {
                    whereString = whereString.replaceFirst("\\?", primaries[i]);
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        return whereString;
    }

}
