package com.example.runningtracker.ui;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.runningtracker.R;
import com.example.runningtracker.service.ICallBack;
import com.example.runningtracker.service.TrackingService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class RunActivity extends AppCompatActivity implements OnMapReadyCallback, CommentBox.CommentDialogListener {


    private MapView mapView;
    private GoogleMap gMap;

    private CountDownTimer timer;
    private long startTime;
    private long timeLeft;
    private long endTime;
    private boolean timerRunning;
    private final CountDownLatch latch = new CountDownLatch(1);

    private boolean startClicked = false;
    private boolean stopClicked = false; //used for logging end location and saving map
    private boolean pauseClicked = false;
    private boolean startMarker = false;

    private final List<Polyline> polylines = new ArrayList<>();
    private PolylineOptions polylineOptions = new PolylineOptions();
    private TrackingService.MyBinder trackingService = null;

    private String commentOnFinish;


    //Creates and binds the service
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        mapView = findViewById(R.id.mapView);

        //Initial Button config
        Button startButton = findViewById(R.id.runButton);
        startButton.setEnabled(false);

        //Synchronize map and display it
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);

        //Adds options for tracking route
        polylineOptions.width(8f);
        polylineOptions.color(Color.RED);

        //Starts and binds service on RunActivity create
        this.startService(new Intent(this, TrackingService.class));
        this.bindService(new Intent(this, TrackingService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        //registers callbacks for location and distance updates
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            Log.d("g53mdp", "MainActivity onServiceConnected");
            trackingService = (TrackingService.MyBinder) service;
            trackingService.registerCallback(callBack);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            Log.d("g53mdp", "MainActivity onServiceDisconnected");
            trackingService.unregisterCallback(callBack);
            trackingService = null;

        }
    };

    ICallBack callBack = new ICallBack() {

        //Handles location update
        @Override
        public void locationUpdate(Location location) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            if(stopClicked){ // creates marker at stop location
                gMap.addMarker(new MarkerOptions().position(latLng).title("Stop Location"));

            }

            if (startClicked) {//creates marker at start location only if paused hasnt been clicked
                if (startMarker && !pauseClicked) {
                    gMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .position(latLng).title("Start Location"));
                    startMarker = false;
                }
                polylineOptions.add(latLng);
                Polyline polyline = gMap.addPolyline(polylineOptions);
                polylines.add(polyline);
            }

            else { // If neither button has been pressed create new markers as user moves around
                gMap.clear();
                gMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
            }
            gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            gMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
        }

        @Override// Sets distanceText to input parameter distance and formats it to have 2 decimal places
        public void distanceUpdate(double distance) {
            if(startClicked) {
                TextView distanceText = (TextView) findViewById(R.id.distanceText);
                DecimalFormat df = new DecimalFormat("#.##");
                distanceText.setText(df.format(distance)+ "Km");
            }
        }
    };

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.gMap = googleMap;
        gMap.clear();
    }


    //Starts service and timer
    public void onClickStart(View v) {

        startMarker = true;
        startClicked = true;

        startTrackingService();
        startTimer();
    }

    //Exits the run activity without saving an entry to the database
    public void onClickExit(View v) {

        finish();
        Intent intent = new Intent(RunActivity.this, TrackingService.class);
        intent.setAction("ExitService");
        startService(intent);

    }

    //Stops Tracking service, destroys RunActivity and clears google map
    public void onClickStop(View v) {
        stopClicked = true;

        if (!startClicked) {
            Toast.makeText(this, "There is no active run to finish", Toast.LENGTH_SHORT).show();
            return;
        }

        pauseTimer();
        openDialog();
        this.startClicked = false;
        gMap.clear();
        polylineOptions = new PolylineOptions();
        polylines.removeAll(polylines);

    }

    //Creates new instance of CommentBox class and displays it
    public void openDialog() {
        CommentBox commentBox = new CommentBox();
        commentBox.show(getSupportFragmentManager(), "Comment Box");
    }


    //Sends intent to TrackingService in order to start it
    private void startTrackingService() {
        if (!checkTrackingService()) {
            Intent intent = new Intent(RunActivity.this, TrackingService.class);
            intent.setAction("StartService");
            startService(intent);
        }
    }

    //Sends intent to TrackingService in order to stop it
    private void stopTrackingService() {
        if (checkTrackingService()) {
            Intent intent = new Intent(RunActivity.this, TrackingService.class);
            intent.setAction("StopService");
            intent.putExtra("timeRan", getTimeRan());
            intent.putExtra("comment", commentOnFinish);

            startService(intent);
            Toast.makeText(this, "Run finished", Toast.LENGTH_SHORT).show();
            finish();

        }
    }

    //Checks to see if tracking service is running in the Background
    // returns 0 if running and 1 if not running
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

    //sets text of timer to whatever was entered in the input edittext field
    //checks to see if input is empty
    //checks to see if 0 was entered
    public void onClickSet(View v) {
        EditText editText = findViewById(R.id.editTimerText);
        String input = editText.getText().toString();
        if (input.length() == 0) {
            Toast.makeText(RunActivity.this, "Minute Field can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        long milliSec = Long.parseLong(input) * 60000;
        if (milliSec == 0) {
            Toast.makeText(RunActivity.this, "Please enter a valid minute entry", Toast.LENGTH_SHORT).show();
            return;
        }


        try{
            Integer.parseInt(input);
        }
        catch(NumberFormatException e){
            Toast.makeText(RunActivity.this, "Please input whole numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        Button startButton = findViewById(R.id.runButton);
        startButton.setEnabled(true);

        setTimerText(milliSec);
        editText.setText("");



    }

    //Pauses timer
    public void onClickPause (View v){
        if(timerRunning){
            pauseClicked = true;
            pauseTimer();
        }
    }

    //Sets timerText to input parameter milliSec
    public void setTimerText(long millisec) {
        this.startTime = millisec;
        resetTimer();
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    //Resets timer
    public void resetTimer() {
        timeLeft = startTime;
        updateTimerText();
    }

    //Pauses timer
    public void pauseTimer() {
        timer.cancel();
        timerRunning = false;
    }

    //Updates timer text to hh:mm:ss format
    public void updateTimerText() {
        int hours = (int) (timeLeft / 1000) / 3600;
        int minutes = (int) ((timeLeft / 1000) % 3600) / 60;
        int seconds = (int) (timeLeft / 1000) % 60;

        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        }
        TextView timerText = findViewById(R.id.timerText);
        timerText.setText(timeLeftFormatted);
    }

    //Starts timer by initializing countdown timer and endTime variables
    //when timer runs out CommentBox dialog is initialized
    private void startTimer() {
        endTime = System.currentTimeMillis() + timeLeft;
        timer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long l) {
                timeLeft = l;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                openDialog();

            }
        }.start();

        timerRunning = true;

    }

    public long getTimeRan(){

        return startTime - timeLeft;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        stopTrackingService();
        finish();
        Log.d("RunActivtiy", "ondestroy");

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();

    }

    //applies comment input in dialog box to commentOnFinish global variable which is based back to service
    @Override
    public void applyTexts(String finalComment) {
        this.commentOnFinish = finalComment;
        stopTrackingService();
    }
}

