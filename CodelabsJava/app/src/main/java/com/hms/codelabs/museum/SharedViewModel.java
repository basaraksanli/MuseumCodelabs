package com.hms.codelabs.museum;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.button.MaterialButton;
import com.hms.codelabs.museum.listeners.LocationResultListener;
import com.hms.codelabs.museum.listeners.MuseumSearchResultListener;
import com.hms.codelabs.museum.models.Constant;
import com.hms.codelabs.museum.models.Exhibit;
import com.hms.codelabs.museum.utils.GuideUtils;
import com.hms.codelabs.museum.utils.LocationManager;
import com.hms.codelabs.museum.utils.SearchUtils;
import com.hms.codelabs.museum.utils.TTSUtils;
import com.huawei.hms.mlsdk.common.MLApplication;
import com.huawei.hms.site.api.model.Site;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressLint("StaticFieldLeak")
public class SharedViewModel extends AndroidViewModel {

    private final TTSUtils ttsUtils;
    private final Context mContext;

    public final SearchUtils searchUtils;
    public final GuideUtils guideUtils;

    public MutableLiveData<Integer> page = new MutableLiveData<>(1);
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public MutableLiveData<ArrayList<Site>> museumList = new MutableLiveData<>(new ArrayList<>());

    public MutableLiveData<String> currentMuseumName = new MutableLiveData<>("");
    public MutableLiveData<Exhibit> currentExhibit = new MutableLiveData<>(null);
    public MutableLiveData<Integer> searchRange = new MutableLiveData<>(Constant.DEFAULT_SEARCH_RANGE);


    /**
     * Image binding to ImageView
     * @param view
     * @param currentExhibit
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    @BindingAdapter("app:image")
    public static void setImage(ImageView view, Exhibit currentExhibit){
        if(currentExhibit == null)
            view.setImageDrawable(view.getContext().getDrawable(R.drawable.no_image));
        else
            view.setImageDrawable(view.getContext().getDrawable(currentExhibit.getExhibitImage()));
    }


    public SharedViewModel(@NonNull Application application) {
        super(application);
        mContext = application.getApplicationContext();
        ttsUtils = new TTSUtils(mContext.getString(R.string.api_key));
        searchUtils = new SearchUtils( mContext , this);
        guideUtils = new GuideUtils(this);
    }
    /**
     * Text to Speech Start Reading
     */
    public void startTTS(View view) {
        if (currentExhibit.getValue() != null)
            ttsUtils.startTTS(currentExhibit.getValue().getExhibitDescription());
    }

    /**
     * Text to Speech Stop Reading
     */
    public void stopTTS(View view) {
        ttsUtils.stopTTS();
    }



    /**
     * Search Nearby Museums
     * Site Kit - Nearby Search works with pagination logic
     * This function checks for every page of the result(maximum 20 pages)
     * Results of the every page are retrieved async
     * Therefore if the result count 20, search will be assumed finished
     */
    public void searchNearbyMuseums(){
        LocationManager locationManager = new LocationManager(mContext);
        locationManager.getCurrentLocation(new LocationResultListener() {
            @Override
            public void onResult(Location result) throws UnsupportedEncodingException {
                searchUtils.searchMuseums(result, searchRange.getValue(), page.getValue(), new MuseumSearchResultListener() {
                    @Override
                    public void onResult(List<Site> result) {
                        Objects.requireNonNull(museumList.getValue()).addAll(result);
                        museumList.getValue().sort((site1, site2) -> (int) (site1.getDistance() - site2.getDistance()));
                        museumList.postValue(museumList.getValue());
                    }
                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e("Museum Search", "Error: " + errorMessage);
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("Location Result", "Error:" + e.getMessage());
            }
        });
    }
}
