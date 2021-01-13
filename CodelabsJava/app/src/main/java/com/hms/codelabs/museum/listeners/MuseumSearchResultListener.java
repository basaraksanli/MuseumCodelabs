package com.hms.codelabs.museum.listeners;

import com.huawei.hms.site.api.model.Site;

import java.util.List;

/**
 * Museum Search Listener for Site Kit Nearby Search
 */
public interface MuseumSearchResultListener {
    void onResult(List<Site> result);
    void onFailure(String errorMessage);
}
