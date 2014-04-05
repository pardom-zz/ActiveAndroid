package com.activeandroid.test;

import android.test.AndroidTestCase;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.Configuration;
import com.activeandroid.Model;
import com.activeandroid.TableInfo;
import com.activeandroid.annotation.Table;

import java.util.Collection;

public class CacheTest extends AndroidTestCase {

    @Override
    protected void setUp() {
        Configuration conf = new Configuration.Builder(getContext())
                .setDatabaseName("CacheTest")
                .addModelClasses(CacheTestModel.class, CacheTestModel2.class)
                .create();
        ActiveAndroid.initialize(conf, true);
    }

    public void testGetTableInfos() {
        assertNotNull(Cache.getContext());
        Collection<TableInfo> tableInfos = Cache.getTableInfos();
        assertEquals(2, tableInfos.size());

        {
            TableInfo tableInfo = Cache.getTableInfo(CacheTestModel.class);
            assertNotNull(tableInfo);
            assertEquals("CacheTestModel", tableInfo.getTableName());
        }

        {
            TableInfo tableInfo = Cache.getTableInfo(CacheTestModel2.class);
            assertNotNull(tableInfo);
            assertEquals("CacheTestModel2", tableInfo.getTableName());
        }
    }

    @Table(name = "CacheTestModel")
    private static class CacheTestModel extends Model {
    }

    @Table(name = "CacheTestModel2")
    private static class CacheTestModel2 extends Model {
    }
}
