package com.activeandroid.migration;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.activeandroid.util.Log;
import com.activeandroid.util.MigrationUtils;

import java.util.List;

public abstract class Migration {

    public abstract MigrationOperation[] getMigrations() throws Exception;
    public abstract int databaseVersion();

    public void execute(SQLiteDatabase db) {
        try {
            List<String> sqlStatements = MigrationUtils.migrationOperationsToSql(getMigrations());

            for (String statement : sqlStatements) {
                try {
                    db.execSQL(statement);
                } catch (SQLException e) {
                    Log.e("ActiveAndroid: Error running generated SQL statement for migration: " + statement + ".");
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
