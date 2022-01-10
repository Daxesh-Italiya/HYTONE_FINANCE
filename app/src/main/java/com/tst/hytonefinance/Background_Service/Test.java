package com.tst.hytonefinance.Background_Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.tst.hytonefinance.R;

import java.util.Calendar;

public class Test extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, Calendar.getInstance().getTime().toString(), Toast.LENGTH_LONG).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "CHANNEL_ID",
                    "CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("CHANNEL_DESCRIPTION");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }

        showNotification(context);
    }

    public void showNotification(Context context) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, "CHANNEL_ID")
                        .setSmallIcon(R.drawable.application_icon)
                        .setContentTitle("Hello, attention!")
                        .setContentText(Calendar.getInstance().getTime().toString())
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
        mNotificationManager.notify(1, mBuilder.build());
    }
}
