/*
 * Copyright 2010 Google Inc.
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

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utilities for dealing with files.
 * 
 * @author Rodrigo Damazio
 */
public class FileUtils {
	public static final String TAG = FileUtils.class.getSimpleName();

	public FileUtils() {
	}

	/**
	 * The maximum FAT32 path length. See the FAT32 spec at http://msdn.microsoft.com/en-us/windows/hardware/gg463080
	 */
	static final int MAX_FAT32_PATH_LENGTH = 260;

	/**
	 * Returns whether the SD card is available.
	 */
	public static boolean isSdCardAvailable() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}

	/**
	 * Return the file path based on the media type
	 * 
	 * @param context
	 * @param itemType
	 * @return
	 */
	public static File getFilePath() {
		File path = new File(Environment.getExternalStorageDirectory().getPath() + "/sample/log");

		return path;
	}

	/*
	 * Write data to a log file
	 */
	public static boolean writeFile(String fileName, String text) {
		File folder = FileUtils.getFilePath();
		
		if (!folder.exists()) {
			if (!folder.mkdirs()) {
				Log.e(TAG + ": " + folder, "Default Save Path Creation Error");
			}
		}
		
		File logFile = new File(folder, fileName);
		
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
			buf.append(text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			return true;
		}
		
		return false;
	}
}
