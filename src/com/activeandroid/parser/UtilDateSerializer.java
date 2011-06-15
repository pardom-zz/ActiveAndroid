package com.activeandroid.parser;

import java.util.Date;

import com.activeandroid.TypeSerializer;

public class UtilDateSerializer extends TypeSerializer<Date> {
	@Override
	public Class<?> getType() {
		return Date.class;
	}

	@Override
	public SqlType getSqlType() {
		return SqlType.LONG;
	}

	@Override
	public Object serialize(Object data) {
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