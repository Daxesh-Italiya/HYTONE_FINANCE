package com.tst.hytonefinance.Background_Service;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;


import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.Callable;


public class HelperMethods {

    public static boolean isRunningTask = false;
    private static String TAG = "HelperMethods";

    public static boolean isInternetAvailable(Context context) {
        boolean retval = false;

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String send_file = pref.getString("send_file", "wifi");
        boolean isSendFileUsingWifi = send_file.equals("wifi");



        //return  ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;

        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null) {
                    if (networkInfo.getTypeName().toLowerCase().equals("wifi")) {
                        if (networkInfo.isConnected()) {
                            retval = true;
                        }else if(!isSendFileUsingWifi){
                            retval = true;
                        }
                    }
                }

            }
        } catch (NullPointerException npe) {
            Log.w(TAG, "NullPointerException @ HelperMethods.iInternetAvailable");
        }
        return retval;
    }

    public static void waitWithTimeout(Callable testCallable, Object breakValue, long timeoutmillisecond) throws Exception {
        long initmillis = System.currentTimeMillis();
        while (System.currentTimeMillis() - initmillis < timeoutmillisecond) {
            if (testCallable.call().equals(breakValue)) break;
        }
    }

    static void waitWithTimeoutN(Callable testCallable, Object continueValue, long timeoutmillisecond) throws Exception {
        long initmillis = System.currentTimeMillis();
        while (System.currentTimeMillis() - initmillis < timeoutmillisecond) {
            if (testCallable.call() != continueValue) break;
        }
    }


    public static void renameTmpFile(String filepath) {
        try {
            File file = new File(filepath);
            if (!file.exists()) return;
            String path = file.getAbsolutePath();
            String dirpath = path.substring(0, path.lastIndexOf("/"));
            String filename = file.getName();
            String newpath = dirpath + "/" + filename.substring(0, filename.length() - 4);
            File newFile = new File(newpath);
            file.renameTo(newFile);
        } catch (Exception ex) {
            Log.w(TAG, "Exception while renaming file.\n" + ex.getMessage());
        }


    }

    public static void removeBrokenTmpFiles(String dirPath) {
        try {
            File dirPath_ = new File(dirPath);
            File[] tmpFiles = dirPath_.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().toLowerCase().endsWith(".tmp");
                }
            });
            for (File tmpFile : tmpFiles) {
                tmpFile.delete();
            }
        } catch (Exception ex) {
            Log.w(TAG, "Exception while deleting broken tmp files.\n" + ex.getMessage());
        }

    }
    public static float getBattery_percentage(Context context)
    {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float)scale;
        float p = batteryPct * 100;

        return Math.round(p);
        //Log.d("Battery percentage",String.valueOf(Math.round(p)));
    }


    public static String getIMEI(Context context) {
        String retval = "";
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            retval = telephonyManager.getDeviceId();
        }
        return retval;
    }

    public static String getNumber(Context context) {
        String retval = "";
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            retval = telephonyManager.getSimSerialNumber();

        }
        return retval;
    }

    public static String getNumberOperator(Context context) {
        String retval = "";
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            retval = telephonyManager.getSimOperatorName();

        }
        return retval;
    }

    public static String getDeviceUID(Context context) {
        //String serial = Build.SERIAL;
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return  androidId;
    }

    public static String getDeviceNUID(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String line1phonenumber = "";
        if (telephonyManager != null) {
            try {
                line1phonenumber = telephonyManager.getLine1Number();
            } catch (SecurityException secx) {

            }
        }
        return line1phonenumber + "_" + Build.MANUFACTURER + "_" + Build.MODEL;
    }

    public static void createOneTimeExactAlarm(Context context) {


        /*Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 10, intent, 0);


        AlarmManager alarmManager = null;
        alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        if (alarmManager != null) {


            alarmManager.cancel(pendingIntent);//kill any pre-existing alarms
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime() + 60000, pendingIntent);
                //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, now.getTimeInMillis(), EXEC_INTERVAL, pendingIntent);
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime() + 60000, pendingIntent);
                //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,now.getTimeInMillis(), EXEC_INTERVAL, pendingIntent);
            else
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60000, pendingIntent);
                //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,now.getTimeInMillis(), EXEC_INTERVAL, pendingIntent);
        }*/
    }




}
