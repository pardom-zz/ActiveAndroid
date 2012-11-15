package com.activeandroid.serializer;

import java.util.Calendar;

import com.activeandroid.util.SQLiteUtils.SQLiteType;

final public class CalendarSerializer implements TypeSerializer {
	public Class<?> getDeserializedType() {
		return Calendar.class;
	}

	public SQLiteType getSerializedType() {
		return SQLiteType.INTEGER;
	}

	public Long serialize(Object data) {
		return ((Calendar) data).getTimeInMillis();
	}

	public Calendar deserialize(Object data) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis((Long) data);

		return calendar;
	}
}