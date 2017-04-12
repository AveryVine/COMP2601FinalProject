package edu.carleton.COMP2601.finalproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

public class SendPhotoActivity extends AppCompatActivity {

    private static SendPhotoActivity instance;

    private ListView listView;
    private ImageView imageView;
    private ArrayList<String> userList;
    private ArrayAdapter adapter;
    private byte[] bytes;
    private Bitmap image;
    private Bitmap scaledImage;
    private EventReactor eventReactor;

    /*----------
    - Description: runs when the activity first boots up.
                   - Initializes event reactor, Image/list view, user list, adapter, bitmap, static instance and image.
                   - Adds a click listener for elements in list view.
    ----------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_photo);
        instance = this;

        eventReactor = EventReactor.getInstance();
        bytes = MakePhotoActivity.getInstance().getImageBytes();
        image = BitmapFactory.decodeByteArray(bytes,  0, bytes.length, null);

        int nh = (int) ( image.getHeight() * (512.0 / image.getWidth()) );
        scaledImage = Bitmap.createScaledBitmap(image, 512, nh, true);

        listView = (ListView) findViewById(R.id.listView);
        imageView = (ImageView) findViewById(R.id.imageView);

        userList = new ArrayList<>();
        adapter = new ArrayAdapter(this, R.layout.list_component_white_text, userList);
        listView.setAdapter(adapter);
        imageView.setImageBitmap(scaledImage);

        Event event = new Event("GET_USERS");
        event.put(Fields.ACTIVITY, "SendPhotoActivity");
        eventReactor.request(event);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listItemClicked(position);
            }
        });

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
            returnIntent.putExtra("result", 2);
            setResult(2, returnIntent);
            finish();
        }
    }

    /*----------
    - Description: Called when one of the list items in the list view is clicked.
                   Sends a PHOTO_EVENT event to the server.
    - Input: position
    - Return: none
    ----------*/
    private void listItemClicked(int position) {
        String user = userList.get(position);
        Event event = new Event("PHOTO_EVENT");
        System.out.println("Sending message to " + user);
        event.put(Fields.RECIPIENT, user);
        event.put(Fields.BODY, bytes);
        eventReactor.request(event);
        finish();
    }

    /*----------
    - Description: Updates list of users
    - Input: listOfUsers
    - Return: none
    ----------*/
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

    //Static instance of SendPhotoActivity class
    public static SendPhotoActivity getInstance() {
        return instance;
    }
}
