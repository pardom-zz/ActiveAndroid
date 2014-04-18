package com.activeandroid.test;

import com.activeandroid.Configuration;
import com.activeandroid.Model;
import com.activeandroid.annotation.Table;

import android.test.AndroidTestCase;

import java.io.IOException;
import java.util.List;

public class ConfigurationTest extends AndroidTestCase {

    public void testDefaultValue() throws IOException, ClassNotFoundException {
        Configuration conf = new Configuration.Builder(getContext()).create();
        assertNotNull(conf.getContext());
        assertEquals(1024, conf.getCacheSize());
        assertEquals("Application.db", conf.getDatabaseName());
        assertEquals(1, conf.getDatabaseVersion());
        assertNull(conf.getModelClasses());
        assertFalse(conf.isValid());
        assertNull(conf.getTypeSerializers());
        assertEquals(Configuration.SQL_PARSER_LEGACY, conf.getSqlParser());
    }

    public void testCreateConfigurationWithMockModel() {
        Configuration conf = new Configuration.Builder(getContext())
                .addModelClass(ConfigurationTestModel.class)
                .create();
        List<Class<? extends Model>> modelClasses = conf.getModelClasses();
        assertEquals(1, modelClasses.size());
        assertTrue(conf.isValid());
    }

    @Table(name = "ConfigurationTestModel")
    private static class ConfigurationTestModel extends Model {
    }
}
