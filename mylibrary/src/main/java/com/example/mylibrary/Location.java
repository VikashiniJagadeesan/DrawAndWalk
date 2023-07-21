package com.example.mylibrary;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import com.example.GoogleLocationService;
import com.google.android.gms.location.LocationServices;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
public class Location {



   public void StartLocationService(Context context,float minDist) {
        if (!isLocationServiceRunning(context)) {
            Intent intent = new Intent(context, GoogleLocationService.class);
            intent.setAction(ConstantsVar.ACTION_START_LOCATION_SERVICE);
            intent.putExtra("minDist", minDist);
            context.startService(intent);
            System.out.println("min distance received : " + minDist);
            System.out.println("Location service started...");
//            Intent broadcastIntent = new Intent("LOCATION_UPDATE");
//            broadcastIntent.putExtra("minDist", minDist);
//            LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);

        }
    }

    public static void StopLocationService(Context context) {
        try {
//            if (isLocationServiceRunning()) {
            Intent intent = new Intent(context, GoogleLocationService.class);
            intent.setAction(ConstantsVar.ACTION_STOP_LOCATION_SERVICE);
            context.startService(intent);
            System.out.println("Location service stopped...");
        } catch (Exception ignore) {
        }
        // }
    }

   public static boolean isLocationServiceRunning(Context context) {
       System.out.println("is location service");
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocationServices.class.getName().equals(service.service.getClassName())) {

                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }


}
