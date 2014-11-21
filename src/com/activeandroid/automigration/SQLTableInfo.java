package com.activeandroid.automigration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.text.TextUtils;

public final class SQLTableInfo {
	
	public static String constructSchema(String tableName, List<SQLColumnInfo> columns) {
		String schema = "CREATE TABLE " + tableName + "(%s);";
		List<String> tokens = new ArrayList<String>();
		for (SQLColumnInfo column : columns) { 
			tokens.add(column.getColumnDefinition());
		}
		return String.format(schema, TextUtils.join(", ", tokens));
	}
	
	//Note that this class does not validate SQL syntax
	
	private String mTableName;
	private SQLColumnInfo mIdColumnInfo;
	private List<SQLColumnInfo> mColumns;
	private String mSchema;

	public SQLTableInfo(String sqlSchema) {
		
		if (TextUtils.isEmpty(sqlSchema))
			throw new IllegalArgumentException("Cannot construct SqlTableInfo from empty sqlSchema");
		
		sqlSchema = sqlSchema.replaceAll("\\s+", " ");
		this.mSchema = new String(sqlSchema);
		
		if (!sqlSchema.toUpperCase(Locale.US).startsWith("CREATE TABLE") || !sqlSchema.contains("(") || !sqlSchema.contains(")"))
			throw new IllegalArgumentException("sqlSchema doesn't appears to be valid");
		mColumns = new ArrayList<SQLColumnInfo>();
		
		sqlSchema = sqlSchema.replaceAll("(?i)CREATE TABLE ", "");
		mTableName = sqlSchema.substring(0, sqlSchema.indexOf('(')).replace("\"", "");
		
		String columnDefinitions = sqlSchema.substring(sqlSchema.indexOf('(') + 1, sqlSchema.lastIndexOf(')'));
		processColumnsDefinitions(columnDefinitions.split(","));
	}
	
	private void processColumnsDefinitions(String[] columns) {
		for (String columnDef : columns) {
			SQLColumnInfo columnInfo = new SQLColumnInfo(columnDef);
			if (columnInfo.isPrimaryKey()) {
				if (mIdColumnInfo == null)
					mIdColumnInfo = columnInfo;
				else
					throw new IllegalArgumentException("sqlSchema contains multiple primary keys");
			}
			
			mColumns.add(columnInfo);
		}
	}
	
	public String getSchema() {
		return mSchema;
	}

	public SQLColumnInfo getIdColumnInfo() {
		return mIdColumnInfo;
	}
	
	public List<SQLColumnInfo> getColumns() {
		return mColumns;
	}
	
	public String getTableName() {
		return mTableName;
	}
}
