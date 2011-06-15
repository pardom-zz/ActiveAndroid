package com.activeandroid.parser;

import java.util.Calendar;

import com.activeandroid.TypeSerializer;

public class CalendarSerializer extends TypeSerializer<Calendar> {
	@Override
	public Class<?> getType() {
		return Calendar.class;
	}

	@Override
	public com.activeandroid.TypeSerializer.SqlType getSqlType() {
		return SqlType.LONG;
	}

	@Override
	public Object serialize(Object data) {
		return ((Calendar) data).getTimeInMillis();
	}

	@Override
	public Calendar deserialize(Object data) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis((Long) data);

		return calendar;
	}
}
