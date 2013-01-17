package com.activeandroid;

public abstract class DbMetaData {

    public abstract int getDatabaseVersion();

    public abstract String getDatabaseName();

    public abstract String getMigrationPath();

    public boolean isResettable() { return false; }
}
