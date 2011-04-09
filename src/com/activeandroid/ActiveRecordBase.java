package com.activeandroid;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.activeandroid.annotation.Column;

@SuppressWarnings("unchecked")
public abstract class ActiveRecordBase<T> {
	////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS

	@Column(name = "Id")
	private Long mId = null;

	private Application mApplication;
	private Context mContext;
	private DatabaseManager mDatabaseManager;
	private String mTableName = ReflectionUtils.getTableName(this.getClass());

	////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS

	public ActiveRecordBase(Context context) {
		mApplication = ((Application) context.getApplicationContext());
		mContext = context;
		mDatabaseManager = mApplication.getDatabaseManager();

		mApplication.addEntity(this);
	}

	////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS

	public Long getId() {
		return mId;
	}

	/**
	 * Gets the context associated with the object.
	 * @return Context the context associated with the object.
	 */
	public Context getContext() {
		return mContext;
	}

	/**
	 * Gets the name of the database table associated with the object.
	 * @return String the name of the database table associated with the object.
	 */
	public String getTableName() {
		return mTableName;
	}

	// ###  OPERATIONAL METHODS

	/**
	 * Deletes the current object's record from the database. References to this object will be null.
	 */
	public void delete() {
		SQLiteDatabase db = mDatabaseManager.openDB();
		db.delete(mTableName, "Id=?", new String[] { getId().toString() });
		mDatabaseManager.closeDB();

		mApplication.removeEntity(this);
	}

