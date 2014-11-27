package com.activeandroid.internal;

import com.activeandroid.Model;

import android.database.Cursor;
import android.content.ContentValues;


public abstract class ModelFiller {
	public static final String SUFFIX = "$$ActiveAndroidModelFiller";
	public ModelFiller superModelFiller;
	
	public abstract void loadFromCursor(Model model, Cursor cursor);
	public abstract void fillContentValues(Model model, ContentValues contentValues);
}
