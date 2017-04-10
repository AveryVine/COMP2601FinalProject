package edu.carleton.COMP2601.finalproject;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static MainActivity instance;

    private ListView roomListView;
    private ArrayList<String> roomList;
    private ArrayAdapter adapter;
    private String username;
    private EventReactor eventReactor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.mainActivity_title);
        instance = this;

        roomListView = (ListView) findViewById(R.id.roomList);
        roomList = new ArrayList<>();
        adapter = new ArrayAdapter(this, R.layout.list_component, roomList);
        roomListView.setAdapter(adapter);

        eventReactor = new EventReactor();

        promptForName();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Event event = new Event("DISCONNECT_REQUEST");
        eventReactor.request(event);
    }




    private void promptForName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.mainActivity_promptForName_alert);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                username = input.getText().toString();
                if (username.equals("")) {
                    //TODO - enter a valid username?
                    System.exit(0);
                }
                connectToServer();
                roomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        listItemClicked(position);
                    }
                });
            }
        });
        builder.show();
    }



    private void connectToServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Event event = new Event("CONNECT_REQUEST");
                    eventReactor.connect(username);
                    eventReactor.request(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }



    private void listItemClicked(int position) {
        String room = roomList.get(position);
        Intent intent = new Intent(getApplicationContext(), RoomActivity.class);
        intent.putExtra("roomTitle", room);
        intent.putExtra("username", username);
        startActivity(intent);
    }



    public void connectedResponse() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, R.string.mainActivity_connect_toast, Toast.LENGTH_SHORT).show();
            }
        });
    }



    public void roomList(ArrayList<String> listOfRooms) {
        roomList.clear();
        roomList.addAll(listOfRooms);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }



    public static MainActivity getInstance() {
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
