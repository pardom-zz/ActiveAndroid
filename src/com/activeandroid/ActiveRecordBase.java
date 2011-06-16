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
	private String mTableName;

	////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS

	public ActiveRecordBase(Context context) {
		mContext = context.getApplicationContext();

		checkForApplication(mContext);

		mApplication = (Application) mContext;
		mTableName = ReflectionUtils.getTableName(mContext, getClass());

		mApplication.addEntity(this);
	}

	////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS

	/**
	 * Gets the current object's record Id.
	 * @return Long the current object's record Id.
	 */
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
		final SQLiteDatabase db = mApplication.openDatabase();
		db.delete(mTableName, "Id=?", new String[] { getId().toString() });
		//mApplication.closeDatabase();
		mApplication.removeEntity(this);
	}

	/**
	 * Saves the current object as a record to the database. Will insert or update the record based on
	 * its current existence. 
	 */
	public void save() {
		final SQLiteDatabase db = mApplication.openDatabase();
		final ContentValues values = new ContentValues();

		for (Field field : ReflectionUtils.getTableFields(mContext, this.getClass())) {
			final String fieldName = ReflectionUtils.getColumnName(mContext, field);
			Class<?> fieldType = field.getType();

			field.setAccessible(true);

			try {
				Object value = field.get(this);

				if (value != null) {
					final TypeSerializer<?> typeSerializer = mApplication.getParserForType(fieldType);
					if (typeSerializer != null) {
						// serialize data
						value = typeSerializer.serialize(value);
						// set new object type
						if (value != null) {
							fieldType = value.getClass();
						}
					}
				}

				// Try to order by highest use
				if (value == null) {
					values.putNull(fieldName);
				}
				// String
				else if (fieldType.equals(String.class)) {
					values.put(fieldName, value.toString());
				}
				// Boolean
				else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
					values.put(fieldName, (Boolean) value);
				}
				// Long
				else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
					values.put(fieldName, (Long) value);
				}
				// Integer
				else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
					values.put(fieldName, (Integer) value);
				}
				// Float
				else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
					values.put(fieldName, (Float) value);
				}
				// Double
				else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
					values.put(fieldName, (Double) value);
				}
				// Character
				else if (fieldType.equals(Character.class) || fieldType.equals(char.class)) {
					values.put(fieldName, value.toString());
				}
				else if (!fieldType.isPrimitive() && fieldType.getSuperclass() != null
						&& fieldType.getSuperclass().equals(ActiveRecordBase.class)) {

					final long entityId = ((ActiveRecordBase<?>) value).getId();
					values.put(fieldName, entityId);
				}
			}
			catch (IllegalArgumentException e) {
				Log.e(Params.LOGGING_TAG, e.getClass().getName() + ": " + e.getMessage());
			}
			catch (IllegalAccessException e) {
				Log.e(Params.LOGGING_TAG, e.getClass().getName() + ": " + e.getMessage());
			}
		}

		if (mId == null) {
			mId = db.insert(mTableName, null, values);
		}
		else {
			db.update(mTableName, values, "Id=" + mId, null);
		}

		//mApplication.closeDatabase();
	}

	// ###  RELATIONAL METHODS

	/**
	 * Retrieves related entities on a field on the object.
	 * 
	 * @param type the type of this object.
	 * @param through the field on the other object through which this object is related.
	 */
	protected <E> ArrayList<E> getMany(Class<? extends ActiveRecordBase<E>> type, String through) {
		final String tableName = ReflectionUtils.getTableName(mContext, type);
		return query(mContext, type, null, StringUtils.format("{0}.{1}={2}", tableName, through, getId()));
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
		final String tableName = ReflectionUtils.getTableName(context, type);
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
		return delete(context, type, null);
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
		context = context.getApplicationContext();

		checkForApplication(context);

		final Application application = (Application) context;
		final SQLiteDatabase db = application.openDatabase();
		final String table = ReflectionUtils.getTableName(context, type);

		final int count = db.delete(table, where, null);
		//application.closeDatabase();

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
	public static <T> ArrayList<T> query(Context context, Class<? extends ActiveRecordBase<?>> type, String[] columns,
			String where) {
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
	public static <T> ArrayList<T> query(Context context, Class<? extends ActiveRecordBase<?>> type, String[] columns,
			String where, String orderBy) {
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
	public static <T> ArrayList<T> query(Context context, Class<? extends ActiveRecordBase<?>> type, String[] columns,
			String where, String orderBy, String limit) {
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
	public static <T> ArrayList<T> query(Context context, Class<? extends ActiveRecordBase<?>> type, String[] columns,
			String where, String groupBy, String having, String orderBy, String limit) {

		context = context.getApplicationContext();

		checkForApplication(context);

		// Open database
		final Application application = (Application) context.getApplicationContext();
		final SQLiteDatabase db = application.openDatabase();
		final String table = ReflectionUtils.getTableName(context, type);

		// Get cursor from query (selectionArgs is always null)
		final Cursor cursor = db.query(table, columns, where, null, groupBy, having, orderBy, limit);

		// Convert cursor response into list of entities
		final ArrayList<T> entities = processCursor(context, type, cursor);

		// Clean up
		cursor.close();
		//application.closeDatabase();

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
	public static <T> T querySingle(Context context, Class<? extends ActiveRecordBase<?>> type, String[] columns,
			String where) {
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
	public static <T> T querySingle(Context context, Class<? extends ActiveRecordBase<?>> type, String[] columns,
			String where, String orderBy) {
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
	public static <T> T querySingle(Context context, Class<? extends ActiveRecordBase<?>> type, String[] columns,
			String selection, String groupBy, String having, String orderBy) {
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
		context = context.getApplicationContext();

		final Application application = (Application) context;
		final SQLiteDatabase db = application.openDatabase();
		final Cursor cursor = db.rawQuery(sql, null);

		final ArrayList<T> entities = processCursor(context, type, cursor);

		cursor.close();
		//application.closeDatabase();

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

	private static void checkForApplication(Context context) {
		if (!(context instanceof Application)) {
			throw new ClassCastException(
					"Your application must use com.activeandroid.Application or a subclass. Check <application android:name /> in AndroidManifest.xml");
		}
	}

	private static <T> T getFirst(ArrayList<T> entities) {
		if (entities.size() > 0) {
			return entities.get(0);
		}

		return null;
	}

	private static final <T> ArrayList<T> processCursor(Context context, Class<? extends ActiveRecordBase<?>> type,
			Cursor cursor) {

		final ArrayList<T> entities = new ArrayList<T>();

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
			Log.e(Params.LOGGING_TAG, "Missing required constructor: " + e.getMessage());
		}

		return entities;
	}

	private final void loadFromCursor(Context context, Class<? extends ActiveRecordBase<?>> type, Cursor cursor) {
		final ArrayList<Field> fields = ReflectionUtils.getTableFields(context, type);

		for (Field field : fields) {
			final String fieldName = ReflectionUtils.getColumnName(context, field);
			Class<?> fieldType = field.getType();
			final int columnIndex = cursor.getColumnIndex(fieldName);

			if (columnIndex < 0) {
				continue;
			}

			field.setAccessible(true);

			try {
				boolean columnIsNull = cursor.isNull(columnIndex);
				TypeSerializer<?> typeSerializer = mApplication.getParserForType(fieldType);
				Object value = null;

				if (typeSerializer != null) {
					fieldType = TypeSerializer.TYPE_MAPPING.get(typeSerializer.getSqlType());
				}

				if (columnIsNull) {
					field = null;
				}
				else if (fieldType.equals(String.class)) {
					value = cursor.getString(columnIndex);
				}
				else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
					value = cursor.getInt(columnIndex) != 0;
				}
				else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
					value = cursor.getInt(columnIndex);
				}
				else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
					value = cursor.getLong(columnIndex);
				}
				else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
					value = cursor.getFloat(columnIndex);
				}
				else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
					value = cursor.getDouble(columnIndex);
				}
				else if (fieldType.equals(Short.class) || fieldType.equals(short.class)) {
					value = cursor.getInt(columnIndex);
				}
				else if (fieldType.equals(Character.class) || fieldType.equals(char.class)) {
					value = cursor.getString(columnIndex).charAt(0);
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

					value = entity;
				}

				// Use a deserializer if one is available
				if (typeSerializer != null && !columnIsNull) {
					value = typeSerializer.deserialize(value);
				}

				// Set the field value
				if (value != null) {
					field.set(this, value);
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
		final ActiveRecordBase<?> other = (ActiveRecordBase<?>) obj;

		return (this.mTableName == other.mTableName) && (this.mId == other.mId);
	}
}
