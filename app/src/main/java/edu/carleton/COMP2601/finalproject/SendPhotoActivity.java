package edu.carleton.COMP2601.finalproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
//        userList.add("Avery");
//        userList.add("Alexei");
        adapter = new ArrayAdapter(this, R.layout.list_component_white_text, userList);
        listView.setAdapter(adapter);
        imageView.setImageBitmap(scaledImage);

        Event event = new Event("GET_USERS");
        event.put(Fields.ACTIVITY, "SendPhotoActivity");
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

    public static SendPhotoActivity getInstance() {
        return instance;
    }
}
