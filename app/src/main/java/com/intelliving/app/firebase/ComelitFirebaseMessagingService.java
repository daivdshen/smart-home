package com.intelliving.app.firebase;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.intelliving.app.HomeActivity;
import com.intelliving.app.R;
import com.intelliving.app.ExternalUICallActivity;
import com.intelliving.app.utils.Utils;
import com.comelitgroup.module.api.CGCallbackInt;
import com.comelitgroup.module.api.CGError;
import com.comelitgroup.module.api.CGModule;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by simone.mutti on 13/09/17.
 *
 *  ComelitFirebaseMessagingService is used to handle FCM notifications. Based on the content
 *  of the notification an action will be performed. The supported notifications are:
 *
 *  - incoming call
 *
 */

public class ComelitFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TOKEN_KEY = "token";
    private static final String TAG = " ComelitFCMService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(TAG, "received.... " + remoteMessage.getData());
        try {
            //use this api to customize the call notification
            CGModule.getInstance(getApplicationContext()).setCallNotificationStyle(R.drawable.example_ext_unit_icon_comelit, R.string.app_name, R.color.red);

            //use this api to enable/disable the internal management of the ringtone (enabled by default starting from android 10)
            CGModule.getInstance(getApplicationContext()).setEnableRingtone(true);

            if (remoteMessage.getData().size() > 0) {
                CGModule.getInstance(getApplicationContext()).handlePushNotification(remoteMessage, new CGCallbackInt() {
                    @Override
                    public void onConnect() {
                        Log.i(TAG, "connection from push completed");
                    }

                    @Override
                    public void onDisconnect() {
                        Log.i(TAG, "connection from push, disconnected");
                    }

                    @Override
                    public void onError(CGError error) {
                        //just disconnect in case of error
                        Log.i(TAG, "connection from push CGError:"+error);
                        CGModule.getInstance(getApplicationContext()).disconnect();
                    }
                }, HomeActivity.softwareDecode);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onNewToken(String token) {
        // Get updated InstanceID token.
        Log.d(TAG, "New token: " + token);
        storeFirebaseToken(this,token);
    }

    public static String getToken(Context context) {
        try {
            return context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                    .getString(TOKEN_KEY, "");
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public static void updateStoredTokenIfNeeded(Context context) {
        try {
            new Thread(() -> {
                String token = getToken(context);
                if (token.isEmpty()) {
                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String tokenStr = task.getResult();
                                    storeFirebaseToken(context,tokenStr);
                                    Log.d("FCM", "Device Token: " + token);
                                    // 发送token至服务器
                                } else {
                                    Log.e("FCM", "Token获取失败", task.getException());
                                }
                            });


                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static void storeFirebaseToken(Context context, String token) {
        try {

            SharedPreferences sharedpreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(TOKEN_KEY, token);
            editor.commit();
            Log.d(TAG, "Firebase token stored!");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     *  This occurs when there are too many messages (>100) pending for your app
     *  on a particular device at the time it connects or if the device
     *  hasn't connected to FCM in more than one month.
     */
    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

}
