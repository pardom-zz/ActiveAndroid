package com.activeandroid.migration.operation;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.migration.Migration;
import com.activeandroid.migration.MigrationOperation;
import com.activeandroid.util.SQLiteUtils;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

public class CreateIndex extends MigrationOperation {

    Field mField;

    public CreateIndex(Class<? extends Model> model, Field column) {
        super(model);
        mField = column;
    }

    @Override
    public List<String> toSqlString() {
        String columnName = Cache.getTableInfo(mModel).getColumnName(mField);
        if (columnName == null) return null;

        return Arrays.asList(MessageFormat.format(
                "CREATE INDEX IF NOT EXISTS {0} ON {1} ({2})",
                (columnName + "_index"),
                Cache.getTableName(mModel),
                columnName)
        );
    }
}
