package com.hms.codelabs.museum.utils

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.hms.codelabs.museum.R
import com.hms.codelabs.museum.SharedViewModel
import com.hms.codelabs.museum.listeners.ResultListener
import com.hms.codelabs.museum.services.AwarenessServiceManager
import com.huawei.hms.kit.awareness.Awareness
import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier
import com.huawei.hms.kit.awareness.barrier.BarrierUpdateRequest
import com.huawei.hms.kit.awareness.barrier.LocationBarrier
import com.huawei.hms.kit.awareness.barrier.TimeBarrier
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.*
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class SearchUtils(var context: Context, var viewModel: SharedViewModel) {
    private var TAG = "Search Utils"

    /**
     * Search Nearby Museums
     * Site Kit - Nearby Search works with pagination logic
     * This function checks for every page of the result(maximum 20 pages)
     * Results of the every page are retrieved async
     * Therefore if the result count 20, search will be assumed finished
     */
    @Throws(UnsupportedEncodingException::class)
    fun searchMuseums(location: Location, radius: Int, pageIndex: Int, museumSearchResultListener: ResultListener) {
        val apiKey = URLEncoder.encode(context.getString(R.string.api_key), "UTF-8")
        val searchService = SearchServiceFactory.create(context, apiKey)
        val request = NearbySearchRequest()
        request.setLocation(Coordinate(location.latitude, location.longitude))
        request.setRadius(radius * 1000)
        request.setPoiType(LocationType.MUSEUM)
        request.setLanguage("en")
        request.setPageSize(20)
        request.setPageIndex(pageIndex)

        val resultListener: SearchResultListener<NearbySearchResponse> = object : SearchResultListener<NearbySearchResponse> {
            override fun onSearchResult(results: NearbySearchResponse?) {
                if (results == null || results.totalCount <= 0) {
                    return
                }
                val sites = results.getSites()
                if (sites == null || sites.size == 0) {
                    return
                }
                viewModel.isLoading.postValue(false)
                museumSearchResultListener.onResult(sites)
            }

            override fun onSearchError(searchStatus: SearchStatus) {
                Log.e(TAG, "Error : " + searchStatus.errorCode + " " + searchStatus.errorMessage)
                viewModel.isLoading.postValue(false)
                museumSearchResultListener.onFailure(Exception(searchStatus.errorMessage))
            }
        }
        searchService.nearbySearch(request, resultListener)
    }

    /**
     * For every museum nearby, Awareness barriers are added in order to notify the user when he is close
     */
    fun addBarrierToAwarenessKit(site: Site, radius: Double, duration: Long) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val stayBarrier = LocationBarrier.stay(site.location.lat, site.location.lng, radius, duration)
        val timeBarrier = TimeBarrier.inTimeCategory(TimeBarrier.TIME_CATEGORY_NIGHT)
        val combinedBarrier = AwarenessBarrier.and(stayBarrier, AwarenessBarrier.not(timeBarrier))
        val pendingIntent: PendingIntent
        val intent = Intent(context, AwarenessServiceManager::class.java)
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //In Android 8.0 or later, only foreground services can be started when the app is running in the background.
            PendingIntent.getForegroundService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        updateBarrier(site.name, combinedBarrier, pendingIntent)
    }

    /**
     * Update the barriers
     */
    private fun updateBarrier(label: String, barrier: AwarenessBarrier, pendingIntent: PendingIntent) {
        val request = BarrierUpdateRequest.Builder()
                .addBarrier(label, barrier, pendingIntent)
                .build()
        Awareness.getBarrierClient(context.applicationContext).updateBarriers(request).addOnSuccessListener {
            Toast.makeText(context, "Add Barrier is successful", Toast.LENGTH_SHORT).show()
            Log.i("AddBarrier", "add barrier success")
        }.addOnFailureListener { e: Exception? ->
            Toast.makeText(context, "Add Barrier is failed", Toast.LENGTH_SHORT).show()
            Log.e("AddBarrier", "add barrier failed", e)
        }
    }
}