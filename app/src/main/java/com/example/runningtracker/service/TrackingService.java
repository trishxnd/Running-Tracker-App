package com.example.runningtracker.service;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import android.location.Location;

import com.example.runningtracker.db.Repository;
import com.example.runningtracker.db.Run;
import com.example.runningtracker.ui.RunActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.runningtracker.R;
import com.example.runningtracker.db.Repository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TrackingService extends Service {

    private static final String CHANNEL_1_ID = "channel1";
    private static final int NOTIFICATION_ID = 1; //used to identify the specific notification
    private static final int LOCATION_PERMISSION = 1;
    private boolean isTracking;

    private String comment;
    private Location lastLocation = null;


    private double distanceTravelled;
    private long finalTimeRanMinutes;

    private NotificationCompat.Builder reference; //reference to notification to rebuild it when event handling
    private RemoteCallbackList<MyBinder> callbacks = new RemoteCallbackList<MyBinder>();

    //get repo instead of viewmodel inside a service due to best practise guidelines
    private Repository repository = new Repository(getApplication());


    @Override
    public void onCreate() {
//        // TODO Auto-generated method stub
        super.onCreate();
        startService();
        Log.d("tracker", "service onCreate");
    }

    //Updates distance travelled when location result is returned
    //Difference between previous location and new one is added to distance
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

                if (lastLocation == null) {
                    lastLocation = locationResult.getLastLocation();
                }

                distanceTravelled += locationResult.getLastLocation().distanceTo(lastLocation) / 1000;
                lastLocation = locationResult.getLastLocation();
                doDistanceCallBacks(distanceTravelled);

                Log.d("Distance update", "Travelled distance:" + distanceTravelled);


            if (locationResult != null && locationResult.getLastLocation() != null) {
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                Log.d("LOCATION UPDATE", latitude + ", " + longitude);

                Location location = locationResult.getLastLocation();
                doCallBacks(location);
            }
        }
    };

    //Callbacks for locations, to be handled inside of RunActivity
    public void doCallBacks(Location location) {
        int count = callbacks.beginBroadcast();
        for (int i = 0; i < count; i++) {

            callbacks.getBroadcastItem(i).callback.locationUpdate(location);
        }
        callbacks.finishBroadcast();

    }

    //Callbacks for distance, to be handles inside of RunActivity
    public void doDistanceCallBacks(double distanceTravelled) {
        int count = callbacks.beginBroadcast();
        for (int i = 0; i < count; i++) {

            callbacks.getBroadcastItem(i).callback.distanceUpdate(distanceTravelled);
        }
        callbacks.finishBroadcast();
    }

    //Binder object used in order for RunActivity to communicate with Tracking Service
    //Contains commands called for event handling as well as registering callbacks in the binder
    public class MyBinder extends Binder implements IInterface {

        public void registerCallback(ICallBack callback) {
            this.callback = callback;
            callbacks.register(MyBinder.this);

        }

        public void unregisterCallback(ICallBack callback) {
            callbacks.unregister(MyBinder.this);
        }


        ICallBack callback;

        @Override
        public IBinder asBinder() {
            return this;
        }
    }

    // returns binder object upon service bind
    @Override
    public IBinder onBind(Intent intent) {

        return new MyBinder();
    }

    //Starts service and creates notification which can control starting and stopping of service
    //Contains Location request that polls for a new location every 4 seconds which in turn calls onLocation result each time
    @SuppressLint("MissingPermission")
    public void startService() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_1_ID, "Channel 1", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Channel 1");

            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(TrackingService.this, RunActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        //Intent used for Start button where onStartCommand will handle button click based on action key passed
        Intent startIntent = new Intent(TrackingService.this, TrackingService.class);
        startIntent.setAction("StartService");
        PendingIntent pendingStartIntent = PendingIntent.getService(this, 1, startIntent, PendingIntent.FLAG_IMMUTABLE);

        //Intent used for Stop button where onStartCommand will handle button click based on action key passed
        Intent stopIntent = new Intent(TrackingService.this, TrackingService.class);
        stopIntent.setAction("StopService");
        PendingIntent pendingStopIntent = PendingIntent.getService(this, 2, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Running Activity")
                .setContentText("Running Service")
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "Start", pendingStartIntent)
                .addAction(R.drawable.ic_launcher_foreground, "Stop", pendingStopIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        Log.d("Notification", "here1");

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        startForeground(NOTIFICATION_ID, notification.build());

        Log.d("Notification", "here2");
    }



    // Stops service
    //Removes locationCallbacks, stops foreground notification and stops service
    public void stopService(){
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    //Checks for action which corresponds to starting or stopping service
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null){
            String action = intent.getAction();

            if(action!= null){



                if(action.equals("StartService")) {
                    isTracking = true;
                    Log.d("isTracking", "isTracking: " + isTracking);

                    if(!checkTrackingService())
                        startService();

                }
                else if(action.equals("ExitService")){
                    if(checkTrackingService())
                    stopService();
                }
                else if(action.equals("StopService")) {
                    Log.d("onStartCommand", "STOPPPING SERVICE ");


                    finalTimeRanMinutes = intent.getLongExtra("timeRan",0);
                    comment = intent.getStringExtra("comment");


                    double finalSpeed =  getAverageSpeed(distanceTravelled, finalTimeRanMinutes);
                    Run entry = new Run(getTimeLogged(), getCurrentDate(), finalTimeRanMinutes,
                            distanceTravelled, finalSpeed, comment);

                    repository.insert(entry);
                    stopService();
                }
            }
        }

        return START_STICKY;
    }



    public double getAverageSpeed(double distance, long time){

        double inHours = (double) time/(1000*60*60);
        double KmH = distance/inHours;
        Log.d("getAverageSpeed", ""+KmH);
        if((KmH) > 1000){
            return 0.0;
        }
        return KmH;
    }

    //Retrieves system time and returns it as string
    public long getTimeLogged(){
        return Calendar.getInstance().getTimeInMillis();
    }

    //Retrieves system date and returns it as string
    public String getCurrentDate(){
        Date dateAndTime = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(dateAndTime);
    }

    public boolean checkTrackingService() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (TrackingService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Log.d("service", "service onDestroy");

        super.onDestroy();


    }

}