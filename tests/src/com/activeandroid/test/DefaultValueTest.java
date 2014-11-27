package com.activeandroid.test;

import java.util.List;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

public class DefaultValueTest extends ApplicationTestCase<Application> {
	
	private static final int COUNT = 10;
	
	public DefaultValueTest() {
		super(Application.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createApplication();
	}

	@Table(name = "IntegerDefaultValueModel")
	public static class IntegerDefaultValueModel extends MockModel {
		
		@Column(defaultValue = "20")
		Integer defaultField;
		
		public IntegerDefaultValueModel() {
			super();
		}
	}
	
	@Table(name = "BooleanDefaultValueModel")
	public static class BooleanDefaultValueModel extends MockModel {
		
		@Column(defaultValue = "true")
		Boolean defaultField;
		
		public BooleanDefaultValueModel() {
			super();
		}
	}

	@Table(name = "StringDefaultValueModel")
	public static class StringDefaultValueModel extends MockModel {
		
		@Column(defaultValue = "Some string")
		String defaultField;
		
		public StringDefaultValueModel() {
			super();
		}
	}
	
	public void testDefaultValueInteger() {
		initializedActiveAndroid(IntegerDefaultValueModel.class);
		List<IntegerDefaultValueModel> models = insertAndSelectModels(IntegerDefaultValueModel.class);
		for (IntegerDefaultValueModel model : models) {
			assertEquals(Integer.valueOf(20), model.defaultField);
		}
	}
	
	public void testDefaultValueBoolean() {
		initializedActiveAndroid(BooleanDefaultValueModel.class);
		List<BooleanDefaultValueModel> models = insertAndSelectModels(BooleanDefaultValueModel.class);
		for (BooleanDefaultValueModel model : models) {
			assertEquals(Boolean.valueOf(true), model.defaultField);
		}
	}
	
	public void testDefaultValueString() {
		initializedActiveAndroid(StringDefaultValueModel.class);
		List<StringDefaultValueModel> models = insertAndSelectModels(StringDefaultValueModel.class);
		assertNotNull(models);
		for (StringDefaultValueModel model : models) {
			assertEquals("Some string", model.defaultField);
		}
	}
	
	private <T extends Model> List<T> insertAndSelectModels(Class<T> clazz) {
		for (int i = 0; i < COUNT; ++i) {
			T model = null;
			try {
				model = clazz.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			} 
			model.save();
		}
		
		List<T> models = new Select().from(clazz).execute();
		assertEquals(COUNT, models.size());
		return models;
	}
	
	private void initializedActiveAndroid(Class<? extends Model> clazz) {
		getContext().deleteDatabase("default_value.db");
		ActiveAndroid.dispose();
		Configuration configuration = new Configuration.Builder(getContext())
		.addModelClass(clazz)
		.setDatabaseName("default_value.db")
		.create();
		
		ActiveAndroid.initialize(configuration, true);
	}
}
