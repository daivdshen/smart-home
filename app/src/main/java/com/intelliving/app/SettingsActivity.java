package com.intelliving.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import com.intelliving.app.R;
import com.intelliving.app.utils.Utils;

public class SettingsActivity extends AppCompatActivity {


    private EditText hostnameTxt;
    private EditText portTxt;
    private EditText activationCodeTxt;
    private SharedPreferences sharedPref;
    private SwitchCompat comelitInternalCallUISwitch;
    private SwitchCompat softwareVideoDecodeSwitch;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        hostnameTxt = findViewById(R.id.hostnameTxt);
        portTxt = findViewById(R.id.portTxt);
        activationCodeTxt = findViewById(R.id.activationCodeTxt);
        Button audioSettings = findViewById(R.id.audioSettings);
        comelitInternalCallUISwitch = findViewById(R.id.comelitInternalCallUISwitch);
        softwareVideoDecodeSwitch = findViewById(R.id.softwareVideoDecodeSwitch);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("General Settings");

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);

        hostnameTxt.setText(sharedPref.getString(Utils.HOSTNAME_KEY, ""));
        portTxt.setText(sharedPref.getInt(Utils.PORT_KEY, 64100) + "");
        activationCodeTxt.setText(sharedPref.getString(Utils.ACTIVATION_CODE_KEY, ""));

        comelitInternalCallUISwitch.setChecked(sharedPref.getBoolean(Utils.COMELIT_INTERNAL_CALL_UI_KEY,false));
        softwareVideoDecodeSwitch.setChecked(sharedPref.getBoolean(Utils.SOFTWARE_VIDEO_DECODE_KEY,true));

        audioSettings.setOnClickListener(view ->
                startActivity(new Intent(SettingsActivity.this, AudioSettings.class))
        );
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_done) {
            int port = 64100;

            try {
                port = Integer.parseInt(portTxt.getText().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(Utils.HOSTNAME_KEY, hostnameTxt.getText().toString());
            editor.putInt(Utils.PORT_KEY, port);
            editor.putString(Utils.ACTIVATION_CODE_KEY, activationCodeTxt.getText().toString());

            editor.putBoolean(Utils.COMELIT_INTERNAL_CALL_UI_KEY, comelitInternalCallUISwitch.isChecked());
            editor.putBoolean(Utils.SOFTWARE_VIDEO_DECODE_KEY, softwareVideoDecodeSwitch.isChecked());
            
            editor.commit();
            setResult(Activity.RESULT_OK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }
}
