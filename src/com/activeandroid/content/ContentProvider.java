package com.activeandroid.content;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.SparseArray;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.Configuration;
import com.activeandroid.Model;
import com.activeandroid.TableInfo;

public class ContentProvider extends android.content.ContentProvider {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	private static final SparseArray<Class<? extends Model>> TYPE_CODES = new SparseArray<Class<? extends Model>>();

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private static String sAuthority;
	private static SparseArray<String> sMimeTypeCache = new SparseArray<String>();

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean onCreate() {
		ActiveAndroid.initialize(getConfiguration());
		sAuthority = getAuthority();

		final List<TableInfo> tableInfos = new ArrayList<TableInfo>(Cache.getTableInfos());
		final int size = tableInfos.size();
		for (int i = 0; i < size; i++) {
			final TableInfo tableInfo = tableInfos.get(i);
			final int tableKey = (i * 2) + 1;
			final int itemKey = (i * 2) + 2;

			// content://<authority>/<table>
			URI_MATCHER.addURI(sAuthority, tableInfo.getTableName().toLowerCase(), tableKey);
			TYPE_CODES.put(tableKey, tableInfo.getType());

			// content://<authority>/<table>/<id>
			URI_MATCHER.addURI(sAuthority, tableInfo.getTableName().toLowerCase() + "/#", itemKey);
			TYPE_CODES.put(itemKey, tableInfo.getType());
		}

		return true;
	}

	@Override
	public String getType(Uri uri) {
		final int match = URI_MATCHER.match(uri);

		String cachedMimeType = sMimeTypeCache.get(match);
		if (cachedMimeType != null) {
			return cachedMimeType;
		}

		final Class<? extends Model> type = getModelType(uri);
		final boolean single = ((match % 2) == 0);

		StringBuilder mimeType = new StringBuilder();
		mimeType.append("vnd");
		mimeType.append(".");
		mimeType.append(sAuthority);
		mimeType.append(".");
		mimeType.append(single ? "item" : "dir");
		mimeType.append("/");
		mimeType.append("vnd");
		mimeType.append(".");
		mimeType.append(sAuthority);
		mimeType.append(".");
		mimeType.append(Cache.getTableName(type));

		sMimeTypeCache.append(match, mimeType.toString());

		return mimeType.toString();
	}

	// SQLite methods

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final Class<? extends Model> type = getModelType(uri);
		final Long id = Cache.openDatabase().insert(Cache.getTableName(type), null, values);

		if (id != null && id > 0) {
			Uri retUri = createUri(type, id);
			notifyChange(retUri);

			return retUri;
		}

		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		final Class<? extends Model> type = getModelType(uri);
		final int count = Cache.openDatabase().update(Cache.getTableName(type), values, selection, selectionArgs);

		notifyChange(uri);

		return count;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		final Class<? extends Model> type = getModelType(uri);
		final int count = Cache.openDatabase().delete(Cache.getTableName(type), selection, selectionArgs);

		notifyChange(uri);

		return count;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final Class<? extends Model> type = getModelType(uri);
		final Cursor cursor = Cache.openDatabase().query(
				Cache.getTableName(type),
				projection,
				selection,
				selectionArgs,
				null,
				null,
				sortOrder);

		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public static Uri createUri(Class<? extends Model> type, Long id) {
		final StringBuilder uri = new StringBuilder();
		uri.append("content://");
		uri.append(sAuthority);
		uri.append("/");
		uri.append(Cache.getTableName(type).toLowerCase());

		if (id != null) {
			uri.append("/");
			uri.append(id.toString());
		}

		return Uri.parse(uri.toString());
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	protected String getAuthority() {
		return getContext().getPackageName();
	}

	protected Configuration getConfiguration() {
		return new Configuration.Builder(getContext()).create();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private Class<? extends Model> getModelType(Uri uri) {
		final int code = URI_MATCHER.match(uri);
		if (code != UriMatcher.NO_MATCH) {
			return TYPE_CODES.get(code);
		}

		return null;
	}

	private void notifyChange(Uri uri) {
		getContext().getContentResolver().notifyChange(uri, null);
	}
}
