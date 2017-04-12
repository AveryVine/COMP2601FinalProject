package edu.carleton.COMP2601.finalproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.Serializable;

/**
 * Created by AveryVine on 2017-03-19.
 */

public class Client implements Serializable, ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener {
    private final int FINE_LOCATION_PERMISSION_REQUEST = 1;
    private final int COARSE_LOCATION_PERMISSION_REQUEST = 2;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private int cash;
    private String username;
    private Location location;

    public Client(String username) {
        cash = 0;
        this.username = username;
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(GameActivity.getInstance())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        locationRequest = new LocationRequest();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void onStart() {
        googleApiClient.connect();
    }


    public Location getCurrentLocation() {
        System.out.println("Getting last known location");
        if (ActivityCompat.checkSelfPermission(GameActivity.getInstance(),
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(GameActivity.getInstance(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GameActivity.getInstance(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOCATION_PERMISSION_REQUEST);
            ActivityCompat.requestPermissions(GameActivity.getInstance(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    COARSE_LOCATION_PERMISSION_REQUEST);
        }
        else {
            location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            System.out.println("CurrentLocation: " + location);
        }
        return location;
    }

    public boolean purchase(String message, int purchase) {
        if (cash - purchase < 0) {
            GameActivity.getInstance().logs.append("\n" + R.string.client_notEnoughCash_log);
            return false;
        }
        cash -= purchase;
        GameActivity.getInstance().logs.append("\n" + message + " (-$" + purchase + ")");
        return true;
    }


    public void depositCash(int deposit) {
        cash += deposit;
    }

    public int getCash() {
        return cash;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println(connectionResult);
    }

    @Override
    public void onLocationChanged(Location location) {
        System.out.println("LocationChanged: " + location);
        this.location = location;
    }

    public void startLocationUpdates() {
        System.out.println("Starting location updates");
        if (ActivityCompat.checkSelfPermission(GameActivity.getInstance(),
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(GameActivity.getInstance(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GameActivity.getInstance(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOCATION_PERMISSION_REQUEST);
            ActivityCompat.requestPermissions(GameActivity.getInstance(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    COARSE_LOCATION_PERMISSION_REQUEST);
        }
        else {
            System.out.println("All permission accepted");
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }
}
