/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.bsu.android.apps.geofence.utils;

/**
 * Defines app-wide constants and utilities
 */
public final class LocationUtils {

	// Debugging tag for the application
	public static final String APPTAG = "LocationSample";

	// Name of shared preferences repository that stores persistent state
	public static final String SHARED_PREFERENCES = "edu.bsu.android.apps.geofence.SHARED_PREFERENCES";

	// Key for storing the "updates requested" flag in shared preferences
	public static final String KEY_UPDATES_REQUESTED = "edu.bsu.android.apps.geofence.KEY_UPDATES_REQUESTED";

	/*
	 * Define a request code to send to Google Play services This code is returned in Activity.onActivityResult
	 */
	public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	/*
	 * Constants for location update parameters
	 */
	// Milliseconds per second
	public static final int MILLISECONDS_PER_SECOND = 1000;

	// The update interval
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;

	// A fast interval ceiling
	public static final int FAST_CEILING_IN_SECONDS = 1;

	// Update interval in milliseconds
	public static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

	// A fast ceiling of update intervals, used when the app is visible
	public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;

	// Create an empty string for initializing strings
	public static final String EMPTY_STRING = new String();
}
