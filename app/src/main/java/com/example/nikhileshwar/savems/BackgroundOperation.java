package com.example.nikhileshwar.savems;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import java.io.IOException;
import java.util.List;


public class BackgroundOperation extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @SuppressLint({"MissingPermission", "RestrictedApi"})
    @Override
    public void onCreate() {
        super.onCreate();





       MainActivity.locationCallback=new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                SmsManager sms = SmsManager.getDefault();

                try {
                    List<Address> addresses = MainActivity.geocoder.getFromLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude(), 1);
                    if (addresses != null && addresses.size() > 0) {
                        sms.sendTextMessage(MainActivity.defaultNumber, null,
                                "lat : " + locationResult.getLastLocation().getLatitude() + "\nlon : " + locationResult.getLastLocation().getLongitude() + "\n\naddress : " +
                                        addresses.get(0).getAddressLine(0) + "\n\nmessage : " +
                                        MainActivity.msgText.getText().toString(),
                                null, null);
                        //Toast.makeText(BackgroundOperation.this, "sent to "+MainActivity.defaultNumber, Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(BackgroundOperation.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        };

           MainActivity.client.requestLocationUpdates(new LocationRequest().setInterval(5000),MainActivity.locationCallback , Looper.myLooper());


    }



    @Override
    public void onDestroy() {
        super.onDestroy();
//        if(locationManager!=null) {
//
//            locationManager.removeUpdates(locationListener);
//        }
        MainActivity.client.removeLocationUpdates(MainActivity.locationCallback);
        Toast.makeText(this, "service stopped", Toast.LENGTH_SHORT).show();
    }
}
