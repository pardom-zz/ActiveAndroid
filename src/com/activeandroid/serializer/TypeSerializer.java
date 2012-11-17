package com.activeandroid.serializer;

import com.activeandroid.util.SQLiteUtils.SQLiteType;

public abstract class TypeSerializer {
	public abstract Class<?> getDeserializedType();

	public abstract SQLiteType getSerializedType();

	public abstract Object serialize(Object data);

	public abstract Object deserialize(Object data);
}