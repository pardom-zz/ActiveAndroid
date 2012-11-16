package com.activeandroid.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.activeandroid.Model;

public final class ReflectionUtils {
	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public static boolean isModel(Class<?> type) {
		if (type.isPrimitive()) {
			return false;
		}
		else if (type.equals(Model.class)) {
			return true;
		}
		else if (type.getSuperclass() != null) {
			return isModel(type.getSuperclass());
		}

		return false;
	}

	public static boolean isTypeSerializer(Class<?> type) {
		if (type.isPrimitive()) {
			return false;
		}
		else if (type.equals(Model.class)) {
			return true;
		}
		else if (type.getSuperclass() != null) {
			return isModel(type.getSuperclass());
		}

		return false;
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

	public static boolean isSubclassOf(Class<?> type, Class<?> superClass) {
		if (type.isPrimitive()) {
			return false;
		}
		else if (type.equals(superClass)) {
			return true;
		}
		else if (type.getSuperclass() != null) {
			return isSubclassOf(type.getSuperclass(), superClass);
		}

		return false;
	}
}