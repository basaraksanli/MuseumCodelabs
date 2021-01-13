package com.hms.codelabs.museum.listeners

import android.location.Location
import java.io.UnsupportedEncodingException

/**
 * Location Result Listener for Location Kit
 */
interface ResultListener {
    @Throws(UnsupportedEncodingException::class)
    fun onResult(result: Any?)
    fun onFailure(e: Exception?)
}