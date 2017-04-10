package edu.carleton.COMP2601.finalproject;

import android.location.Location;
import android.os.CountDownTimer;
import android.os.Looper;
import android.os.Parcel;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
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
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class DeployUavActivity extends FragmentActivity implements OnMapReadyCallback {

    private static DeployUavActivity instance;

    private GoogleMap mMap;
    private Location currentLocation;
    private HashMap<String, MarkerOptions> userArr;
    private EventReactor eventReactor;
    private String username;

    private static int uavCountdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        username = getIntent().getExtras().getString("username");

        eventReactor = EventReactor.getInstance();
        uavCountdown = 6;
        setContentView(R.layout.activity_maps);
        userArr = new HashMap<>();
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
            this.userArr.put(user, null);
        }
        final Event locationEv = new Event("GET_LOCATION");
        System.out.println("Preparing countdown timer");
        final Timer uavTimer = new Timer();
        uavTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (uavCountdown == 1) {
                    uavTimer.cancel();
                }
                uavCountdown--;
                for (String user : userArr) {
                    System.out.println("Getting location for " + user + " (tick " + uavCountdown + ")");
                    locationEv.put(Fields.RECIPIENT, user);
                    eventReactor.request(locationEv);
                }
            }
        }, 0, 5000);
    }


    public void showLocation(byte[] bytes, final String user) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        Location location = Location.CREATOR.createFromParcel(parcel);
        if (location != null) {
            final LatLng currLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MarkerOptions newMarker;
                    if (user.equals(username)) {
                        newMarker = new MarkerOptions().position(currLocationLatLng).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    }
                    else {
                        newMarker = new MarkerOptions().position(currLocationLatLng).title(user);
                    }
                    userArr.put(user, newMarker);
                    mMap.addMarker(newMarker);
                    System.out.println("Added marker for " + user + ": " + newMarker);
                }
            });
            final LatLngBounds.Builder builder = new LatLngBounds.Builder();
            boolean cancelBuild = false;
            for (MarkerOptions marker: userArr.values()) {
                if (marker == null) {
                    System.out.println(userArr);
                    cancelBuild = true;
                    break;
                }
                builder.include(marker.getPosition());
            }
            if (!cancelBuild) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
                    }
                });
            }
            else {
                System.out.println("Marker build cancelled");
            }
        }
        else {
            System.out.println("Received NULL location");
        }
    }


    public static DeployUavActivity getInstance() { return instance; }
}
