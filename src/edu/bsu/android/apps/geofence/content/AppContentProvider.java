package edu.bsu.android.apps.geofence.content;

import edu.bsu.android.apps.geofence.R;
import edu.bsu.android.apps.geofence.content.layout.SimpleGeofenceColumns;
import edu.bsu.android.apps.geofence.utils.PreferencesUtils;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

public class AppContentProvider extends ContentProvider {
	private static final String TAG = AppContentProvider.class.getSimpleName();
	
	private static final String AUTHORITY = "edu.bsu.android.apps.geofence";
	private static final String DATABASE_NAME = "geofence.db";
	private static final int DATABASE_VERSION = 1;
	
	private final UriMatcher uriMatcher;
	private static SQLiteDatabase db;
	
	/**
	 * Database helper for creating and upgrading the databasae.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SimpleGeofenceColumns.CREATE_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {	
			Log.i("***> Database upgrade", "old version: " + oldVersion + ", new version: " + newVersion);
		}
	}

	/**
	 * Types of url.
	 * 
	 * @author Jimmy Shih
	 */
	private enum UrlType {
		SIMPLE_GEOFENCE, SIMPLE_GEOFENCE_ID
	}

	public AppContentProvider() {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		uriMatcher.addURI(AUTHORITY, SimpleGeofenceColumns.TABLE_NAME, UrlType.SIMPLE_GEOFENCE.ordinal());
		uriMatcher.addURI(AUTHORITY, SimpleGeofenceColumns.TABLE_NAME + "/#", UrlType.SIMPLE_GEOFENCE_ID.ordinal());
	}
	@Override
	public boolean onCreate() {
		if (!canAccess()) {
			return false;
		}
		
		DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
		
		try {
			db = databaseHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			Log.e(TAG, "Unable to open database for writing", e);
		}
		return db != null;
	}

	@Override
	public int delete(Uri url, String where, String[] selectionArgs) {
		if (!canAccess()) {
			return 0;
		}
		
		String table;
		
		switch (getUrlType(url)) {
			case SIMPLE_GEOFENCE:
				table = SimpleGeofenceColumns.TABLE_NAME;
				break;
			default:
				throw new IllegalArgumentException("Unknown URL " + url);
		}

		Log.w(TAG, "Deleting table " + table);
		int count = 0;
		try {
			db.beginTransaction();
			count = db.delete(table, where, selectionArgs);
			db.setTransactionSuccessful();
		} catch (Exception ex) {
			Log.e("error", ex.getMessage());
			ex.printStackTrace();			
		} finally {
			db.endTransaction();
		}
		getContext().getContentResolver().notifyChange(url, null, true);

		return count;
	}

	@Override
	public String getType(Uri url) {
		if (!canAccess()) {
			return null;
		}
		
		switch (getUrlType(url)) {
			case SIMPLE_GEOFENCE:
				return SimpleGeofenceColumns.CONTENT_TYPE;
			case SIMPLE_GEOFENCE_ID:
				return SimpleGeofenceColumns.CONTENT_ITEMTYPE;
			default:
				throw new IllegalArgumentException("Unknown URL " + url);
		}
	}

	@Override
	public Uri insert(Uri url, ContentValues initialValues) {
		if (!canAccess()) {
			return null;
		}
		if (initialValues == null) {
			initialValues = new ContentValues();
		}
		Uri result = null;
		try {
			db.beginTransaction();
			result = insertContentValues(url, getUrlType(url), initialValues);
			db.setTransactionSuccessful();
		} catch (Exception ex) {
			Log.e("error", ex.getMessage());
			ex.printStackTrace();			
		} finally {
			db.endTransaction();
		}
		return result;
	}