	/**
	 * Saves the current object as a record to the database. Will insert or update the record based on
	 * its current existence. 
	 */
	public void save() {
		SQLiteDatabase db = mDatabaseManager.openDB();
		ContentValues values = new ContentValues();

		for (Field field : ReflectionUtils.getTableFields(this.getClass())) {
			String fieldName = ReflectionUtils.getColumnName(field);
			Class<?> fieldType = field.getType();

			field.setAccessible(true);

			try {
				if (field.get(this) == null)
					continue;

				Object value = field.get(this);

				// Boolean
				if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
					values.put(fieldName, (Boolean) value);
				}
				// Date
				else if (fieldType.equals(java.util.Date.class)) {
					values.put(fieldName, ((java.util.Date) field.get(this)).getTime());
				}
				// Date
				else if (fieldType.equals(java.sql.Date.class)) {
					values.put(fieldName, ((java.sql.Date) field.get(this)).getTime());
				}
				// Double
				else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
					values.put(fieldName, (Double) value);
				}
				// Float
				else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
					values.put(fieldName, (Float) value);
				}
				// Integer
				else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
					values.put(fieldName, (Integer) value);
				}
				// Long
				else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
					values.put(fieldName, (Long) value);
				}
				// String
				else if (fieldType.equals(String.class) || fieldType.equals(char.class)) {
					values.put(fieldName, value.toString());
				}
				else if (!fieldType.isPrimitive() && fieldType.getSuperclass() != null
						&& fieldType.getSuperclass().equals(ActiveRecordBase.class)) {

					long entityId = ((ActiveRecordBase<?>) value).getId();

					values.put(fieldName, entityId);
				}

			}
			catch (IllegalArgumentException e) {
				Log.e(Params.LOGGING_TAG, e.getMessage());
			}
			catch (IllegalAccessException e) {
				Log.e(Params.LOGGING_TAG, e.getMessage());
			}
		}

		if (mId == null) {
			mId = db.insert(mTableName, null, values);
		}
		else {
			db.update(mTableName, values, "Id=" + mId, null);
		}

		mDatabaseManager.closeDB();
	}

	// ###  RELATIONAL METHODS

	/**
	 * Retrieves related entities on a field on the object.
	 * 
	 * @param type the type of this object.
	 * @param through the field on the other object through which this object is related.
	 */
	protected <E> ArrayList<E> getMany(Class<? extends ActiveRecordBase<E>> type, String through) {
		String table = ReflectionUtils.getTableName(type);
		return query(mContext, type, null, StringUtils.format("{0}.{1}={2}", table, through, getId()));
	}

	// ###  QUERY SHORTCUT METHODS

	/**
	 * Load a single record by primary key.
	 * 
	 * @param context the current context.
	 * @param type the type of this object.
	 * @param id the primary key id of the record.
	 * @return <T> T - ActiveRecordBase
	 */
	public static <T> T load(Context context, Class<? extends ActiveRecordBase<?>> type, long id) {
		final String tableName = ReflectionUtils.getTableName(type);
		final String selection = StringUtils.format("{0}.Id = {1}", tableName, id);

		return querySingle(context, type, null, selection);
	}

	/**
	 * Load the first record in a table.
	 * 
	 * @param context the current context.
	 * @param type the type of this object.
	 * @return <T> T - ActiveRecordBase
	 */
	public static <T> T first(Context context, Class<? extends ActiveRecordBase<?>> type) {
		return querySingle(context, type, null);
	}

	/**
	 * Load the last record in a table.
	 * 
	 * @param context the current context.
	 * @param type the type of this object
	 * @return <T> T - ActiveRecordBase
	 */
	public static <T> T last(Context context, Class<? extends ActiveRecordBase<?>> type) {		
		return querySingle(context, type, null, null, "Id DESC");
	}

	// ### STANDARD METHODS

	/**
	 * Delete all records in the table.
	 * 
	 * @param context the current context.
	 * @param type the type of this object.
	 * @return int the number of records affected.
	 */
	public static <T> int delete(Context context, Class<? extends ActiveRecordBase<?>> type) {
		return delete(context, type, "1");
	}

	/**
	 * Delete the record specified by primary key.
	 * 
	 * @param context the current context.
	 * @param type the type of this object.
	 * @param id the primary key to delete.
	 * @return boolean returns true if the record existed and was deleted.
	 */
	public static <T> boolean delete(Context context, Class<? extends ActiveRecordBase<?>> type, long id) {
		return delete(context, type, "Id=" + id) > 0;
	}

	/**
	 * Delete records in the table specified by the where clause.
	 * @param <T>
	 * @param context
	 * @param type
	 * @param where
	 * @return int the number of records affected.
	 */
	public static <T> int delete(Context context, Class<? extends ActiveRecordBase<?>> type, String where) {
		DatabaseManager dbManager = ((Application) context.getApplicationContext()).getDatabaseManager();
		SQLiteDatabase db = dbManager.openDB();
		String table = ReflectionUtils.getTableName(type);

		int count = db.delete(table, where, null);
		dbManager.closeDB();

		return count;
	}

	// find & overloads

	/**
	 * Return an ArrayList of all records for the specified type.
	 * 
	 * @param context the current context.
	 * @param type the type of this object.
	 * @return ArrayList<T> ArrayList of objects returned by the query
	 */
	public static <T> ArrayList<T> query(Context context, Class<? extends ActiveRecordBase<?>> type) {
		return query(context, type, null, null, null, null, null, null);
	}

	/**
	 * Return an ArrayList of all records for the specified type. Includes only the specified columns
	 * 
	 * @param context the current context.
	 * @param type the type of this object.
	 * @param columns the columns to select, or null for all columns.
	 * @return ArrayList<T> ArrayList of objects returned by the query.
	 */
	public static <T> ArrayList<T> query(Context context, Class<? extends ActiveRecordBase<?>> type, String[] columns) {
		return query(context, type, columns, null, null, null, null, null);
	}
	
	/**
	 * Return an ArrayList of all records for the specified type and where clause. Includes only the specified columns
	 * 
	 * @param context the current context.
	 * @param type the type of this object.
	 * @param columns the columns to select, or null for all columns.
	 * @param where where clause applied to the query.
	 * @return ArrayList<T> ArrayList of objects returned by the query.
	 */
	public static <T> ArrayList<T> query(Context context, Class<? extends ActiveRecordBase<?>> type, String[] columns, String where) {
		return query(context, type, columns, where, null, null, null, null);
	}
	
	/**
	 * Return an ArrayList of all records for the specified type, where, and order by clauses. Includes only the specified columns
	 * 
	 * @param context the current context.
	 * @param type the type of this object.
	 * @param columns the columns to select, or null for all columns.
	 * @param where where clause applied to the query, or null for no clause.
	 * @param orderBy order by clause applied to the query, or null for no clause.
	 * @return ArrayList<T> ArrayList of objects returned by the query.
	 */
	public static <T> ArrayList<T> query(Context context, Class<? extends ActiveRecordBase<?>> type, String[] columns, String where, String orderBy) {
		return query(context, type, columns, where, null, null, orderBy, null);
	}
	
	/**
	 * Return an ArrayList of all records for the specified type, where, order by, and limit clauses. Includes only the specified columns
	 * 
	 * @param context the current context.
	 * @param type the type of this object.
	 * @param columns the columns to select, or null for all columns.
	 * @param where where clause applied to the query, or null for no clause.
	 * @param orderBy order by clause applied to the query, or null for no clause.
	 * @param limit limit clause applied to the query (including distinct), or null for no clause.
	 * @return ArrayList<T> ArrayList of objects returned by the query.
	 */
	public static <T> ArrayList<T> query(Context context, Class<? extends ActiveRecordBase<?>> type, String[] columns, String where, String orderBy, String limit) {
		return query(context, type, columns, where, null, null, orderBy, limit);
	}
	
	/**
	 * Return an ArrayList of all records for the specified type, where, order by, group by, having, and limit clauses. Includes only the specified columns
	 * 
	 * @param context the current context.
	 * @param type the type of this object.
	 * @param columns the columns to select, or null for all columns.
	 * @param where where clause applied to the query, or null for no clause.
	 * @param groupBy group by clause applied to the query, or null for no clause.
	 * @param having having clause applied to the query, or null for no clause.
	 * @param orderBy order by clause applied to the query, or null for no clause.
	 * @param limit limit clause applied to the query (including distinct), or null for no clause.
	 * @return ArrayList<T> ArrayList of objects returned by the query.
	 */
	public static <T> ArrayList<T> query(Context context, Class<? extends ActiveRecordBase<?>> type, String[] columns, String where, String groupBy, String having, String orderBy, String limit) {
		// Open database
		final DatabaseManager dbManager = ((Application) context.getApplicationContext()).getDatabaseManager();
		final SQLiteDatabase db = dbManager.openDB();
		final String table = ReflectionUtils.getTableName(type);

		// Get cursor from query (selectionArgs is always null)
		Cursor cursor = db.query(table, columns, where, null, groupBy, having, orderBy, limit);

		// Convert cursor response into list of entities
		ArrayList<T> entities = processCursor(context, type, cursor);

		// Clean up
		cursor.close();
		dbManager.closeDB();

		return entities;
	}

	/**
	 * Return a single object for the specified type. Includes only the specified columns
	 * 
	 * @param context the current context.
	 * @param type the type of this object.
	 * @param columns the columns to select, or null for all columns.
	 * @return T object returned by the query.
	 */
	public static <T> T querySingle(Context context, Class<? extends ActiveRecordBase<?>> type, String[] columns) {
		return (T) getFirst(query(context, type, columns, null, null, "1"));
	}
	
	/**
	 * Return a single object for the specified type, and where clauses. Includes only the specified columns
	 * 
	 * @param context the current context.
	 * @param type the type of this object.
	 * @param columns the columns to select, or null for all columns.
	 * @param where where clause applied to the query, or null for no clause.
	 * @return T object returned by the query.
	 */
	public static <T> T querySingle(Context context, Class<? extends ActiveRecordBase<?>> type, String[] columns, String where) {
		return (T) getFirst(query(context, type, columns, where, null, "1"));
	}
	
	/**
	 * Return a single object for the specified type, where, and order by clauses. Includes only the specified columns
	 * 
	 * @param context the current context.
	 * @param type the type of this object.
	 * @param columns the columns to select, or null for all columns.
	 * @param where where clause applied to the query, or null for no clause.
	 * @param orderBy order by clause applied to the query, or null for no clause.
	 * @return T object returned by the query.
	 */
	public static <T> T querySingle(Context context, Class<? extends ActiveRecordBase<?>> type, String[] columns, String where, String orderBy) {
		return (T) getFirst(query(context, type, columns, where, orderBy, "1"));
	}

	/**
	 * Return a single object for the specified type, where, order by, group by, and having clauses. Includes only the specified columns
	 * 
	 * @param context the current context.
	 * @param type the type of this object.
	 * @param columns the columns to select, or null for all columns.
	 * @param where where clause applied to the query, or null for no clause.
	 * @param groupBy group by clause applied to the query, or null for no clause.
	 * @param having having clause applied to the query, or null for no clause.
	 * @param orderBy order by clause applied to the query, or null for no clause.
	 * @return T object returned by the query.
	 */
	public static <T> T querySingle(Context context, Class<? extends ActiveRecordBase<?>> type, String[] columns, String selection, String groupBy, String having, String orderBy) {
		return (T) getFirst(query(context, type, columns, selection, groupBy, having, orderBy, "1"));
	}

	// raw sql query

	/**
	 * Return an ArrayList of all records for the specified SQL query
	 * 
	 * @param context the current context.
	 * @param type the type of this object.
	 * @param sql the SQL query string.
	 */
	public static final <T> ArrayList<T> rawQuery(Context context, Class<? extends ActiveRecordBase<?>> type, String sql) {
		DatabaseManager dbManager = ((Application) context.getApplicationContext()).getDatabaseManager();
		SQLiteDatabase db = dbManager.openDB();
		Cursor cursor = db.rawQuery(sql, null);

		ArrayList<T> entities = processCursor(context, type, cursor);

		cursor.close();
		dbManager.closeDB();

		return entities;
	}

	/**
	 * Return a single object for the specified SQL query
	 * 
	 * @param context the current context.
	 * @param type the type of this object.
	 * @param sql the SQL query string.
	 */
	public static final <T> T rawQuerySingle(Context context, Class<? extends ActiveRecordBase<?>> type, String sql) {
		return (T) getFirst(rawQuery(context, type, sql));
	}

	////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS

	private static <T> T getFirst(ArrayList<T> entities) {
		if (entities.size() > 0) {
			return entities.get(0);
		}

		return null;
	}

	private static final <T> ArrayList<T> processCursor(Context context, Class<? extends ActiveRecordBase<?>> type,
			Cursor cursor) {
		ArrayList<T> entities = new ArrayList<T>();

		try {

			Constructor<?> entityConstructor = type.getConstructor(Context.class);

			if (cursor.moveToFirst()) {
				do {
					T entity = (T) entityConstructor.newInstance(context);
					((ActiveRecordBase<T>) entity).loadFromCursor(context, type, cursor);
					entities.add(entity);
				}
				while (cursor.moveToNext());
			}

		}
		catch (IllegalArgumentException e) {
			Log.e(Params.LOGGING_TAG, e.getMessage());
		}
		catch (InstantiationException e) {
			Log.e(Params.LOGGING_TAG, e.getMessage());
		}
		catch (IllegalAccessException e) {
			Log.e(Params.LOGGING_TAG, e.getMessage());
		}
		catch (InvocationTargetException e) {
			Log.e(Params.LOGGING_TAG, e.getMessage());
		}
		catch (SecurityException e) {
			Log.e(Params.LOGGING_TAG, e.getMessage());
		}
		catch (NoSuchMethodException e) {
			Log.e(Params.LOGGING_TAG, e.getMessage());
		}

		return entities;
	}

	private final void loadFromCursor(Context context, Class<? extends ActiveRecordBase<?>> type, Cursor cursor) {
		final ArrayList<Field> fields = ReflectionUtils.getTableFields(type);

		for (Field field : fields) {
			final String fieldName = ReflectionUtils.getColumnName(field);
			final Class<?> fieldType = field.getType();
			final int columnIndex = cursor.getColumnIndex(fieldName);

			if (columnIndex < 0) {
				continue;
			}

			field.setAccessible(true);

			try {

				if(cursor.isNull(columnIndex)) {
					field = null;
				}
				else if (!fieldType.isPrimitive() && fieldType.getSuperclass() != null
						&& fieldType.getSuperclass().equals(ActiveRecordBase.class)) {

					long entityId = cursor.getLong(columnIndex);
					Class<? extends ActiveRecordBase<?>> entityType = (Class<? extends ActiveRecordBase<?>>) fieldType;

					Application application = ((Application) context.getApplicationContext());
					ActiveRecordBase<?> entity = application.getEntity(entityType, entityId);

					if (entity == null) {
						entity = ActiveRecordBase.load(context, entityType, entityId);
					}

					field.set(this, entity);
				}
				else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
					field.set(this, cursor.getInt(columnIndex) != 0);
				}
				else if (fieldType.equals(char.class)) {
					field.set(this, cursor.getString(columnIndex).charAt(0));
				}
				else if (fieldType.equals(java.util.Date.class)) {
					field.set(this, new java.util.Date(cursor.getLong(columnIndex)));
				}
				else if (fieldType.equals(java.sql.Date.class)) {
					field.set(this, new java.sql.Date(cursor.getLong(columnIndex)));
				}
				else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
					field.set(this, cursor.getDouble(columnIndex));
				}
				else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
					field.set(this, cursor.getFloat(columnIndex));
				}
				else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
					field.set(this, cursor.getInt(columnIndex));
				}
				else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
					field.set(this, cursor.getLong(columnIndex));
				}
				else if (fieldType.equals(String.class)) {
					field.set(this, cursor.getString(columnIndex));
				}

			}
			catch (IllegalArgumentException e) {
				Log.e(Params.LOGGING_TAG, e.getMessage());
			}
			catch (IllegalAccessException e) {
				Log.e(Params.LOGGING_TAG, e.getMessage());
			}
			catch (SecurityException e) {
				Log.e(Params.LOGGING_TAG, e.getMessage());
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES

	@Override
	public boolean equals(Object obj) {
		ActiveRecordBase<?> other = (ActiveRecordBase<?>) obj;

		return (this.mTableName == other.mTableName) && (this.mId == other.mId);
	}
}
