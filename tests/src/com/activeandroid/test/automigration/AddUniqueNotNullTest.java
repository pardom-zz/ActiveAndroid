package com.activeandroid.test.automigration;

import java.util.List;

import android.database.Cursor;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

public class AddUniqueNotNullTest extends AutoMigrationTest {
	
	private static final String TABLE = "add_unique_table";
	
	@Table(name = TABLE)
	public static class AddUniqueMigrationModel extends Model {
		@Column(name = "textValue", notNull = true, unique = true)		
		public String textValue;
		@Column(name = "boolValue")
		public boolean boolValue;
		@Column(name = "floatValue")
		public float floatValue;
		
		public AddUniqueMigrationModel() {

		}
	}
	
	public AddUniqueNotNullTest() {
		super(TABLE);
	}
		
	public void testMigrationUniqueNotNullFieldAdded() {
		createOldDatabase();
		initializeActiveAndroid(AddUniqueMigrationModel.class);
		List<AddUniqueMigrationModel> migrationModels = new Select().from(AddUniqueMigrationModel.class).execute();
		assertEquals(10, migrationModels.size());
		for (int i = 0; i < 10; ++i) {
			AddUniqueMigrationModel migrationModel = migrationModels.get(i);
			assertEquals(Long.valueOf(i + 1), migrationModel.getId());
			assertEquals("Text " + i, migrationModel.textValue);
			assertEquals(i % 2 == 0, migrationModel.boolValue);
			assertEquals((float) i, migrationModel.floatValue);
		}
		
		Cursor cursor = ActiveAndroid.getDatabase().query(TABLE, null, null, null, null, null, null);
		assertTrue(cursor.getColumnIndex("unusedColumn") == -1);
	}
	
	
}
