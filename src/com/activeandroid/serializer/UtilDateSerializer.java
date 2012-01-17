package com.activeandroid.serializer;

import java.util.Date;

final public class UtilDateSerializer extends TypeSerializer {
	@Override
	public Class<?> getDeserializedType() {
		return Date.class;
	}

	@Override
	public SerializedType getSerializedType() {
		return SerializedType.LONG;
	}

	@Override
	public Long serialize(Object data) {
		if (data == null) {
			return null;
		}

		return ((Date) data).getTime();
	}

	@Override
	public Date deserialize(Object data) {
		if (data == null) {
			return null;
		}

		return new Date((Long) data);
	}
}