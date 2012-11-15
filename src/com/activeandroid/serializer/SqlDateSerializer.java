package com.activeandroid.serializer;

import java.sql.Date;

import com.activeandroid.util.SQLiteUtils.SQLiteType;

final public class SqlDateSerializer implements TypeSerializer {
	public Class<?> getDeserializedType() {
		return Date.class;
	}

	public SQLiteType getSerializedType() {
		return SQLiteType.INTEGER;
	}

	public Long serialize(Object data) {
		if (data == null) {
			return null;
		}

		return ((Date) data).getTime();
	}

	public Date deserialize(Object data) {
		if (data == null) {
			return null;
		}

		return new Date((Long) data);
	}
}