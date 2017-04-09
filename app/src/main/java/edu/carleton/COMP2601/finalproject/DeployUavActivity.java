package edu.carleton.COMP2601.finalproject;

import android.location.Location;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class DeployUavActivity extends FragmentActivity implements OnMapReadyCallback {

    private static DeployUavActivity instance;

    private GoogleMap mMap;
    private Location currentLocation;
    private ArrayList<String> userArr;

    private final int FINE_LOCATION_PERMISSION_REQUEST = 1;
    private final int COARSE_LOCATION_PERMISSION_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        currentLocation = (Location) getIntent().getExtras().get("location");
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

        LatLng currLocationLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//        if (ActivityCompat.checkSelfPermission(GameActivity.getInstance(),
//                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(GameActivity.getInstance(),
//                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(GameActivity.getInstance(),
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    FINE_LOCATION_PERMISSION_REQUEST);
//            ActivityCompat.requestPermissions(GameActivity.getInstance(),
//                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
//                    COARSE_LOCATION_PERMISSION_REQUEST);
//        }
//        else {
//            mMap.setMyLocationEnabled(true);
//        }

        mMap.addMarker(new MarkerOptions().position(currLocationLatLng).title("Current Location"));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(currLocationLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        Event ev = new Event("GET_USERS");
        EventReactor.getInstance().request(ev);
    }


    public void updateUserList(final ArrayList<String> userArr) {
        this.userArr = userArr;
        //call a function that will get every players location and
        //put it on the map
        final Event locationEv = new Event("GET_LOCATION");
        new CountDownTimer(60000, 5000) {

            public void onTick(long millisUntilFinished) {
                //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                //send get location event
                for (String user : userArr) {
                    locationEv.put(Fields.RECIPIENT, user);
                    EventReactor.getInstance().request(locationEv);
                }
            }

            public void onFinish() {
                //mTextField.setText("done!");
                System.out.println("UAV finished.");
            }
        }.start();
    }


    public void showLocation(Location loc) {
        LatLng currLocationLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
        mMap.addMarker(new MarkerOptions().position(currLocationLatLng).title("Current Location"));
    }


    public static DeployUavActivity getInstance() { return instance; }
}
