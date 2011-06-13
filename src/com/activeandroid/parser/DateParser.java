package com.activeandroid.parser;

import java.util.Date;

import com.activeandroid.TypeParser;

public class DateParser extends TypeParser<Date> {
	@Override
	public Class<?> getType() {
		return Date.class;
	}

	@Override
	public Object save(Date data) {
		return data.getTime();
	}

	@Override
	public Date load(Object data) {
		return new Date(Long.parseLong(data.toString()));
	}
}
