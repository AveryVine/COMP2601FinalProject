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

    public Client() {
        cash = 0;
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(GameActivity.getInstance())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        locationRequest = new LocationRequest();
    }

    public void onStart() {
        googleApiClient.connect();
    }

    public void onStop() {
        googleApiClient.disconnect();
    }

    public void onPause() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    public void onResume() {
        if (googleApiClient.isConnected())
            startLocationUpdates();
    }


    public Location getCurrentLocation() {
        System.out.println("Getting last known location");
        Location location = null;
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
            if (location != null) {
                Toast.makeText(GameActivity.getInstance(), "Location: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(GameActivity.getInstance(), "Location is null", Toast.LENGTH_SHORT).show();
            }
        }
        return location;
    }


    public void depositCash(int deposit) {
        cash += deposit;
    }

    public int getCash() {
        return cash;
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
        System.out.println("Location changed");
        Toast.makeText(GameActivity.getInstance(), "Location: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
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
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }
}
