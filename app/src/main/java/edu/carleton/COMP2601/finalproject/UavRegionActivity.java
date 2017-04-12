package edu.carleton.COMP2601.finalproject;

import android.content.Intent;
import android.location.Location;
import android.os.Parcel;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class UavRegionActivity extends FragmentActivity implements OnMapReadyCallback {

    private static UavRegionActivity instance;

    GoogleMap mMap;
    private static HashMap<String, Marker> userMap;
    private EventReactor eventReactor;
    private String username;
    private Timer uavTimer;

    private static int uavCountdown = 0;
    private int radius;
    private boolean firstTime;


    /*----------
    - Description: runs when the activity first boots up.
                   - Initializes static instance, event reactor and boolean value.
                   - Creates a map fragment.
                   - Sets the map layout
    ----------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        firstTime = true;

        radius = 500;

        username = getIntent().getExtras().getString("username");

        eventReactor = EventReactor.getInstance();
        setContentView(R.layout.activity_maps);
        if (uavCountdown == 0) {
            userMap = new HashMap<>();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /*----------
    - Description: Creates a map and sends a new GET_USERS event to the server.
    - Input: none
    - Return: none
    ----------*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Event ev = new Event("GET_USERS");
        ev.put(Fields.ACTIVITY, "UavRegionActivity");
        eventReactor.request(ev);
    }

    /*----------
        - Description: Updates list of users
        - Input: userArr
        - Return: none
        ----------*/
    public void updateUserList(final ArrayList<String> userArr) {
        for (String user: userArr) {
            userMap.put(user, null);
        }
        if (uavCountdown == 0) {
            uavCountdown = 100;
            uavTimer = new Timer();
            uavTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (uavCountdown == 0) {
                        uavTimer.cancel();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                GameActivity.getInstance().logs.append("\nUAV region completed");
                                GameActivity.getInstance().uavRegionActive = false;
                                finish();
                            }
                        });
                    }
                    else {
                        uavCountdown--;
                        System.out.println("Getting self location");
                        Location location = GameActivity.getInstance().getClientLocation();
                        if (location != null) {
                            System.out.println("Location is not null");
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            final MarkerOptions newMarker = new MarkerOptions().position(latLng).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Marker oldMarker = userMap.get(username);
                                    if (oldMarker != null) {
                                        oldMarker.remove();
                                    }
                                    userMap.put(username, instance.mMap.addMarker(newMarker));
                                    System.out.println("Added marker for " + username + ": " + newMarker);
                                }
                            });
                        }
                        for (String user : userArr) {
                            if (!user.equals(username)) {
                                System.out.println("Getting location for " + user + " (tick " + uavCountdown + ")");
                                Event event = new Event("GET_LOCATION");
                                event.put(Fields.ACTIVITY, "UavRegionActivity");
                                event.put(Fields.RECIPIENT, user);
                                eventReactor.request(event);
                            }
                        }
                    }
                }
            }, 0, 3000);
        }
        else {
            if (firstTime) {
                zoomCamera();
            }
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


    /*----------
    - Description: Responsible for animating camera in map to correct distance.
    - Input: none
    - Return: none
    ----------*/
    public void zoomCamera() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final LatLngBounds.Builder builder = new LatLngBounds.Builder();
                int numMarkers = 0;
                for (Marker marker: userMap.values()) {
                    if (marker != null) {
                        LatLng currLatLng = userMap.get(username).getPosition();
                        float[] results = new float[1];
                        Location.distanceBetween(currLatLng.latitude, currLatLng.longitude, marker.getPosition().latitude, marker.getPosition().longitude, results);
                        System.out.println("Results: " + results[0]);
                        if (results[0] < radius) {
                            builder.include(marker.getPosition());
                            numMarkers++;
                        }
                    }
                }
                if (userMap.get(username) != null) {
                    if (numMarkers > 1) {
                        instance.mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
                    }
                    else {
                        LatLng currLatLng = userMap.get(username).getPosition();
                        instance.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currLatLng, 18));
                    }
                    firstTime = false;
                }
                else {
                    System.out.println("Map Zoom Cancelled");
                }
            }
        });
    }

    /*----------
        - Description: Shows the current location of a given user.
        - Input: bytes, final user
        - Return: none
        ----------*/
    public void showLocation(byte[] bytes, final String user) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        Location location = Location.CREATOR.createFromParcel(parcel);
        if (location != null) {
            final LatLng currLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            final MarkerOptions newMarker = new MarkerOptions().position(currLocationLatLng).title(user);
            if (instance.mMap != null && userMap.get(username) != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LatLng currLatLng = userMap.get(username).getPosition();
                        float[] results = new float[1];
                        Location.distanceBetween(currLatLng.latitude, currLatLng.longitude, newMarker.getPosition().latitude, newMarker.getPosition().longitude, results);
                        System.out.println("Results: " + results[0]);
                        if (results[0] < radius) {
                            Marker oldMarker = userMap.get(user);
                            if (oldMarker != null) {
                                oldMarker.remove();
                            }
                            userMap.put(user, instance.mMap.addMarker(newMarker));
                            System.out.println("Added marker for " + user + ": " + newMarker);
                        }
                        if (firstTime) {
                            zoomCamera();
                        }
                    }
                });
            }
        }
        else {
            System.out.println("Received NULL location");
        }
    }

    //Static instance of UavRegionActivity class
    public static UavRegionActivity getInstance() {
        return instance;
    }
}
