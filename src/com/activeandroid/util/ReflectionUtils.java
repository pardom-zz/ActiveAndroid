package com.activeandroid.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.serializer.TypeSerializer;

import dalvik.system.DexFile;

public final class ReflectionUtils {
	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public static boolean isModelSubclass(Class<?> type) {
		if (type.isPrimitive()) {
			return false;
		}
		else if (type.equals(Model.class)) {
			return true;
		}
		else if (type.getSuperclass() != null) {
			return isModelSubclass(type.getSuperclass());
		}

		return false;
	}

	// Names

	public static String getTableName(Class<?> type) {
		final String cachedValue = Cache.getTableName(type);
		if (cachedValue != null) {
			return cachedValue;
		}

		String tableName = null;
		final Table annotation = type.getAnnotation(Table.class);

		if (annotation != null) {
			tableName = annotation.name();
		}
		else {
			tableName = type.getSimpleName();
		}

		Cache.addTableName(type, tableName);

		return tableName;
	}

	public static String getColumnName(Field field) {
		final String cachedValue = Cache.getColumnName(field);
		if (cachedValue != null) {
			return cachedValue;
		}

		String columnName = null;

		final Column annotation = field.getAnnotation(Column.class);
		if (annotation != null) {
			columnName = annotation.name();
		}

		Cache.addColumnName(field, columnName);

		return columnName;
	}

	// ActiveAndroid subclasses

	@SuppressWarnings("unchecked")
	public static ArrayList<Class<? extends Model>> getModelClasses(Context context) {
		final ArrayList<Class<? extends Model>> modelClasses = new ArrayList<Class<? extends Model>>();

		try {
			final String path = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).sourceDir;
			final DexFile dexfile = new DexFile(path);
			final Enumeration<String> entries = dexfile.entries();

			while (entries.hasMoreElements()) {
				final String name = entries.nextElement();

				if (name.contains("com.activeandroid")) {
					continue;
				}

				try {
					Class<?> discoveredClass = Class.forName(name, false, context.getClass().getClassLoader());
					if (discoveredClass != null && ReflectionUtils.isModelSubclass(discoveredClass)) {
						modelClasses.add((Class<? extends Model>) discoveredClass);
					}
				}
				catch (ClassNotFoundException e) {
					Log.e("ClassNotFoundException: " + e.getMessage());
				}
			}
		}
		catch (IOException e) {
			Log.e("IOException: " + e.getMessage());
		}
		catch (NameNotFoundException e) {
			Log.e("NameNotFoundException: " + e.getMessage());
		}

		return modelClasses;
	}

	public static HashMap<Class<?>, TypeSerializer> getParsers(Context context) {
		HashMap<Class<?>, TypeSerializer> parsers = new HashMap<Class<?>, TypeSerializer>();

		try {
			final String path = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).sourceDir;
			final DexFile dexfile = new DexFile(path);
			final Enumeration<String> entries = dexfile.entries();

			while (entries.hasMoreElements()) {
				final String name = entries.nextElement();

				if (name.contains("com.activeandroid") && !name.contains("serializer")) {
					continue;
				}

				Class<?> discoveredClass = null;
				Class<?> superClass = null;

				try {
					discoveredClass = Class.forName(name, false, context.getClass().getClassLoader());
					superClass = discoveredClass.getSuperclass();

					if (discoveredClass != null && superClass != null) {
						if (superClass.equals(TypeSerializer.class)) {
							TypeSerializer instance = (TypeSerializer) discoveredClass.newInstance();
							Class<?> cls = instance.getDeserializedType();

							parsers.put(cls, instance);
						}
					}
				}
				catch (ClassNotFoundException e) {
					Log.e("ClassNotFoundException: " + e.getMessage());
				}
				catch (InstantiationException e) {
					Log.e("InstantiationException: " + e.getMessage());
				}
				catch (IllegalAccessException e) {
					Log.e("IllegalAccessException: " + e.getMessage());
				}
			}
		}
		catch (IOException e) {
			Log.e("IOException: " + e.getMessage());
		}
		catch (NameNotFoundException e) {
			Log.e("NameNotFoundException: " + e.getMessage());
		}

		return parsers;
	}

	public static ArrayList<Field> getColumnFields(Class<?> type) {
		final ArrayList<Field> cachedValue = Cache.getClassFields(type);
		if (cachedValue != null) {
			return cachedValue;
		}

		final ArrayList<Field> typeFields = new ArrayList<Field>();
		typeFields.add(getIdField(type));

		Field[] fields = type.getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(Column.class)) {
				typeFields.add(field);
			}
		}

		Cache.addClassFields(type, typeFields);

		return typeFields;
	}

	// Meta-data

	@SuppressWarnings("unchecked")
	public static <T> T getMetaData(Context context, String name) {
		try {
			final ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
					PackageManager.GET_META_DATA);

			if (ai.metaData != null) {
				return (T) ai.metaData.get(name);
			}
		}
		catch (Exception e) {
			Log.w("Couldn't find meta-data: " + name);
		}

		return null;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private static Field getIdField(Class<?> type) {
		if (type.equals(Model.class)) {
			try {
				return type.getDeclaredField("mId");
			}
			catch (NoSuchFieldException e) {
				Log.e("Impossible!", e);
			}
		}
		else if (type.getSuperclass() != null) {
			return getIdField(type.getSuperclass());
		}

		return null;
	}
}