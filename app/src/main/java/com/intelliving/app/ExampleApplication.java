package com.intelliving.app;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.comelitgroup.module.api.CGModule;

import io.dcloud.application.DCloudApplication;

/**
 * This ensure native libraries are loaded before any code can try to access them
 */
public class ExampleApplication extends DCloudApplication implements DefaultLifecycleObserver {
    private static final String TAG = "ExampleApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        // App entered foreground (at least one activity is in a started state)
        Log.d(TAG, "App entered foreground");
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        // App entered background (no activities are in a started state)
        Log.d(TAG, "App entered background");
        disconnectFromSystem();
    }

    private void disconnectFromSystem(){
        if(getResources().getBoolean(R.bool.disable_disconnect)) {
            Log.d(TAG, "Disconnect is disabled");
            return;
        }
        CGModule.getInstance(getApplicationContext()).disconnect();
    }
}
