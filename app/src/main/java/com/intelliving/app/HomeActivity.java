package com.intelliving.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.alibaba.fastjson.JSONObject;
import com.google.firebase.BuildConfig;
import com.intelliving.app.R;
import com.intelliving.app.firebase.ComelitFirebaseMessagingService;
import com.intelliving.app.utils.Utils;
import com.comelitgroup.module.api.CGAudioSettings;
import com.comelitgroup.module.api.CGCallStartReceiver;
import com.comelitgroup.module.api.CGCallbackInt;
import com.comelitgroup.module.api.CGError;
import com.comelitgroup.module.api.CGModule;
import com.comelitgroup.module.api.CGParameter;
import com.comelitgroup.module.api.CGResponse;
import com.comelitgroup.module.managers.VipSystemManager;
import com.google.firebase.messaging.RemoteMessage;

import io.dcloud.PandoraEntry;

public class HomeActivity extends PandoraEntry implements CGCallbackInt, LifecycleObserver {

    private static final String TAG = "HomeActivity";

    RemoteMessage pushMessage = null;

    CGCallbackInt callback;
    CGCallStartReceiver callStartEventCallback;

    CGModule cgModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ComelitFirebaseMessagingService.updateStoredTokenIfNeeded(this);
        Intent intent = getIntent();
        initAppLifecycleHandler();

        handleIntent(intent);
        System.out.println("onCreate..................12231");

        //use this api to enable/disable the internal management of the ringtone (enabled by default starting from android 10)
        cgModule= CGModule.getInstance(getApplicationContext());
        cgModule.setEnableRingtone(true);

