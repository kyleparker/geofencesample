package edu.bsu.android.apps.geofence.content;

import java.util.ArrayList;

import edu.bsu.android.apps.geofence.content.layout.SimpleGeofenceColumns;
import edu.bsu.android.apps.geofence.objects.SimpleGeofence;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class ContentProviderUtilsImpl implements ContentProviderUtils {
	private final ContentResolver contentResolver;

	public ContentProviderUtilsImpl(ContentResolver contentResolver) { 
		this.contentResolver = contentResolver;
	}

	@Override
	public ContentValues createContentValues(SimpleGeofence simpleGeofence) {
		ContentValues values = new ContentValues();

		// Values id < 0 indicate no id is available:
		if (simpleGeofence.getId() > 0) {
			values.put(SimpleGeofenceColumns._ID, simpleGeofence.getId());
		}

		values.put(SimpleGeofenceColumns.EXPRIATION_DURATION, simpleGeofence.getExpirationDuration());
		values.put(SimpleGeofenceColumns.LATITUDE, simpleGeofence.getLatitude());
		values.put(SimpleGeofenceColumns.LONGITUDE, simpleGeofence.getLongitude());
		values.put(SimpleGeofenceColumns.NAME, simpleGeofence.getName());
		values.put(SimpleGeofenceColumns.RADIUS, simpleGeofence.getRadius());
		values.put(SimpleGeofenceColumns.TRANSITION_TYPE, simpleGeofence.getTransitionType());

		return values;
	}

	@Override
	public SimpleGeofence createSimpleGeofence(Cursor cursor) {
		int idxId = cursor.getColumnIndex(SimpleGeofenceColumns._ID);
		int idxExpirationDuration = cursor.getColumnIndex(SimpleGeofenceColumns.EXPRIATION_DURATION);
		int idxLatitude = cursor.getColumnIndex(SimpleGeofenceColumns.LATITUDE);
		int idxLongitude = cursor.getColumnIndex(SimpleGeofenceColumns.LONGITUDE);
		int idxName = cursor.getColumnIndex(SimpleGeofenceColumns.NAME);
		int idxRadius = cursor.getColumnIndex(SimpleGeofenceColumns.RADIUS);
		int idxTransitionType = cursor.getColumnIndex(SimpleGeofenceColumns.TRANSITION_TYPE);

		SimpleGeofence simpleGeofence = new SimpleGeofence();

		if (idxId > -1) {
			simpleGeofence.setId(cursor.getLong(idxId));
		}
		if (idxExpirationDuration > -1) {
			simpleGeofence.setExpirationDuration(cursor.getLong(idxExpirationDuration));
		}
		if (idxLatitude > -1) {
			simpleGeofence.setLatitude(cursor.getDouble(idxLatitude));
		}
		if (idxLongitude > -1) {
			simpleGeofence.setLongitude(cursor.getDouble(idxLongitude));
		}
		if (idxName > -1) {
			simpleGeofence.setName(cursor.getString(idxName));
		}
		if (idxRadius > -1) {
			simpleGeofence.setRadius(cursor.getFloat(idxRadius));
		}
		if (idxTransitionType > -1) {
			simpleGeofence.setTransitionType(cursor.getInt(idxTransitionType));
		}

		return simpleGeofence;
	}

	@Override
	public Uri insertSimpleGeofence(SimpleGeofence simpleGeofence) {
		return contentResolver.insert(SimpleGeofenceColumns.CONTENT_URI, createContentValues(simpleGeofence));
	}

	@Override
	public ArrayList<SimpleGeofence> getList() {
		ArrayList<SimpleGeofence> simpleGeofence = new ArrayList<SimpleGeofence>();

		Cursor cursor = contentResolver.query(SimpleGeofenceColumns.CONTENT_URI, null, null, null, null);
		
		if (cursor != null) {
			simpleGeofence.ensureCapacity(cursor.getCount());
			
			if (cursor.moveToFirst()) {
				do {
					simpleGeofence.add(createSimpleGeofence(cursor));
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		
		return simpleGeofence;
	}

	@Override
	public SimpleGeofence getById(long id) {
		if (id < 0) {
			return null;
		}

		String selection = SimpleGeofenceColumns.TABLE_NAME + "." + SimpleGeofenceColumns._ID + " = ?";
		String[] selectionArgs = new String[] { Long.toString(id) };

		Cursor cursor = contentResolver.query(SimpleGeofenceColumns.CONTENT_URI, null, selection, selectionArgs, null);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					return createSimpleGeofence(cursor);
				}
			} catch (RuntimeException e) {
				e.printStackTrace();
			} finally {
				cursor.close();
			}
		}
		
		return null;
	}

	@Override
	public void updateSimpleGeofence(SimpleGeofence simpleGeofence) {
		contentResolver.update(SimpleGeofenceColumns.CONTENT_URI, createContentValues(simpleGeofence), 
				SimpleGeofenceColumns._ID + "=" + simpleGeofence.getId(), null);
	}

	@Override
	public void deleteSimpleGeofence(SimpleGeofence simpleGeofence) {
		contentResolver.delete(SimpleGeofenceColumns.CONTENT_URI, SimpleGeofenceColumns._ID + "=" + simpleGeofence.getId(), null);
	}
}
