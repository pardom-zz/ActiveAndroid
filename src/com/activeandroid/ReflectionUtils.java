package com.activeandroid;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.serializer.TypeSerializer;

import dalvik.system.DexFile;

final class ReflectionUtils {

	// ###############################
	// ### Table/Column names

	public static String getTableName(Class<?> type) {
		final String cachedValue = Registry.getInstance().getTableName(type);
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

		Registry.getInstance().addTableName(type, tableName);

		return tableName;
	}

	public static String getColumnName(Field field) {
		final String cachedValue = Registry.getInstance().getColumnName(field);
		if (cachedValue != null) {
			return cachedValue;
		}

		String columnName = null;

		final Column annotation = field.getAnnotation(Column.class);
		if (annotation != null) {
			columnName = annotation.name();
		}

		Registry.getInstance().addColumnName(field, columnName);

		return columnName;
	}

	// ############################### 
	// ### Dex reflection

	@SuppressWarnings("unchecked")
	public static ArrayList<Class<? extends Model>> getModelClasses() {
		final Context context = Registry.getInstance().getContext();
		final ArrayList<Class<? extends Model>> modelClasses = new ArrayList<Class<? extends Model>>();

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

					if (discoveredClass != null && superClass != null) {
						if (superClass.equals(Model.class)) {
							modelClasses.add((Class<? extends Model>) discoveredClass);
						}
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

	public static Integer getMetaDataInteger(String name) {
		final Context context = Registry.getInstance().getContext();
		Integer value = null;

		try {
			final PackageManager pm = context.getPackageManager();
			final ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			value = ai.metaData.getInt(name);

			Log.v(name + ": " + value);
		}
		catch (Exception e) {
			Log.w("Couldn't find meta data string: " + name);
		}

		return value;
	}

	public static String getMetaDataString(String name) {
		final Context context = Registry.getInstance().getContext();
		String value = null;

		try {
			final PackageManager pm = context.getPackageManager();
			final ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			value = ai.metaData.getString(name);

			Log.v(name + ": " + value);
		}
		catch (Exception e) {
			Log.w("Couldn't find meta data string: " + name);
		}

		return value;
	}

	public static HashMap<Class<?>, TypeSerializer> getParsers() {
		final Context context = Registry.getInstance().getContext();
		HashMap<Class<?>, TypeSerializer> parsers = new HashMap<Class<?>, TypeSerializer>();

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

	public static ArrayList<Field> getTableFields(Class<?> type) {
		final ArrayList<Field> cachedValue = Registry.getInstance().getClassFields(type);
		if (cachedValue != null) {
			return cachedValue;
		}

		final ArrayList<Field> typeFields = new ArrayList<Field>();

		try {
			typeFields.add(type.getSuperclass().getDeclaredField("mId"));
		}
		catch (SecurityException e) {
			Log.e("SecurityException: " + e.getMessage());
		}
		catch (NoSuchFieldException e) {
			Log.e("NoSuchFieldException: " + e.getMessage());
		}

		Field[] fields = type.getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(Column.class)) {
				typeFields.add(field);
			}
		}

		Registry.getInstance().addClassFields(type, typeFields);

		return typeFields;
	}

	// ###############################
	// ### Data types

	public static boolean typeIsSQLiteReal(Class<?> type) {
		return type.equals(Double.class) || type.equals(double.class) || type.equals(Float.class)
				|| type.equals(float.class);
	}

	public static boolean typeIsSQLiteInteger(Class<?> type) {
		return type.equals(Boolean.class) || type.equals(boolean.class) || type.equals(Integer.class)
				|| type.equals(int.class) || type.equals(Long.class) || type.equals(long.class)
				|| type.equals(Short.class) || type.equals(short.class)
				|| (!type.isPrimitive() && type.getSuperclass() != null && type.getSuperclass().equals(Model.class));
	}

	public static boolean typeIsSQLiteText(Class<?> type) {
		return type.equals(String.class) || type.equals(Character.class) || type.equals(char.class);
	}
}