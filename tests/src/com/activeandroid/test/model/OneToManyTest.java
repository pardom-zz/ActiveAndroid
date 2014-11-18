package com.activeandroid.test.model;

import java.util.ArrayList;
import java.util.List;

import com.activeandroid.Model;
import com.activeandroid.model.OneToManyTypedRelation;
import com.activeandroid.test.MockModel;

public class OneToManyTest extends ModelTestCase {
	
	public static class MockOneToManyRelation extends OneToManyTypedRelation<MockModel> {
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
		
		OneToManyTypedRelation.setRelations(MockOneToManyRelation.class, mockModelsHolder, mockModels);
		
		mockModels = OneToManyTypedRelation.getRelations(MockOneToManyRelation.class, mockModelsHolder);
		assertTrue(mockModels.size() == 5);
	}
}
