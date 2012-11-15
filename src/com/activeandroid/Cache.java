package com.activeandroid;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.activeandroid.serializer.TypeSerializer;
import com.activeandroid.util.Log;
import com.activeandroid.util.ReflectionUtils;

public final class Cache {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private static Context sContext;
	private static DatabaseHelper sDatabaseHelper;

	private static Set<Model> sEntities;
	private static HashMap<Class<?>, String> sTableNames;
	private static HashMap<Field, String> sColumnNames;
	private static HashMap<Class<?>, ArrayList<Field>> sClassFields;
	private static HashMap<Class<?>, TypeSerializer> sParsers;

	private static boolean sIsInitialized = false;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	private Cache() {
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public static synchronized void initialize(Context context) {
		if (sIsInitialized) {
			Log.v("ActiveAndroid already initialized.");
			return;
		}

		sContext = context.getApplicationContext();

		sDatabaseHelper = new DatabaseHelper(sContext);
		sParsers = ReflectionUtils.getParsers(sContext);

		sEntities = new HashSet<Model>();
		sTableNames = new HashMap<Class<?>, String>();
		sClassFields = new HashMap<Class<?>, ArrayList<Field>>();
		sColumnNames = new HashMap<Field, String>();

		openDatabase();

		sIsInitialized = true;

		Log.v("ActiveAndroid initialized succesfully.");
	}

	public static synchronized void clear() {
		sEntities = new HashSet<Model>();
		sTableNames = new HashMap<Class<?>, String>();
		sClassFields = new HashMap<Class<?>, ArrayList<Field>>();
		sColumnNames = new HashMap<Field, String>();

		Log.v("Cache cleared.");
	}

	public static synchronized void dispose() {
		sDatabaseHelper = null;
		sParsers = null;

		sEntities = null;
		sTableNames = null;
		sClassFields = null;
		sColumnNames = null;

		closeDatabase();

		sIsInitialized = false;

		Log.v("ActiveAndroid disposed. Call initialize to use library.");
	}

	public static synchronized SQLiteDatabase openDatabase() {
		return sDatabaseHelper.getWritableDatabase();
	}

	public static synchronized void closeDatabase() {
		sDatabaseHelper.close();
	}

	public static Context getContext() {
		return sContext;
	}

	public static synchronized void addClassFields(Class<?> type, ArrayList<Field> fields) {
		sClassFields.put(type, fields);
	}

	public static synchronized void addColumnName(Field field, String columnName) {
		sColumnNames.put(field, columnName);
	}

	public static synchronized void addEntity(Model entity) {
		sEntities.add(entity);
	}

	public static synchronized void addTableName(Class<?> type, String tableName) {
		sTableNames.put(type, tableName);
	}

	public static synchronized ArrayList<Field> getClassFields(Class<?> type) {
		return sClassFields.get(type);
	}

	public static synchronized String getColumnName(Field field) {
		return sColumnNames.get(field);
	}

	public static synchronized Model getEntity(Class<? extends Model> entityType, long id) {
		for (Model entity : sEntities) {
			if (entity != null) {
				if (entity.getClass() != null && entity.getClass() == entityType) {
					if (entity.getId() != null && entity.getId() == id) {
						return entity;
					}
				}
			}
		}

		return null;
	}

	public static synchronized TypeSerializer getParserForType(Class<?> fieldType) {
		return sParsers.get(fieldType);
	}

	public static synchronized String getTableName(Class<?> type) {
		return sTableNames.get(type);
	}

	public static synchronized void removeEntity(Model entity) {
		sEntities.remove(entity);
	}
}