package com.activeandroid;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.activeandroid.serializer.TypeSerializer;
import com.activeandroid.util.Log;

public final class Cache {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private static Context sContext;

	private static ModelInfo sModelInfo;
	private static DatabaseHelper sDatabaseHelper;

	private static Set<Model> sEntities;

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

		sModelInfo = new ModelInfo(sContext);
		sDatabaseHelper = new DatabaseHelper(sContext);

		sEntities = new HashSet<Model>();

		openDatabase();

		sIsInitialized = true;

		Log.v("ActiveAndroid initialized succesfully.");
	}

	public static synchronized void clear() {
		sEntities = new HashSet<Model>();
		Log.v("Cache cleared.");
	}

	public static synchronized void dispose() {
		sEntities = null;
		sModelInfo = null;
		sDatabaseHelper = null;

		closeDatabase();

		sIsInitialized = false;

		Log.v("ActiveAndroid disposed. Call initialize to use library.");
	}

	// Database access

	public static synchronized SQLiteDatabase openDatabase() {
		return sDatabaseHelper.getWritableDatabase();
	}

	public static synchronized void closeDatabase() {
		sDatabaseHelper.close();
	}

	// Context access

	public static Context getContext() {
		return sContext;
	}

	// Entity cache

	public static synchronized void addEntity(Model entity) {
		sEntities.add(entity);
	}

	public static synchronized Model getEntity(Class<? extends Model> type, long id) {
		for (Model entity : sEntities) {
			if (entity != null && entity.getClass() != null && entity.getClass() == type && entity.getId() != null
					&& entity.getId() == id) {

				return entity;
			}
		}

		return null;
	}

	public static synchronized void removeEntity(Model entity) {
		sEntities.remove(entity);
	}

	// Model cache

	public static synchronized List<TableInfo> getTableInfos() {
		return sModelInfo.getTableInfos();
	}

	public static synchronized TableInfo getTableInfo(Class<? extends Model> type) {
		return sModelInfo.getTableInfo(type);
	}

	public static synchronized List<Field> getClassFields(Class<? extends Model> type) {
		return sModelInfo.getTableInfo(type).getFields();
	}

	public static synchronized TypeSerializer getParserForType(Class<?> Type) {
		return sModelInfo.getParser(Type);
	}

	public static synchronized String getTableName(Class<? extends Model> type) {
		return sModelInfo.getTableInfo(type).getTableName();
	}
}