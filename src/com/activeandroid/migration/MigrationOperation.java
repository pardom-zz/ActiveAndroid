package com.activeandroid.migration;

import com.activeandroid.Model;

public abstract class MigrationOperation {
    protected Class<? extends Model> mModel;

    public MigrationOperation(Class<? extends Model> model) {
        mModel = model;
    }

    public abstract String toSqlString();
}
