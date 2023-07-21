package com.example;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.mylibrary.ConstantsVar;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.List;


public class GoogleLocationService extends Service {

    public static double latitude, longitude, accuracy;
    private double previousLat = 0.0;
    private ArrayList<ArrayList<Double>> validLocation = new ArrayList<>();
    private static double toRadians(double degree) {
        return degree * Math.PI / 180.0;
    }

    public static double distanceBetweenLatitudes(double lat1, double lat2) {
        double R = 6371000.0;
        double lat1_rad = toRadians(lat1);
        double lat2_rad = toRadians(lat2);
        double dLat = lat2_rad - lat1_rad;
        double a = Math.pow(Math.sin(dLat / 2.0), 2);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        double distance = R * c;
        return distance;
    }

    public final LocationCallback locationCallback = new LocationCallback() {
        @Override

        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            if (locationResult != null && locationResult.getLastLocation() != null) {
                latitude = locationResult.getLastLocation().getLatitude();
                longitude = locationResult.getLastLocation().getLongitude();
                accuracy = locationResult.getLastLocation().getAccuracy();

                if (previousLat != 0.0) {
                    ArrayList<Double> Coordinateslocation = new ArrayList<>();
                    double distance = distanceBetweenLatitudes(previousLat, latitude);

                    Intent intent = new Intent("LOCATION_UPDATE");
                    intent.putExtra("latitude", latitude);
                    intent.putExtra("longitude", longitude);
                    intent.putExtra("accuracy", accuracy);
                    intent.putExtra("distance", distance);
                    System.out.println("distance is : " + distance +"meter");
                    if(distance >= 1.00){
                      //  System.out.println("Location_Update Service: " + latitude + " Lon: " + longitude + "  " + accuracy);
                        Coordinateslocation.add(latitude);
                        Coordinateslocation.add(longitude);
                        validLocation.add(Coordinateslocation);
                        System.out.println("ArrayList : " + validLocation);
                        sendBroadcast(intent);
                    }
                    else {
                        System.out.println("not valid");
                    }
                }
                previousLat = latitude;
            } else {
                System.out.println("Location not available.");
            }
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }

    private void StartLocationService(float minDist) {
        System.out.println("StartLocationService() called");
        System.out.println("Using minDist value: " + minDist);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void StopLocationService() {
        System.out.println("StopLocationService() called");
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        System.out.println("onStartCommand is called");

        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                System.out.println("serviceAction:" + action);
                if (action.equals(ConstantsVar.ACTION_START_LOCATION_SERVICE)) {
                    float minDist = intent.getFloatExtra("minDist", 0.00f); // Retrieve the minDist value from the intent
                    System.out.println("min distance received : " + minDist);
                    StartLocationService(minDist);
                } else if (action.equals(ConstantsVar.ACTION_STOP_LOCATION_SERVICE)) {
                    StopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
