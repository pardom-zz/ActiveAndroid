package com.activeandroid;

import java.util.HashMap;
import java.util.Map;

public abstract class TypeSerializer<T> {
	final static Map<SqlType, Class<?>> TYPE_MAPPING = new HashMap<SqlType, Class<?>>() {
		private static final long serialVersionUID = 2372163661642835762L;
		{
			put(SqlType.DOUBLE, Double.class);
			put(SqlType.FLOAT, Float.class);
			put(SqlType.INT, Integer.class);
			put(SqlType.LONG, Long.class);
			put(SqlType.SHORT, Short.class);
			put(SqlType.STRING, String.class);
		}
	};

	public enum SqlType {
		DOUBLE, FLOAT, INT, LONG, SHORT, STRING
	}

	public abstract Class<?> getType();

	public abstract SqlType getSqlType();

	public abstract Object serialize(Object data);

	public abstract T deserialize(Object data);
}
