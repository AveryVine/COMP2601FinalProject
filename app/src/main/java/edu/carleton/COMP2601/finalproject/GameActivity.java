package edu.carleton.COMP2601.finalproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by AveryVine on 2017-03-18.
 */

public class GameActivity extends AppCompatActivity {
    private static GameActivity instance;
    private final int FINE_LOCATION_PERMISSION_REQUEST = 1;
    private final int COARSE_LOCATION_PERMISSION_REQUEST = 2;

    private Button button_camera;
    private Button button_map;
    private Button button_deployUAV;
    private Button button_uavRegion;
    private Button button_proximitySensor;
    private Button button_disarmCameras;
    private Button button_incognito;
    private Button button_gpsDecoy;

    boolean deployUAVActive = false;
    boolean uavRegionActive = false;

    TextView logs;

    private byte[] bytes;

    private Client client;

    private EventReactor eventReactor;

    /*----------
    - Description: runs when the activity first boots up.
                   - Sets the activity_game view
                   - Initializes static instance, client and every UI button on the view
                   - Sets click-listeners for each button
    ----------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setTitle(getString(R.string.gameActivity_title) + "0");
        instance = this;
        client = new Client(getIntent().getExtras().getString("username"));

        button_camera = (Button) findViewById(R.id.button_camera);
        button_map = (Button) findViewById(R.id.button_map);
        button_deployUAV = (Button) findViewById(R.id.button_deployUAV);
        button_uavRegion = (Button) findViewById(R.id.button_uavRegion);
        button_proximitySensor = (Button) findViewById(R.id.button_proximitySensor);
        button_disarmCameras = (Button) findViewById(R.id.button_disarmCameras);
        button_incognito = (Button) findViewById(R.id.button_incognito);
        button_gpsDecoy = (Button) findViewById(R.id.button_gpsDecoy);
        logs = (TextView) findViewById(R.id.textView_logs);

        logs.setText("Game has begun");

        eventReactor = EventReactor.getInstance();
        Event event = new Event("CONNECTED_TO_GAME");
        eventReactor.request(event);

        button_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(), MakePhotoActivity.class);
                        startActivityForResult(intent, 1);
                    }
                }).start();
            }
        });

        button_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("location", client.getCurrentLocation());
                startActivityForResult(intent, 1);
            }
        });

        button_deployUAV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean completePurchase = true;
                if (uavRegionActive) {
                    completePurchase = false;
                    logs.append("\n" + getString(R.string.gameActivity_uavRegionActive_log));
                }
                else if (!deployUAVActive) {
                    completePurchase = client.purchase("Deployed UAV", 400);
                }
                if (completePurchase) {
                    updateCashTitle();
                    deployUAVActive = true;
                    Intent intent = new Intent(getApplicationContext(), DeployUavActivity.class);
                    intent.putExtra("username", client.getUsername());
                    startActivityForResult(intent, 1);
                }
                else {
                    GameActivity.getInstance().logs.append("\n" + getString(R.string.client_notEnoughCash_log));
                }
            }
        });

        button_uavRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean completePurchase = true;
                if (deployUAVActive) {
                    completePurchase = false;
                    logs.append("\n" + getString(R.string.gameActivity_deployUavActive_log));
                }
                else if (!uavRegionActive) {
                    completePurchase = client.purchase("Deployed UAV Region", 200);
                }
                if (completePurchase) {
                    updateCashTitle();
                    uavRegionActive = true;
                    Intent intent = new Intent(getApplicationContext(), UavRegionActivity.class);
                    intent.putExtra("username", client.getUsername());
                    startActivityForResult(intent, 1);
                }
                else {
                    GameActivity.getInstance().logs.append("\n" + getString(R.string.client_notEnoughCash_log));
                }
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
    }


    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("onStart");
        client.onStart();
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
    - Description: Deposits the given amount of cash to the client's account
    - Input: final deposit
    - Return: none
    ----------*/
    public void cashDeposit(final int deposit) {
        client.depositCash(deposit);
        updateCashTitle();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logs.append("\n" + getString(R.string.gameActivity_receivedDeposit_log) + deposit);
            }
        });
    }

    /*----------
    - Description: Updates client's Cash Title UI
    - Input: none
    - Return: none
    ----------*/
    public void updateCashTitle() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle("\n" + getString(R.string.gameActivity_title) + client.getCash());
            }
        });
    }

    //Getter for client's current location
    public Location getClientLocation() {
        return client.getCurrentLocation();
    }


    /*----------
    - Description: Sends the client location to the server (SEND_LOCATION event)
    - Input: id, activity
    - Return: none
    ----------*/
    public void sendClientLocation(String id, String activity) {
        Event event = new Event("SEND_LOCATION");
        event.put(Fields.ACTIVITY, activity);
        Parcel p = Parcel.obtain();
        getClientLocation().writeToParcel(p, 0);
        final byte[] b = p.marshall();
        event.put(Fields.BODY, b);
        event.put(Fields.RECIPIENT, id);
        eventReactor.request(event);
        p.recycle();
    }

    /*----------
    - Description: Sets bytes and starts the PhotoConfirmationActivity activity.
    - Input: sender, bytes
    - Return: none
    ----------*/
    public void photoReceived(String sender, byte[] bytes) {
        this.bytes = bytes;
        Intent intent = new Intent(getApplicationContext(), PhotoConfirmationActivity.class);
        intent.putExtra("sender", sender);
        startActivityForResult(intent, 1);
    }

    /*----------
    - Description: Sets bytes and starts the PhotoResponseActivity activity.
    - Input: sender, bytes, success
    - Return: none
    ----------*/
    public void photoResponseReceived(String sender, byte[] bytes, boolean success) {
        this.bytes = bytes;
        Intent intent = new Intent(getApplicationContext(), PhotoResponseActivity.class);
        intent.putExtra("sender", sender);
        intent.putExtra("success", success);
        startActivityForResult(intent, 1);
    }

    //Getter for bytes array
    public byte[] getImageBytes() {
        return bytes;
    }

    //Static instance of GameActivity class
    public static GameActivity getInstance() {
        return instance;
    }

    /*----------
    - Description: Creates a dialog box that notifies user that feature selected is unavailable.
    - Input: none
    - Return: none
    ----------*/
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

    /*----------
    - Description: Creates a toast for user if a certain permission has not been granted
    - Input: requestCode, permissions, grantResults
    - Return: none
    ----------*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    client.getCurrentLocation();
                } else {
                    Toast.makeText(this, "Permission denied for Fine Location", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case COARSE_LOCATION_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    client.getCurrentLocation();
                } else {
                    Toast.makeText(this, "Permission denied for Coarse Location", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}
