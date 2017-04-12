package edu.carleton.COMP2601.finalproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import java.util.ArrayList;

public class PhotoResponseActivity extends AppCompatActivity {

    private ImageView imageView, xImageView;
    private Bitmap image, scaledImage;
    private byte[] bytes;

    private EventReactor eventReactor;

    private static PhotoResponseActivity instance;

    private String opponent;
    private boolean success;

    /*----------
    - Description: runs when the activity first boots up.
                   - Initializes Image views, event reactor, bitmaps and sets the image.
    ----------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_response);
        instance = this;
        opponent = getIntent().getExtras().getString("sender");
        success = getIntent().getExtras().getBoolean("success");
        bytes = GameActivity.getInstance().getImageBytes();

        eventReactor = EventReactor.getInstance();

        imageView = (ImageView) findViewById(R.id.imageView);
        xImageView = (ImageView) findViewById(R.id.xImageView);

        if (success) {
            String killConfirmed = R.string.photoResponseActivity_killConfirmed + " " + opponent;
            setTitle(killConfirmed);
            GameActivity.getInstance().logs.append("\n" + killConfirmed);
            Event event = new Event("GET_USERS");
            event.put(Fields.ACTIVITY, "PhotoResponseActivity");
            eventReactor.request(event);
        }
        else {
            String targetEscaped = R.string.photoResponseActivity_targetEscaped + " " + opponent;
            setTitle(targetEscaped);
            GameActivity.getInstance().logs.append("\n" + targetEscaped);
            imageView.setImageAlpha(100);
            xImageView.setImageAlpha(0);
        }

        image = BitmapFactory.decodeByteArray(bytes,  0, bytes.length, null);

        int nh = (int) ( image.getHeight() * (512.0 / image.getWidth()) );
        scaledImage = Bitmap.createScaledBitmap(image, 512, nh, true);

        imageView.setImageBitmap(scaledImage);
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
    - Description: Returns a new game over intent if user list size is smaller than 2
    - Input: userList
    - Return: none
    ----------*/
    public void users(ArrayList<String> userList) {
        if (userList.size() < 2) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", 2);
            setResult(2, returnIntent);
            System.out.println("The game will end");
        }
    }

    //Static instance of PhotoResponseActivity
    public static PhotoResponseActivity getInstance() {
        return instance;
    }
}
