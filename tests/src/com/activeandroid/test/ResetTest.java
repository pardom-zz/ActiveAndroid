package com.activeandroid.test;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.activeandroid.Model;
import com.activeandroid.annotation.Table;

import android.test.AndroidTestCase;

import java.io.IOException;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ResetTest extends AndroidTestCase {

    public void testReset() {
        Configuration conf = new Configuration.Builder(getContext())
			.setDatabaseVersion(1)
			.setDatabaseName("ResetTest")
			.addModelClass(ResetTestFirstModel.class)
			.create();

		ActiveAndroid.initialize(conf, true);
		assertTrue(checkIfTableExists("ResetTestFirstModel"));
		assertFalse(checkIfTableExists("ResetTestSecondModel"));
		ActiveAndroid.dispose();

        Configuration conf2 = new Configuration.Builder(getContext())
			.setDatabaseVersion(2)
			.setDatabaseName("ResetTest")
			.setResetDatabase(true)
			.addModelClass(ResetTestSecondModel.class)
			.create();

		ActiveAndroid.initialize(conf2, true);
		assertFalse(checkIfTableExists("ResetTestFirstModel"));
		assertTrue(checkIfTableExists("ResetTestSecondModel"));
    }

	public boolean checkIfTableExists(String tableName) {
		SQLiteDatabase db = ActiveAndroid.getDatabase();
		Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
		if (cursor.getCount() > 0) {
			cursor.close();
			return true;
		}
		cursor.close();
		return false;
	}

    @Table(name = "ResetTestFirstModel")
    private static class ResetTestFirstModel extends Model {
    }

    @Table(name = "ResetTestSecondModel")
    private static class ResetTestSecondModel extends Model {
    }
}
