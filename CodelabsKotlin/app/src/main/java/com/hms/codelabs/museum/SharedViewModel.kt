package com.hms.codelabs.museum

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.hms.codelabs.museum.listeners.ResultListener
import com.hms.codelabs.museum.models.Constant
import com.hms.codelabs.museum.models.Exhibit
import com.hms.codelabs.museum.utils.GuideUtils
import com.hms.codelabs.museum.utils.LocationManager
import com.hms.codelabs.museum.utils.SearchUtils
import com.hms.codelabs.museum.utils.TTSUtils
import com.huawei.hms.site.api.model.Site
import java.io.UnsupportedEncodingException

@SuppressLint("StaticFieldLeak")
class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private val ttsUtils: TTSUtils
    private val mContext: Context = application.applicationContext

    val searchUtils: SearchUtils
    val guideUtils: GuideUtils
    var page = MutableLiveData(1)
    var isLoading = MutableLiveData(false)
    var museumList = MutableLiveData(ArrayList<Site>())
    var currentMuseumName = MutableLiveData("")
    var currentExhibit = MutableLiveData<Exhibit?>(null)
    var searchRange = MutableLiveData(Constant.DEFAULT_SEARCH_RANGE)

    /**
     * Text to Speech Start Reading
     */
    fun startTTS(view: View?) {
        if (currentExhibit.value != null) currentExhibit.value!!.exhibitDescription?.let { ttsUtils.startTTS(it) }
    }

    /**
     * Text to Speech Stop Reading
     */
    fun stopTTS(view: View?) {
        ttsUtils.stopTTS()
    }

    /**
     * Search Nearby Museums
     * Site Kit - Nearby Search works with pagination logic
     * This function checks for every page of the result(maximum 20 pages)
     * Results of the every page are retrieved async
     * Therefore if the result count 20, search will be assumed finished
     */
    fun searchNearbyMuseums() {
        val locationManager = LocationManager(mContext)
        locationManager.getCurrentLocation(object : ResultListener {
            @Throws(UnsupportedEncodingException::class)
            override fun onResult(locationResult: Any?) {
                searchUtils.searchMuseums(locationResult as Location, searchRange.value!!, page.value!!, object : ResultListener {
                    override fun onResult(searchResult: Any?) {
                        museumList.value!!.addAll(searchResult as ArrayList<Site>)
                        museumList.value!!.sortBy { it.distance }
                        museumList.postValue(museumList.value)
                    }

                    override fun onFailure(e: Exception?) {
                        Log.e("Museum Search", "Error: $e")
                    }
                })
            }

            override fun onFailure(e: Exception?) {
                Log.e("Location Result", "Error:" + e?.message)
            }
        })
    }

    companion object {
        /**
         * Image binding to ImageView
         * @param view
         * @param currentExhibit
         */
        @JvmStatic
        @SuppressLint("UseCompatLoadingForDrawables")
        @BindingAdapter("app:image")
        fun setImage(view: ImageView, currentExhibit: Exhibit?) {
            if (currentExhibit == null) view.setImageDrawable(view.context.getDrawable(R.drawable.no_image)) else view.setImageDrawable(view.context.getDrawable(currentExhibit.exhibitImage))
        }
    }

    init {
        ttsUtils = TTSUtils(mContext.getString(R.string.api_key))
        searchUtils = SearchUtils(mContext, this)
        guideUtils = GuideUtils(this)
    }
}