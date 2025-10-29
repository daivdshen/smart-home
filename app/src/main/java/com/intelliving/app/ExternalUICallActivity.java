package com.intelliving.app;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.appcompat.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.intelliving.app.R;
import com.comelitgroup.libcomelit.type.CallDirection;
import com.comelitgroup.libcomelit.type.Role;
import com.comelitgroup.module.api.CGCallActivityInformation;
import com.comelitgroup.module.api.CGCallEventCallback;
import com.comelitgroup.module.api.CGCallReleaseType;
import com.comelitgroup.module.api.CGModule;
import com.comelitgroup.module.api.CGPresenter;
import com.comelitgroup.module.api.CGVideoReceiver;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Example of External call UI for the Comelit SDK.
 * ExternalUICallActivity interacts with a CGPresenter of the Comelit SDK to comunicate ui actions.
 * The Comelit SDK will then give a feed back of the actions to the HomeActivity which can notify (if it needs it) ExternalUICallActivity.
 * ExternalUICallActivity should also implements the CGVideoReceiver to display the video when software decode is used.
 * */
public class ExternalUICallActivity extends AppCompatActivity  implements CGVideoReceiver, CGCallEventCallback {

    private final String TAG = "callActivity";

    //intent extras for this activity
    public static final String EXTRA_SOFTWARE_DECODE = "softwareDecode";
    public static final String EXTRA_CALL_CALLER_NAME = "callDescription";
    public static final String EXTRA_CALL_DIRECTION = "callDirection";

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    //needed if hardware video decode is used
    private SurfaceView videoSurfaceView;

    //needed if software video decode is used
    private ImageView videoImageView;

    //needed to let the comelit SDK handle the call logic
    private CGPresenter presenter;

    //internal field
    private String callDescription = "Comelit call";
    private boolean softwareDecode = false;
    private boolean micOn;
    private boolean audioOn;

    private Button opendoorBtn;
    private Button answerBtn;
    private Button muteBtn;
    private Button closeBtn;
    private Button stealBtn;
    private LinearLayout buttonsLayout;
    private TextView infoLabel;

    private boolean fromBackground = false;
    private boolean answeredFromNotification = false;
    /**
     * Activity lifecycle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_uicall);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey(EXTRA_SOFTWARE_DECODE)) {
                softwareDecode = bundle.getBoolean(EXTRA_SOFTWARE_DECODE);
            }

            if (bundle.containsKey(EXTRA_CALL_CALLER_NAME)){
                callDescription = bundle.getString(EXTRA_CALL_CALLER_NAME);
            } else {
                callDescription = "";
            }

            String direction;
            if (bundle.containsKey(CGCallActivityInformation.EXTRA_CALL_INFORMATION)){
                CGCallActivityInformation callInformation = (CGCallActivityInformation)bundle.getSerializable(CGCallActivityInformation.EXTRA_CALL_INFORMATION);
                callDescription = callInformation.getCallerName();
                direction = callInformation.getCallDirection() == CallDirection.OUT ? "outgoing call\n"
                            : callInformation.getCallDirection() == CallDirection.IN  ? "incoming call\n"
                            : "calling\n";
                fromBackground = callInformation.isFromBackground();
                answeredFromNotification = callInformation.isAnsweredFromNotification();
            } else {
                direction = "";
            }

            callDescription = direction + callDescription;

        }

        micOn = true;
        audioOn = false;

        opendoorBtn = findViewById(R.id.buttonOpendoor);
        answerBtn = findViewById(R.id.buttonAnswer);
        muteBtn = findViewById(R.id.buttonMicrophone);
        closeBtn = findViewById(R.id.buttonCloseCall);
        stealBtn = findViewById(R.id.buttonStealVideo);
        videoImageView = findViewById(R.id.imageView);
        videoSurfaceView = findViewById(R.id.surfaceView);
        buttonsLayout = findViewById(R.id.linearLayout);
        infoLabel = findViewById(R.id.textViewCallDescription);

        infoLabel.setText(callDescription);

        //get the current call presenter from comelit SDK
        presenter = CGModule.getInstance(this).getCurrentPresenter();

        //initialize ui actions
        opendoorBtn.setOnClickListener(view -> presenter.openDoor());

        answerBtn.setOnClickListener(view -> {
            if (PermissionChecker.checkSelfPermission(getApplicationContext(),
                    "android.permission.RECORD_AUDIO")
                    != PermissionChecker.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(ExternalUICallActivity.this,
                        new String[]{android.Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

            } else {
                presenter.toogleAnswerCall();
            }
        });

        muteBtn.setOnClickListener(view -> presenter.toogleMute());

        closeBtn.setOnClickListener(view -> {
            presenter.release();
            finish();
        });

        stealBtn.setOnClickListener(v -> presenter.onStealVideoButtonPressed());

        CGModule.getInstance(getApplicationContext()).setCallEventCallback(this); //this is to subscribe for ui feedback events
        presenter.onCallEventReceiverReady(); //new api to call when you are ready for events

        if (answeredFromNotification) {
            presenter.toogleAnswerCall();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onPause();
    }

    /**
     * Internal helper methods
     */
    private void initVideo() {

        //initialize the surfaceview or the imageview, this depends on which video decode you have specified (software or hardware)
        if (softwareDecode){
            videoSurfaceView.setVisibility(View.GONE);
            videoImageView.setVisibility(View.VISIBLE);
        }else{
            videoSurfaceView.setVisibility(View.VISIBLE);
            videoImageView.setVisibility(View.GONE);
            presenter.setVideoSurface(videoSurfaceView);
        }
        presenter.setVideoReceiver(this);
    }

