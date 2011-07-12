package com.activeandroid;

import java.util.Collection;
import java.util.Iterator;

final class StringUtils {
	public static String format(String format, Object... args) {
		final int len = args.length;
		for (int i = len - 1; i >= 0; i--) {
			format = format.replace("{" + i + "}", args[i] != null ? args[i].toString() : "null");
		}

		return format;
	}

	public static String join(Collection<String> value, String separator) {
		if (value == null || value.isEmpty()) {
			return "";
		}

		Iterator<String> iterator = value.iterator();
		StringBuilder builder = new StringBuilder(iterator.next());
		while (iterator.hasNext()) {
			builder.append(separator).append(iterator.next());
		}

		return builder.toString();
	}

	public static String join(String[] value, String separator) {
		final int len = value.length;
		if (value == null || len < 1) {
			return "";
		}

		String retVal = value[0];
		if (len > 1) {
			for (int i = 1; i < len; i++) {
				retVal += separator + value[i];
			}
		}

		return retVal;
	}
}
