package com.activeandroid.migration.operation;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.migration.MigrationOperation;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

public class RenameTable extends MigrationOperation {

    String mNewTableName;
    String mOldTableName;

    public RenameTable(Class<? extends Model> model, String oldTableName) {
        super(model);
        mNewTableName = Cache.getTableName(mModel);
        mOldTableName = oldTableName;
    }

    public RenameTable(String oldTableName, String newTableName) {
        super(null);
        mNewTableName = newTableName;
        mOldTableName = oldTableName;
    }

    @Override
    public List<String> toSqlString() {
        return Arrays.asList(MessageFormat.format(
                "ALTER TABLE {0} RENAME TO {1}",
                mOldTableName,
                mNewTableName));
    }
}
