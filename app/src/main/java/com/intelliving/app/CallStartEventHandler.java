package com.intelliving.app;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.comelitgroup.libcomelit.type.CallDirection;
import com.comelitgroup.libcomelit.type.CallFlag;
import com.comelitgroup.module.api.CGCallStartReceiver;

import java.util.LinkedList;

import static com.intelliving.app.ExternalUICallActivity.EXTRA_CALL_CALLER_NAME;
import static com.intelliving.app.ExternalUICallActivity.EXTRA_CALL_DIRECTION;
import static com.intelliving.app.ExternalUICallActivity.EXTRA_SOFTWARE_DECODE;

public class CallStartEventHandler implements CGCallStartReceiver {
    private static final String TAG = "CallStartEventHandler";

    private Context context;
    private boolean softwareDecode;

    public CallStartEventHandler(Context context, boolean softwareDecode){
        this.context = context;
        this.softwareDecode = softwareDecode;
    }


    @Override
    public void onCallStart(String caller, CallDirection callDirection, LinkedList<CallFlag> linkedList, boolean isFloorCall) {
        Log.i(TAG,"onCallStart");
        Intent intent = new Intent(context,ExternalUICallActivity.class);
        intent.putExtra(EXTRA_SOFTWARE_DECODE,softwareDecode);
        intent.putExtra(EXTRA_CALL_CALLER_NAME,caller);
        intent.putExtra(EXTRA_CALL_DIRECTION,callDirection);
        context.startActivity(intent);
    }
}
