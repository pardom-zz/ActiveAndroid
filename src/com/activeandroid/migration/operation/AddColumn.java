package com.activeandroid.migration.operation;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.migration.MigrationOperation;
import com.activeandroid.util.SQLiteUtils;

import java.lang.reflect.Field;
import java.text.MessageFormat;

public class AddColumn extends MigrationOperation {

    Field mField;

    public AddColumn(Class<? extends Model> model, Field column) {
        super(model);
        mField = column;
    }

    @Override
    public String toSqlString() {
        return MessageFormat.format(
                "ALTER TABLE {0} ADD COLUMN {1}",
                Cache.getTableName(mModel),
                SQLiteUtils.createColumnDefinition(Cache.getTableInfo(mModel), mField));
    }
}
