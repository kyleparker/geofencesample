package edu.bsu.android.apps.geofence.content;

import java.util.ArrayList;

import edu.bsu.android.apps.geofence.objects.SimpleGeofence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public interface ContentProviderUtils {

	/**
	 * Creates a SimpleGeofence object from a given cursor.
	 * 
	 * @param cursor a cursor pointing at a db or provider with simpleGeofence
	 * @return a new SimpleGeofence object
	 */
	public SimpleGeofence createSimpleGeofence(Cursor cursor);

	/**
	 * Creates the ContentValues for a given SimpleGeofence object.
	 * 
	 * Note: If the simpleGeofence object has an id<0 the id column will not be filled.
	 * 
	 * @param invitation a given simpleGeofence object
	 * @return a filled in ContentValues object 
	 */
	public ContentValues createContentValues(SimpleGeofence simpleGeofence);

	/**
	 * Inserts an simpleGeofence in the simpleGeofence provider. 
	 * 
	 * @param simpleGeofence the simpleGeofence object to insert
	 * @return the content provider URI for the inserted simpleGeofence
	 */
	public Uri insertSimpleGeofence(SimpleGeofence simpleGeofence);

	/**
	 * Retrieve the simpleGeofence from the database
	 * 
	 * @return the SimpleGeofence array or null if no simpleGeofence information is available
	 */
	public ArrayList<SimpleGeofence> getList();

	/**
	 * Retrieve the simpleGeofence from the database
	 * 
	 * @return the SimpleGeofence object or null if no simpleGeofence information is available
	 */
	public SimpleGeofence getById(long id);

	/**
	 * Updates a invitation in the invitation provider. 
	 * 
	 * @param invitation the invitation object to update
	 */
	public void updateSimpleGeofence(SimpleGeofence simpleGeofence);

	/**
	 * Deletes a simpleGeofence from the SimpleGeofence provider. 
	 * 
	 * @param simpleGeofence the simpleGeofence object to delete
	 */
	void deleteSimpleGeofence(SimpleGeofence simpleGeofence);

	/**
	 * A factory which can produce instances of {@link TravelerProviderUtilsImpl}, and can be overridden in tests (a.k.a. poor man's guice).
	 */
	public static class Factory {
		private static Factory instance = new Factory();

		/**
		 * Creates and returns an instance of {@link TravelerProviderUtilsImpl} which uses the given context to access its data.
		 */
		public static ContentProviderUtils get(Context context) {
			return instance.newForContext(context);
		}

		/**
		 * Returns the global instance of this factory.
		 */
		public static Factory getInstance() {
			return instance;
		}

		/**
		 * Overrides the global instance for this factory, to be used for testing. If used, don't forget to set it back to the original value after
		 * the test is run.
		 */
		public static void overrideInstance(Factory factory) {
			instance = factory;
		}

		/**
		 * Creates an instance of {@link TravelerProviderUtilsImpl}.
		 */
		protected ContentProviderUtils newForContext(Context context) {
			return new ContentProviderUtilsImpl(context.getContentResolver());
		}
	}
}
