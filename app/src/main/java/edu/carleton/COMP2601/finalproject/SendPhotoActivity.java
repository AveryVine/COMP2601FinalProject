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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == -1) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", resultCode);
            setResult(-1,returnIntent);
            finish();
        }
    }

    private void listItemClicked(int position) {
        String user = userList.get(position);
        Event event = new Event("PHOTO_EVENT");
        System.out.println("Sending message to " + user);
        event.put(Fields.RECIPIENT, user);
        event.put(Fields.BODY, bytes);
        eventReactor.request(event);
//        finish();
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

    public static SendPhotoActivity getInstance() {
        return instance;
    }
}
