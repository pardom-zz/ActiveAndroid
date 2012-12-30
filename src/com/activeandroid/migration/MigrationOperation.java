package com.activeandroid.migration;

import com.activeandroid.Model;

import java.util.List;

public abstract class MigrationOperation {
    protected Class<? extends Model> mModel;

    public MigrationOperation(Class<? extends Model> model) {
        mModel = model;
    }

    public abstract List<String> toSqlString();
}
