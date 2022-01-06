package com.tst.hytonefinance;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private TextView myTextView;
    private ImageView myImageView;
    private static final int SELECT_PICTURE = 1;
    private String selectedImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myTextView = findViewById(R.id.textview);
        myImageView = findViewById(R.id.imageView);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_SMS,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                PackageManager.PERMISSION_GRANTED
        );

        findViewById(R.id.select_image)
                .setOnClickListener(new View.OnClickListener() {

                    // opens activity to select image
                    public void onClick(View arg0) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent,
                                "select image"), SELECT_PICTURE);
                    }
                });
    }

    // shows image and stores its path
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                myImageView.setImageURI(selectedImageUri);
                selectedImagePath = getPath(selectedImageUri);
            }
        }
    }

    // gets path of selected image
    public String getPath(Uri uri) {
        if( uri == null ) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return uri.getPath();
    }

    public void upload(View view){
        uploadImage();
    }

    // sends image to api and gets response // todo: need to modify this function
    private void uploadImage() {
        File file = new File(selectedImagePath);

        Retrofit retrofit = NetworkClient.getRetrofit();

        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part parts = MultipartBody.Part.createFormData("newImage", file.getName(), requestBody);
        RequestBody someData = RequestBody.create(MediaType.parse("text/plain"), "This is a new Image");
        UploadApis uploadApis = retrofit.create(UploadApis.class);
        Call call = uploadApis.uploadImage(parts, someData);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                // code for success
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                // code for failure
            }
        });
    }

    public void readSMS(View view){
        readSms();
    }

    // prints last sms body
    private void readSms() {
        Cursor getAllSms = getContentResolver().query(Uri.parse("content://sms"), null, null, null, null);
        getAllSms.moveToFirst();
        myTextView.setText(getAllSms.getString(12));
        getAllSms.close();
    }

    // prints all call history
    public void readCallLog(View view) {

        StringBuffer sb = new StringBuffer();
        Cursor managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        sb.append("Call Details :");
        while (managedCursor.moveToNext()) {
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = managedCursor.getString(duration);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            sb.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- " + dir + " \nCall Date:--- " + callDayTime + " \nCall duration in sec :--- " + callDuration);
            sb.append("\n----------------------------------");
        }
        managedCursor.close();
        myTextView.setText(sb);
    }
}

