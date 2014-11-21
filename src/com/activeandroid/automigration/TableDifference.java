package com.activeandroid.automigration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.activeandroid.TableInfo;
import com.activeandroid.automigration.AutoMigration.IncompatibleColumnTypesException;
import com.activeandroid.util.SQLiteUtils;

class TableDifference {
	
	
	private TableInfo mTableInfo;
	private SQLTableInfo mSqlTableInfo;
	private Map<SQLColumnInfo, SQLColumnInfo> mDifferences;
	private List<SQLColumnInfo> mCurrentVersionTableDefinitions;
	
	public TableDifference(TableInfo tableInfo, SQLTableInfo sqlTableInfo) {
		this.mTableInfo = tableInfo;
		this.mSqlTableInfo = sqlTableInfo;
		this.mDifferences = new HashMap<SQLColumnInfo, SQLColumnInfo>();
		this.mCurrentVersionTableDefinitions = new ArrayList<SQLColumnInfo>();
		
		for (Field field : tableInfo.getFields()) {
			SQLColumnInfo sqlColumnInfo = new SQLColumnInfo(SQLiteUtils.createColumnDefinition(tableInfo, field));
			mCurrentVersionTableDefinitions.add(sqlColumnInfo);
			
			boolean found = false;
			for (SQLColumnInfo existingColumnInfo : sqlTableInfo.getColumns()) {
				if (existingColumnInfo.getName().equalsIgnoreCase(sqlColumnInfo.getName()) == false)
					continue;
				
				found = true;
				
				if (existingColumnInfo.getColumnDefinition().equalsIgnoreCase(sqlColumnInfo.getColumnDefinition()) == false) {
					if (existingColumnInfo.getType() == sqlColumnInfo.getType()) {
						mDifferences.put(sqlColumnInfo, existingColumnInfo);
					} else {
						throw new IncompatibleColumnTypesException(tableInfo.getTableName(), existingColumnInfo.getName(), existingColumnInfo.getType(), sqlColumnInfo.getType());
					}
				}
				break;
			}
			if (!found)
				mDifferences.put(sqlColumnInfo, null);
		}
	}
	
	public boolean isOnlyAdd() {
		for (SQLColumnInfo sqlColumnInfo : mDifferences.keySet()) {
			if (mDifferences.get(sqlColumnInfo) != null || sqlColumnInfo.isPrimaryKey() || sqlColumnInfo.isUnique())
				return false;
		}
		return true;
	}
	
	public boolean isEmpty() {
		return mDifferences.size() == 0;
	}
	
	public Map<SQLColumnInfo, SQLColumnInfo> getDifferences() {
		return mDifferences;
	}
	
	public List<SQLColumnInfo> getNewSchemaColumnInfos() {
		return mCurrentVersionTableDefinitions;
	}
	
	public TableInfo getTableInfo() {
		return mTableInfo;
	}
	
	public SQLTableInfo getSqlTableInfo() {
		return mSqlTableInfo;
	}
}
