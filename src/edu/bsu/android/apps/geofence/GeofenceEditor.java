package edu.bsu.android.apps.geofence;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import edu.bsu.android.apps.geofence.content.ContentProviderUtils;
import edu.bsu.android.apps.geofence.objects.SimpleGeofence;
import edu.bsu.android.apps.geofence.utils.Constants;
import edu.bsu.android.apps.geofence.utils.FileUtils;
import edu.bsu.android.apps.geofence.utils.GeofenceUtils;
import edu.bsu.android.apps.geofence.utils.GeofenceUtils.REMOVE_TYPE;
import edu.bsu.android.apps.geofence.utils.GeofenceUtils.REQUEST_TYPE;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

public class GeofenceEditor extends FragmentActivity {
	/*
	 * Use to set an expiration time for a geofence. After this amount of time Location Services will stop tracking the geofence. Remember to
	 * unregister a geofence when you're finished with it. Otherwise, your app will use up battery. To continue monitoring a geofence indefinitely,
	 * set the expiration time to Geofence#NEVER_EXPIRE.
	 */
	private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
	private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS * DateUtils.HOUR_IN_MILLIS;

	private ContentProviderUtils mProvider;

	private long mId = -1L;
	
	private EditText mLatitude;
	private EditText mLongitude;
	private EditText mRadius;
	private EditText mName;

	private DecimalFormat mLatLngFormat;
	private DecimalFormat mRadiusFormat;

	private SimpleGeofence mSimpleGeofence;
	// Store a list of geofences to add
	List<Geofence> mCurrentGeofences;
	
	// Store the list of geofences to remove
	private List<String> mGeofenceIdsToRemove;
	
	// An instance of an inner class that receives broadcasts from listeners and from the IntentService that receives geofence transition events
	private GeofenceSampleReceiver mBroadcastReceiver;

	// An intent filter for the broadcast receiver
	private IntentFilter mIntentFilter;

	// Store the current request
	private REQUEST_TYPE mRequestType;

	// Store the current type of removal
	private REMOVE_TYPE mRemoveType;

	// Add geofences handler
	private GeofenceRequester mGeofenceRequester;
	// Remove geofences handler
	private GeofenceRemover mGeofenceRemover;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Attach to the main UI
		setContentView(R.layout.activity_editor);

		mProvider = ContentProviderUtils.Factory.get(this);
		getExtras();

		// Set the pattern for the latitude and longitude format
		String latLngPattern = getString(R.string.lat_lng_pattern);

		// Set the format for latitude and longitude
		mLatLngFormat = new DecimalFormat(latLngPattern);

		// Localize the format
		mLatLngFormat.applyLocalizedPattern(mLatLngFormat.toLocalizedPattern());

		// Set the pattern for the radius format
		String radiusPattern = getString(R.string.radius_pattern);

		// Set the format for the radius
		mRadiusFormat = new DecimalFormat(radiusPattern);

		// Localize the pattern
		mRadiusFormat.applyLocalizedPattern(mRadiusFormat.toLocalizedPattern());

		// Create a new broadcast receiver to receive updates from the listeners and service
		mBroadcastReceiver = new GeofenceSampleReceiver();

		// Create an intent filter for the broadcast receiver
		mIntentFilter = new IntentFilter();

