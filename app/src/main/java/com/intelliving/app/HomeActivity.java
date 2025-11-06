package com.intelliving.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.alibaba.fastjson.JSONObject;
import com.google.firebase.BuildConfig;
import com.intelliving.app.R;
import com.intelliving.app.firebase.ComelitFirebaseMessagingService;
import com.intelliving.app.utils.NotificationPermissionHelper;
import com.intelliving.app.utils.PermissionUtils;
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

public class HomeActivity extends PandoraEntry implements CGCallbackInt {

    private static final String TAG = "HomeActivity";

    public static final boolean withUI = true;
    public static final boolean softwareDecode = true;

    CGCallbackInt callback;
    CGCallStartReceiver callStartEventCallback;

    CGModule cgModule;
    private static final int REQUEST_CODE_FULL_SCREEN_INTENT = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ComelitFirebaseMessagingService.updateStoredTokenIfNeeded(this);
        Intent intent = getIntent();

        handleIntent(intent);
        Log.i(TAG,"onCreate..................12231");

        //use this api to enable/disable the internal management of the ringtone (enabled by default starting from android 10)
        cgModule = CGModule.getInstance(getApplicationContext());
        cgModule.setEnableRingtone(true);

//        CGModule.getInstance(getApplicationContext()).setCallStartReceiver(callStartEventCallback);

        connectToSystem(this,"perfect10.tplinkdns.com",64100,"8i1vg2","","","");

        // 检查并请求通知权限
        if (!NotificationPermissionHelper.hasNotificationPermission(this)) {
            NotificationPermissionHelper.requestNotificationPermission(this);
        }

        checkAndRequestFullScreenIntentPermission();
    }

    @TargetApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private static boolean isAppEligibleForFullScreenIntent(Context context) {
        try {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // 检查应用是否在允许列表中
            return notificationManager.canUseFullScreenIntent();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void checkAndRequestFullScreenIntentPermission() {
        if (!PermissionUtils.hasFullScreenIntentPermission(this)) {
            requestFullScreenIntentPermission();
        } else {
            // 已经有权限，执行相关操作
            Log.i(TAG,"has checkAndRequestFullScreenIntentPermission ");
        }

        // 2. Android 16+ 特殊检查
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            boolean isOk=isAppEligibleForFullScreenIntent(getApplicationContext());

        }
    }

    private void requestFullScreenIntentPermission() {
        // 检查是否需要显示权限说明
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.USE_FULL_SCREEN_INTENT)) {
            showPermissionRationaleDialog();
        } else {
            // 直接请求权限
            requestPermissionDirectly();
        }
    }

    private void requestPermissionDirectly() {
        requestPermissions(
                new String[]{android.Manifest.permission.USE_FULL_SCREEN_INTENT},
                REQUEST_CODE_FULL_SCREEN_INTENT
        );
    }
    private void showPermissionRationaleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Full screen intent permission is required")
                .setMessage("This permission allows the application to display important notifications (such as call reminders) on the lock screen and in full screen mode, providing a better user experience")
                .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissionDirectly();
                    }
                })
                .setNegativeButton("Refuse", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(HomeActivity.this, "Permission denied, some functions may not be available", Toast.LENGTH_SHORT).show();
                    }
                })
                .setCancelable(false)
                .show();
    }

    // 处理权限请求结果
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        NotificationPermissionHelper.handlePermissionResult(
                requestCode,
                grantResults,
                new NotificationPermissionHelper.OnPermissionResultListener() {
                    @Override
                    public void onGranted() {
                        // 权限被授予，执行相应操作
                        Toast.makeText(HomeActivity.this, "Notification permission has been enabled", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDenied() {
                        // 权限被拒绝，提示用户
                        Toast.makeText(HomeActivity.this, "Notification permission has been disabled, which may affect some functions", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    public String connectToSystem(Context context, String hostname, int port, String activationCode, String userId, String unitId, String serverHost){
        if(hostname==null || "".equals(hostname)){
            return "Vcp is empty";
        }
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
                            CGModule.getInstance(getApplicationContext()).setEnableRingtone(true);
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
            CGModule.getInstance(context).setEnableRingtone(true);
            CGResponse cgResponse= CGModule.getInstance(context).connect(parameters);
        }catch (Exception e){
            e.printStackTrace();
            return "Connect error";
        }

        return "";
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
        Log.d(TAG,"onDestroy");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG,"onBackPressed");
    }

    /**
     * internal method to handle launch intent for this activity
     */
    private void handleIntent(Intent intent){
        SharedPreferences vcpInfo = getSharedPreferences("VCP_INFO", MODE_PRIVATE);
        if(!"".equals(vcpInfo.getString("host",""))){
            connectToSystem(vcpInfo.getString("host",""),vcpInfo.getInt("port",64100),vcpInfo.getString("actCode",""));
        }
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

        Log.i(TAG,"Connect to system");
        CGResponse cgResponse= CGModule.getInstance(getApplicationContext()).connect(parameters);
        Log.i(TAG,cgResponse.toString());
    }

    private String getHomeTitle() {
        return getString(R.string.menu_home) + " " + BuildConfig.VERSION_NAME;
    }
}
