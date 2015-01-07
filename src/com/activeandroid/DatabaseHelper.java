package com.activeandroid;

/*
 * Copyright (C) 2010 Michael Pardo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import com.activeandroid.util.IOUtils;
import com.activeandroid.util.Log;
import com.activeandroid.util.NaturalOrderComparator;
import com.activeandroid.util.SQLiteUtils;
import com.activeandroid.util.SqlParser;

public final class DatabaseHelper extends SQLiteOpenHelper {
	/**
	 * Table name in database.
	 */
	public static final String TABLE_NAME = "table_schema";

	/**
	 * The name column in table_schema.
	 */
	public static final String COLUMN_NAME = "name";

	/**
	 * The type column in table_schema.
	 */
	public static final String COLUMN_TYPE = "type";

	/**
	 * Constant for normal table.
	 */
	public static final int NORMAL_TABLE = 0;

	/**
	 * Constant for intermediate join table.
	 */
	public static final int INTERMEDIATE_JOIN_TABLE = 1;
	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	public final static String MIGRATION_PATH = "migrations";

	//////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE FIELDS
    //////////////////////////////////////////////////////////////////////////////////////

    private final String mSqlParser;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public DatabaseHelper(Configuration configuration) {
		super(configuration.getContext(), configuration.getDatabaseName(), null, configuration.getDatabaseVersion());
		copyAttachedDatabase(configuration.getContext(), configuration.getDatabaseName());
		mSqlParser = configuration.getSqlParser();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDEN METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onOpen(SQLiteDatabase db) {
		executePragmas(db);
	};

	@Override
	public void onCreate(SQLiteDatabase db) {
		executePragmas(db);
		executeCreate(db);
		executeMigrations(db, -1, db.getVersion());
		executeCreateIndex(db);
		updateDataTableSchema(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		executePragmas(db);
		executeCreate(db);
		updateTableSchema(db);//增加表记录
		dropTables(db);
		updateTables(db);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public void copyAttachedDatabase(Context context, String databaseName) {
		final File dbPath = context.getDatabasePath(databaseName);

		// If the database already exists, return
		if (dbPath.exists()) {
			return;
		}

		// Make sure we have a path to the file
		dbPath.getParentFile().mkdirs();

		// Try to copy database file
		try {
			final InputStream inputStream = context.getAssets().open(databaseName);
			final OutputStream output = new FileOutputStream(dbPath);

			byte[] buffer = new byte[8192];
			int length;

			while ((length = inputStream.read(buffer, 0, 8192)) > 0) {
				output.write(buffer, 0, length);
			}

			output.flush();
			output.close();
			inputStream.close();
		}
		catch (IOException e) {
			Log.e("Failed to open file", e);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private void executePragmas(SQLiteDatabase db) {
		if (SQLiteUtils.FOREIGN_KEYS_SUPPORTED) {
			db.execSQL("PRAGMA foreign_keys=ON;");
			Log.i("Foreign Keys supported. Enabling foreign key features.");
		}
	}

	private void executeCreateIndex(SQLiteDatabase db) {
		db.beginTransaction();
		try {
			for (TableInfo tableInfo : Cache.getTableInfos()) {
				String[] definitions = SQLiteUtils.createIndexDefinition(tableInfo);

				for (String definition : definitions) {
					db.execSQL(definition);
				}
			}
			db.setTransactionSuccessful();
		}
		finally {
			db.endTransaction();
		}
	}

	private void executeCreate(SQLiteDatabase db) {
		db.beginTransaction();
		try {
			for (TableInfo tableInfo : Cache.getTableInfos()) {
				db.execSQL(SQLiteUtils.createTableDefinition(tableInfo));
			}
			db.setTransactionSuccessful();
		}
		finally {
			db.endTransaction();
		}
	}

	private void updateDataTableSchema(SQLiteDatabase db){
		db.beginTransaction();
		try {
			for (TableInfo tableInfo : Cache.getTableInfos()) {
				if(!tableInfo.getTableName().equalsIgnoreCase(TABLE_NAME)){
					db.execSQL(SQLiteUtils.createTableDefinition(tableInfo));
					ContentValues values = new ContentValues();
					values.put(COLUMN_NAME, tableInfo.getTableName());
					values.put(COLUMN_TYPE, 0);
					db.insert(TABLE_NAME, null, values);
				}
			}
			db.setTransactionSuccessful();
		}
		finally {
			db.endTransaction();
		}
	}
	
	private void dropTables(SQLiteDatabase db){
		List<String> droptables = findTablesToDrop(db);
		if(droptables==null || droptables.isEmpty())return;
		db.beginTransaction();
		try {
			for(int i=0,j=droptables.size();i<j;i++){
				db.execSQL("drop table if exists " + droptables.get(i));
				db.execSQL("delete from  "+TABLE_NAME+" where name = '" + droptables.get(i)+"' ");
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

	}
	
	private void updateTables(SQLiteDatabase db) {
		for (TableInfo tableModel : Cache.getTableInfos()) {
			//---------------移除多余的列--------------------
			//得到需要移除的列
			String tableName = tableModel.getTableName();
			TableModel tableModelDB = getTableModel(tableName,db);
			if(tableName.equalsIgnoreCase(TABLE_NAME) || tableModelDB==null){
				continue;
			}
			List<String> removeColumnNames = new ArrayList<String>();
			Map<String, String> dbColumnsMap = tableModelDB.getColumns();
			Set<String> dbColumnNames = dbColumnsMap.keySet();
			for (String dbColumnName : dbColumnNames) {
				if (!tableModel.mFiledNames.containsKey(dbColumnName) && !dbColumnName.equalsIgnoreCase("ID")) {
					removeColumnNames.add(dbColumnName);
				}
			}
			
			if (removeColumnNames != null && !removeColumnNames.isEmpty()) {
				
				String alterToTempTableSQL = "alter table "+tableName+" rename to "+tableName + "_temp";
				String createNewTableSQL = generateCreateNewTableSQL(removeColumnNames, tableName, db);
				String dataMigrationSQL = generateDataMigrationSQL(removeColumnNames, tableName, db);
				String dropTempTableSQL = "drop table if exists " + tableName + "_temp";
				String[] sqls = { alterToTempTableSQL, createNewTableSQL, dataMigrationSQL, dropTempTableSQL };

				db.beginTransaction();
				try {
					if (sqls != null) {
						for (String sql : sqls) {
							db.execSQL(sql);
						}
					}
					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}
			}
			
			
			//添加数据库没有的列
			List<String> newColumnsList =new  ArrayList<String>();
			for (String columnName : tableModel.mFiledNames.keySet()) {
				boolean isNewColumn = true;
				for (String dbColumnName : getTableModel(tableModel.getTableName(),db).getColumnNames()) {
					if (columnName.equalsIgnoreCase(dbColumnName)) {
						isNewColumn = false;
						break;
					}
				}
				if (isNewColumn) {
					if (!columnName.equalsIgnoreCase("id")) {
						newColumnsList.add(SQLiteUtils.createColumnDefinition(tableModel,tableModel.mFiledNames.get(columnName)));
					}
				}
			}
			
			if(newColumnsList !=null && !newColumnsList.isEmpty()){
				db.beginTransaction();
				try {
					for (String columnName : newColumnsList) {
						StringBuilder addColumnSQL = new StringBuilder();
						addColumnSQL.append("alter table ").append(tableModel.getTableName()).append(" add column ").append(columnName).append(" ");
						Log.d("add column sql is >> " + addColumnSQL);
						db.execSQL(addColumnSQL.toString());
					}
					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}
			}
			// changeColumnsType(findColumnTypesToChange());
		}
	}
	
	
	//增加新表
	private String generateCreateNewTableSQL(Collection<String> removeColumnNames, String tableName,SQLiteDatabase db) {
		TableModel tableModelDB = getTableModel(tableName,db);
		for (String removeColumnName : removeColumnNames) {
			tableModelDB.removeColumnIgnoreCases(removeColumnName);
		}
		Map<String, String> columnsMap = tableModelDB.getColumns();
		Set<String> columnNames = columnsMap.keySet();
		removeId(columnNames);
		StringBuilder createTableSQL = new StringBuilder("create table ");
		createTableSQL.append(tableName).append(" (");
		createTableSQL.append("Id integer primary key autoincrement,");
		Iterator<String> i = columnNames.iterator();
		if (!i.hasNext()) {
			createTableSQL.deleteCharAt(createTableSQL.length() - 1);
		}
		boolean needSeparator = false;
		while (i.hasNext()) {
			if (needSeparator) {
				createTableSQL.append(", ");
			}
			needSeparator = true;
			String columnName = i.next();
			createTableSQL.append(columnName).append(" ").append(columnsMap.get(columnName));
		}
		createTableSQL.append(")");
		Log.d("add column sql is >> " + createTableSQL);
		return createTableSQL.toString();
	}
	
	//合成数据
	private String generateDataMigrationSQL(Collection<String> removeColumnNames, String tableName,SQLiteDatabase db) {
		List<String> columnNames = new ArrayList<String>();
		for (String columnName : getTableModel(tableName,db).getColumnNames()) {
			if (!removeColumnNames.contains(columnName)) {
				columnNames.add(columnName);
			}
		}
		if (!columnNames.isEmpty()) {
			StringBuilder sql = new StringBuilder();
			sql.append("insert into ").append(tableName).append("(");
			boolean needComma = false;
			for (String columnName : columnNames) {
				if (needComma) {
					sql.append(", ");
				}
				needComma = true;
				sql.append(columnName);
			}
			sql.append(") ");
			sql.append("select ");
			needComma = false;
			for (String columnName : columnNames) {
				if (needComma) {
					sql.append(", ");
				}
				needComma = true;
				sql.append(columnName);
			}
			sql.append(" from ").append(tableName + "_temp");
			return sql.toString();
		} else {
			return null;
		}
	}
	
	private void updateTableSchema(SQLiteDatabase db) {
		List<String> putTableSchemaNames = new ArrayList<String>();
		List<String> tablesDB = getTablesDB(db);
		
		Collection<TableInfo> tableInfos = Cache.getTableInfos();
		for (Iterator iterator = tableInfos.iterator(); iterator.hasNext();) {
			TableInfo tableInfo = (TableInfo) iterator.next();
			String tableName = tableInfo.getTableName();
			if(shouldCreateThisTable(tablesDB,tableName) && !tableName.equalsIgnoreCase(TABLE_NAME)){
				db.beginTransaction();
				try {
					ContentValues values = new ContentValues();
					values.put(COLUMN_NAME, tableName);
					values.put(COLUMN_TYPE, 0);
					db.insert(TABLE_NAME, null, values);
					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}
			}
		}
	}
	private boolean shouldCreateThisTable(List<String> tablesDB,String tableName) {
		boolean ishave = false;
		for (Iterator iterator = tablesDB.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			if(string.equalsIgnoreCase(tableName)){
				ishave = true;
				break;
			}
		}
		return !ishave;
	}
	
	
	private void removeId(Set<String> columnNames) {
		String idName = "";
		for (String columnName : columnNames) {
			if (columnName.equalsIgnoreCase("ID")) {
				idName = columnName;
				break;
			}
		}
		if (!TextUtils.isEmpty(idName)) {
			columnNames.remove(idName);
		}
	}
	
	public TableModel getTableModel(String tableName, SQLiteDatabase db) {
		if (!isTableExists(tableName, db)) {
			return null;
		}
		TableModel tableModelDB = new TableModel();
		tableModelDB.setTableName(tableName);
		String checkingColumnSQL = "pragma table_info(" + tableName + ")";
		Cursor cursor = null;
		try {
			cursor = db.rawQuery(checkingColumnSQL, null);
			if (cursor.moveToFirst()) {
				do {
					String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
					String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
					tableModelDB.addColumn(name, type);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return tableModelDB;
	}
	
	public boolean isTableExists(String tableName, SQLiteDatabase db) {
		boolean exist = false;
		try {
			List<String> tableNames = getTablesDB(db);
			for (String string : tableNames) {
				if(tableName.equalsIgnoreCase(string)){
					exist = true;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			exist = false;
		}
		return exist;
	}
	
	private List<String> findTablesToDrop(SQLiteDatabase db) {
		List<String> dropTableNames = new ArrayList<String>();
		Cursor cursor = null;
		try {
			cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
			if (cursor.moveToFirst()) {
				do {
					String tableName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
					int tableType = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
					if (shouldDropThisTable(tableName, tableType) && !tableName.equalsIgnoreCase(TABLE_NAME)) {
						Log.d("need to drop " + tableName);
						dropTableNames.add(tableName);
					}
				} while (cursor.moveToNext());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return dropTableNames;
	}
	
	private List<String> getTablesDB(SQLiteDatabase db) {
		List<String> tableNames = new ArrayList<String>();
		Cursor cursor = null;
		try {
			cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
			if (cursor.moveToFirst()) {
				do {
					String tableName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
					int tableType = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
					if(tableType == NORMAL_TABLE){
						tableNames.add(tableName);
					}
				} while (cursor.moveToNext());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return tableNames;
	}
	
	private boolean shouldDropThisTable(String tableName, int tableType) {
		return !containsIgnoreCases(Cache.getTableInfos(), tableName)&& tableType == NORMAL_TABLE;
	}
	
	public boolean containsIgnoreCases(Collection<TableInfo> collection, String string) {
		if (collection == null) {
			return false;
		}
		if (string == null) {
			return collection.contains(null);
		}
		boolean contains = false;
		for (TableInfo element : collection) {
			if (string.equalsIgnoreCase(element.getTableName())) {
				contains = true;
				break;
			}
		}
		return contains;
	}
	
	private boolean executeMigrations(SQLiteDatabase db, int oldVersion, int newVersion) {
		boolean migrationExecuted = false;
		try {
			final List<String> files = Arrays.asList(Cache.getContext().getAssets().list(MIGRATION_PATH));
			Collections.sort(files, new NaturalOrderComparator());

			db.beginTransaction();
			try {
				for (String file : files) {
					try {
						final int version = Integer.valueOf(file.replace(".sql", ""));

						if (version > oldVersion && version <= newVersion) {
							executeSqlScript(db, file);
							migrationExecuted = true;

							Log.i(file + " executed succesfully.");
						}
					}
					catch (NumberFormatException e) {
						Log.w("Skipping invalidly named file: " + file, e);
					}
				}
				db.setTransactionSuccessful();
			}
			finally {
				db.endTransaction();
			}
		}
		catch (IOException e) {
			Log.e("Failed to execute migrations.", e);
		}

		return migrationExecuted;
	}

	private void executeSqlScript(SQLiteDatabase db, String file) {

	    InputStream stream = null;

		try {
		    stream = Cache.getContext().getAssets().open(MIGRATION_PATH + "/" + file);

		    if (Configuration.SQL_PARSER_DELIMITED.equalsIgnoreCase(mSqlParser)) {
		        executeDelimitedSqlScript(db, stream);

		    } else {
		        executeLegacySqlScript(db, stream);

		    }

		} catch (IOException e) {
			Log.e("Failed to execute " + file, e);

		} finally {
		    IOUtils.closeQuietly(stream);

		}
	}

	private void executeDelimitedSqlScript(SQLiteDatabase db, InputStream stream) throws IOException {

	    List<String> commands = SqlParser.parse(stream);

	    for(String command : commands) {
	        db.execSQL(command);
	    }
	}

	private void executeLegacySqlScript(SQLiteDatabase db, InputStream stream) throws IOException {

	    InputStreamReader reader = null;
        BufferedReader buffer = null;

        try {
            reader = new InputStreamReader(stream);
            buffer = new BufferedReader(reader);
            String line = null;

            while ((line = buffer.readLine()) != null) {
                line = line.replace(";", "").trim();
                if (!TextUtils.isEmpty(line)) {
                    db.execSQL(line);
                }
            }

        } finally {
            IOUtils.closeQuietly(buffer);
            IOUtils.closeQuietly(reader);

        }
	}
}
