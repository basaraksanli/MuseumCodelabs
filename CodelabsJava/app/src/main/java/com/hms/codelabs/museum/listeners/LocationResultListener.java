package com.hms.codelabs.museum.listeners;


import android.location.Location;

import java.io.UnsupportedEncodingException;

/**
 * Location Result Listener for Location Kit
 */
public interface LocationResultListener {
    void onResult(Location result) throws UnsupportedEncodingException;
    void onFailure(Exception e);
}
