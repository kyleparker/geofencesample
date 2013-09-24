package edu.bsu.android.apps.geofence;

import java.util.ArrayList;

import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import edu.bsu.android.apps.geofence.R;
import edu.bsu.android.apps.geofence.content.ContentProviderUtils;
import edu.bsu.android.apps.geofence.objects.SimpleGeofence;
import edu.bsu.android.apps.geofence.utils.FileUtils;
import edu.bsu.android.apps.geofence.utils.LocationUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

public class MapActivity extends Activity implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private static final LatLng MUNCIE = new LatLng(40.20379, -85.40804);
	
	private GoogleMap map;

	// Stores the current instantiation of the location client in this object
	private LocationClient mLocationClient;
	// A request to connect to Location Services
	private LocationRequest mLocationRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		setupMap();
	}
	
	private void setupMap() {
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

		// Enabling MyLocation Layer of Google Map
		map.setMyLocationEnabled(true);
		map.setIndoorEnabled(true);

		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();

		// Set the update interval
		mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		// Set the interval ceiling to one minute
		mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

		// Create a new location client, using the enclosing class to handle callbacks.
		mLocationClient = new LocationClient(this, this, this);

		// Move the camera instantly to Muncie with a zoom of 15.
		//map.moveCamera(CameraUpdateFactory.newLatLngZoom(MUNCIE, 15));

		// Zoom in, animating the camera.
		//map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
		
		CameraPosition cameraPosition = new CameraPosition.Builder().zoom(18f).target(MUNCIE).tilt(30) // Sets the tilt of the camera to 30 degrees
				.build(); // Creates a CameraPosition from the builder

		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		
		ContentProviderUtils provider = ContentProviderUtils.Factory.get(this);
		ArrayList<SimpleGeofence> store = provider.getList();
		
		for (SimpleGeofence geo : store) {
			// Instantiates a new CircleOptions object and defines the center and radius
			CircleOptions co = new CircleOptions()
			    .center(new LatLng(geo.getLatitude(), geo.getLongitude()))
			    .radius(geo.getRadius())
			    .strokeWidth(1f)
			    .fillColor(0x324754ff);  

			// Get back the mutable Circle
			Circle circle = map.addCircle(co);
			Log.i("***> geofence", circle.getId());
		}
	}

	/*
	 * Called when the Activity is no longer visible at all. Stop updates and disconnect.
	 */
	@Override
	public void onStop() {

		// If the client is connected
		if (mLocationClient.isConnected()) {
			stopPeriodicUpdates();
		}

		// After disconnect() is called, the client is considered "dead".
		mLocationClient.disconnect();

		super.onStop();
	}

	/*
	 * Called when the Activity is restarted, even before it becomes visible.
	 */
	@Override
	public void onStart() {
		super.onStart();

		// Connect the client. Don't re-start any requests here; instead, wait for onResume()
		mLocationClient.connect();

	}

	/*
	 * Called by Location Services when the request to connect the client finishes successfully. At this point, you can request the current location
	 * or start periodic updates
	 */
	@Override
	public void onConnected(Bundle bundle) {
		startPeriodicUpdates();
	}

	/*
	 * Called by Location Services if the connection to the location client drops because of an error.
	 */
	@Override
	public void onDisconnected() {
	}

	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

		/*
		 * Google Play services can resolve some errors it detects. If the error has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {

				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this, LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

				/*
				 * Thrown if Google Play services canceled the original PendingIntent
				 */

			} catch (IntentSender.SendIntentException e) {

				// Log the error
				e.printStackTrace();
			}
		} else {

			// If no resolution is available, display a dialog to the user with the error.
			Log.e("error", connectionResult.getErrorCode() + "");
		}
	}

	/**
	 * Report location updates to the UI.
	 * 
	 * @param location The updated location.
	 */
	@Override
	public void onLocationChanged(Location location) {
		/*
		// Getting latitude of the current location
		double latitude = location.getLatitude();

		// Getting longitude of the current location
		double longitude = location.getLongitude();

		// Creating a LatLng object for the current location
		LatLng latLng = new LatLng(latitude, longitude);

		CameraPosition cameraPosition = new CameraPosition.Builder().zoom(18f).target(latLng).tilt(30) // Sets the tilt of the camera to 30 degrees
				.build(); // Creates a CameraPosition from the builder

		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		 */
		
		Log.i("***> onLocationChanged", location.getLatitude() + ", " + location.getLongitude());
		FileUtils.writeFile("log.txt", location.getLatitude() + ", " + location.getLongitude());
		// Showing the current location in Google Map
		// map.moveCamera(CameraUpdateFactory.newLatLng(latLng));

		// Zoom in the Google Map
		// map.animateCamera(CameraUpdateFactory.zoomTo(15));
	}

	/**
	 * In response to a request to start updates, send a request to Location Services
	 */
	private void startPeriodicUpdates() {
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	/**
	 * In response to a request to stop updates, send a request to Location Services
	 */
	private void stopPeriodicUpdates() {
		mLocationClient.removeLocationUpdates(this);
	}
}