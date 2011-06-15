package com.activeandroid;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import dalvik.system.DexFile;

final class ReflectionUtils {
	public static Integer getColumnLength(Field field) {
		Integer retVal = null;

		final Column annotation = field.getAnnotation(Column.class);
		if (annotation != null) {
			final int length = annotation.length();
			if (length > -1) {
				retVal = length;
			}
		}

		return retVal;
	}

	public static String getColumnName(Field field) {
		final Column annotation = field.getAnnotation(Column.class);
		if (annotation != null) {
			return annotation.name();
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Class<? extends ActiveRecordBase<?>>> getEntityClasses(Context context) {
		final ArrayList<Class<? extends ActiveRecordBase<?>>> entityClasses = new ArrayList<Class<? extends ActiveRecordBase<?>>>();

		try {
			final String path = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).sourceDir;
			final DexFile dexfile = new DexFile(path);
			final Enumeration<String> entries = dexfile.entries();

			while (entries.hasMoreElements()) {
				final String name = entries.nextElement();
				Class<?> discoveredClass = null;
				Class<?> superClass = null;

				try {
					discoveredClass = Class.forName(name, false, context.getClass().getClassLoader());
					superClass = discoveredClass.getSuperclass();
				}
				catch (ClassNotFoundException e) {
					Log.e(Params.LOGGING_TAG, e.getMessage());
				}

				if (discoveredClass != null && superClass != null) {
					if (superClass.equals(ActiveRecordBase.class)) {
						entityClasses.add((Class<? extends ActiveRecordBase<?>>) discoveredClass);
					}
				}
			}

		}
		catch (IOException e) {
			Log.e(Params.LOGGING_TAG, e.getMessage());
		}
		catch (NameNotFoundException e) {
			Log.e(Params.LOGGING_TAG, e.getMessage());
		}

		return entityClasses;
	}

	public static String getMetaDataString(Context context, String name) {
		String value = null;

		try {
			final PackageManager pm = context.getPackageManager();
			final ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			value = ai.metaData.getString(name);

			if (Params.LOGGING_ENABLED) {
				Log.v(Params.LOGGING_TAG, name + ": " + value);
			}
		}
		catch (Exception e) {
			Log.w(Params.LOGGING_TAG, "Couldn't find meta data string: " + name);
		}

		return value;
	}

	public static Integer getMetaDataInteger(Context context, String name) {
		Integer value = null;

		try {
			final PackageManager pm = context.getPackageManager();
			final ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			value = ai.metaData.getInt(name);

			if (Params.LOGGING_ENABLED) {
				Log.v(Params.LOGGING_TAG, name + ": " + value);
			}
		}
		catch (Exception e) {
			Log.w(Params.LOGGING_TAG, "Couldn't find meta data string: " + name);
		}

		return value;
	}

	public static Map<Class<?>, TypeSerializer<?>> getParsers(Context context) {
		Map<Class<?>, TypeSerializer<?>> parsers = new HashMap<Class<?>, TypeSerializer<?>>();

		try {
			final String path = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).sourceDir;
			final DexFile dexfile = new DexFile(path);
			final Enumeration<String> entries = dexfile.entries();

			while (entries.hasMoreElements()) {
				final String name = entries.nextElement();
				Class<?> discoveredClass = null;
				Class<?> superClass = null;

				try {
					discoveredClass = Class.forName(name, false, context.getClass().getClassLoader());
					superClass = discoveredClass.getSuperclass();
				}
				catch (ClassNotFoundException e) {
					Log.e(Params.LOGGING_TAG, e.getMessage());
				}

				if (discoveredClass != null && superClass != null) {
					if (superClass.equals(TypeSerializer.class)) {
						TypeSerializer<?> instance = (TypeSerializer<?>) discoveredClass.newInstance();
						Class<?> cls = instance.getType();

						parsers.put(cls, instance);
					}
				}
			}

		}
		catch (IOException e) {
			Log.e(Params.LOGGING_TAG, e.getMessage());
		}
		catch (NameNotFoundException e) {
			Log.e(Params.LOGGING_TAG, e.getMessage());
		}
		catch (InstantiationException e) {
			Log.e(Params.LOGGING_TAG, e.getMessage());
		}
		catch (IllegalAccessException e) {
			Log.e(Params.LOGGING_TAG, e.getMessage());
		}

		return parsers;
	}

	public static ArrayList<Field> getTableFields(Context context, Class<?> type) {
		final Application app = (Application) context.getApplicationContext();
		final ArrayList<Field> cachedValue = app.getClassFields(type);
		if(cachedValue != null) {
			return cachedValue;
		}
		
		final ArrayList<Field> typeFields = new ArrayList<Field>();

		try {
			typeFields.add(type.getSuperclass().getDeclaredField("mId"));
		}
		catch (SecurityException e) {
			Log.e(Params.LOGGING_TAG, e.getMessage());
		}
		catch (NoSuchFieldException e) {
			Log.e(Params.LOGGING_TAG, e.getMessage());
		}

		Field[] fields = type.getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(Column.class)) {
				typeFields.add(field);
			}
		}
		
		app.addClassFields(type, typeFields);

		return typeFields;
	}

	public static String getTableName(Context context, Class<?> type) {
		final Application app = (Application) context.getApplicationContext();
		final String cachedValue = app.getTableName(type);
		if(cachedValue != null) {
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
		
		app.addTableName(type, tableName);

		return tableName;
	}

	public static boolean typeIsSQLiteReal(Class<?> type) {
		return type.equals(Double.class) || type.equals(double.class) || type.equals(Float.class)
				|| type.equals(float.class);
	}

	public static boolean typeIsSQLiteInteger(Class<?> type) {
		return type.equals(Boolean.class)
				|| type.equals(boolean.class)
				|| type.equals(Integer.class)
				|| type.equals(int.class)
				|| type.equals(Long.class)
				|| type.equals(long.class)
				|| type.equals(Short.class)
				|| type.equals(short.class)
				|| (!type.isPrimitive() && type.getSuperclass() != null && type.getSuperclass().equals(
						ActiveRecordBase.class));
	}

	public static boolean typeIsSQLiteString(Class<?> type) {
		return type.equals(String.class) || type.equals(Character.class) || type.equals(char.class);
	}
}
