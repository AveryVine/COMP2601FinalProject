package edu.carleton.COMP2601.finalproject;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by AveryVine on 2017-03-18.
 */

public class GameActivity extends AppCompatActivity {
    private static GameActivity instance;

    private Button button_camera;
    private Button button_map;
    private Button button_deployUAV;
    private Button button_uavRegion;
    private Button button_proximitySensor;
    private Button button_disarmCameras;
    private Button button_incognito;
    private Button button_gpsDecoy;

    private EventReactor eventReactor;
    private int cash = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setTitle("Cash: $0");
        instance = this;

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
                button_cameraOnClick();
            }
        });

        button_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_mapOnClick();
            }
        });

        button_deployUAV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_deployUAVOnClick();
            }
        });

        button_uavRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_uavRegionOnClick();
            }
        });

        button_proximitySensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_proximitySensorOnClick();
            }
        });

        button_disarmCameras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_disarmCamerasOnClick();
            }
        });

        button_incognito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_incognitoOnClick();
            }
        });

        button_gpsDecoy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_gpsDecoyOnClick();
            }
        });
        //TODO - any more onCreate stuff
    }



    @Override
    protected void onStop() {
        super.onStop();
        //TODO - any stuff related to leaving a game
    }



    private void button_cameraOnClick() {
        FEATURE_UNAVAILABLE();
    }



    private void button_mapOnClick() {
        FEATURE_UNAVAILABLE();
    }



    private void button_deployUAVOnClick() {
        FEATURE_UNAVAILABLE();
    }



    private void button_uavRegionOnClick() {
        FEATURE_UNAVAILABLE();
    }



    private void button_proximitySensorOnClick() {
        FEATURE_UNAVAILABLE();
    }



    private void button_disarmCamerasOnClick() {
        FEATURE_UNAVAILABLE();
    }



    private void button_incognitoOnClick() {
        FEATURE_UNAVAILABLE();
    }



    private void button_gpsDecoyOnClick() {
        FEATURE_UNAVAILABLE();
    }



    public void cashDeposit(int deposit) {
        cash += deposit;
        setTitle("Cash: $" + cash);
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

}
