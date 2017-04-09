package edu.carleton.COMP2601.finalproject;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoConfirmation extends AppCompatActivity {

    private ImageView imageView;
    private Button buttonYes;
    private Button buttonNo;
    private TextView textView;

    private String opponent;
    private Bitmap image, scaledImage;
    private byte[] bytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_confirmation);
        opponent = getIntent().getExtras().getString("sender");
        bytes = GameActivity.getInstance().getImageBytes();
        setTitle("Sniped By: " + opponent);

        imageView = (ImageView) findViewById(R.id.imageView);
        buttonYes = (Button) findViewById(R.id.buttonYes);
        buttonNo = (Button) findViewById(R.id.buttonNo);
        textView = (TextView) findViewById(R.id.textView);

        image = BitmapFactory.decodeByteArray(bytes,  0, bytes.length, null);

        int nh = (int) ( image.getHeight() * (512.0 / image.getWidth()) );
        scaledImage = Bitmap.createScaledBitmap(image, 512, nh, true);

        imageView.setImageBitmap(scaledImage);

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FEATURE_UNAVAILABLE();
            }
        });

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FEATURE_UNAVAILABLE();
            }
        });
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