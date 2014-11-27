package com.activeandroid.test.model;

import java.util.ArrayList;
import java.util.List;

import com.activeandroid.Model;
import com.activeandroid.model.OneToManyRelation;
import com.activeandroid.test.MockModel;

public class OneToManyTest extends ModelTestCase {
	
	public static class MockOneToManyRelation extends OneToManyRelation<MockModel> {
		public MockOneToManyRelation() {
			super();
		}
	}
	
	public void testOneToManyRelation() {
		
		MockModel mockModelsHolder = new MockModel();
		mockModelsHolder.save();
		
		List<Model> mockModels = new ArrayList<Model>();
		for (int i = 0; i < 5; ++i) {
			MockModel mockModel = new MockModel();
			mockModel.save();
			mockModels.add(mockModel);
		}
		
		OneToManyRelation.setRelations(MockOneToManyRelation.class, mockModelsHolder, mockModels);
		
		mockModels = OneToManyRelation.getRelations(MockOneToManyRelation.class, mockModelsHolder);
		assertTrue(mockModels.size() == 5);
	}
}
