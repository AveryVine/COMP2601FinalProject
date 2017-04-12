package edu.carleton.COMP2601.finalproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by AveryVine on 2017-03-17.
 */

public class RoomActivity extends AppCompatActivity {
    private static RoomActivity instance;

    private ListView userListView;
    private Button startButton;
    private ArrayList<String> userList;
    private ArrayAdapter adapter;
    private EventReactor eventReactor;
    private String room, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        room = getIntent().getExtras().getString("roomTitle");
        username = getIntent().getExtras().getString("username");
        setTitle(room);
        instance = this;

        userListView = (ListView) findViewById(R.id.userList);
        startButton = (Button) findViewById(R.id.startButton);
        userList = new ArrayList<>();
        adapter = new ArrayAdapter(this, R.layout.list_component, userList);
        userListView.setAdapter(adapter);

        eventReactor = EventReactor.getInstance();

        Event event = new Event("LOAD_ROOM");
        event.put(Fields.ACTIVITY, "RoomActivity");
        event.put(Fields.BODY, room);
        eventReactor.request(event);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userList.size() > 1) {
                    Event event = new Event("START_GAME_REQUEST");
                    eventReactor.request(event);
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RoomActivity.this);
                    builder.setTitle(R.string.roomActivity_unableToStartGame_alert);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.out.println(R.string.roomActivity_unableToStartGame_alert);
                        }
                    });
                    builder.show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Event event = new Event("LEAVE_ROOM");
        event.put(Fields.BODY, room);
        eventReactor.request(event);
    }



    public void users(ArrayList<String> listOfUsers) {
        userList.clear();
        userList.addAll(listOfUsers);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }



    public void startGame() {
        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
        intent.putExtra("username", username);
        startActivityForResult(intent, 1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == -1) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", resultCode);
            setResult(-1,returnIntent);
            finish();
        }
        if (resultCode == 0) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", resultCode);
            setResult(0,returnIntent);
            finish();
        }
        if (resultCode == 2) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", resultCode);
            setResult(2, returnIntent);
            finish();
        }
    }


    public static RoomActivity getInstance() {
        return instance;
    }

}
