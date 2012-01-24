package com.activeandroid;

public final class QueryUtils {
	public static String getTableName(Class<?> type) {
		return ReflectionUtils.getTableName(type);
	}
}