        connectToSystem(this,"perfect10.tplinkdns.com",64100,"57cdd7","","","");

    }

    public String connectToSystem(Context context, String hostname, int port, String activationCode, String userId, String unitId, String serverHost){
        if(hostname==null || "".equals(hostname)){
            return "Vcp is empty";
        }
//        hostname="192.168.2.200";
        activationCode="57cdd7";
//        port=64300;
        Log.i("VcpInterface", "hostname ....................."+hostname+" "+activationCode+" "+userId+" "+unitId);
        try {

            SharedPreferences sharedPreferences = context.getSharedPreferences("VCP_INFO", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("host", hostname);
            editor.putInt("port", port);
            editor.putString("actCode", activationCode);
            editor.apply();
        }catch (Exception e) {
            return "SharedPreferences error";
        }
        String token="";
        try {
            token= ComelitFirebaseMessagingService.getToken(context);

            Log.i("VcpInterface", "Hostname: " + hostname + ", port: " + port + ", activationCode: " + activationCode
                    + ", token: " + token );
            if (token.isEmpty()) {
                Log.e("VcpInterface", "invalid push token!");
                return "Token null";
            }
        }catch (Exception e){
            return "Token error";
        }

        try{
            boolean withUI = true;
            boolean softwareDecode = true;//sharedPreference.getBoolean(Utils.SOFTWARE_VIDEO_DECODE_KEY,true);

            String repeatKey="hello5";
            SharedPreferences sharedPreferences = context.getSharedPreferences("VCP_INFO", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(repeatKey, "");
            editor.apply();


            CGAudioSettings audioSettings = Utils.loadAudioSettings(context).build();
            CGParameter parameters = new CGParameter.CGParameterBuilder(hostname, port, activationCode).
                    setConnectionCallback(new CGCallbackInt() {
                        @Override
                        public void onConnect() {
                            Log.i("VcpInterface","connect success!");
                        }

                        @Override
                        public void onDisconnect() {

                        }

                        @Override
                        public void onError(CGError cgError) {
                            Log.e("VcpInterface","connect error!"+cgError);
                            if("".equals(sharedPreferences.getString(repeatKey,"")) && cgError==CGError.ACTIVATION_CODE_ERROR){
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(repeatKey, "true");
                                editor.apply();

                                //重新请求，生成code
                                JSONObject param=new JSONObject();
                                param.put("ownerId",userId);
                                param.put("unitId",unitId);
//                                String actCode=dispatch(serverHost,param.toJSONString());
//                                editor.putString(repeatKey, "");
//                                editor.apply();
//                                if(!"".equals(actCode)){
//                                    connectToSystem(context, hostname,port, actCode, userId, unitId, serverHost);
//                                }
                            }

                        }
                    }).
                    setPushToken(token).
                    useComelitUI(withUI).
                    enableSoftwareDecode(softwareDecode).
                    setAudioSettings(audioSettings).
                    build();

            CGResponse cgResponse= CGModule.getInstance(context).connect(parameters);
        }catch (Exception e){
            e.printStackTrace();
            return "Connect error";
        }

        return "";
    }

    /**
     * lifecycle event init/clean
     */
    private void initAppLifecycleHandler(){
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG,"onNewIntent");
        handleIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectFromSystem();
        cleanAppLifecycleHandler();
        Log.d(TAG,"onDestroy");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        disconnectFromSystem();
        Log.d(TAG,"onBackPressed");
    }

    /**
     * lifecycle event receiver
     */

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        Log.i(TAG," app moved to background");
        disconnectFromSystem();
        runOnUiThread(() -> finish());
    }

    /**
     * internal method to handle launch intent for this activity
     */
    private void handleIntent(Intent intent){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //if we are already connected it means that the user opened the app from the launcher
            //we just need to get tell the sdk that the has been opened so it can start our call activity
            if (CGModule.getInstance(getApplicationContext()).status() == VipSystemManager.VipSystemConnectionStatus.VIP_CONNECTED) {
                SharedPreferences sharedPreference = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
                boolean withUI = sharedPreference.getBoolean(Utils.COMELIT_INTERNAL_CALL_UI_KEY,false);
                boolean softwareDecode = sharedPreference.getBoolean(Utils.SOFTWARE_VIDEO_DECODE_KEY,true);
                if (!withUI) {
                    callStartEventCallback = new CallStartEventHandler(this, softwareDecode);
                    CGModule.getInstance(getApplicationContext()).setCallStartReceiver(callStartEventCallback);
                }
                CGModule.getInstance(getApplicationContext()).notifyPendingCall();
                onConnect();
                return;
            }
        }
        SharedPreferences vcpInfo = getSharedPreferences("VCP_INFO", MODE_PRIVATE);
        if(!"".equals(vcpInfo.getString("host",""))){
            connectToSystem(vcpInfo.getString("host",""),vcpInfo.getInt("port",64100),vcpInfo.getString("actCode",""));
        }
    }

    private void cleanAppLifecycleHandler(){
        ProcessLifecycleOwner.get().getLifecycle().removeObserver(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }


    /**
     * CGCallbackInt inteface methods
     */

    @Override
    public void onConnect() {
        Log.i(TAG, "Connected");
    }

    @Override
    public void onDisconnect() {
        Log.i(TAG, "onDisconnect");

    }

    @Override
    public void onError(CGError cgError) {
        Log.i(TAG, "Connection error");
    }

    /**
     * internal methods
     * */
    private void connectToSystem(String hostname,int port,String activationCode){
        SharedPreferences sharedPreference = getSharedPreferences(
                getString(R.string.preference_file_key), MODE_PRIVATE);

//        String hostname = "192.168.2.200";//sharedPreference.getString("hostname", "");
//        int port = 64100;//sharedPreference.getInt("port", 64100);
////        acdotg
//        String activationCode = "h6z5io";//sharedPreference.getString("activation_code", "");
        String token = ComelitFirebaseMessagingService.getToken(this);
        boolean withUI = true;//sharedPreference.getBoolean(Utils.COMELIT_INTERNAL_CALL_UI_KEY,true);
        boolean softwareDecode = true;//sharedPreference.getBoolean(Utils.SOFTWARE_VIDEO_DECODE_KEY,true);

        callback = this;
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

        if (pushMessage == null){
            Log.i(TAG,"Connect to system");
           CGResponse cgResponse= CGModule.getInstance(getApplicationContext()).connect(parameters);
           Log.i(TAG,cgResponse.toString());
        } else {

            Log.i(TAG,"Connect to system for call");
            if (withUI)
                CGModule.getInstance(getApplicationContext()).handlePushNotification(pushMessage,callback,softwareDecode);
            else
                CGModule.getInstance(getApplicationContext()).handlePushNotificationWithoutUI(pushMessage,callback,softwareDecode);

            pushMessage = null;
        }
        System.out.println("connect success....");
    }

    private void disconnectFromSystem(){
        if(getResources().getBoolean(R.bool.disable_disconnect)) {
            Log.d(TAG, "Disconnect is disabled");
            return;
        }
        CGModule.getInstance(getApplicationContext()).disconnect();
    }

    private String getHomeTitle() {
        return getString(R.string.menu_home) + " " + BuildConfig.VERSION_NAME;
    }
}
