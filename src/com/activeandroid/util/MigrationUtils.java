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
            ArrayList<Class> migrationClasses = ReflectionUtils.findClasses(context, migrationPackage);

            for(Class c : migrationClasses) {
                if (ReflectionUtils.isSubclassOf(c, Migration.class)) {
                    Migration m = (Migration)c.newInstance();
                    if (m.databaseVersion() > oldVersion && m.databaseVersion() <= newVersion) {
                        migrations.add(m);
                    }
                }
            }

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

        String[] statements = new String[ops.length];
        for (int i = 0; i < ops.length; i++) {
            statements[i] = ops[i].toSqlString();
        }

        return statements;
    }

}
