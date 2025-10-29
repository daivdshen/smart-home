package com.intelliving.app;

import static io.dcloud.common.util.RuningAcitvityUtil.getActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.comelitgroup.module.api.CGAudioSettings;
import com.comelitgroup.module.api.CGCallbackInt;
import com.comelitgroup.module.api.CGError;
import com.comelitgroup.module.api.CGModule;
import com.comelitgroup.module.api.CGParameter;
import com.comelitgroup.module.api.CGResponse;
import com.intelliving.app.firebase.ComelitFirebaseMessagingService;
import com.intelliving.app.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class VcpInterface {

    public void sendVcpInfo(Context context,String host,int port,String actCode){
        SharedPreferences sharedPreferences = context.getSharedPreferences("VCP_INFO", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("host", host);
        editor.putInt("port", port);
        editor.putString("actCode", actCode);
        editor.apply();
    }
    public String connectToSystem(Context context,String hostname,int port,String activationCode,String userId,String unitId,String serverHost){
        if(hostname==null || "".equals(hostname)){
            return "Vcp is empty";
        }
//        hostname="192.168.2.200";
        activationCode="57cdd7";
//        port=64300;
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
            boolean withUI = true;
            boolean softwareDecode = true;//sharedPreference.getBoolean(Utils.SOFTWARE_VIDEO_DECODE_KEY,true);

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
                                String actCode=dispatch(serverHost,param.toJSONString());
                                editor.putString(repeatKey, "");
                                editor.apply();
                                if(!"".equals(actCode)){
                                    connectToSystem(context, hostname,port, actCode, userId, unitId, serverHost);
                                }
                            }

                        }
                    }).
                    setPushToken(token).
                    useComelitUI(withUI).
                    enableSoftwareDecode(softwareDecode).
                    setAudioSettings(audioSettings).
                    build();

               CGResponse cgResponse= CGModule.getInstance(context).connect(parameters);
            }catch (Exception e){
            e.printStackTrace();
            return "Connect error";
        }

        return "";
    }

    private String dispatch(String host,String params){
        Log.i("VcpInterface","host:/home-service/vcp/reGeneratorCode"+host);
        URL url = null;
        try {
            url = new URL(host+"/home-service/vcp/reGeneratorCode");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("content-type", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            os.write(params.getBytes());
            os.close();

            int responseCode = connection.getResponseCode();
            System.out.println("host:"+responseCode);
            System.out.println("host:"+params);
            Log.i("VcpInterface","responseCode:"+responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String result = stringBuilder.toString();
                System.out.println("Response: " + result);
                JSONObject obj=JSONObject.parseObject(result);
                Log.i("VcpInterface","receive:"+result+"  len:"+inputStream.available());
                return obj.getString("data")==null?"":obj.getString("data");
            }else{
                Log.i("VcpInterface","request error");
            }
            connection.disconnect();

        } catch (IOException e) {
            Log.i("VcpInterface","error:"+e.getMessage());
            return "";
        }

        return "";
    }
}
