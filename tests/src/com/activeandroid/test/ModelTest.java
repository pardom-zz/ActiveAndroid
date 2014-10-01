/*
 * Copyright (C) 2013 Vojtech Sigler.
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

package com.activeandroid.test;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.TableInfo;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple test now covering equals and hashcode methods.
 */
public class ModelTest extends ActiveAndroidTestCase {

	/**
	 * Equals should be type-safe.
	 */	
	public void testEqualsNonModel() {
		MockModel model = new MockModel();

		assertFalse(model.equals("Dummy"));
		assertFalse(model.equals(null));
	}

	/**
	 * Equals should not be true for different model classes.
	 */	
	public void testEqualsDifferentModel() {
		Model model1 = new MockModel();
		Model model2 = new AnotherMockModel();

		assertFalse(model1.equals(model2));
	}

	/**
	 * A new object does not have PK assigned yet,
	 * therefore by default it is equal only to itself.
	 */	
	public void testEqualsOnNew() {
		MockModel model1 = new MockModel();
		MockModel model2 = new MockModel();

		assertFalse(model1.equals(model2));
		assertFalse(model2.equals(model1));
		assertTrue(model1.equals(model1));  //equal only to itself
	}

	/**
	 * Two different rows in a table should not be equal (different ids).
	 */	
	public void testEqualsDifferentRows() {
		MockModel model1 = new MockModel();
		MockModel model2 = new MockModel();
		MockModel model3;

		model1.save();
		model2.save();
		model3 = Model.load(MockModel.class, model1.getId());

        // Not equal to each other.
		assertFalse(model1.equals(model2));
		assertFalse(model2.equals(model1));

        // Equal to each other when loaded.
		assertTrue(model1.equals(model3));
		assertTrue(model1.equals(model3));

        // Loaded model is not equal to a different model.
		assertFalse(model3.equals(model2));
		assertFalse(model2.equals(model3));
	}

	/**
	 * Tests hashcode for new instances.
	 */	
	public void testHashCode() {
		Set<Model> set = new HashSet<Model>();
		Model m1 = new MockModel();
		Model m2 = new MockModel();
		Model m3 = new AnotherMockModel();

		assertFalse(m1.hashCode() == m2.hashCode()); // hashes for unsaved models must not match
		set.add(m1);
		set.add(m2);
		assertEquals(2, set.size()); //try in a set

		assertFalse(m1.hashCode() == m3.hashCode());
		set.add(m3);
		assertEquals(3, set.size());
	}

	/**
	 * Two rows in a table should have different hashcodes.
	 */
	public void testHashCodeDifferentRows() {
		Set<Model> set = new HashSet<Model>();
		Model m1 = new MockModel();
		Model m2 = new MockModel();
		Model m3;

		m1.save();
		m2.save();
		m3 = Model.load(MockModel.class, m1.getId());

		assertEquals(m1.hashCode(), m3.hashCode());
		assertFalse(m1.hashCode() == m2.hashCode());
		set.add(m1);
		set.add(m2);
		set.add(m3);
		assertEquals(2, set.size());
	}

    /**
     * Column names should default to the field name.
     */
    public void testColumnNamesDefaulToFieldNames() {
        TableInfo tableInfo = Cache.getTableInfo(MockModel.class);

        for ( Field field : tableInfo.getFields() ) {
            // Id column is a special case, we'll ignore that one.
            if ( field.getName().equals("mId") ) continue;

            assertEquals(field.getName(), tableInfo.getColumnName(field));
        }
    }

    /**
     * Boolean should handle integer (0/1) and boolean (false/true) values.
     */
    public void testBooleanColumnType() {
        MockModel mockModel = new MockModel();
        mockModel.booleanField = false;
        Long id = mockModel.save();

        boolean databaseBooleanValue = MockModel.load( MockModel.class, id ).booleanField;

        assertEquals( false, databaseBooleanValue );

        // Test passing both a integer and a boolean into the where conditional.
        assertEquals(
                mockModel,
                new Select().from(MockModel.class).where("booleanField = ?", 0).executeSingle() );

        assertEquals(
                mockModel,
                new Select().from(MockModel.class).where("booleanField = ?", false).executeSingle() );

        assertNull( new Select().from(MockModel.class).where("booleanField = ?", 1).executeSingle() );

        assertNull( new Select().from(MockModel.class).where("booleanField = ?", true).executeSingle() );
    }

	/**
     * Test to check the join of two (or more) tables with some fields in common when not use a projection on select.
     * Test the issue #106 (https://github.com/pardom/ActiveAndroid/issues/106)
     */
    public void testJoinWithSameNames(){
        //create a parent entity and store
        ParentJoinMockModel parent = new ParentJoinMockModel();
        parent.booleanField = true;
        parent.dateField = new Date();
        parent.doubleField = 2.0;
        parent.intField = 1;
        parent.save();

        //the values to assign to child
        Date dateValue = new Date();
        double doubleValue = 30.0;
        int intValue = 3;

        //create two child entities, relate with parent and save
        ChildMockModel child1 = new ChildMockModel();
        child1.booleanField = false;
        child1.dateField = dateValue;
        child1.doubleField = doubleValue;
        child1.intField = intValue;
        child1.parent = parent;
        child1.save();

        ChildMockModel child2 = new ChildMockModel();
        child2.booleanField = false;
        child2.dateField = dateValue;
        child2.doubleField = doubleValue;
        child2.intField = intValue;
        child2.parent = parent;
        child2.save();

        //Store the ids assigned to child entities when persists
        List<Long> ids = new ArrayList<Long>();
        ids.add(child1.getId());
        ids.add(child2.getId());

        //make the query with a join
        List<ChildMockModel> result = new Select().from(ChildMockModel.class).
                join(ParentJoinMockModel.class).on("ParentJoinMockModel.Id = ChildMockModel.parent").execute();

        //check result
        assertNotNull(result);
        assertEquals(result.size(), 2);
        for(ChildMockModel currentModel : result){
            assertFalse(currentModel.booleanField);
            assertEquals(currentModel.intField, intValue);
            assertEquals(currentModel.doubleField, doubleValue);
            assertTrue(ids.contains(currentModel.getId()));
        }

    }

	/**
	 * Mock model as we need 2 different model classes.
	 */
	@Table(name = "AnotherMockTable")
	public static class AnotherMockModel extends Model {}

    /**
     * Mock model to test joins with same names.
     * It's a copy from MockModel.
     */
    @Table(name = "ParentJoinMockModel")
    public static class ParentJoinMockModel extends Model {
        @Column
        public Date dateField;

        @Column
        public double doubleField;

        @Column
        public int intField;

        @Column
        public boolean booleanField;
    }

    /**
     * Mock model to test joins with same names.
     * Extends from ParentJoinMockModel to have the same columns.
     * Have a relationship with ParentJoinMockModel to make te join query.
     */
    @Table(name = "ChildMockModel")
    public static class ChildMockModel extends ParentJoinMockModel {
        @Column
        ParentJoinMockModel parent;
    }
}
