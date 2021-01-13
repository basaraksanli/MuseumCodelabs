package com.hms.codelabs.museum.utils

import android.content.Context
import android.location.Location
import com.hms.codelabs.museum.listeners.ResultListener
import com.huawei.hms.location.FusedLocationProviderClient
import java.io.UnsupportedEncodingException

class LocationManager(var mContext: Context) {
    /**
     * Get Last Location
     * @param resultListener
     */
    fun getCurrentLocation(resultListener: ResultListener) {
        val fusedLocationProviderClient = FusedLocationProviderClient(mContext)
        val task = fusedLocationProviderClient.lastLocation // Define callback for success in obtaining the last known location.
                .addOnSuccessListener { location: Location? ->
                    if (location == null)
                        resultListener.onFailure(Exception("Location result is null"))
                    else {
                        try {
                            resultListener.onResult(location)
                        } catch (e: UnsupportedEncodingException) {
                            e.printStackTrace()
                        }
                    }
                } // Define callback for failure in obtaining the last known location.
                .addOnFailureListener { e: Exception? -> resultListener.onFailure(e) }
    }
}