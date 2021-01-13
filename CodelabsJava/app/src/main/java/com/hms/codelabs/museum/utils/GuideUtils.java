package com.hms.codelabs.museum.utils;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.hms.codelabs.museum.SharedViewModel;
import com.hms.codelabs.museum.models.Constant;
import com.hms.codelabs.museum.models.Exhibit;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.nearby.Nearby;
import com.huawei.hms.nearby.StatusCode;
import com.huawei.hms.nearby.discovery.Distance;
import com.huawei.hms.nearby.message.GetOption;
import com.huawei.hms.nearby.message.Message;
import com.huawei.hms.nearby.message.MessageEngine;
import com.huawei.hms.nearby.message.MessageHandler;
import com.huawei.hms.nearby.message.MessagePicker;
import com.huawei.hms.nearby.message.Policy;
import com.huawei.hms.nearby.message.StatusCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GuideUtils {

    SharedViewModel viewModel;

    private MessageEngine messageEngine;
    private MessageHandler mMessageHandler;

    /**
     * Downloaded artifact List
     * artifact distances to the user list
     */
    public static ArrayList<Exhibit> downloadedExhibits = new ArrayList<>();
    public static HashMap<Integer, Distance> exhibitDistances = new HashMap<>();

    public GuideUtils(SharedViewModel viewModel){
        this.viewModel = viewModel;
    }

    /**
     * Start Scanning Beacons nearby
     * Message engine is defined and registered here
     * 3 function of the message handler are used
     * OnFound OnDistanceChanged OnLost
     */
    public void startScanning(Activity activity) {
        messageEngine = Nearby.getMessageEngine(activity);
        messageEngine.registerStatusCallback(new StatusCallback());

        mMessageHandler = new MessageHandler() {
            @Override
            public void onFound(Message message) {
                super.onFound(message);
                doOnFound(message);
            }

            @Override
            public void onLost(Message message) {
                super.onLost(message);
                doOnLost(message);
            }

            @Override
            public void onDistanceChanged(Message message, Distance distance) {
                super.onDistanceChanged(message, distance);
                doOnDistanceChanged(message, distance);
            }
        };


        MessagePicker msgPicker = new MessagePicker.Builder().includeAllTypes().build();
        Policy policy = new Policy.Builder().setTtlSeconds(Policy.POLICY_TTL_SECONDS_INFINITE).build();
        GetOption getOption = new GetOption.Builder().setPicker(msgPicker).setPolicy(policy).build();
        Task<Void> task = Nearby.getMessageEngine(activity).get(mMessageHandler, getOption);

        /**
         * Message Handler is registered to message engine here
         */
        task.addOnSuccessListener(aVoid -> Toast.makeText(activity.getApplicationContext(), "SUCCESS", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Log.e("Beacon", "register failed:", e);
                    if (e instanceof ApiException) {
                        switch (((ApiException) e).getStatusCode()) {
                            case StatusCode.STATUS_MESSAGE_AUTH_FAILED: {
                                Toast.makeText(activity.getApplicationContext(), "configuration_error", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            case StatusCode.STATUS_MESSAGE_APP_UNREGISTERED: {
                                Toast.makeText(activity.getApplicationContext(), "permission_error", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            default: {
                                Toast.makeText(activity.getApplicationContext(), "start get beacon message failed", Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            }
                        }
                    } else {
                        Toast.makeText(activity.getApplicationContext(), "start get beacon message failed", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    /**
     * Do on lost function. It removes the beacon and its artifact's information
     */
    private void doOnLost(Message message) {
        if (message == null) {
            return;
        }
        String messageContent = new String(message.getContent());
        int id = Integer.parseInt(messageContent);
        exhibitDistances.remove(id);
        downloadedExhibits.remove(id);
    }

    /**
     * Disable searching nearby beacons
     */
    public void ungetMessageEngine() {
        if (messageEngine != null && mMessageHandler != null) {
            Log.i("Beacon", "unget");
            messageEngine.unget(mMessageHandler);
        }
    }
    /**
     * doOnFound downloads the beacon's artifact's information
     */
    private void doOnFound(Message message) {
        if (message == null) {
            return;
        }
        String type = message.getType();
        String messageContent = new String(message.getContent());
        Log.wtf("Beacon", "New Message:$messageContent type:$type");
        if (type.equalsIgnoreCase("No"))
            downloadArtifact(messageContent);
    }
    /**
     * Download Artifact Information
     */
    private void downloadArtifact(String messageContent) {
        Exhibit exhibit = Constant.exhibitInfo.get(Integer.parseInt(messageContent));
        downloadedExhibits.add(exhibit);
    }

    /**
     * doOnDistanceChanged, calculates every artifact's distance to the user. If the user close enough to closest beacon, UI updates and shows the related artifact info
     */
    private void doOnDistanceChanged(Message message, Distance distance) {
        if (message == null) {
            return;
        }
        String type = message.getType();
        String messageContent = new String(message.getContent());
        Log.wtf("Beacon", "New Message:" + messageContent + " type:" + type + "Distance: " + distance.getMeters());
        if (type.equalsIgnoreCase("No"))
            operateOnDistanceChanged(messageContent, distance);
    }

    /**
     * extension of doOnDistanceChanged
     */
    private void operateOnDistanceChanged(String messageContent, Distance distance) {
        int id = Integer.parseInt(messageContent);
        exhibitDistances.put(id, distance);

        Map.Entry<Integer, Distance> closestIndex = findClosest();

        int exhibitRange = Constant.EXHIBIT_DETECT_RANGE;

        if (closestIndex.getValue().getMeters() < exhibitRange) {
            Exhibit closestInfo = findExhibitInformation(closestIndex.getKey());
            updateUI(closestInfo , closestIndex, exhibitRange);
        }
    }

    /**
     * Updates Virtual Guide UI
     */
    private void updateUI(Exhibit closestInfo, Map.Entry<Integer, Distance> closestIndex, int exhibitRange) {
        if (closestIndex.getValue().getMeters()< exhibitRange){
            if (closestInfo != null) {
                viewModel.currentExhibit.postValue(closestInfo);
                viewModel.currentMuseumName.postValue(closestInfo.getMuseumName());
            }
        } else{
            viewModel.currentExhibit.setValue(null);
        }
    }


    /**
     * find the artifact in the downloaded artifacts
     */
    private Exhibit findExhibitInformation(int id) {
        for (Exhibit exhibit : downloadedExhibits) {
            if (exhibit.getExhibitID() == id)
                return exhibit;
        }
        return null;
    }

    /**
     * find the closes artifact in the list
     */
    private Map.Entry<Integer, Distance> findClosest() {
        Map.Entry<Integer, Distance> closest = null;
        for (Map.Entry<Integer, Distance> entry : exhibitDistances.entrySet()) {
            if (closest == null || entry.getValue().compareTo(closest.getValue()) < 0) {
                closest = entry;
            }
        }
        return closest;
    }

}
