package com.intelliving.app;

import android.util.Log;

import com.intelliving.app.firebase.ComelitFirebaseMessagingService;
import com.intelliving.app.utils.Utils;
import com.comelitgroup.module.api.CGAudioSettings;
import com.comelitgroup.module.api.CGCallStartReceiver;
import com.comelitgroup.module.api.CGCallbackInt;
import com.comelitgroup.module.api.CGModule;
import com.comelitgroup.module.api.CGParameter;

import io.dcloud.application.DCloudApplication;

/**
 * This ensure native libraries are loaded before any code can try to access them
 */
public class ExampleApplication extends DCloudApplication {
    private static final String TAG = "ExampleApplication";
    CGCallbackInt callback;
    CGCallStartReceiver callStartEventCallback;

    @Override
    public void onCreate() {
        super.onCreate();

        CGModule.getInstance(this);
//        connectToSystem();
    }

    private void connectToSystem(){

        String hostname = "192.168.2.200";//sharedPreference.getString("hostname", "");
        int port = 64100;//sharedPreference.getInt("port", 64100);
        String activationCode = "0w3k42";//sharedPreference.getString("activation_code", "");
        String token = ComelitFirebaseMessagingService.getToken(this);
        boolean withUI = false;//sharedPreference.getBoolean(Utils.COMELIT_INTERNAL_CALL_UI_KEY,false);
        boolean softwareDecode = false;//sharedPreference.getBoolean(Utils.SOFTWARE_VIDEO_DECODE_KEY,true);

        callStartEventCallback = new CallStartEventHandler(this,softwareDecode);

        Log.i(TAG, "Hostname: " + hostname + ", port: " + port + ", activationCode: " + activationCode
                + ", token: " + token + ", withUI: " + withUI + ", softwareDecode: " + softwareDecode);

        if (token.isEmpty()) {
            Log.e(TAG,"invalid push token!");
            return;
        }
        //read audio settings from app preferences (optional)
        CGAudioSettings audioSettings = Utils.loadAudioSettings(this).build();

        CGParameter parameters = new CGParameter.CGParameterBuilder(hostname, port, activationCode).
                setConnectionCallback(callback).
                setPushToken(token).
                useComelitUI(withUI).
                enableSoftwareDecode(softwareDecode).
                setAudioSettings(audioSettings).
                build();


        if (!withUI) {
            Log.i(TAG,"using external UI, set call start receiver");
            CGModule.getInstance(getApplicationContext()).setCallStartReceiver(callStartEventCallback);
        }
        CGModule.getInstance(this).connect(parameters);
//
//        if (pushMessage == null){
//            Log.i(TAG,"Connect to system");
//            CGModule.getInstance(getApplicationContext()).connect(parameters);
//        } else {
//
//            Log.i(TAG,"Connect to system for call");
//            if (withUI)
//                CGModule.getInstance(getApplicationContext()).handlePushNotification(pushMessage,callback,softwareDecode);
//            else
//                CGModule.getInstance(getApplicationContext()).handlePushNotificationWithoutUI(pushMessage,callback,softwareDecode);
//
//            pushMessage = null;
//        }
    }


}
