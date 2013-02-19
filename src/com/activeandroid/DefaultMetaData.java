package com.activeandroid;

import android.content.Context;

import com.activeandroid.util.ReflectionUtils;

public class DefaultMetaData extends DbMetaData {

    //////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE CONSTANTS
    //////////////////////////////////////////////////////////////////////////////////////

    private final static String AA_DB_NAME = "AA_DB_NAME";
    private final static String AA_DB_VERSION = "AA_DB_VERSION";

    private final static String MIGRATION_PATH = "migrations";

    private Context mContext;

    //////////////////////////////////////////////////////////////////////////////////////
    // SINGLETON
    //////////////////////////////////////////////////////////////////////////////////////

    private static DefaultMetaData sInstance;

    public synchronized static DefaultMetaData getInstanse(Context context) {
        if (sInstance == null) {
            sInstance = new DefaultMetaData(context);
        }
        return sInstance;
    }

    // hide constructor
    private DefaultMetaData(Context context) {
        mContext = context;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // OVERRIDEN METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int getDatabaseVersion() {
        Integer aaVersion = ReflectionUtils.getMetaData(mContext, AA_DB_VERSION);

        if (aaVersion == null || aaVersion == 0) {
            aaVersion = 1;
        }

        return aaVersion;
    }

    @Override
    public String getDatabaseName() {
        String aaName = ReflectionUtils.getMetaData(mContext, AA_DB_NAME);

        if (aaName == null) {
            aaName = "Application.db";
        }

        return aaName;
    }

    @Override
    public String getMigrationPath() {
        return MIGRATION_PATH;
    }

}
