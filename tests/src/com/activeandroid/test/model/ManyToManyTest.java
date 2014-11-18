package com.activeandroid.test.model;

import java.util.ArrayList;
import java.util.List;

import com.activeandroid.Model;
import com.activeandroid.model.ManyToManyRelation;
import com.activeandroid.test.MockModel;

public class ManyToManyTest extends ModelTestCase {
	
	public static class MockModelList extends MockModel {
		
	}
	
	public static class MockManyToManyRelation extends ManyToManyRelation<MockModelList, MockModel> {

		@Override
		public Class<MockModelList> getEntity1Class() {
			return MockModelList.class;
		}

		@Override
		public Class<MockModel> getEntity2Class() {
			return MockModel.class;
		}
	}
	
	public void testManyToManyRelationForward() throws Exception {
		List<MockModel> mockModels = createChildEntities(MockModel.class);
		List<MockModelList> mockModelLists = new ArrayList<ManyToManyTest.MockModelList>();
		for (int i = 0; i < 3; ++i) {
			MockModelList mockModelList = new MockModelList();
			mockModelList.save();
			ManyToManyRelation.setRelationsF(MockManyToManyRelation.class, mockModelList, mockModels);
			mockModelLists.add(mockModelList);
		}
		
		for (MockModelList list : mockModelLists) {
			verifyMockModels(ManyToManyRelation.getRelationsF(MockManyToManyRelation.class, list), MockModel.class);			
		}
	}
	
	public void testManyToManyRelationReverse() throws Exception {
		List<MockModelList> mockModelLists = createChildEntities(MockModelList.class);
		List<MockModel> mockModels = new ArrayList<MockModel>();
		for (int i = 0; i < 3; ++i) {
			MockModel mockModel = new MockModel();
			mockModel.save();
			ManyToManyRelation.setRelationsR(MockManyToManyRelation.class, mockModel, mockModelLists);
			mockModels.add(mockModel);
		}
		for (MockModel mockModel : mockModels) {
			verifyMockModels(ManyToManyRelation.getRelationsR(MockManyToManyRelation.class, mockModel), MockModelList.class);			
		}
	}
		
	private void verifyMockModels(List<? extends MockModel> mockModels, Class<? extends MockModel> clazz) {
		assertTrue(mockModels.size() == 5);
		for (int i = 0; i < 5; ++i) {
			MockModel mockModel = mockModels.get(i);
			assertTrue(mockModel.getClass() == clazz);
			assertTrue(mockModel.intField == i);
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends Model> List<T> createChildEntities(Class<? extends MockModel> clazz) throws IllegalAccessException, InstantiationException {
		List<T> mockModels = new ArrayList<T>();
		for (int i = 0; i < 5; ++i) {
			MockModel mockModel = clazz.newInstance();
			mockModel.intField = i;
			mockModel.save();
			mockModels.add((T) mockModel);
		}
		return mockModels;
	}
}
