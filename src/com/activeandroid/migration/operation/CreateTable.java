package com.activeandroid.migration.operation;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.migration.MigrationOperation;
import com.activeandroid.util.SQLiteUtils;

import java.util.Arrays;
import java.util.List;

public class CreateTable extends MigrationOperation {

    public CreateTable(Class<? extends Model> model) {
        super(model);
    }

    @Override
    public List<String> toSqlString() {
        return Arrays.asList(SQLiteUtils.createTableDefinition(Cache.getTableInfo(mModel)));
    }
}
