package edu.carleton.COMP2601.finalproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.io.Serializable;

/**
 * Created by AveryVine on 2017-03-19.
 */

public class Client implements Serializable, LocationListener {
    private final int PERMISSION_ACCESS_FINE_LOCATION = 0;

    private int cash;
    private LocationManager locationManager;
    private Location location;
    private Criteria criteria;
    private String provider;

    public Client(LocationManager locationManager) {
        cash = 0;
        this.locationManager = locationManager;
        criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        System.out.println("Provider: " + provider);
    }



    public Location getCurrentLocation() {
        System.out.println("Getting current location...");
        if (provider != null && !provider.equals("")) {
            System.out.println("Provider found");
            if (ContextCompat.checkSelfPermission(GameActivity.getInstance().getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                System.out.println("Need to request permissions...");
                ActivityCompat.requestPermissions(GameActivity.getInstance(), new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                        PERMISSION_ACCESS_FINE_LOCATION);
            }
            else {
                System.out.println("Permissions exist for Fine Location");
                Location location = locationManager.getLastKnownLocation(provider);
                locationManager.requestLocationUpdates(provider, 15000, 1, this);

                if (location != null)
                    onLocationChanged(location);
                else
                    Toast.makeText(GameActivity.getInstance().getApplicationContext(), "No Location Provider Found Check Your Code", Toast.LENGTH_SHORT).show();
            }
        }
        System.out.println("Location: " + location);
        return location;
    }



    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        Toast.makeText(GameActivity.getInstance().getApplicationContext(), location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }



    public void depositCash(int deposit) {
        cash += deposit;
    }

    public int getCash() {
        return cash;
    }
}
