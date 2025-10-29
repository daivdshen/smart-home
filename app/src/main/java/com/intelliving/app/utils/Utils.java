package com.intelliving.app.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;

import com.intelliving.app.R;
import com.comelitgroup.module.api.CGAudioSettings;

import java.util.Random;

/**
 * Created by Simone Mutti on 2019.
 * Comelit Group SpA
 * simone.mutti@comelit.it
 */
public class Utils {


    public static final String AUDIO_SETTINGS_PREFERENCES = "a3rdpartyexample.preferences.audiosettings";


    public static final String HOSTNAME_KEY = "hostname";
    public static final String PORT_KEY = "port";
    public static final String ACTIVATION_CODE_KEY = "activation_code";
    public static final String COMELIT_INTERNAL_CALL_UI_KEY = "COMELIT_INTERNAL_CALL_UI_KEY";
    public static final String SOFTWARE_VIDEO_DECODE_KEY = "SOFTWARE_VIDEO_DECODE_KEY";

    public static final String NOISE_SUPPRESSOR_KEY = "advanced_noise_suppressor";
    public static final String ANDROID_ECHO_CANCELLATION_KEY = "advanced_echo_cancellation";
    public static final String NATIVE_ECHO_CANCELLATION_KEY = "native_echo_cancellation";
    public static final String NATIVE_ECHO_LEVEL_KEY = "native_echo_level";
    public static final String NATIVE_ECHO_DELAY_KEY = "native_echo_delay";
    public static final String GAIN_MIC_KEY = "gain_mic";
    public static final String GAIN_SPK_KEY = "gain_spk";
    public static final String LIMIT_AMPL_KEY = "limit_ampl";

    public static final boolean NOISE_SUPPRESSOR_DEFAULT = true;
    public static final boolean ANDROID_ECHO_CANCELLATION_DEFAULT = false;
    public static final boolean NATIVE_ECHO_CANCELLATION_DEFAULT = true;
    public static final int NATIVE_ECHO_LEVEL_DEFAULT = 3;
    public static final int NATIVE_ECHO_DELAY_DEFAULT = 120;
    public static final float GAIN_MIC_DEFAULT_VALUE = 1.0F;
    public static final float GAIN_SPK_DEFAULT_VALUE = 1.0F;

    public static int calculateNoOfColumns(Context context,
            float columnWidthDp) { // For example columnWidthdp=180
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns =
                (int) (screenWidthDp / columnWidthDp + 0.5); // +0.5 for correct rounding to int.
        return noOfColumns;
    }

    public static int getPreviewForElement(String id, DataItem.VipType type) {
        Random ran = new Random(id.hashCode());
        int imgIndex;

        switch (type) {
            case EXTERNAL_UNIT:
                imgIndex = ran.nextInt(4) + 1;
                if (imgIndex == 1) {
                    return R.drawable.external_unit_1;
                } else if (imgIndex == 2) {
                    return R.drawable.external_unit_2;
                } else if (imgIndex == 3) {
                    return R.drawable.external_unit_3;
                } else {
                    return R.drawable.external_unit_4;
                }
            case INTERNAL_UNIT:
                imgIndex = ran.nextInt(4) + 1;
                if (imgIndex == 1) {
                    return R.drawable.internal_unit_1;
                } else if (imgIndex == 2) {
                    return R.drawable.internal_unit_2;
                } else if (imgIndex == 3) {
                    return R.drawable.internal_unit_3;
                } else {
                    return R.drawable.internal_unit_4;
                }
            case RTSP_CAMERA:
            case PALIP:
                imgIndex = ran.nextInt(2) + 1;
                if (imgIndex == 1) {
                    return R.drawable.camera_1;
                } else {
                    return R.drawable.camera_2;
                }
            case SWITCHBOARD:
                return R.drawable.switchboard_1;
            case ACTUATOR:
                imgIndex = ran.nextInt(3) + 1;
                if (imgIndex == 1) {
                    return R.drawable.opendoor_1;
                } else if (imgIndex == 2) {
                    return R.drawable.opendoor_2;
                } else {
                    return R.drawable.opendoor_3;
                }
            case OPENDOOR:
                imgIndex = ran.nextInt(2) + 1;
                if (imgIndex == 1) {
                    return R.drawable.opendoor_1;
                } else {
                    return R.drawable.opendoor_2;
                }

        }
        return R.drawable.external_unit_1;
    }

    public static CGAudioSettings.CGAudioSettingsBuilder loadAudioSettings(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AUDIO_SETTINGS_PREFERENCES,MODE_PRIVATE);
        CGAudioSettings.CGAudioSettingsBuilder audioSettingsBuilder = new CGAudioSettings.CGAudioSettingsBuilder();
        audioSettingsBuilder.setNativeEchoCancellation(sharedPreferences.getBoolean(NATIVE_ECHO_CANCELLATION_KEY,NATIVE_ECHO_CANCELLATION_DEFAULT));
        audioSettingsBuilder.setNativeEchoLevel(sharedPreferences.getInt(NATIVE_ECHO_LEVEL_KEY,NATIVE_ECHO_LEVEL_DEFAULT));
        audioSettingsBuilder.setNativeEchoDelay(sharedPreferences.getInt(NATIVE_ECHO_DELAY_KEY,NATIVE_ECHO_DELAY_DEFAULT));

        audioSettingsBuilder.setAndroidEchoCancellation(sharedPreferences.getBoolean(ANDROID_ECHO_CANCELLATION_KEY,ANDROID_ECHO_CANCELLATION_DEFAULT));
        audioSettingsBuilder.setNoiseSuppressor(sharedPreferences.getBoolean(NOISE_SUPPRESSOR_KEY,NOISE_SUPPRESSOR_DEFAULT));
        audioSettingsBuilder.setLimitAmplification(sharedPreferences.getInt(LIMIT_AMPL_KEY,Short.MAX_VALUE));

        audioSettingsBuilder.setGainSpeaker(sharedPreferences.getFloat(GAIN_SPK_KEY,GAIN_SPK_DEFAULT_VALUE));
        audioSettingsBuilder.setGainMicrophone(sharedPreferences.getFloat(GAIN_MIC_KEY,GAIN_MIC_DEFAULT_VALUE));
        return audioSettingsBuilder;
    }

    public static void saveAudioSettings(Context context, CGAudioSettings audioSettings) {
        SharedPreferences.Editor editor = context.getSharedPreferences(AUDIO_SETTINGS_PREFERENCES,MODE_PRIVATE).edit();
        editor.putBoolean(NATIVE_ECHO_CANCELLATION_KEY,audioSettings.isNativeEchoCancellation());
        editor.putInt(NATIVE_ECHO_LEVEL_KEY,audioSettings.getNativeEchoLevel());
        editor.putInt(NATIVE_ECHO_DELAY_KEY,audioSettings.getNativeEchoDelay());

        editor.putBoolean(ANDROID_ECHO_CANCELLATION_KEY,audioSettings.isAndroidEchoCancellation());
        editor.putBoolean(NOISE_SUPPRESSOR_KEY,audioSettings.isNoiseSuppressor());
        editor.putInt(LIMIT_AMPL_KEY,audioSettings.getLimitAmplification());

        editor.putFloat(GAIN_SPK_KEY,audioSettings.getGainSpeaker());
        editor.putFloat(GAIN_MIC_KEY,audioSettings.getGainMicrophone());
        editor.apply();
    }
}
