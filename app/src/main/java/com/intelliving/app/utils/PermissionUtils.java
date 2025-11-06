package com.intelliving.app.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;


public class PermissionUtils {
    public static boolean hasFullScreenIntentPermission(Context context) {
        return ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.USE_FULL_SCREEN_INTENT
        ) == PackageManager.PERMISSION_GRANTED;
    }
}
