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
package edu.bsu.android.apps.geofence.objects;

import com.google.android.gms.location.Geofence;

/**
 * A single Geofence object, defined by its center (latitude and longitude position) and radius.
 */
public class SimpleGeofence {
    // Instance variables
    private long id;
    private double latitude;
    private double longitude;
    private String name;
    private float radius;
    private long expirationDuration;
    private int transitionType;
    
    /**
     * Get the geofence ID
     * @return A SimpleGeofence ID
     */
    public long getId() {
        return id;
    }
    public void setId(long value) {
        this.id = value;
    }

    /**
     * Get the geofence latitude
     * @return A latitude value
     */
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double value) {
        this.latitude = value;
    }

    /**
     * Get the geofence longitude
     * @return A longitude value
     */
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double value) {
        this.longitude = value;
    }

    /**
     * Get the geofence name
     * @return A name value
     */
    public String getName() {
        return name;
    }
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Get the geofence radius
     * @return A radius value
     */
    public float getRadius() {
        return radius;
    }
    public void setRadius(float value) {
        this.radius = value;
    }

    /**
     * Get the geofence expiration duration
     * @return Expiration duration in milliseconds
     */
    public long getExpirationDuration() {
        return expirationDuration;
    }
    public void setExpirationDuration(long value) {
        this.expirationDuration = value;
    }

    /**
     * Get the geofence transition type
     * @return Transition type (see Geofence)
     */
    public int getTransitionType() {
        return transitionType;
    }
    public void setTransitionType(int value) {
        this.transitionType = value;
    }

    /**
     * Creates a Location Services Geofence object from a SimpleGeofence.
     *
     * @return A Geofence object
     */
    public Geofence toGeofence() {
        // Build a new Geofence object
        return new Geofence.Builder()
                       .setRequestId(Long.toString(id))
                       .setTransitionTypes(transitionType)
                       .setCircularRegion(latitude, longitude, radius)
                       .setExpirationDuration(expirationDuration)
                       .build();
    }
}
