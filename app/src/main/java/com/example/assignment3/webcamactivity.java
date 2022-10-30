package com.example.assignment3;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Webcam activity
 * display the title information and image for each camera marker
 * */
public class webcamactivity extends AppCompatActivity {

    public static String TAG = "testImage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webcamactivity);

        //get textview
        TextView tv = findViewById(R.id.camTv);

        //get information passed from intent
        String title = getIntent().getStringExtra("title");
        String image = getIntent().getStringExtra("image");
        Log.d(TAG, "onCreate: " + image);

        //set text
        tv.setText(title);
        //set image
        WebView wv = findViewById(R.id.webView);

        //wv.getSettings().setUseWideViewPort(true);
        wv.loadUrl(image);

    }
}