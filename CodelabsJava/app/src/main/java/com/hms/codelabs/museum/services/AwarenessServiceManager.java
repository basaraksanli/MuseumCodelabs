package com.hms.codelabs.museum.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import com.huawei.hms.kit.awareness.barrier.BarrierStatus;


/**
 * Creates an IntentService.  Invoked by the subclass's constructor.
 */
public class AwarenessServiceManager extends IntentService {
    public AwarenessServiceManager() {
        super("AwarenessService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            Log.e("Barrier Service:", "Intent is null");
            return;
        }
        // Barrier information is transferred through intents. Parse the barrier information using the Barrier.extract method.
        BarrierStatus barrierStatus = BarrierStatus.extract(intent);
        // Obtain the label and current status of the barrier through BarrierStatus.
        String barrierLabel = barrierStatus.getBarrierLabel();
        int status = barrierStatus.getPresentStatus();
        if(status == BarrierStatus.TRUE)
            MuseumNotificationManager.sendNotification(this, "There is an awesome nearby Museum waiting for you to explore! ", barrierLabel);

    }


    /**
     * Create notification channel for Awareness Barriers
     */
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(getSystemService(NotificationManager.class) == null)
                return;
            Notification notification = new Notification.Builder(this, MuseumNotificationManager.CHANNEL_ID).build();
            startForeground(1, notification);
        }
    }
}
