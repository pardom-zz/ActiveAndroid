package com.activeandroid.serializer;

import java.util.HashMap;
import java.util.Map;

public abstract class TypeSerializer {
	final public static Map<SerializedType, Class<?>> TYPE_MAPPING = new HashMap<SerializedType, Class<?>>() {
		private static final long serialVersionUID = 2372163661642835762L;
		{
			put(SerializedType.BOOLEAN, Boolean.class);
			put(SerializedType.CHARACTER, Character.class);
			put(SerializedType.DOUBLE, Double.class);
			put(SerializedType.FLOAT, Float.class);
			put(SerializedType.INTEGER, Integer.class);
			put(SerializedType.LONG, Long.class);
			put(SerializedType.SHORT, Short.class);
			put(SerializedType.STRING, String.class);
		}
	};

	public enum SerializedType {
		BOOLEAN, CHARACTER, DOUBLE, FLOAT, INTEGER, LONG, SHORT, STRING
	}

	public abstract Class<?> getDeserializedType();

	public abstract SerializedType getSerializedType();

	public abstract Object serialize(Object data);

	public abstract Object deserialize(Object data);
}