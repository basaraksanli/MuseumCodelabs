package com.hms.codelabs.museum.utils;

import android.content.Context;
import android.location.Location;

import com.hms.codelabs.museum.listeners.LocationResultListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.location.FusedLocationProviderClient;

import java.io.UnsupportedEncodingException;

public class LocationManager {

    Context mContext;

    public LocationManager(Context context) {
        this.mContext = context;
    }

    /**
     * Get Last Location
     * @param locationResultListener
     */
    public void getCurrentLocation(LocationResultListener locationResultListener){

        FusedLocationProviderClient fusedLocationProviderClient = new FusedLocationProviderClient(mContext);

        fusedLocationProviderClient.getLastLocation()
                // Define callback for success in obtaining the last known location.
                .addOnSuccessListener(location -> {
                    if (location == null)
                        locationResultListener.onFailure(new Exception("Location result is null"));
                    else {
                        try {
                            locationResultListener.onResult(location);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    // Define logic for processing the Location object upon success.
                    // ...
                })
                // Define callback for failure in obtaining the last known location.
                .addOnFailureListener(locationResultListener::onFailure);
    }
}
