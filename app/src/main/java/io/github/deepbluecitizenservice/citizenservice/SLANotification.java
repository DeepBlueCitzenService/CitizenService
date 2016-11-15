package io.github.deepbluecitizenservice.citizenservice;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

public class SLANotification extends Service{
    private String category, location;
    private Class activityToCall;


    public SLANotification(Class activityToCall, String category, String location){
        this.activityToCall = activityToCall;
        this.category = category;
        this.location = location;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        int requestCode = 30;
        Intent intent = new Intent(this, activityToCall);

        PendingIntent pi = PendingIntent.getActivity(this, requestCode, intent, 0);
        NotificationCompat.Builder mBuilder = (android.support.v7.app.NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Update problem")
                .setContentText("Update problem: "+ this.category+" in "+ this.location)
                .setAutoCancel(true)
                .setContentIntent(pi);

        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(requestCode, mBuilder.build());
    }
}
