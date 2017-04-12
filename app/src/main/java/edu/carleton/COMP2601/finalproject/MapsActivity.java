package edu.carleton.COMP2601.finalproject;

import android.content.Intent;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Timer mapTimer;
    private Marker currentMarker;

    /*----------
    - Description: runs when the activity first boots up
    ----------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapTimer.cancel();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    /*----------
    - Description: Creates a map and places a marker at the current client's location every 3 secs.
    - Input: none
    - Return: none
    ----------*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Location currentLocation = GameActivity.getInstance().getClientLocation();
        if (currentLocation != null) {
            LatLng currLocationLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

            currentMarker = mMap.addMarker(new MarkerOptions().position(currLocationLatLng).title("Current Location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currLocationLatLng, 18));

            mapTimer = new Timer();
            mapTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentMarker.remove();
                            Location currentLocation = GameActivity.getInstance().getClientLocation();
                            LatLng currLocationLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            currentMarker = mMap.addMarker(new MarkerOptions().position(currLocationLatLng).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        }
                    });
                }
            }, 3000, 3000);
        }
    }

    /*----------
    - Description: Called when the subsequent activity returns. Calls the corresponding function.
    - Input: requestCode, resultCode, data
    - Return: none
    ----------*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == -1) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", resultCode);
            setResult(-1,returnIntent);
            finish();
        }
        if (resultCode == 2) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", resultCode);
            setResult(2, returnIntent);
            finish();
        }
    }
}
