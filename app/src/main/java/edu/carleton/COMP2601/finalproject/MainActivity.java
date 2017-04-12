package edu.carleton.COMP2601.finalproject;

import android.content.DialogInterface;
import android.content.Intent;
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


    /*----------
    - Description: runs when the activity first boots up
    ----------*/
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


    /*----------
    - Description: Sends a disconnect request message to the server
    - Input: none
    - Return: none
    ----------*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Event event = new Event("DISCONNECT_REQUEST");
        eventReactor.request(event);
    }

    /*----------
    - Description: Called when the subsequent activity returns. Calls the corresponding function.
    - Input: requestCode, resultCode, data
    - Return: none
    ----------*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        connectedResponse();
        if(resultCode == -1) {
            gameOver();
        }
        if (resultCode == 2) {
            youWin();
        }
    }

    /*----------
    - Description: Creates a dialog box for the game winner.
    - Input: none
    - Return: none
    ----------*/
    private void youWin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.mainActivity_youWin_alert);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (username.equals("")) {
                    //TODO - enter a valid username?
                    System.exit(0);
                }
            }
        });
        builder.show();
    }

    /*----------
    - Description: Creates a dialog box for the game loser.
    - Input: none
    - Return: none
    ----------*/
    private void gameOver() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.mainActivity_gameOver_alert);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (username.equals("")) {
                    //TODO - enter a valid username?
                    System.exit(0);
                }
            }
        });
        builder.show();
    }

    /*----------
    - Description: Creates a dialog box which prompts a player for their name.
    - Input: none
    - Return: none
    ----------*/
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

    /*----------
    - Description: Creates a new event Connect request event, and connects to the server using event reactor
    - Input: none
    - Return: none
    ----------*/
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


    /*----------
    - Description: Moves the player into the room they selected.
    - Input: position
    - Return: none
    ----------*/
    private void listItemClicked(int position) {
        String room = roomList.get(position);
        Intent intent = new Intent(getApplicationContext(), RoomActivity.class);
        intent.putExtra("roomTitle", room);
        intent.putExtra("username", username);
        startActivityForResult(intent, 1);
    }


    /*----------
    - Description: Sends a GET_ROOMS event to the server.
    - Input: none
    - Return: none
    ----------*/
    public void connectedResponse() {
        Event event = new Event("GET_ROOMS");
        eventReactor.request(event);
    }


    /*----------
    - Description: Clears and updates the current list of rooms.
    - Input: listOfRooms
    - Return: none
    ----------*/
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


    //Static instance of MainActivity
    public static MainActivity getInstance() {
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
}
