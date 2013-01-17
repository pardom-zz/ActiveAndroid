package com.activeandroid.test.multidb;

import com.activeandroid.DbMetaData;

public class Db2MetaData extends DbMetaData {

    public Db2MetaData() {}

    public Db2MetaData(String dbName) {
        mDbName = dbName;
    }

    private String mDbName = "db2";

    @Override
    public int getDatabaseVersion() {
        return 1;
    }

    @Override
    public String getDatabaseName() {
        return mDbName;
    }

    @Override
    public String getMigrationPath() {
        return "migration/db2";
    }

    @Override
    public boolean isResettable() {
        return true;
    }

}
