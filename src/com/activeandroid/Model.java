package com.activeandroid;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.activeandroid.annotation.Column;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.serializer.TypeSerializer;
import com.activeandroid.util.Log;
import com.activeandroid.util.ReflectionUtils;

@SuppressWarnings("unchecked")
public abstract class Model {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	@Column(name = "Id")
	private Long mId = null;
	private String mTableName;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public Model() {
		mTableName = ReflectionUtils.getTableName(getClass());
		Cache.addEntity(this);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the current object's record Id.
	 * @return Long the current object's record Id.
	 */
	public Long getId() {
		return mId;
	}

	/**
	 * Convenience method to delete a record by primary key Id.
	 * @param type the type of this object.
	 * @param id the primary key Id of the record to be deleted.
	 */
	public static void delete(Class<? extends Model> type, long id) {
		new Delete().from(type).where("Id=?", id).execute();
	}

	/**
	 * Convenience method to load a record by primary key Id.
	 * 
	 * @param type the type of this object.
	 * @param id the primary key id of the record to be loaded.
	 * @return <T> object returned by the query.
	 */
	public static <T extends Model> T load(Class<? extends Model> type, long id) {
		return new Select().from(type).where("Id=?", id).executeSingle();
	}

	/**
	 * Delete the object's record from the database.
	 */
	public void delete() {
		Cache.openDatabase().delete(mTableName, "Id=?", new String[] { getId().toString() });
		Cache.removeEntity(this);
	}

	/**
	 * Saves the object as a record to the database. Will insert or update the record based on
	 * its current existence. 
	 */
	public void save() {
		final SQLiteDatabase db = Cache.openDatabase();
		final ContentValues values = new ContentValues();

		for (Field field : ReflectionUtils.getColumnFields(this.getClass())) {
			final String fieldName = ReflectionUtils.getColumnName(field);
			Class<?> fieldType = field.getType();

			field.setAccessible(true);

			try {
				Object value = field.get(this);

				if (value != null) {
					final TypeSerializer typeSerializer = Cache.getParserForType(fieldType);
					if (typeSerializer != null) {
						// serialize data
						value = typeSerializer.serialize(value);
						// set new object type
						if (value != null) {
							fieldType = value.getClass();
						}
					}
				}

				if (value == null) {
					values.putNull(fieldName);
				}
				else if (fieldType.equals(String.class)) {
					values.put(fieldName, value.toString());
				}
				else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
					values.put(fieldName, (Boolean) value);
				}
				else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
					values.put(fieldName, (Long) value);
				}
				else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
					values.put(fieldName, (Integer) value);
				}
				else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
					values.put(fieldName, (Float) value);
				}
				else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
					values.put(fieldName, (Double) value);
				}
				else if (fieldType.equals(Character.class) || fieldType.equals(char.class)) {
					values.put(fieldName, value.toString());
				}
				else if (ReflectionUtils.isModelSubclass(fieldType)) {
					final long entityId = ((Model) value).getId();
					values.put(fieldName, entityId);
				}
			}
			catch (IllegalArgumentException e) {
				Log.e(e.getClass().getName(), e);
			}
			catch (IllegalAccessException e) {
				Log.e(e.getClass().getName(), e);
			}
		}

		if (mId == null) {
			mId = db.insert(mTableName, null, values);
		}
		else {
			db.update(mTableName, values, "Id=" + mId, null);
		}
	}

	/**
	 * Return an ArrayList of all records for the specified SQL query
	 * 
	 * @param context the current context.
	 * @param type the type of this object.
	 * @param sql the SQL query string.
	 * @return ArrayList<T> ArrayList of objects returned by the query.
	 */
	public static final <T extends Model> ArrayList<T> rawQuery(Class<? extends Model> type, String sql,
			String[] selectionArgs) {

		final Cursor cursor = Cache.openDatabase().rawQuery(sql, selectionArgs);
		final ArrayList<T> entities = processCursor(type, cursor);
		cursor.close();

		return entities;
	}

	/**
	 * Return a single object for the specified SQL query
	 * 
	 * @param context the current context.
	 * @param type the type of this object.
	 * @param sql the SQL query string.
	 * @return <T> object returned by the query.
	 */
	public static final <T extends Model> T rawQuerySingle(Class<? extends Model> type, String sql,
			String[] selectionArgs) {

		return (T) getFirst(rawQuery(type, sql, selectionArgs));
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Retrieves related entities on a field on the object.
	 * 
	 * @param type the type of this object.
	 * @param foreignKey the field on the other object through which this object is related.
	 * @return ArrayList<E> ArrayList of objects returned by the query.
	 */
	protected final <E extends Model> ArrayList<E> getMany(Class<? extends Model> type, String foreignKey) {
		final String tableName = ReflectionUtils.getTableName(type);
		return new Select().from(type).where(tableName + "." + foreignKey + "=?", getId()).execute();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private static <T extends Model> T getFirst(ArrayList<T> entities) {
		if (entities.size() > 0) {
			return entities.get(0);
		}

		return null;
	}

	private static final <T extends Model> ArrayList<T> processCursor(Class<? extends Model> type, Cursor cursor) {
		final ArrayList<T> entities = new ArrayList<T>();

		try {
			Constructor<?> entityConstructor = type.getConstructor();

			if (cursor.moveToFirst()) {
				do {
					T entity = (T) entityConstructor.newInstance();
					((Model) entity).loadFromCursor(type, cursor);
					entities.add(entity);
				}
				while (cursor.moveToNext());
			}

		}
		catch (IllegalArgumentException e) {
			Log.e(e.getMessage());
		}
		catch (InstantiationException e) {
			Log.e(e.getMessage());
		}
		catch (IllegalAccessException e) {
			Log.e(e.getMessage());
		}
		catch (InvocationTargetException e) {
			Log.e(e.getMessage());
		}
		catch (SecurityException e) {
			Log.e(e.getMessage());
		}
		catch (NoSuchMethodException e) {
			Log.e("Missing required constructor: " + e.getMessage());
		}

		return entities;
	}

	private final void loadFromCursor(Class<? extends Model> type, Cursor cursor) {
		final ArrayList<Field> fields = ReflectionUtils.getColumnFields(type);

		for (Field field : fields) {
			final String fieldName = ReflectionUtils.getColumnName(field);
			Class<?> fieldType = field.getType();
			final int columnIndex = cursor.getColumnIndex(fieldName);

			if (columnIndex < 0) {
				continue;
			}

			field.setAccessible(true);

			try {
				boolean columnIsNull = cursor.isNull(columnIndex);
				TypeSerializer typeSerializer = Cache.getParserForType(fieldType);
				Object value = null;

				if (typeSerializer != null) {
					fieldType = typeSerializer.getDeserializedType();
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
				else if (ReflectionUtils.isModelSubclass(fieldType)) {
					long entityId = cursor.getLong(columnIndex);
					Class<? extends Model> entityType = (Class<? extends Model>) fieldType;

					Model entity = Cache.getEntity(entityType, entityId);

					if (entity == null) {
						entity = new Select().from(entityType).where("Id=?", entityId).executeSingle();
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
				Log.e(e.getMessage());
			}
			catch (IllegalAccessException e) {
				Log.e(e.getMessage());
			}
			catch (SecurityException e) {
				Log.e(e.getMessage());
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDEN METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean equals(Object obj) {
		final Model other = (Model) obj;

		return this.mId != null && (this.mTableName == other.mTableName) && (this.mId == other.mId);
	}
}