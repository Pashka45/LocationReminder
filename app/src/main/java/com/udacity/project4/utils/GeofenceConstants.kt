package com.udacity.project4.utils

import java.util.concurrent.TimeUnit

internal object GeofenceConstants {
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)

    const val GEOFENCE_RADIUS_IN_METERS = 1000f
    const val EXTRA_GEOFENCE_INDEX = "GEOFENCE_INDEX"
}