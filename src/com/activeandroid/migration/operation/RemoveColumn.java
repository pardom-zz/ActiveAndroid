package com.activeandroid.migration.operation;

import android.text.TextUtils;
import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.migration.MigrationOperation;
import com.activeandroid.util.MigrationUtils;
import com.activeandroid.util.SQLiteUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RemoveColumn extends MigrationOperation {

    String mRemoveColumn;

    public RemoveColumn(Class<? extends Model> model, String column) {
        super(model);
        mRemoveColumn = column;
    }

    @Override
    public List<String> toSqlString() {
        String tableName = Cache.getTableName(mModel);

        ArrayList<String> ops = new ArrayList<String>();

        ops.addAll(new RenameTable(tableName, (tableName + "_temp")).toSqlString());
        ops.add(SQLiteUtils.createTableDefinition(Cache.getTableInfo(mModel), Arrays.asList(mRemoveColumn)));
        ops.add(MessageFormat.format("INSERT INTO {0}({1}) SELECT {1} FROM {0}_temp", tableName, MigrationUtils.columnList(mModel)));
        ops.addAll(new DropTable(tableName + "_temp").toSqlString());

        return ops;
    }

}
