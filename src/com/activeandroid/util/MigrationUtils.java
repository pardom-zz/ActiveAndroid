package com.activeandroid.util;

import android.app.Application;
import android.content.Context;
import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.migration.Migration;
import com.activeandroid.migration.MigrationOperation;

import java.io.IOException;
import java.lang.reflect.Field;
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

    public static List<String> migrationOperationsToSql(MigrationOperation[] ops) {
        if (ops == null) return null;

        ArrayList<String> statements = new ArrayList<String>();
        for (int i = 0; i < ops.length; i++) {
            statements.addAll(ops[i].toSqlString());
        }

        return statements;
    }

    public static String renamedColumnList(Class<? extends Model> model, String oldColumnName, String newColumnName) {
        ArrayList<Field> fields = new ArrayList<Field>(Cache.getTableInfo(model).getFields());
        if (fields.isEmpty()) return "";

        String columns = "";
        String column = Cache.getTableInfo(model).getColumnName(fields.get(0));

        columns += column.equals(oldColumnName) ? newColumnName : column;
        for (int i = 1; i < fields.size(); i++) {
            column = Cache.getTableInfo(model).getColumnName(fields.get(i));
            columns += ", ";
            columns += column.equals(oldColumnName) ? newColumnName : column;
        }

        return columns;
    }

    public static String columnList(Class<? extends Model> model) {
        ArrayList<Field> fields = new ArrayList<Field>(Cache.getTableInfo(model).getFields());
        if (fields.isEmpty()) return "";

        String columns = Cache.getTableInfo(model).getColumnName(fields.get(0));
        for (int i = 1; i < fields.size(); i++) {
            columns += ", " + Cache.getTableInfo(model).getColumnName(fields.get(i));
        }

        return columns;
    }

}
