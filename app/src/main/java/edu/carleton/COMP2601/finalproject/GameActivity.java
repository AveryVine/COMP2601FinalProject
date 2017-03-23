package edu.carleton.COMP2601.finalproject;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by AveryVine on 2017-03-18.
 */

public class GameActivity extends AppCompatActivity implements LocationListener {
    private static GameActivity instance;
    private final int PERMISSION_ACCESS_FINE_LOCATION = 0;

    private Button button_camera;
    private Button button_map;
    private Button button_deployUAV;
    private Button button_uavRegion;
    private Button button_proximitySensor;
    private Button button_disarmCameras;
    private Button button_incognito;
    private Button button_gpsDecoy;

    private Client client;

    private EventReactor eventReactor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setTitle("Cash: $0");
        instance = this;

        client = new Client((LocationManager) getSystemService(Context.LOCATION_SERVICE));

        button_camera = (Button) findViewById(R.id.button_camera);
        button_map = (Button) findViewById(R.id.button_map);
        button_deployUAV = (Button) findViewById(R.id.button_deployUAV);
        button_uavRegion = (Button) findViewById(R.id.button_uavRegion);
        button_proximitySensor = (Button) findViewById(R.id.button_proximitySensor);
        button_disarmCameras = (Button) findViewById(R.id.button_disarmCameras);
        button_incognito = (Button) findViewById(R.id.button_incognito);
        button_gpsDecoy = (Button) findViewById(R.id.button_gpsDecoy);

        eventReactor = EventReactor.getInstance();
        Event event = new Event("CONNECTED_TO_GAME");
        eventReactor.request(event);

        button_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, 1);
                }
            }
        });

        button_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FEATURE_UNAVAILABLE();
            }
        });

        button_deployUAV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FEATURE_UNAVAILABLE();
            }
        });

        button_uavRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FEATURE_UNAVAILABLE();
            }
        });

        button_proximitySensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FEATURE_UNAVAILABLE();
            }
        });

        button_disarmCameras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FEATURE_UNAVAILABLE();
            }
        });

        button_incognito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FEATURE_UNAVAILABLE();
            }
        });

        button_gpsDecoy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FEATURE_UNAVAILABLE();
            }
        });


        client.getCurrentLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Permissions granted for Fine Location");
                } else { System.exit(1); }
                break;
        }
    }



    public void getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        String mprovider = locationManager.getBestProvider(criteria, false);

        if (mprovider != null && !mprovider.equals("")) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location location = locationManager.getLastKnownLocation(mprovider);
            locationManager.requestLocationUpdates(mprovider, 15000, 1, this);

            if (location != null)
                onLocationChanged(location);
            else
                System.out.println("No provider found");
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        //TODO - any stuff related to leaving a game
    }




    public void cashDeposit(int deposit) {
        client.depositCash(deposit);
        setTitle("Cash: $" + client.getCash());
    }



    public static GameActivity getInstance() {
        return instance;
    }



    private void FEATURE_UNAVAILABLE() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.FEATURE_UNAVAILABLE_ALERT);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println(R.string.FEATURE_UNAVAILABLE_ALERT);
            }
        });
        builder.show();
    }

    @Override
    public void onLocationChanged(Location location) {

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
}
