package com.activeandroid.test.model;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.activeandroid.test.ActiveAndroidTestCase;

public class ModelTestCase extends ActiveAndroidTestCase {
	@Override
	protected void setUp() throws Exception {
		Configuration configuration = new Configuration.Builder(getContext())
        .setDatabaseName("model.db")
        .setDatabaseVersion(2)
        .create();
		ActiveAndroid.initialize(configuration, true);
	}
}