    /**
     * CGVideoReceiver interface, in this method you will receive the video frame
     * (already decoded) if you use software decode and image view.
     *
     * otherwise this callback is not invoked the frame is decoded internally with native decoder
     * and displayed on your surface view automatically (remember to call setVideoSurface on the presenter)
     */
    @Override
    public void onFrameReceived(byte[] data, int width, int height, int length) {
        if (videoImageView != null) {
            final Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            Buffer buf = ByteBuffer.wrap(data);
            b.copyPixelsFromBuffer(buf);
            runOnUiThread(() -> videoImageView.setImageBitmap(b));
        }
    }

    @Override
    public void onCallResume() { }

    @Override
    public void onCallPause() { }

    @Override
    public void onCallEnd() {
        if(fromBackground) {
            if(!getResources().getBoolean(R.bool.disable_disconnect)) {
                CGModule.getInstance(getApplicationContext()).disconnect();
            }
        }
        finish();
    }

    @Override
    public void onCallCapabilityVideo() {
        initVideo();
    }

    @Override
    public void onCallCapabilityDoor() {
        runOnUiThread(() -> opendoorBtn.setVisibility(View.VISIBLE));
    }

    @Override
    public void onCallCapabilityAudio() {
        runOnUiThread(() -> {
            answerBtn.setVisibility(View.VISIBLE);
            muteBtn.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onCallCapabilityPeerType(Role vipRole) {
        //todo update icon for differnt types of caller
    }

    //API CHANGED NAME TO REFLECT IT'S REAL NATURE OF A TOGGLE (YOU CAN OPEN AND CLOSE THE AUDIO)
    @Override
    public void onCallToggleAnswer() {
        runOnUiThread(() -> {
            audioOn = !audioOn;
            if (audioOn) {
                answerBtn.setBackground(AppCompatResources.getDrawable(this,R.drawable.example_release_button_comelit));
            } else {
                answerBtn.setBackground(AppCompatResources.getDrawable(this,R.drawable.example_answer_button_comelit));
            }
        });
    }

    @Override
    public void onCallToggleMic() {
        runOnUiThread(() -> {
            micOn = !micOn;
            if (micOn){
                muteBtn.setBackgroundResource(R.drawable.example_mic_on_button_comelit);
            }else{
                muteBtn.setBackgroundResource(R.drawable.example_mic_off_button_comelit);
            }
        });
    }

    //NEW API, NOW YOU HAVE ONLY ONE OnCallRelease() with the cause passed as a parameter
    @Override
    public void onCallRelease(CGCallReleaseType cgCallReleaseType) {
        final  String cause;
        switch (cgCallReleaseType){

            case CALL_RELEASE_NORMAL:
            case CALL_RELEASE_BY_USER:
                cause = "Call release";
                break;
            case CALL_RELEASE_TIMEOUT:
                cause = "Call timeout";
                break;
            case CALL_RELEASE_REJECTED:
                cause = "Call rejected";
                break;
            case CALL_RELEASE_DIVERTED:
                cause = "Call diverted";
                break;
            case CALL_RELEASE_DEVICE_NOT_FOUND:
                cause = "Call release Device not found";
                break;
            case CALL_RELEASE_SYSTEM_BUSY:
                cause = "Call release, system busy";
                break;
            case CALL_RELEASE_NOT_SELECTED:
                cause = "Call answered by another device";
                break;
            case CALL_RELEASE_ERROR:
                cause = "Call release error";
                break;
             default:
                cause ="";
                break;
        }
        runOnUiThread(() -> {
            videoImageView.setVisibility(View.GONE);
            videoSurfaceView.setVisibility(View.GONE);
            buttonsLayout.setVisibility(View.GONE);
            stealBtn.setVisibility(View.GONE);
            infoLabel.setText(cause);
        });
    }

    @Override
    public void onCallRequestVideo() { }

    @Override
    public void onCallOpenDoor() { }

    @Override
    public void onCallStartRec() { }

    @Override
    public void onCallStopRec(String s) { }

    @Override
    public void onCallVirtualButton1() { }

    @Override
    public void onCallVirtualButton2() { }

    @Override
    public void onCallFirstFrame() { }
}
