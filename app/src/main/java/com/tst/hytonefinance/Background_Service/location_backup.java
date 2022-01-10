package com.tst.hytonefinance.Background_Service;

import android.Manifest;
//import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
//import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.tst.hytonefinance.Database.Location_DB;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class location_backup extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{
    Context context;
    //********************** Location ***********
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager locationManager;
    private LocationRequest mLocationRequest;
    private Location_DB location_db;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        location_db=new Location_DB(context);
    }



    private String get_currant_date_time()
    {
        //---------- Get Currant Date nd Time ---------------
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => "+c.getTime());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return df.format(c.getTime());
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        Log.i("TAG", "Connection Suspended");
        mGoogleApiClient.connect();
//        Text_lable.setText("Connection Suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("TAG", "Connection failed. Error: " + connectionResult.getErrorCode());
//        Text_lable.setText("Connection failed. Error: " + connectionResult.getErrorCode());
    }


    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        String Ans="Longitude : "+longitude+",\nLatitude : "+latitude;


        Boolean check_data_flag = location_db.Insert_Subject_data(String.valueOf(longitude),String.valueOf(latitude),get_currant_date_time());
        Log.e("Location:",String.valueOf(check_data_flag),null);


    }
}
