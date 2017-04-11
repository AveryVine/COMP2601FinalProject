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

public class DeployUavActivity extends FragmentActivity implements OnMapReadyCallback {

    private static DeployUavActivity instance;

    GoogleMap mMap;
    private static HashMap<String, Marker> userMap;
    private EventReactor eventReactor;
    private String username;
    private Timer uavTimer;

    private static int uavCountdown = 0;
    private boolean firstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        firstTime = true;

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


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Event ev = new Event("GET_USERS");
        ev.put(Fields.ACTIVITY, "DeployUavActivity");
        eventReactor.request(ev);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == -1) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", resultCode);
            setResult(-1,returnIntent);
            finish();
        }
    }


    public void updateUserList(final ArrayList<String> userArr) {
        for (String user: userArr) {
            userMap.put(user, null);
        }
        if (uavCountdown == 0) {
            uavCountdown = 20;
            uavTimer = new Timer();
            uavTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (uavCountdown == 0) {
                        uavTimer.cancel();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                GameActivity.getInstance().logs.append("\nUAV scan completed");
                                GameActivity.getInstance().deployUAVActive = false;
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
                                event.put(Fields.ACTIVITY, "DeployUavActivity");
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

    public void zoomCamera() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final LatLngBounds.Builder builder = new LatLngBounds.Builder();
                boolean cancelBuild = false;
                for (Marker marker: userMap.values()) {
                    if (marker == null) {
                        System.out.println(userMap);
                        cancelBuild = true;
                        break;
                    }
                    builder.include(marker.getPosition());
                }
                if (!cancelBuild) {
                    instance.mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
                    firstTime = false;
                }
                else {
                    System.out.println("Map Zoom Cancelled");
                }
            }
        });
    }


    public void showLocation(byte[] bytes, final String user) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        Location location = Location.CREATOR.createFromParcel(parcel);
        if (location != null) {
            final LatLng currLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            final MarkerOptions newMarker = new MarkerOptions().position(currLocationLatLng).title(user);
            if (instance.mMap != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Marker oldMarker = userMap.get(user);
                        if (oldMarker != null) {
                            oldMarker.remove();
                        }
                        userMap.put(user, instance.mMap.addMarker(newMarker));
                        System.out.println("Added marker for " + user + ": " + newMarker);
                    }
                });
                if (firstTime) {
                    zoomCamera();
                }
            }
        }
        else {
            System.out.println("Received NULL location");
        }
    }


    public static int getUavCountdown() {
        return uavCountdown;
    }


    public static DeployUavActivity getInstance() {
        return instance;
    }
}
