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

import com.activeandroid.Model;
import com.activeandroid.annotation.Table;
import java.util.HashSet;
import java.util.Set;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

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
	 * Equals should not consider two new objects of the same
	 * class non-equal.
	 */	
	public void testEqualsOnNew() {
		MockModel model1 = new MockModel();
		MockModel model2 = new MockModel();

		assertTrue(model1.equals(model2));
		assertTrue(model2.equals(model1));
	}

	/**
	 * Two different rows in a table should not be equal (different ids).
	 */	
	public void testEqualsDifferentRows() {
		MockModel model1 = new MockModel();
		MockModel model2 = new MockModel();

		model1.save();
		model2.save();

		assertFalse(model1.equals(model2));
		assertFalse(model2.equals(model1));
	}

	/**
	 * Tests hashcode for new instances.
	 */	
	public void testHashCode() {
		Set<Model> set = new HashSet<Model>();
		Model m1 = new MockModel();
		Model m2 = new MockModel();
		Model m3 = new AnotherMockModel();

		assertEquals(m1.hashCode(), m2.hashCode());
		set.add(m1);
		set.add(m2);
		assertEquals(1, set.size());

		assertFalse(m1.hashCode() == m3.hashCode());
		set.add(m3);
		assertEquals(2, set.size());
	}

	/**
	 * Two rows in a table should have different hashcodes.
	 */
	public void testHashCodeDifferentRows() {
		Set<Model> set = new HashSet<Model>();
		Model m1 = new MockModel();
		Model m2 = new MockModel();

		m1.save();
		m2.save();

		assertFalse(m1.hashCode() == m2.hashCode());
		set.add(m1);
		set.add(m2);
		assertEquals(2, set.size());
	}

	/**
	 * Mock model as we need 2 different model classes.
	 */
	@Table(name = "AnotherMockTable")
	public static class AnotherMockModel extends Model {}
}
