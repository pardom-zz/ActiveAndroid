package com.activeandroid.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.SparseArray;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.TableInfo;
import com.activeandroid.util.ReflectionUtils;

@SuppressLint("DefaultLocale")
public class ContentProvider extends android.content.ContentProvider {
    //////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE CONSTANTS
    //////////////////////////////////////////////////////////////////////////////////////

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    private static final SparseArray<Class<? extends Model>> TYPE_CODES = new SparseArray<Class<? extends Model>>();

    //////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    //////////////////////////////////////////////////////////////////////////////////////

    private final String mAuthority = getContext().getPackageName();

    //////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreate() {
        ActiveAndroid.initialize((Application) getContext().getApplicationContext());

        List<TableInfo> tableInfos = new ArrayList<TableInfo>(Cache.getTableInfos());
        for (int i = 0; i < tableInfos.size(); i++) {
            TableInfo tableInfo = tableInfos.get(i);

            URI_MATCHER.addURI(mAuthority, tableInfo.getTableName().toLowerCase(Locale.getDefault()), i);
            TYPE_CODES.put(i, tableInfo.getType());
        }

        return true;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    // SQLite methods

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Class<? extends Model> type = getModelType(uri);
        Long id = Cache.openDatabase(ReflectionUtils.getDbMetaDataClass(type)).insert(
                Cache.getTableName(type), null, values);

        if (id != null && id > 0) {
            Uri retUri = createUri(type, id);
            notifyChange(retUri);

            return retUri;
        }

        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Class<? extends Model> type = getModelType(uri);
        int count = Cache.openDatabase(ReflectionUtils.getDbMetaDataClass(type)).update(
                Cache.getTableName(type), values, selection, selectionArgs);

        notifyChange(uri);

        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Class<? extends Model> type = getModelType(uri);
        int count = Cache.openDatabase(ReflectionUtils.getDbMetaDataClass(type)).delete(
                Cache.getTableName(type), selection, selectionArgs);

        notifyChange(uri);

        return count;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Class<? extends Model> type = getModelType(uri);
        return Cache.openDatabase(ReflectionUtils.getDbMetaDataClass(type)).query(
                Cache.getTableName(type), projection, selection, selectionArgs, null, null,
                sortOrder);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    private Class<? extends Model> getModelType(Uri uri) {
        int code = URI_MATCHER.match(uri);
        if (code != UriMatcher.NO_MATCH) {
            return TYPE_CODES.get(code);
        }

        return null;
    }

    private Uri createUri(Class<? extends Model> type, Long id) {
        return Uri.parse("content://" + mAuthority + "/" + Cache.getTableName(type).toLowerCase() + "/" + id);
    }

    private void notifyChange(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null);
    }
}