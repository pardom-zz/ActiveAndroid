package com.activeandroid.util;

import android.app.Application;
import android.content.Context;
import com.activeandroid.migration.Migration;
import com.activeandroid.migration.MigrationOperation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MigrationUtils {

    private final static String AA_MIGRATION_PACKAGE = "AA_MIGRATION_PACKAGE";

    public static List<Migration> getMigrations(Context context, int oldVersion, int newVersion) {

        ArrayList<Migration> migrations = new ArrayList<Migration>();

        try {

            String migrationPackage = ReflectionUtils.getMetaData(context, AA_MIGRATION_PACKAGE);
            Class[] migrationClasses = ReflectionUtils.findClasses(migrationPackage);

            for(Class c : migrationClasses) {
                if (c.isAssignableFrom(Migration.class)) {
                    Migration m = (Migration)c.newInstance();
                    if (m.databaseVersion() > oldVersion && m.databaseVersion() <= newVersion) {
                        migrations.add(m);
                    }
                }
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return migrations;
    }

    public static String[] migrationOperationsToSql(MigrationOperation[] ops) {
        if (ops == null) return null;

        ArrayList<String> statements = new ArrayList<String>(ops.length);
        for (MigrationOperation op : ops) {
            statements.add(op.toSqlString());
        }
        return (String[]) statements.toArray();
    }

}
