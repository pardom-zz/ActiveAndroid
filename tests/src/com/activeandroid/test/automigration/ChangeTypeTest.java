package com.activeandroid.test.automigration;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.automigration.AutoMigration;

public class ChangeTypeTest extends AutoMigrationTest {
	
	private static final String TABLE = "change_type_model";

	@Table(name = TABLE)
	public static class AddColumnMigrationModel extends Model {
		@Column(name = "textValue")		
		public boolean textValue;
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
	
	public ChangeTypeTest() {
		super(TABLE);
	}
	
	public void testMigrationNewFieldsAdded() {
		createOldDatabase();
		try {
			initializeActiveAndroid(AddColumnMigrationModel.class);
		} catch (Exception e) {
			assertEquals(AutoMigration.IncompatibleColumnTypesException.class, e.getClass());
			assertTrue(e.getMessage().contains("textValue"));
			assertTrue(e.getMessage().contains("TEXT"));
			assertTrue(e.getMessage().contains("INTEGER"));
			assertTrue(e.getMessage().contains(TABLE));
			return;
		}
		fail("Exception was not thrown during auto migration");
	}
}
