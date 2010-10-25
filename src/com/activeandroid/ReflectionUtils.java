package com.activeandroid;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

final class ReflectionUtils {
	public static Integer getColumnLength(Field field) {
		Integer retVal = null;

		Column annotation = field.getAnnotation(Column.class);
		if (annotation != null) {
			final int length = annotation.length();
			if (length > -1) {
				retVal = length;
			}
		}

		return retVal;
	}

	public static String getColumnName(Field field) {
		Column annotation = field.getAnnotation(Column.class);
		if (annotation != null) {
			return annotation.name();
		}

		return null;
	}

	public static ArrayList<Field> getTableFields(Class<?> type) {
		ArrayList<Field> typeFields = new ArrayList<Field>();

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

		return typeFields;
	}

	public static String getTableName(Class<?> type) {
		String tableName = null;
		Table annotation = type.getAnnotation(Table.class);

		if (annotation != null) {
			tableName = annotation.name();
		}
		else {
			tableName = type.getSimpleName();
		}

		return tableName;
	}

	public static boolean typeIsSQLiteFloat(Class<?> type) {
		return type.equals(Double.class) || type.equals(double.class) || type.equals(Float.class)
			|| type.equals(float.class);
	}

	public static boolean typeIsSQLiteInteger(Class<?> type) {
		return type.equals(Boolean.class)
			|| type.equals(boolean.class)
			|| type.equals(java.util.Date.class)
			|| type.equals(java.sql.Date.class)
			|| type.equals(Integer.class)
			|| type.equals(int.class)
			|| type.equals(Long.class)
			|| type.equals(long.class)
			|| (!type.isPrimitive() 
				&& type.getSuperclass() != null 
				&& type.getSuperclass().equals(ActiveRecordBase.class));
	}

	public static boolean typeIsSQLiteString(Class<?> type) {
		return type.equals(String.class) 
			|| type.equals(char.class);
	}
}
