package com.activeandroid.automigration;

import java.util.ArrayList;
import java.util.Locale;

import com.activeandroid.util.SQLiteUtils.SQLiteType;

import android.text.TextUtils;

public class SQLColumnInfo {

	private String mColumnDefinition;
	private String mName;
	private SQLiteType mType;

	public SQLColumnInfo(String columnDefinition) {
		ArrayList<String> tokens = new ArrayList<String>();
		for (String token : columnDefinition.split(" ")) {
			if (TextUtils.isEmpty(token) == false)
				tokens.add(token);
		}

		if (tokens.size() < 2)
			throw new IllegalArgumentException("Failed to parse '" + columnDefinition + "' as sql column definition");
		
		this.mColumnDefinition = TextUtils.join(" ", tokens.subList(1, tokens.size()));

		this.mName = tokens.get(0);
		this.mType = SQLiteType.valueOf(tokens.get(1).toUpperCase(Locale.US));

	}

	public String getName() {
		return mName;
	}

	public SQLiteType getType() {
		return mType;
	}
	
	public String getColumnDefinition() {
		return mName + " " + mColumnDefinition;
	}
	
	public boolean isPrimaryKey() {
		return mColumnDefinition.toUpperCase(Locale.US).contains("PRIMARY KEY");
	}
	
	public boolean isUnique() {
		return mColumnDefinition.toUpperCase(Locale.US).contains("UNIQUE");
	}
	
}
