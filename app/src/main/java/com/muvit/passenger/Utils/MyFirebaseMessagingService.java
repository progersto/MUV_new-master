/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.muvit.passenger.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.muvit.passenger.Activities.FareSummeryActivity;
import com.muvit.passenger.Activities.HomeActivity;
import com.muvit.passenger.Activities.RideInformationActivity;
import com.muvit.passenger.Activities.StartedRideInformationActivity;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";


    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.e(TAG, "From: " + remoteMessage.getData());
        Log.e(TAG, "Notification Message Body: " + remoteMessage.getData().get("body"));
        try {
            //sendNotification(remoteMessage.getData().get("body"));
            String message = (remoteMessage.getData().get("body"));
            JSONObject obj = new JSONObject(message);
            if (obj.getString("type").equalsIgnoreCase("data")) {

                if (obj.getString("redirect").equalsIgnoreCase("fareEstimate")) {
                    EventBus.getDefault().post(new MessageEvent("fareEstimate",
                            "Your ride has been Completed"));
                    //Open FareSummary after ride complete.
                    JSONObject dataAns = obj.getJSONObject("dataAns");
                    Intent i = new Intent(this, FareSummeryActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.putExtra("rideId", dataAns.getString("completedRideId"));
                    if(dataAns.has("timeTaken")) {
                        i.putExtra("timeTaken", dataAns.getString("timeTaken"));
                    }
                    if(dataAns.has("driverId")) {
                        i.putExtra("driverId", dataAns.getString("driverId"));
                    }
                    if(dataAns.has("carId")) {
                        i.putExtra("carId", dataAns.getString("carId"));
                    }
                    startActivity(i);
                } else if (obj.getString("redirect").equalsIgnoreCase("rideInfo")) {
                    //Open Ride info.
                    PrefsUtil.with(getApplicationContext()).write("lastSendId","");
                    JSONObject dataAns = obj.getJSONObject("dataAns");
                    Intent i = new Intent(this, RideInformationActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.putExtra("rideId", dataAns.getString("rideId"));
                    EventBus.getDefault().post(new MessageEvent("rideInfo","Your ride has been Accepted."));
//                    i.putExtra("driverId", dataAns.getString("driverId"));
   //                 i.putExtra("distanceInMeter", dataAns.getString("distanceInMeter"));
                    startActivity(i);
                } else if (obj.getString("redirect").equalsIgnoreCase("cancelride")) {
                    //On Cancel Ride.
                    PrefsUtil.with(getApplicationContext()).write("lastSendId","");
                    EventBus.getDefault().post(new MessageEvent("cancelride","Your ride has been canceled."));
                    sendNotification("Your ride has been canceled.");
                    /*JSONObject dataAns = obj.getJSONObject("dataAns");
                    Intent i = new Intent(this, RideInformationActivity.class);
                    i.putExtra("rideId", dataAns.getString("rideId"));
                    i.putExtra("driverId", dataAns.getString("driverId"));
                    i.putExtra("distanceInMeter", dataAns.getString("distanceInMeter"));
                    startActivity(i);*/
                } else if (obj.getString("redirect").equalsIgnoreCase("rideStarted")) {
                    //On Cancel Ride.
                    EventBus.getDefault().post(new MessageEvent("ridestarted","Your ride has started. Driver tracking will now be unavailable"));
                    JSONObject dataAns = obj.getJSONObject("dataAns");

                    Intent i = new Intent(this, StartedRideInformationActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.putExtra("rideId", dataAns.getString("rideId"));
                    startActivity(i);
                } else if (obj.getString("redirect").equalsIgnoreCase("driverArrived")) {
                    //On Cancel Ride.
                    EventBus.getDefault().post(new MessageEvent("ridestarted","Your driver has arrived. Driver tracking will now be unavailable"));
                    sendNotification("Your driver has arrived you will contacted and ride will start shortly");
                    JSONObject dataAns = obj.getJSONObject("dataAns");

                   /* Intent i = new Intent(this, StartedRideInformationActivity.class);
                    i.putExtra("rideId", dataAns.getString("rideId"));
                    startActivity(i);*/
                }
            } else if (obj.getString("type").equalsIgnoreCase("notification")) {
                sendNotification(obj.getString("message"));
            }
        } catch (Exception e) {

            sendNotification(e.getMessage());
            e.printStackTrace();
        }
        //sendNotification(remoteMessage.getData().get("body"));
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        /*try {
           // sendNotification(remoteMessage.getData().get("body1"));
            String message = messageBody+"";
            JSONObject obj = new JSONObject(message);
            if (obj.getString("type").equalsIgnoreCase("data")) {

            }
        }catch (Exception e){
            e.printStackTrace();
        }*/
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new Notification();
        notification.defaults = Notification.DEFAULT_ALL;
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.nofication_icon)
                .setContentTitle("Book N Ride")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(notification.vibrate)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify((int) System.currentTimeMillis() /* ID of notification */, notificationBuilder.build());
    }


}