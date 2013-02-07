package com.activeandroid.migration.operation;

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
import java.util.HashMap;
import java.util.List;

public class RenameColumn extends MigrationOperation {

    String mOldColumnName;
    String mNewColumnName;

    public RenameColumn(Class<? extends Model> model, String oldName, String newName) {
        super(model);
        mOldColumnName = oldName;
        mNewColumnName = newName;
    }

    @Override
    public List<String> toSqlString() {
        String tableName = Cache.getTableName(mModel);

        ArrayList<String> ops = new ArrayList<String>();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(mOldColumnName, mNewColumnName);

        ops.addAll(new RenameTable(tableName, (tableName + "_temp")).toSqlString());
        ops.add(SQLiteUtils.createTableDefinition(Cache.getTableInfo(mModel), new ArrayList<String>(), map));
        ops.add(MessageFormat.format("INSERT INTO {0}({1}) SELECT {2} FROM {0}_temp",
                tableName,
                MigrationUtils.renamedColumnList(mModel, mOldColumnName, mNewColumnName),
                MigrationUtils.columnList(mModel)));
        ops.addAll(new DropTable(tableName + "_temp").toSqlString());

        return ops;
    }


}
