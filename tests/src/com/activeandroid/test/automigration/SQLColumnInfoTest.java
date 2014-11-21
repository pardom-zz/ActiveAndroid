package com.activeandroid.test.automigration;

import com.activeandroid.automigration.SQLColumnInfo;
import com.activeandroid.test.ActiveAndroidTestCase;

public class SQLColumnInfoTest extends ActiveAndroidTestCase {
	
	public void testPrimaryKey() {
		SQLColumnInfo sqlColumnInfo = new SQLColumnInfo("id integer primary key");
		assertTrue(sqlColumnInfo.isPrimaryKey());
	}
	
	public void testNoPrimaryKey() {
		SQLColumnInfo sqlColumnInfo = new SQLColumnInfo("id integer key");
		assertFalse(sqlColumnInfo.isPrimaryKey());
	}
	
	public void testUniqueInName() {
		SQLColumnInfo sqlColumnInfo = new SQLColumnInfo("unique_id integer");
		assertFalse(sqlColumnInfo.isUnique());
	}
	
	public void testUnique() {
		SQLColumnInfo sqlColumnInfo = new SQLColumnInfo("id integer unique on conflict replace");
		assertTrue(sqlColumnInfo.isUnique());
	}
	
	public void testNotUnique() {
		SQLColumnInfo sqlColumnInfo = new SQLColumnInfo("unique_id text not null default \"not null\"");
		assertFalse(sqlColumnInfo.isUnique());
	}
}
