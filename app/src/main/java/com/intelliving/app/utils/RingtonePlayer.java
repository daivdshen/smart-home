package com.intelliving.app.utils;

import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.media.Ringtone;

public class RingtonePlayer {
    public static void playDefaultRingtone(Context context) {
        try {
            // 获取默认铃声的 URI
            Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

            // 如果没有设置铃声，使用通知音
            if (notificationUri == null) {
                notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            if (notificationUri != null) {
                Ringtone ringtone = RingtoneManager.getRingtone(context, notificationUri);
                if (ringtone != null) {
                    ringtone.play();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopRingtone(Context context) {
        try {
            Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if (notificationUri == null) {
                notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            if (notificationUri != null) {
                Ringtone ringtone = RingtoneManager.getRingtone(context, notificationUri);
                if (ringtone != null && ringtone.isPlaying()) {
                    ringtone.stop();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