	@Override
	public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sort) {
		if (!canAccess()) {
			return null;
		}

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		String sortOrder = null;
		switch (getUrlType(url)) {
			case SIMPLE_GEOFENCE:
				queryBuilder.setTables(SimpleGeofenceColumns.TABLE_NAME);
				sortOrder = sort != null ? sort : SimpleGeofenceColumns.DEFAULT_SORT_ORDER;
				break;
			case SIMPLE_GEOFENCE_ID:
				queryBuilder.setTables(SimpleGeofenceColumns.TABLE_NAME);
				queryBuilder.appendWhere(SimpleGeofenceColumns._ID + " = " + url.getPathSegments().get(1));
				break;
			default:
				throw new IllegalArgumentException("Unknown URL " + url);
		}

		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), url);
		return cursor;
	}

	@Override
	public int update(Uri url, ContentValues values, String where, String[] selectionArgs) {
		if (!canAccess()) {
			return 0;
		}
		String table;
		String whereClause;
		switch (getUrlType(url)) {
			case SIMPLE_GEOFENCE:
				table = SimpleGeofenceColumns.TABLE_NAME;
				whereClause = where;
				break;
			case SIMPLE_GEOFENCE_ID:
				table = SimpleGeofenceColumns.TABLE_NAME;
				whereClause = SimpleGeofenceColumns._ID + "=" + url.getPathSegments().get(1);
				if (!TextUtils.isEmpty(where)) {
					whereClause += " AND (" + where + ")";
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown URL " + url);
		}

		int count = 0;
		try {
			db.beginTransaction();
			count = db.update(table, values, whereClause, selectionArgs);
			db.setTransactionSuccessful();
		} catch (Exception ex) {
			Log.i("update", ex.getMessage());
			ex.printStackTrace();
		} finally {
			db.endTransaction();
		}
		getContext().getContentResolver().notifyChange(url, null, true);
		return count;
	}

	/**
	 * Returns true if the caller can access the content provider.
	 */
	private boolean canAccess() {
		if (Binder.getCallingPid() == Process.myPid()) {
			return true;
		} else {
			return PreferencesUtils.getBoolean(getContext(), R.string.allow_access_key, PreferencesUtils.ALLOW_ACCESS_DEFAULT);
		}
	}

	/**
	 * Gets the {@link UrlType} for a url.
	 * 
	 * @param url the url
	 */
	private UrlType getUrlType(Uri url) {
		return UrlType.values()[uriMatcher.match(url)];
	}

	/**
	 * Inserts a content based on the url type.
	 * 
	 * @param url the content url
	 * @param urlType the url type
	 * @param contentValues the content values
	 */
	private Uri insertContentValues(Uri url, UrlType urlType, ContentValues contentValues) {
		switch (urlType) {
			case SIMPLE_GEOFENCE:
				return insertSimpleGeofence(url, contentValues);
			default:
				throw new IllegalArgumentException("Unknown url " + url);
		}
	}

	/**
	 * Inserts an invitation object.
	 * 
	 * @param url the content url
	 * @param contentValues the content values
	 */
	private Uri insertSimpleGeofence(Uri url, ContentValues contentValues) {
		boolean hasLatitude = contentValues.containsKey(SimpleGeofenceColumns.LATITUDE);
		boolean hasLongitude = contentValues.containsKey(SimpleGeofenceColumns.LONGITUDE);
		boolean hasRadius = contentValues.containsKey(SimpleGeofenceColumns.RADIUS);

		if (!hasLatitude || !hasLongitude || !hasRadius) {
			throw new IllegalArgumentException("Latitude, Longitude, and Radius values are required.");
		}

		long rowId = db.insert(SimpleGeofenceColumns.TABLE_NAME, SimpleGeofenceColumns._ID, contentValues);

		if (rowId >= 0) {
			Uri uri = ContentUris.appendId(SimpleGeofenceColumns.CONTENT_URI.buildUpon(), rowId).build();
			getContext().getContentResolver().notifyChange(url, null, true);
			return uri;
		}

		throw new SQLiteException("Failed to insert row into " + url);
	}
}
