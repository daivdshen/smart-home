package com.intelliving.app;

import static com.intelliving.app.utils.Utils.NATIVE_ECHO_DELAY_DEFAULT;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import com.intelliving.app.R;
import com.intelliving.app.utils.Utils;
import com.comelitgroup.module.api.CGAudioSettings;

/**
 * Created by simone.mutti on 18/04/18.
 */
public class AudioSettings extends AppCompatActivity {

    private static final String TAG = "AASettings";

    TextView textViewSeekLevel;
    SeekBar seekBarLevel;
    TextView textViewSeekDelay;
    SeekBar seekBarDelay;
    TextView textViewSeekMic;
    SeekBar seekBarMic;
    TextView textViewSeekSpk;
    SeekBar seekBarSpk;
    TextView textViewSeekLimit;
    SeekBar seekBarLimit;

    CheckBox ns;
    CheckBox aec;
    CheckBox nec;
    Toolbar toolbar;

    private CGAudioSettings.CGAudioSettingsBuilder audioSettingsBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_settings);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        toolbar = findViewById(R.id.home_toolbar);

        setup(getString(R.string.menu_settings));

        audioSettingsBuilder = Utils.loadAudioSettings(this);

        ns = findViewById(R.id.checkBoxNS);
        ns.setOnClickListener(view -> {
            boolean checked = ((CheckBox) view).isChecked();
            audioSettingsBuilder.setNoiseSuppressor(checked);
            Log.i(TAG, "Noise suppressor: " + checked);
        });

        aec = findViewById(R.id.checkBoxAEC);
        aec.setOnClickListener(view -> {
            boolean checked = ((CheckBox) view).isChecked();
            audioSettingsBuilder.setAndroidEchoCancellation(checked);
            Log.i(TAG, "Advanced echo cancellation: " + checked);
        });

        nec = findViewById(R.id.checkBoxNativeEcho);
        nec.setOnClickListener(view -> {
            boolean checked = ((CheckBox) view).isChecked();
            audioSettingsBuilder.setNativeEchoCancellation(checked);
            Log.i(TAG, "Native echo cancellation: " + checked);
            if (checked) {
                seekBarLevel.setEnabled(true);
                seekBarDelay.setEnabled(true);
            } else {
                seekBarLevel.setEnabled(false);
                seekBarDelay.setEnabled(false);
            }
        });

        seekBarLevel = findViewById(R.id.seekBarLevel);
        textViewSeekLevel = findViewById(R.id.textViewSeekLevel);

        String seekLevelText = seekBarLevel.getProgress() + "/" + seekBarLevel.getMax();
        textViewSeekLevel.setText(seekLevelText);
        seekBarLevel.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress = 0;

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                            int progresValue, boolean fromUser) {
                        progress = progresValue;
                        String seekLevelText = progress + "/" + seekBar.getMax();
                        textViewSeekLevel.setText(seekLevelText);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        audioSettingsBuilder.setNativeEchoLevel(progress);
                    }
                });


        seekBarDelay = findViewById(R.id.seekBarDelay);
        textViewSeekDelay = findViewById(R.id.textViewSeekDelay);

        String seekDelayText = seekBarDelay.getProgress() + "/" + seekBarDelay.getMax();
        textViewSeekDelay.setText(seekDelayText);
        seekBarDelay.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress = 0;
                    final int SHIFT = 4;
                    final int MAX = 4;

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                            int progresValue, boolean fromUser) {
                        progress = progresValue - SHIFT;
                        String seekDelayText = progress + "/" + MAX;
                        textViewSeekDelay.setText(seekDelayText);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        int delay = NATIVE_ECHO_DELAY_DEFAULT + (40
                                * progress);
                        audioSettingsBuilder.setNativeEchoDelay(delay);
                    }
                });


        seekBarMic = findViewById(R.id.seekBarMic);
        textViewSeekMic = findViewById(R.id.textViewSeekMic);

        String seekMicText = seekBarMic.getProgress() + "/" + seekBarMic.getMax();
        textViewSeekMic.setText(seekMicText);
        seekBarMic.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress = 0;

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                            int progresValue, boolean fromUser) {
                        progress = progresValue;
                        String seekMicText = progress + "/" + seekBar.getMax();
                        textViewSeekMic.setText(seekMicText);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        float value = progress / 10f;
                        audioSettingsBuilder.setGainMicrophone(value);
                    }
                });

        seekBarSpk = findViewById(R.id.seekBarSpk);
        textViewSeekSpk = findViewById(R.id.textViewSeekSpk);

        String seekSpkText = seekBarSpk.getProgress() + "/" + seekBarSpk.getMax();
        textViewSeekSpk.setText(seekSpkText);
        seekBarSpk.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress = 0;

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                            int progresValue, boolean fromUser) {
                        progress = progresValue;
                        String seekSpkText = progress + "/" + seekBar.getMax();
                        textViewSeekSpk.setText(seekSpkText);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        float value = progress / 10f;
                        audioSettingsBuilder.setGainSpeaker(value);
                    }
                });

        seekBarLimit = findViewById(R.id.seekBarLimit);
        textViewSeekLimit = findViewById(R.id.textViewSeekLimit);

        String seekLimitText = seekBarLimit.getProgress() + "/" + seekBarLimit.getMax();
        textViewSeekLimit.setText(seekLimitText);
        seekBarLimit.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress = 0;

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                            int progresValue, boolean fromUser) {
                        progress = progresValue;
                        String seekLimitText = progress + "/" + seekBar.getMax();
                        textViewSeekLimit.setText(seekLimitText);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        audioSettingsBuilder.setLimitAmplification(progress);
                    }
                });


        setDefault();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Utils.saveAudioSettings(this,audioSettingsBuilder.build());
    }

    private void setDefault() {

        CGAudioSettings currentSettings = audioSettingsBuilder.build();
        boolean value = currentSettings.isNoiseSuppressor();
        ns.setChecked(value);

        boolean value1 = currentSettings.isAndroidEchoCancellation();
        aec.setChecked(value1);

        boolean value2 = currentSettings.isNativeEchoCancellation();
        nec.setChecked(value2);

        seekBarLevel.setProgress(currentSettings.getNativeEchoLevel());
        int delay = currentSettings.getNativeEchoDelay();
        int progress = ((delay - NATIVE_ECHO_DELAY_DEFAULT) / 40) + 4;
        seekBarDelay.setProgress(progress);
        seekBarLevel.setEnabled(value2);
        seekBarDelay.setEnabled(value2);

        float vv = currentSettings.getGainMicrophone();
        int mic = (int) (10 * vv);
        seekBarMic.setProgress(mic);

        float vv1 = currentSettings.getGainSpeaker();
        int spk = (int) (10 * vv1);
        seekBarSpk.setProgress(spk);

        seekBarLimit.setMax(Short.MAX_VALUE);
        seekBarLimit.setProgress(currentSettings.getLimitAmplification());
    }

    protected void setup(String title) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(title);
    }

}