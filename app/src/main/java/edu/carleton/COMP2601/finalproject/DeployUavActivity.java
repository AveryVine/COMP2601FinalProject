package edu.carleton.COMP2601.finalproject;

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
    private static HashMap<String, MarkerOptions> userMap;
    private EventReactor eventReactor;
    private String username;
    private Timer uavTimer;

    private static int uavCountdown = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

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


    public void updateUserList(final ArrayList<String> userArr) {
        for (String user: userArr) {
            userMap.put(user, null);
        }
        final Event event = new Event("GET_LOCATION");
        event.put(Fields.ACTIVITY, "DeployUavActivity");
        if (uavCountdown == 0) {
            uavCountdown = 6;
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
                            userMap.put(username, newMarker);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    System.out.println(mMap);
                                    instance.mMap.addMarker(newMarker);
                                    System.out.println("Added marker for " + username + ": " + newMarker);
                                }
                            });
                        }

                        for (String user : userArr) {
                            if (!user.equals(username)) {
                                System.out.println("Getting location for " + user + " (tick " + uavCountdown + ")");
                                event.put(Fields.RECIPIENT, user);
                                eventReactor.request(event);
                            }
                        }
                    }
                }
            }, 0, 5000);
        }
        else {
            zoomCamera();
        }
    }

    public void zoomCamera() {
        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
        boolean cancelBuild = false;
        for (MarkerOptions marker: userMap.values()) {
            if (marker == null) {
                System.out.println(userMap);
                cancelBuild = true;
                break;
            }
            builder.include(marker.getPosition());
        }
        if (!cancelBuild) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    instance.mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
                }
            });
        }
        else {
            System.out.println("Map View Refresh Cancelled");
        }
    }


    public void showLocation(byte[] bytes, final String user) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        Location location = Location.CREATOR.createFromParcel(parcel);
        if (location != null) {
            final LatLng currLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            final MarkerOptions newMarker = new MarkerOptions().position(currLocationLatLng).title(user);
            userMap.put(user, newMarker);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println(mMap);
                    instance.mMap.addMarker(newMarker);
                    System.out.println("Added marker for " + user + ": " + newMarker);
                }
            });
            zoomCamera();
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
