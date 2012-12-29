package com.activeandroid.migration;

import com.activeandroid.migration.operation.AddColumn;

public abstract class Migration {

    public MigrationOperation[] change() {
        return null;
    }

    public String[] executeSql() {
        return null;
    }

    public abstract int databaseVersion();
}