		// Action for broadcast Intents that report successful addition of geofences
		mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);

		// Action for broadcast Intents that report successful removal of geofences
		mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);

		// Action for broadcast Intents containing various types of geofencing errors
		mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);

		// All Location Services sample apps use this category
		mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

		// Instantiate the current List of geofences
		mCurrentGeofences = new ArrayList<Geofence>();

		// Instantiate a Geofence requester
		mGeofenceRequester = new GeofenceRequester(this);

		// Instantiate a Geofence remover
		mGeofenceRemover = new GeofenceRemover(this);

		// Get handles to the Geofence editor fields in the UI
		mLatitude = (EditText) findViewById(R.id.value_latitude);
		mLongitude = (EditText) findViewById(R.id.value_longitude);
		mRadius = (EditText) findViewById(R.id.value_radius);
		mName = (EditText) findViewById(R.id.value_name);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {

		// If the request code matches the code sent in onConnectionFailed
			case GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST:

				switch (resultCode) {
				// If Google Play services resolved the problem
					case Activity.RESULT_OK:

						// If the request was to add geofences
						if (GeofenceUtils.REQUEST_TYPE.ADD == mRequestType) {

							// Toggle the request flag and send a new request
							mGeofenceRequester.setInProgressFlag(false);

							// Restart the process of adding the current geofences
							mGeofenceRequester.addGeofences(mCurrentGeofences);

							// If the request was to remove geofences
						} else if (GeofenceUtils.REQUEST_TYPE.REMOVE == mRequestType) {

							// Toggle the removal flag and send a new removal request
							mGeofenceRemover.setInProgressFlag(false);

							// If the removal was by Intent
							if (GeofenceUtils.REMOVE_TYPE.INTENT == mRemoveType) {

								// Restart the removal of all geofences for the PendingIntent
								mGeofenceRemover.removeGeofencesByIntent(mGeofenceRequester.getRequestPendingIntent());

								// If the removal was by a List of geofence IDs
							} else {

								// Restart the removal of the geofence list
								mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);
							}
						}
						break;

					// If any other result was returned by Google Play services
					default:

						// Report that Google Play services was unable to resolve the problem.
						Log.d(GeofenceUtils.APPTAG, getString(R.string.no_resolution));
				}

				// If any other request code was received
			default:
				// Report that this Activity received an unknown requestCode
				Log.d(GeofenceUtils.APPTAG, getString(R.string.unknown_activity_request_code, requestCode));

				break;
		}
	}

	/*
	 * Whenever the Activity resumes, reconnect the client to Location Services and reload the last geofences that were set
	 */
	@Override
	protected void onResume() {
		super.onResume();
		// Register the broadcast receiver to receive status updates
		LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, mIntentFilter);
		
		if (mId > 0) {
			mSimpleGeofence = mProvider.getById(mId);
			
			if (mSimpleGeofence != null) {
				mLatitude.setText(mLatLngFormat.format(mSimpleGeofence.getLatitude()));
				mLongitude.setText(mLatLngFormat.format(mSimpleGeofence.getLongitude()));
				mRadius.setText(mRadiusFormat.format(mSimpleGeofence.getRadius()));
				mName.setText(mSimpleGeofence.getName());
			}
		}
	}

	public void getExtras() {
		Bundle extras = getIntent().getExtras();

		if (extras != null) {
			mId = extras.getLong(Constants.EXTRA_ID);
		}
	}

	/*
	 * Inflate the app menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;

	}

	/*
	 * Respond to menu item selections
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.map:
				startActivity(newIntent(this, MapActivity.class));
				return true;
			case R.id.add_geofence:
				startActivity(newIntent(this, GeofenceEditor.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Verify that Google Play services is available before making a request.
	 * 
	 * @return true if Google Play services is available, otherwise false
	 */
	private boolean servicesConnected() {

		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {

			// In debug mode, log the status
			Log.d(GeofenceUtils.APPTAG, getString(R.string.play_services_available));

			// Continue
			return true;

			// Google Play services was not available for some reason
		} else {

			// Display an error dialog
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
			if (dialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(dialog);
				errorFragment.show(getSupportFragmentManager(), GeofenceUtils.APPTAG);
			}
			return false;
		}
	}

	/**
	 * Called when the user clicks the "Remove geofences" button
	 * 
	 * @param view The view that triggered this callback
	 */
	public void onUnregisterByPendingIntentClicked(View view) {
		/*
		 * Remove all geofences set by this app. To do this, get the PendingIntent that was added when the geofences were added and use it as an
		 * argument to removeGeofences(). The removal happens asynchronously; Location Services calls onRemoveGeofencesByPendingIntentResult()
		 * (implemented in the current Activity) when the removal is done
		 */

		/*
		 * Record the removal as remove by Intent. If a connection error occurs, the app can automatically restart the removal if Google Play services
		 * can fix the error
		 */
		// Record the type of removal
		mRemoveType = GeofenceUtils.REMOVE_TYPE.INTENT;

		/*
		 * Check for Google Play services. Do this after setting the request type. If connecting to Google Play services fails, onActivityResult is
		 * eventually called, and it needs to know what type of request was in progress.
		 */
		if (!servicesConnected()) {

			return;
		}

		// Try to make a removal request
		try {
			/*
			 * Remove the geofences represented by the currently-active PendingIntent. If the PendingIntent was removed for some reason, re-create it;
			 * since it's always created with FLAG_UPDATE_CURRENT, an identical PendingIntent is always created.
			 */
			mGeofenceRemover.removeGeofencesByIntent(mGeofenceRequester.getRequestPendingIntent());

		} catch (UnsupportedOperationException e) {
			// Notify user that previous request hasn't finished.
			Toast.makeText(this, R.string.remove_geofences_already_requested_error, Toast.LENGTH_LONG).show();
		}

	}

	/**
	 * Called when the user clicks the "Remove geofence 1" button
	 * 
	 * @param view The view that triggered this callback
	 */
	public void onUnregisterGeofenceClicked(View view) {
		/*
		 * Remove the geofence by creating a List of geofences to remove and sending it to Location Services. The List contains the id of geofence 1
		 * ("1"). The removal happens asynchronously; Location Services calls onRemoveGeofencesByPendingIntentResult() (implemented in the current
		 * Activity) when the removal is done.
		 */

		// Create a List of 1 Geofence with the ID "1" and store it in the global list
		mGeofenceIdsToRemove = Collections.singletonList(Long.toString(mId));

		/*
		 * Record the removal as remove by list. If a connection error occurs, the app can automatically restart the removal if Google Play services
		 * can fix the error
		 */
		mRemoveType = GeofenceUtils.REMOVE_TYPE.LIST;

		/*
		 * Check for Google Play services. Do this after setting the request type. If connecting to Google Play services fails, onActivityResult is
		 * eventually called, and it needs to know what type of request was in progress.
		 */
		if (!servicesConnected()) {

			return;
		}

		// Try to remove the geofence
		try {
			mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);
			
			mProvider.deleteSimpleGeofence(mSimpleGeofence);

			// Catch errors with the provided geofence IDs
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			// Notify user that previous request hasn't finished.
			Toast.makeText(this, R.string.remove_geofences_already_requested_error, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Called when the user clicks the "Register geofences" button. Get the geofence parameters for each geofence and add them to a List. Create the
	 * PendingIntent containing an Intent that Location Services sends to this app's broadcast receiver when Location Services detects a geofence
	 * transition. Send the List and the PendingIntent to Location Services.
	 */
	public void onRegisterClicked(View view) {

		/*
		 * Record the request as an ADD. If a connection error occurs, the app can automatically restart the add request if Google Play services can
		 * fix the error
		 */
		mRequestType = GeofenceUtils.REQUEST_TYPE.ADD;

		/*
		 * Check for Google Play services. Do this after setting the request type. If connecting to Google Play services fails, onActivityResult is
		 * eventually called, and it needs to know what type of request was in progress.
		 */
		if (!servicesConnected()) {

			return;
		}

		/*
		 * Check that the input fields have values and that the values are with the permitted range
		 */
		if (!checkInputFields()) {
			return;
		}

		/*
		 * Create a version of geofence 1 that is "flattened" into individual fields. This allows it to be stored in SharedPreferences.
		 */
		mSimpleGeofence = new SimpleGeofence();
		mSimpleGeofence.setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS);
		mSimpleGeofence.setLatitude(Double.valueOf(mLatitude.getText().toString()));
		mSimpleGeofence.setLongitude(Double.valueOf(mLongitude.getText().toString()));
		mSimpleGeofence.setRadius(Float.valueOf(mRadius.getText().toString()));
		mSimpleGeofence.setName(mName.getText().toString());
		mSimpleGeofence.setTransitionType(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);

		if (mId < 0) {
			// Store this flat version in SharedPreferences
			mProvider.insertSimpleGeofence(mSimpleGeofence);
		} else {
			mSimpleGeofence.setId(mId);
			mProvider.updateSimpleGeofence(mSimpleGeofence);
		}
		
		/*
		 * Add Geofence objects to a List. toGeofence() creates a Location Services Geofence object from a flat object
		 */
		mCurrentGeofences.add(mSimpleGeofence.toGeofence());

		// Start the request. Fail if there's already a request in progress
		try {
			// Try to add geofences
			mGeofenceRequester.addGeofences(mCurrentGeofences);
		} catch (UnsupportedOperationException e) {
			// Notify user that previous request hasn't finished.
			Toast.makeText(this, R.string.add_geofences_already_requested_error, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Check all the input values and flag those that are incorrect
	 * 
	 * @return true if all the widget values are correct; otherwise false
	 */
	private boolean checkInputFields() {
		// Start with the input validity flag set to true
		boolean inputOK = true;

		/*
		 * Latitude, longitude, and radius values can't be empty. If they are, highlight the input field in red and put a Toast message in the UI.
		 * Otherwise set the input field highlight to black, ensuring that a field that was formerly wrong is reset.
		 */
		if (TextUtils.isEmpty(mLatitude.getText())) {
			mLatitude.setBackgroundColor(Color.RED);
			Toast.makeText(this, R.string.geofence_input_error_missing, Toast.LENGTH_LONG).show();

			// Set the validity to "invalid" (false)
			inputOK = false;
		} else {
			mLatitude.setBackgroundColor(Color.BLACK);
		}

		if (TextUtils.isEmpty(mLongitude.getText())) {
			mLongitude.setBackgroundColor(Color.RED);
			Toast.makeText(this, R.string.geofence_input_error_missing, Toast.LENGTH_LONG).show();

			// Set the validity to "invalid" (false)
			inputOK = false;
		} else {
			mLongitude.setBackgroundColor(Color.BLACK);
		}
		if (TextUtils.isEmpty(mRadius.getText())) {
			mRadius.setBackgroundColor(Color.RED);
			Toast.makeText(this, R.string.geofence_input_error_missing, Toast.LENGTH_LONG).show();

			// Set the validity to "invalid" (false)
			inputOK = false;
		} else {
			mRadius.setBackgroundColor(Color.BLACK);
		}

		/*
		 * If all the input fields have been entered, test to ensure that their values are within the acceptable range. The tests can't be performed
		 * until it's confirmed that there are actual values in the fields.
		 */
		if (inputOK) {
			/*
			 * Get values from the latitude, longitude, and radius fields.
			 */
			double lat1 = Double.valueOf(mLatitude.getText().toString());
			double lng1 = Double.valueOf(mLongitude.getText().toString());
			float rd1 = Float.valueOf(mRadius.getText().toString());

			/*
			 * Test latitude and longitude for minimum and maximum values. Highlight incorrect values and set a Toast in the UI.
			 */

			if (lat1 > GeofenceUtils.MAX_LATITUDE || lat1 < GeofenceUtils.MIN_LATITUDE) {
				mLatitude.setBackgroundColor(Color.RED);
				Toast.makeText(this, R.string.geofence_input_error_latitude_invalid, Toast.LENGTH_LONG).show();

				// Set the validity to "invalid" (false)
				inputOK = false;
			} else {
				mLatitude.setBackgroundColor(Color.BLACK);
			}

			if ((lng1 > GeofenceUtils.MAX_LONGITUDE) || (lng1 < GeofenceUtils.MIN_LONGITUDE)) {
				mLongitude.setBackgroundColor(Color.RED);
				Toast.makeText(this, R.string.geofence_input_error_longitude_invalid, Toast.LENGTH_LONG).show();

				// Set the validity to "invalid" (false)
				inputOK = false;
			} else {
				mLongitude.setBackgroundColor(Color.BLACK);
			}
			
			if (rd1 < GeofenceUtils.MIN_RADIUS) {
				mRadius.setBackgroundColor(Color.RED);
				Toast.makeText(this, R.string.geofence_input_error_radius_invalid, Toast.LENGTH_LONG).show();

				// Set the validity to "invalid" (false)
				inputOK = false;
			} else {
				mRadius.setBackgroundColor(Color.BLACK);
			}
		}

		// If everything passes, the validity flag will still be true, otherwise it will be false.
		return inputOK;
	}

	/**
	 * Define a Broadcast receiver that receives updates from connection listeners and the geofence transition service.
	 */
	public class GeofenceSampleReceiver extends BroadcastReceiver {
		/*
		 * Define the required method for broadcast receivers This method is invoked when a broadcast Intent triggers the receiver
		 */
		@Override
		public void onReceive(Context context, Intent intent) {

			// Check the action code and determine what to do
			String action = intent.getAction();
			// Toast.makeText(context, "onReceive action: " + action, Toast.LENGTH_LONG).show();

			// Intent contains information about errors in adding or removing geofences
			if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {

				handleGeofenceError(context, intent);

				// Intent contains information about successful addition or removal of geofences
			} else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED)
					|| TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {

				handleGeofenceStatus(context, intent);

				// Intent contains information about a geofence transition
			} else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_TRANSITION)) {

				handleGeofenceTransition(context, intent);

				// The Intent contained an invalid action
			} else {
				Log.e(GeofenceUtils.APPTAG, getString(R.string.invalid_action_detail, action));
				Toast.makeText(context, R.string.invalid_action, Toast.LENGTH_LONG).show();
			}
		}

		/**
		 * If you want to display a UI message about adding or removing geofences, put it here.
		 * 
		 * @param context A Context for this component
		 * @param intent The received broadcast Intent
		 */
		private void handleGeofenceStatus(Context context, Intent intent) {
			Toast.makeText(context, "geofence status", Toast.LENGTH_LONG).show();

		}

		/**
		 * Report geofence transitions to the UI
		 * 
		 * @param context A Context for this component
		 * @param intent The Intent containing the transition
		 */
		private void handleGeofenceTransition(Context context, Intent intent) {
			/*
			 * If you want to change the UI when a transition occurs, put the code here. The current design of the app uses a notification to inform
			 * the user that a transition has occurred.
			 */
			int transition = LocationClient.getGeofenceTransition(intent);
			List<Geofence> crossed = LocationClient.getTriggeringGeofences(intent);

			Toast.makeText(context, "geofence transition: " + transition + "|" + crossed.get(transition), Toast.LENGTH_LONG).show();
			FileUtils.writeFile("transition.txt", transition + "|" + crossed.get(transition));
		}

		/**
		 * Report addition or removal errors to the UI, using a Toast
		 * 
		 * @param intent A broadcast Intent sent by ReceiveTransitionsIntentService
		 */
		private void handleGeofenceError(Context context, Intent intent) {
			String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
			Log.e(GeofenceUtils.APPTAG, msg);
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Define a DialogFragment to display the error dialog generated in showErrorDialog.
	 */
	public static class ErrorDialogFragment extends DialogFragment {

		// Global field to contain the error dialog
		private Dialog mDialog;

		/**
		 * Default constructor. Sets the dialog field to null
		 */
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		/**
		 * Set the dialog to display
		 * 
		 * @param dialog An error dialog
		 */
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		/*
		 * This method must return a Dialog to the DialogFragment.
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

	/**
	 * Creates an intent with {@link Intent#FLAG_ACTIVITY_CLEAR_TOP} and {@link Intent#FLAG_ACTIVITY_NEW_TASK}.
	 * 
	 * @param context the context
	 * @param cls the class
	 */
	public static final Intent newIntent(Context context, Class<?> cls) {
		try {
			return new Intent(context, cls).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		} catch (Exception ex) {
			return new Intent();
		}
	}
}
