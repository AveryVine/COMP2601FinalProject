package edu.carleton.COMP2601.finalproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

public class PhotoResponseActivity extends AppCompatActivity {

    private ImageView imageView, xImageView;
    private Bitmap image, scaledImage;
    private byte[] bytes;

    private String opponent;
    private boolean success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_response);
        opponent = getIntent().getExtras().getString("sender");
        success = getIntent().getExtras().getBoolean("success");
        bytes = GameActivity.getInstance().getImageBytes();

        imageView = (ImageView) findViewById(R.id.imageView);
        xImageView = (ImageView) findViewById(R.id.xImageView);

        if (success) {
            setTitle("Kill Confirmed: " + opponent);
            GameActivity.getInstance().logs.append("\nKill Confirmed: " + opponent);
        }
        else {
            setTitle("Target Escaped: " + opponent);
            GameActivity.getInstance().logs.append("\nTarget Escaped: " + opponent);
            imageView.setImageAlpha(100);
            xImageView.setImageAlpha(0);
        }

        image = BitmapFactory.decodeByteArray(bytes,  0, bytes.length, null);

        int nh = (int) ( image.getHeight() * (512.0 / image.getWidth()) );
        scaledImage = Bitmap.createScaledBitmap(image, 512, nh, true);

        imageView.setImageBitmap(scaledImage);
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
}
