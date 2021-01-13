package com.hms.codelabs.museum.utils;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.hms.codelabs.museum.R;
import com.hms.codelabs.museum.SharedViewModel;
import com.hms.codelabs.museum.listeners.MuseumSearchResultListener;
import com.hms.codelabs.museum.services.AwarenessServiceManager;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.kit.awareness.Awareness;
import com.huawei.hms.kit.awareness.barrier.AwarenessBarrier;
import com.huawei.hms.kit.awareness.barrier.BarrierUpdateRequest;
import com.huawei.hms.kit.awareness.barrier.LocationBarrier;
import com.huawei.hms.kit.awareness.barrier.TimeBarrier;
import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.Coordinate;
import com.huawei.hms.site.api.model.LocationType;
import com.huawei.hms.site.api.model.NearbySearchRequest;
import com.huawei.hms.site.api.model.NearbySearchResponse;
import com.huawei.hms.site.api.model.SearchStatus;
import com.huawei.hms.site.api.model.Site;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class SearchUtils {
    Context context;
    String TAG = "Search Utils";
    SharedViewModel viewModel;

    public SearchUtils(Context context, SharedViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;
    }

    /**
     * Search Nearby Museums
     * Site Kit - Nearby Search works with pagination logic
     * This function checks for every page of the result(maximum 20 pages)
     * Results of the every page are retrieved async
     * Therefore if the result count 20, search will be assumed finished
     */
    public void searchMuseums(Location location, int radius, int pageIndex, MuseumSearchResultListener museumSearchResultListener) throws UnsupportedEncodingException {
        String api_key = URLEncoder.encode(context.getString(R.string.api_key), "UTF-8");
        SearchService searchService = SearchServiceFactory.create(context, api_key);
        NearbySearchRequest request = new NearbySearchRequest();
        request.setLocation(new Coordinate(location.getLatitude(), location.getLongitude()));
        request.setRadius(radius * 1000);
        request.setPoiType(LocationType.MUSEUM);
        request.setLanguage("en");
        request.setPageSize(20);
        request.setPageIndex(pageIndex);

        SearchResultListener<NearbySearchResponse> resultListener = new SearchResultListener<NearbySearchResponse>() {
            @Override
            public void onSearchResult(NearbySearchResponse results) {
                if (results == null || results.totalCount <= 0) {
                    return;
                }
                List<Site> sites = results.getSites();
                if (sites == null || sites.size() == 0) {
                    return;
                }
                viewModel.isLoading.postValue(false);
                museumSearchResultListener.onResult(sites);
            }

            @Override
            public void onSearchError(SearchStatus searchStatus) {
                Log.e(TAG, "Error : " + searchStatus.errorCode + " " + searchStatus.errorMessage);
                viewModel.isLoading.postValue(false);
                museumSearchResultListener.onFailure(searchStatus.errorMessage);
            }
        };
        searchService.nearbySearch(request, resultListener);
    }

    /**
     * For every museum nearby, Awareness barriers are added in order to notify the user when he is close
     */
    public void addBarrierToAwarenessKit(Site site, double radius, long duration) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        AwarenessBarrier stayBarrier = LocationBarrier.stay(site.location.lat, site.location.lng, radius, duration);
        AwarenessBarrier timeBarrier = TimeBarrier.inTimeCategory(TimeBarrier.TIME_CATEGORY_NIGHT);
        AwarenessBarrier combinedBarrier = AwarenessBarrier.and(stayBarrier, AwarenessBarrier.not(timeBarrier));
        PendingIntent pendingIntent;
        Intent intent = new Intent(context, AwarenessServiceManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //In Android 8.0 or later, only foreground services can be started when the app is running in the background.
            pendingIntent = PendingIntent.getForegroundService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        updateBarrier(site.name, combinedBarrier, pendingIntent);
    }

    /**
     * Update the barriers
     */
    private void updateBarrier(String label, AwarenessBarrier barrier, PendingIntent pendingIntent) {
        BarrierUpdateRequest request = new BarrierUpdateRequest.Builder()
                .addBarrier(label, barrier, pendingIntent)
                .build();
        Awareness.getBarrierClient(context.getApplicationContext()).updateBarriers(request).addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Add Barrier is successful", Toast.LENGTH_SHORT).show();
                    Log.i("AddBarrier", "add barrier success");
                }
        ).addOnFailureListener(e -> {
            Toast.makeText(context, "Add Barrier is failed", Toast.LENGTH_SHORT).show();
            Log.e("AddBarrier", "add barrier failed", e);
        });
    }
}
