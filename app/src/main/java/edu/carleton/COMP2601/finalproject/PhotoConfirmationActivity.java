package edu.carleton.COMP2601.finalproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoConfirmationActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button buttonYes;
    private Button buttonNo;

    private String opponent;
    private Bitmap image, scaledImage;
    private byte[] bytes;

    private EventReactor eventReactor;

    /*----------
    - Description: runs when the activity first boots up.
                   - Initializes Image view, buttons, bitmaps and click-listeners.
    ----------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_confirmation);
        opponent = getIntent().getExtras().getString("sender");
        bytes = GameActivity.getInstance().getImageBytes();
        setTitle("Sniped By: " + opponent);
        eventReactor = EventReactor.getInstance();

        imageView = (ImageView) findViewById(R.id.imageView);
        buttonYes = (Button) findViewById(R.id.buttonYes);
        buttonNo = (Button) findViewById(R.id.buttonNo);

        image = BitmapFactory.decodeByteArray(bytes,  0, bytes.length, null);

        int nh = (int) ( image.getHeight() * (512.0 / image.getWidth()) );
        scaledImage = Bitmap.createScaledBitmap(image, 512, nh, true);

        imageView.setImageBitmap(scaledImage);

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Event event = new Event("KILL_CONFIRMED");
                event.put(Fields.RECIPIENT, opponent);
                event.put(Fields.BODY, bytes);
                eventReactor.request(event);

                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", -1);
                setResult(-1,returnIntent);
                finish();
            }
        });

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Event event = new Event("TARGET_ESCAPED");
                event.put(Fields.RECIPIENT, opponent);
                event.put(Fields.BODY, bytes);
                eventReactor.request(event);
                finish();
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
            returnIntent.putExtra("result", resultCode);
            setResult(2, returnIntent);
            finish();
        }
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
