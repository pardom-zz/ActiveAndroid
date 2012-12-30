package com.activeandroid.migration.operation;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.migration.MigrationOperation;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

public class RemoveIndex extends MigrationOperation {

    Field mField;

    public RemoveIndex(Class<? extends Model> model, Field column) {
        super(model);
        mField = column;
    }

    @Override
    public List<String> toSqlString() {
        String columnName = Cache.getTableInfo(mModel).getColumnName(mField);
        if (columnName == null) return null;

        return Arrays.asList(MessageFormat.format(
                "DROP INDEX IF EXISTS {0}",
                (columnName + "_index")));
    }
}
