package com.activeandroid;

import java.util.Calendar;

final class CalendarSerializer extends TypeSerializer {
	@Override
	public Class<?> getDeserializedType() {
		return Calendar.class;
	}

	@Override
	public TypeSerializer.SerializedType getSerializedType() {
		return SerializedType.LONG;
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
