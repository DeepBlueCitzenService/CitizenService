package io.github.deepbluecitizenservice.citizenservice;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

public class SLANotification extends BroadcastReceiver{
    public final static String CATEGORY = "CATEGORY", LOCATION = "LOCATION",
            PROBLEM_KEY="PROBLEM_KEY", URL_KEY = "URL_KEY";
    public SLANotification(){

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();

        Intent startSolutionActivityIntent = new Intent(context, SolutionDialogActivity.class);
        startSolutionActivityIntent.putExtra(SLANotification.PROBLEM_KEY, (String)extras.get(SLANotification.PROBLEM_KEY));
        startSolutionActivityIntent.putExtra(SLANotification.URL_KEY, (String) extras.get(SLANotification.URL_KEY));

        //Change to solution intent
        //Open solution intent on clicking button
        PendingIntent actionPendingIntent = PendingIntent.getActivity(context, 2, startSolutionActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = (android.support.v7.app.NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.sla_update_problem_title))
                .setContentText(context.getString(R.string.sla_update_problem_details_start) + extras.get(LOCATION) + " (" + extras.get(CATEGORY) + ")")
                .setVibrate(new long[]{0, 300, 0})
                .setAutoCancel(true)
                .setContentIntent(actionPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify((int)System.currentTimeMillis(), mBuilder.build());
    }
}
