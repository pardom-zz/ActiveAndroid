package com.activeandroid.test.multidb;

import android.database.sqlite.SQLiteDatabase;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.DbMetaData;
import com.activeandroid.DefaultMetaData;
import com.activeandroid.test.ActiveAndroidTestCase;

public class Db2TestCase extends ActiveAndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DbMetaData meta = new Db2MetaData();
        ActiveAndroid.registerDbMetaData(getApplication(), meta);
    }

    public void testRegisterDb2() {
        assertNotNull(ActiveAndroid.getDatabase(Db2MetaData.class));
        assertNotNull(ActiveAndroid.getDatabase(Db2MockModel.class));
        assertSame(ActiveAndroid.getDatabase(Db2MockModel.class),
                   ActiveAndroid.getDatabase(Db2MetaData.class));
        assertNotSame(ActiveAndroid.getDatabase(Db2MockModel.class),
                      ActiveAndroid.getDatabase(DefaultMetaData.class));
        assertEquals("/data/data/com.activeandroid.test/databases/db2",
                ActiveAndroid.getDatabase(Db2MockModel.class).getPath());
    }

    public void testResetDb2() {
        DbMetaData meta = new Db2MetaData("db3");
        ActiveAndroid.registerDbMetaData(getApplication(), meta);

        SQLiteDatabase db = ActiveAndroid.getDatabase(Db2MetaData.class);
        assertEquals("/data/data/com.activeandroid.test/databases/db3", db.getPath());
    }

}
