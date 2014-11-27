package com.activeandroid.internal;

import android.content.ContentValues;
import android.database.Cursor;

import com.activeandroid.Model;

public class EmptyModelFiller extends ModelFiller {

	@Override
	public void loadFromCursor(Model model, Cursor cursor) {
		if (superModelFiller != null)
			superModelFiller.loadFromCursor(model, cursor);
	}

	@Override
	public void fillContentValues(Model model, ContentValues contentValues) {
		if (superModelFiller != null)
			superModelFiller.fillContentValues(model, contentValues);
	}
}
