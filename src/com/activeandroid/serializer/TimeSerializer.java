package com.activeandroid.serializer;

import android.text.format.Time;

public class TimeSerializer extends TypeSerializer {
	public Class<?> getDeserializedType() {
		return Time.class;
	}

	public Class<?> getSerializedType() {
		return long.class;
	}

	public Long serialize(Object data) {
		if (data == null) {
			return null;
		}

		return ((Time) data).toMillis(false);
	}

	public Time deserialize(Object data) {
		if (data == null) {
			return null;
		}
		Time t = new Time();
		t.set((Long) data);
		return t;
	}
}
