package io.github.deepbluecitizenservice.citizenservice;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

public class SLANotification extends BroadcastReceiver{
    public final static String CATEGORY = "CATEGORY", LOCATION = "LOCATION", PROBLEM_KEY="PROBLEM_KEY";
    public SLANotification(){

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();

        Intent startActivityIntent = new Intent(context, MainActivity.class);
        startActivityIntent.putExtra(SLANotification.PROBLEM_KEY, (String)extras.get(SLANotification.PROBLEM_KEY));

        //Start main activity if notification is clicked
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Change to solution intent
        //Open solution intent on clicking button
        PendingIntent actionPendingIntent = PendingIntent.getActivity(context, 2, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = (android.support.v7.app.NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Update problem")
                .setContentText("Update problem: " + extras.get(CATEGORY) + " in " + extras.get(LOCATION))
                .addAction(R.drawable.ic_camera, "Add solution", actionPendingIntent)
                .setVibrate(new long[]{0, 300, 0})
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify((int)System.currentTimeMillis(), mBuilder.build());
    }
}
