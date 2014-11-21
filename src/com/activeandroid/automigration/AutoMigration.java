package com.activeandroid.automigration;

import java.util.Random;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.activeandroid.Cache;
import com.activeandroid.TableInfo;
import com.activeandroid.util.Log;
import com.activeandroid.util.SQLiteUtils;
import com.activeandroid.util.SQLiteUtils.SQLiteType;

public class AutoMigration {
	
	public static class IncompatibleColumnTypesException extends RuntimeException {
		private static final long serialVersionUID = -6200636421142104030L;
		
		public IncompatibleColumnTypesException(String table, String column, SQLiteType typeA, SQLiteType typeB) {
			super("Failed to match column " + column + " type " + typeA + " to " + typeB + " in " + table + " table");
		}
	}
	
	public static void migrate(SQLiteDatabase db, int newVersion) {
		db.beginTransaction();
		try {
			for (TableInfo tableInfo : Cache.getTableInfos()) {
				processTableInfo(db, tableInfo);
			}
			db.execSQL("PRAGMA user_version = " + newVersion);
			Log.v("Automatic migration successfull, schemas updated to version " + newVersion);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
	
	private static void processTableInfo(SQLiteDatabase db, TableInfo tableInfo) {
		SQLTableInfo sqlTableInfo = getSqlTableInfo(db, tableInfo);
		if (sqlTableInfo != null) {
			TableDifference tableDifference = new TableDifference(tableInfo, sqlTableInfo);
			if (tableDifference.isEmpty() == false) {
				applyDifference(db, tableDifference);
			} else {
				Log.v("Table " + tableInfo.getTableName() + " does not have any difference, skipping it");
			}
		} else {
			Log.v("Table " + tableInfo.getTableName() + " does not exist. Creating new");
			db.execSQL(SQLiteUtils.createTableDefinition(tableInfo));
		}
	}
	
	private static void applyDifference(SQLiteDatabase db, TableDifference tableDifference) {
		TableInfo tableInfo = tableDifference.getTableInfo();
		if (Log.isEnabled()) {
			Log.v("Migrating table " + tableInfo.getTableName() + 
					" from schema '" + tableDifference.getSqlTableInfo().getSchema() + 
					"' to schema '" + SQLiteUtils.createTableDefinition(tableInfo) + "'");
		}
		
		if (tableDifference.isOnlyAdd()) {
			Log.v("Table " + tableInfo.getTableName() + " has added columns without primary / unique keys, no existing columns affected");
			for (SQLColumnInfo columnInfo : tableDifference.getDifferences().keySet()) {
				addColumnToTable(db, tableDifference, columnInfo);
				Log.v("Added " + columnInfo.getName() + " column to " + tableInfo.getTableName());
			}
		} else {
			Log.v("Table " + tableInfo.getTableName() + " has modified existing columns, moving data to newly created table");
			
			String temporaryTableName = "TEMP_" + (tableInfo.getTableName() + "_" + new Random().nextInt(1000));
			db.execSQL("ALTER TABLE " + tableInfo.getTableName() + " RENAME TO " + temporaryTableName);
			Log.v("Renamed " + tableInfo.getTableName() + " to " + temporaryTableName);
			
			db.execSQL(SQLiteUtils.createTableDefinition(tableInfo));
			Log.v("Created new table " + tableInfo.getTableName() + " with new schema");
					
			transferColumns(db, temporaryTableName, tableDifference);
			Log.v("Rows from temporary table " + temporaryTableName + " transferred to newly created table with new schema " + tableInfo.getTableName()); 
					
			db.execSQL("DROP TABLE " + temporaryTableName);
			Log.v("Dropped temporary table " + temporaryTableName);
		}
	}

	private static void addColumnToTable(SQLiteDatabase db, TableDifference tableDifference, SQLColumnInfo columnInfo) {
		db.execSQL("ALTER TABLE " + tableDifference.getTableInfo().getTableName() + " ADD COLUMN " + columnInfo.getColumnDefinition());
	}
	
	private static void transferColumns(SQLiteDatabase db, String sourceTable, TableDifference tableDifference) {
		Cursor sourceCursor = db.query(sourceTable, null, null, null, null, null, null);
		ContentValues contentValues = new ContentValues();
		try {
			
			while (sourceCursor.moveToNext()) {
				contentValues.clear();
				for (SQLColumnInfo columnInfo : tableDifference.getNewSchemaColumnInfos()) {
					if (tableDifference.getDifferences().containsKey(columnInfo)) {
						SQLColumnInfo mappedColumnInfo = tableDifference.getDifferences().get(columnInfo); 
						if (mappedColumnInfo != null) {
							putValueFromCursor(contentValues, sourceCursor, mappedColumnInfo, columnInfo);
						}
					} else {
						putValueFromCursor(contentValues, sourceCursor, columnInfo, columnInfo);
					}
				}
				db.insert(tableDifference.getTableInfo().getTableName(), null, contentValues);
			}
		} finally {
			sourceCursor.close();
		}
	}
	
	private static void putValueFromCursor(ContentValues contentValues, Cursor cursor, SQLColumnInfo sourceColumnInfo, SQLColumnInfo targetColumnInfo) {
		switch (sourceColumnInfo.getType()) {
		case INTEGER:
			contentValues.put(targetColumnInfo.getName(), cursor.getInt(cursor.getColumnIndex(sourceColumnInfo.getName())));
			break;
			
		case TEXT:
			contentValues.put(targetColumnInfo.getName(), cursor.getString(cursor.getColumnIndex(sourceColumnInfo.getName())));
			break;
			
		case REAL:
			contentValues.put(targetColumnInfo.getName(), cursor.getDouble(cursor.getColumnIndex(sourceColumnInfo.getName())));
			break;
			
		case BLOB:
			contentValues.put(targetColumnInfo.getName(), cursor.getBlob(cursor.getColumnIndex(sourceColumnInfo.getName())));
			break;
		}
	}
	
	private static SQLTableInfo getSqlTableInfo(SQLiteDatabase db, TableInfo tableInfo) {
		Cursor cursor = db.query("sqlite_master", new String[] { "sql" }, "tbl_name = ?", new String[] { tableInfo.getTableName() }, null, null, null);
		if (cursor.moveToNext()) {
			return new SQLTableInfo(cursor.getString(0));
		}
		return null;
	}
}
