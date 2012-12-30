package com.activeandroid.migration.operation;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.migration.MigrationOperation;
import com.activeandroid.util.SQLiteUtils;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddColumn extends MigrationOperation {

    Field mField;

    public AddColumn(Class<? extends Model> model, Field column) {
        super(model);
        mField = column;
    }

    @Override
    public List<String> toSqlString() {
        return Arrays.asList( MessageFormat.format(
                "ALTER TABLE {0} ADD COLUMN {1}",
                Cache.getTableName(mModel),
                SQLiteUtils.createColumnDefinition(Cache.getTableInfo(mModel), mField))
        );
    }
}
