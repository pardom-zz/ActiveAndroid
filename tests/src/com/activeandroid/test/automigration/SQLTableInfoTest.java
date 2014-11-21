package com.activeandroid.test.automigration;

import com.activeandroid.automigration.SQLColumnInfo;
import com.activeandroid.automigration.SQLTableInfo;
import com.activeandroid.test.ActiveAndroidTestCase;
import com.activeandroid.util.SQLiteUtils.SQLiteType;

public class SQLTableInfoTest extends ActiveAndroidTestCase {
	
	public void testEmptyParam() {
		try {
			new SQLTableInfo(""); 
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("empty"));
			return;
		}
		fail(IllegalArgumentException.class.getSimpleName() + " was not thrown");
	}
	
	public void testInvalidSchema() {
		try {
			new SQLTableInfo("test(id integer primary key);"); 
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("valid"));
			return;
		}
		fail(IllegalArgumentException.class.getSimpleName() + " was not thrown");
	}
	
	public void testInvalidSchemaNoBrackets() {
		try {
			new SQLTableInfo("create table test;"); 
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("valid"));
			return;
		}
		fail(IllegalArgumentException.class.getSimpleName() + " was not thrown");
	}
	
	public void testTableName() {
		SQLTableInfo sqlTableInfo = new SQLTableInfo("create table \"test\"(id integer);");
		assertEquals("test", sqlTableInfo.getTableName());
	}
	
	public void testTableWithoutPrimaryKey() {
		SQLTableInfo sqlTableInfo = new SQLTableInfo("create table test(id integer);");
		assertNull(sqlTableInfo.getIdColumnInfo());
	}
	
	public void testTableWithMultipleSpaces() {
		SQLTableInfo sqlTableInfo = new SQLTableInfo("create    table    test(id    integer     primary    key);");
		assertNotNull(sqlTableInfo.getIdColumnInfo());
		verifyColumn(sqlTableInfo.getIdColumnInfo(), "id", SQLiteType.INTEGER);
	}
	
	public void testTableWithPrimaryKey() {
		SQLTableInfo sqlTableInfo = new SQLTableInfo("create table test(id integer primary key);");
		assertNotNull(sqlTableInfo.getIdColumnInfo());
		verifyColumn(sqlTableInfo.getIdColumnInfo(), "id", SQLiteType.INTEGER);
	}
	
	public void testMultipleColumnsWithoutPrimaryKey() {
		SQLTableInfo sqlTableInfo = new SQLTableInfo("CREATE TABLE test(id integer key, my_value TEXT, boolean_value INTEGER);");
		assertNull(sqlTableInfo.getIdColumnInfo());
		assertTrue(sqlTableInfo.getColumns().size() == 3);
		verifyColumn(sqlTableInfo.getColumns().get(0), "id", SQLiteType.INTEGER);
		verifyColumn(sqlTableInfo.getColumns().get(1), "my_value", SQLiteType.TEXT);
		verifyColumn(sqlTableInfo.getColumns().get(2), "boolean_value", SQLiteType.INTEGER);
	}
	
	public void testCreateSchema() {
		String sqlSchema = "CREATE TABLE test(id integer key, my_value TEXT, boolean_value INTEGER);";
		SQLTableInfo sqlTableInfo = new SQLTableInfo(sqlSchema);
		String createdSchema = SQLTableInfo.constructSchema(sqlTableInfo.getTableName(), sqlTableInfo.getColumns()); 
		assertTrue(createdSchema.equalsIgnoreCase(sqlSchema));
	}
	
	private void verifyColumn(SQLColumnInfo columnInfo, String name, SQLiteType type) {
		assertEquals(name, columnInfo.getName());
		assertEquals(type, columnInfo.getType());
	}
}
