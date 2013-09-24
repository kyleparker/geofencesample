/*
 * Copyright 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.bsu.android.apps.geofence.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Utilities to access preferences stored in {@link SharedPreferences}.
 */
public class PreferencesUtils {

	private static final String SETTINGS_NAME = "SettingsActivity";
	
	/*
	 * Preferences values. The defaults need to match the defaults in the xml files.
	 */
	public static final boolean ALLOW_ACCESS_DEFAULT = true;

	private PreferencesUtils() {
	}

	/**
	 * Gets a preference key
	 * 
	 * @param context the context
	 * @param keyId the key id
	 */
	public static String getKey(Context context, int keyId) {
		return context.getString(keyId);
	}

	/**
	 * Gets a boolean preference value.
	 * 
	 * @param context the context
	 * @param keyId the key id
	 * @param defaultValue the default value
	 */
	public static boolean getBoolean(Context context, int keyId, boolean defaultValue) {
		try {
			SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
			return sharedPreferences.getBoolean(getKey(context, keyId), defaultValue);
		} catch (Exception ex) {
			ex.printStackTrace();
			return defaultValue;
		}
	}

	/**
	 * Sets a boolean preference value.
	 * 
	 * @param context the context
	 * @param keyId the key id
	 * @param value the value
	 */
	public static void setBoolean(Context context, int keyId, boolean value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(getKey(context, keyId), value);
		editor.apply();
	}
}
