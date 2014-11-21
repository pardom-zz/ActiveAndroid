package com.activeandroid.test.automigration;

import java.util.List;

import android.database.Cursor;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

public class AddColumnsTest extends AutoMigrationTest {
	
	private static final String TABLE = "add_column_model";

	@Table(name = TABLE)
	public static class AddColumnMigrationModel extends Model {
		@Column(name = "textValue")		
		public String textValue;
		@Column(name = "boolValue")
		public boolean boolValue;
		@Column(name = "floatValue")
		public float floatValue;
		@Column(name = "newString")
		public String newString;
		@Column(name = "newFloat")
		public float newFloat;
		
		public AddColumnMigrationModel() {

		}
	}
	
	public AddColumnsTest() {
		super(TABLE);
	}
	
	public void testMigrationNewFieldsAdded() {
		createOldDatabase();
		initializeActiveAndroid(AddColumnMigrationModel.class);
		List<AddColumnMigrationModel> migrationModels = new Select().from(AddColumnMigrationModel.class).execute();
		assertEquals(10, migrationModels.size());
		for (int i = 0; i < 10; ++i) {
			AddColumnMigrationModel migrationModel = migrationModels.get(i);
			assertEquals(Long.valueOf(i + 1), migrationModel.getId());
			assertEquals("Text " + i, migrationModel.textValue);
			assertEquals(i % 2 == 0, migrationModel.boolValue);
			assertEquals((float) i, migrationModel.floatValue);
			assertNull(migrationModel.newString);
		}
		
		Cursor cursor = ActiveAndroid.getDatabase().query(TABLE, null, null, null, null, null, null);
		assertTrue(cursor.getColumnIndex("unusedColumn") != -1);
	}
	
}
