package com.activeandroid.migration.operation;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.migration.MigrationOperation;
import com.activeandroid.util.SQLiteUtils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

public class DropTable extends MigrationOperation {

    String mTableName;

    public DropTable(Class<? extends Model> model) {
        super(model);
        mTableName = Cache.getTableName(mModel);
    }

    public DropTable(String tableName) {
        super(null);
        mTableName = tableName;
    }

    @Override
    public List<String> toSqlString() {
        return Arrays.asList(MessageFormat.format(
                "DROP TABLE IF EXISTS {0}",
                mTableName));
    }
}
