package edu.bsu.android.apps.geofence.content.layout;

import android.net.Uri;
import android.provider.BaseColumns;

public interface SimpleGeofenceColumns extends BaseColumns { 

	public static final String TABLE_NAME = "simple_geofence";
	public static final Uri CONTENT_URI = Uri.parse("content://edu.bsu.android.apps.geofence/" + TABLE_NAME);
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.bsu." + TABLE_NAME;
	public static final String CONTENT_ITEMTYPE = "vnd.android.cursor.item/vnd.bsu." + TABLE_NAME;
	public static final String DEFAULT_SORT_ORDER = "_id";
	
	// Columns
	public static final String LATITUDE = "latitude"; 
	public static final String LONGITUDE = "longitude"; 
	public static final String NAME = "name"; 
	public static final String RADIUS = "radius"; 
	public static final String EXPRIATION_DURATION = "expirationDuration"; 
	public static final String TRANSITION_TYPE = "transitionType"; 

	public static final String CREATE_TABLE = 
			"CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + 
				_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				LATITUDE + " INTEGER," + 
				LONGITUDE + " INTEGER," + 
				NAME + " TEXT," + 
				RADIUS + " INTEGER," + 
				EXPRIATION_DURATION + " INTEGER," + 
				TRANSITION_TYPE + " INTEGER" + 
			");";
}