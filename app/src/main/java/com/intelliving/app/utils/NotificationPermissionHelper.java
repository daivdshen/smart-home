package com.intelliving.app.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NotificationPermissionHelper {
    // 通知权限请求码
    public static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    /**
     * 检查是否拥有通知权限
     */
    public static boolean hasNotificationPermission(Activity activity) {
        // Android 13及以上需要检查POST_NOTIFICATIONS权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    activity,
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 13以下默认拥有通知权限
            return true;
        }
    }

    /**
     * 请求通知权限
     */
    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 检查是否需要显示权限说明
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    android.Manifest.permission.POST_NOTIFICATIONS
            )) {
                // 显示权限说明对话框
                showPermissionRationaleDialog(activity);
            } else {
                // 直接请求权限
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                );
            }
        }
    }

    /**
     * 显示权限说明对话框
     */
    private static void showPermissionRationaleDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("需要通知权限")
                .setMessage("In order to promptly push you important messages and reminders, please allow us to send notifications")
                .setPositiveButton("Sure", (dialog, which) -> {
                    // 用户确认后再次请求权限
                    ActivityCompat.requestPermissions(
                            activity,
                            new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                            NOTIFICATION_PERMISSION_REQUEST_CODE
                    );
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 处理权限请求结果
     */
    public static void handlePermissionResult(
            int requestCode,
            int[] grantResults,
            OnPermissionResultListener listener
    ) {
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予
                if (listener != null) {
                    listener.onGranted();
                }
            } else {
                // 权限被拒绝
                if (listener != null) {
                    listener.onDenied();
                }
            }
        }
    }

    // 权限结果监听接口
    public interface OnPermissionResultListener {
        void onGranted();
        void onDenied();
    }
}
