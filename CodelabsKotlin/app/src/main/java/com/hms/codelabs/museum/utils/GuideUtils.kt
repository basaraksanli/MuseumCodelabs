package com.hms.codelabs.museum.utils

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.hms.codelabs.museum.SharedViewModel
import com.hms.codelabs.museum.models.Constant
import com.hms.codelabs.museum.models.Exhibit
import com.huawei.hms.common.ApiException
import com.huawei.hms.nearby.Nearby
import com.huawei.hms.nearby.StatusCode
import com.huawei.hms.nearby.discovery.Distance
import com.huawei.hms.nearby.message.*
import java.util.*

class GuideUtils(var viewModel: SharedViewModel) {
    private var messageEngine: MessageEngine? = null
    private var mMessageHandler: MessageHandler? = null

    /**
     * Start Scanning Beacons nearby
     * Message engine is defined and registered here
     * 3 function of the message handler are used
     * OnFound OnDistanceChanged OnLost
     */
    fun startScanning(activity: Activity) {
        messageEngine = Nearby.getMessageEngine(activity)
        messageEngine!!.registerStatusCallback(StatusCallback())
        mMessageHandler = object : MessageHandler() {
            override fun onFound(message: Message) {
                super.onFound(message)
                doOnFound(message)
            }

            override fun onLost(message: Message) {
                super.onLost(message)
                doOnLost(message)
            }

            override fun onDistanceChanged(message: Message, distance: Distance) {
                super.onDistanceChanged(message, distance)
                doOnDistanceChanged(message, distance)
            }
        }
        val msgPicker = MessagePicker.Builder().includeAllTypes().build()
        val policy = Policy.Builder().setTtlSeconds(Policy.POLICY_TTL_SECONDS_INFINITE).build()
        val getOption = GetOption.Builder().setPicker(msgPicker).setPolicy(policy).build()
        val task = Nearby.getMessageEngine(activity)[mMessageHandler, getOption]
        /**
         * Message Handler is registered to message engine here
         */
        task.addOnSuccessListener { Toast.makeText(activity.applicationContext, "SUCCESS", Toast.LENGTH_SHORT).show() }
                .addOnFailureListener { e: Exception? ->
                    Log.e("Beacon", "register failed:", e)
                    if (e is ApiException) {
                        when (e.statusCode) {
                            StatusCode.STATUS_MESSAGE_AUTH_FAILED -> {
                                Toast.makeText(activity.applicationContext, "configuration_error", Toast.LENGTH_SHORT).show()
                            }
                            StatusCode.STATUS_MESSAGE_APP_UNREGISTERED -> {
                                Toast.makeText(activity.applicationContext, "permission_error", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(activity.applicationContext, "start get beacon message failed", Toast.LENGTH_SHORT)
                                        .show()
                            }
                        }
                    } else {
                        Toast.makeText(activity.applicationContext, "start get beacon message failed", Toast.LENGTH_SHORT)
                                .show()
                    }
                }
    }

    /**
     * Do on lost function. It removes the beacon and its artifact's information
     */
    private fun doOnLost(message: Message?) {
        message?.let {
            val messageContent = String(it.content)
            val id = messageContent.toInt()
            exhibitDistances.remove(id)
            downloadedExhibits.removeAt(id)
        }
    }

    /**
     * Disable searching nearby beacons
     */
    fun ungetMessageEngine() {
        if (messageEngine != null && mMessageHandler != null) {
            Log.i("Beacon", "unget")
            messageEngine!!.unget(mMessageHandler)
        }
    }

    /**
     * doOnFound downloads the beacon's artifact's information
     */
    private fun doOnFound(message: Message?) {
        message?.let {
            val type = it.type
            val messageContent = String(it.content)
            Log.wtf("Beacon", "New Message:\$messageContent type:\$type")
            if (type.equals("No", ignoreCase = true)) downloadArtifact(messageContent)
        }
    }

    /**
     * Download Artifact Information
     */
    private fun downloadArtifact(messageContent: String) {
        val exhibit = Constant.exhibitInfo[messageContent.toInt()]
        downloadedExhibits.add(exhibit)
    }

    /**
     * doOnDistanceChanged, calculates every artifact's distance to the user. If the user close enough to closest beacon, UI updates and shows the related artifact info
     */
    private fun doOnDistanceChanged(message: Message?, distance: Distance) {
        message?.let {
            val type = it.type
            val messageContent = String(it.content)
            Log.wtf("Beacon", "New Message:" + messageContent + " type:" + type + "Distance: " + distance.meters)
            if (type.equals("No", ignoreCase = true)) operateOnDistanceChanged(messageContent, distance)
        }
    }

    /**
     * extension of doOnDistanceChanged
     */
    private fun operateOnDistanceChanged(messageContent: String, distance: Distance) {
        val id = messageContent.toInt()
        exhibitDistances[id] = distance
        val closestIndex = findClosest()
        val exhibitRange = Constant.EXHIBIT_DETECT_RANGE
        if (closestIndex!!.value.meters < exhibitRange) {
            val closestInfo = findExhibitInformation(closestIndex.key)
            updateUI(closestInfo, closestIndex, exhibitRange)
        }
    }

    /**
     * Updates Virtual Guide UI
     */
    private fun updateUI(closestInfo: Exhibit?, closestIndex: Map.Entry<Int, Distance>?, exhibitRange: Int) {
        if (closestIndex!!.value.meters < exhibitRange) {
            closestInfo?.let {
                viewModel.currentExhibit.postValue(it)
                viewModel.currentMuseumName.postValue(it.museumName)
            }
        } else {
            viewModel.currentExhibit.setValue(null)
        }
    }

    /**
     * find the artifact in the downloaded artifacts
     */
    private fun findExhibitInformation(id: Int): Exhibit? {
        for (exhibit in downloadedExhibits) {
            if (exhibit.exhibitID == id) return exhibit
        }
        return null
    }

    /**
     * find the closes artifact in the list
     */
    private fun findClosest(): Map.Entry<Int, Distance>? {
        var closest: Map.Entry<Int, Distance>? = null
        for (entry in exhibitDistances.entries) {
            if (closest == null || entry.value < closest.value) {
                closest = entry
            }
        }
        return closest
    }

    companion object {
        /**
         * Downloaded artifact List
         * artifact distances to the user list
         */
        var downloadedExhibits = ArrayList<Exhibit>()
        var exhibitDistances = HashMap<Int, Distance>()
    }
}