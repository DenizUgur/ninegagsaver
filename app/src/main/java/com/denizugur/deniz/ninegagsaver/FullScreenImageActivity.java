package com.denizugur.deniz.ninegagsaver;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class FullScreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        Intent intent = getIntent();
        String file_path = intent.getStringExtra("BitmapImage");

        Bitmap bitmap = BitmapFactory.decodeFile(file_path);

        TouchImageView imageView = (TouchImageView)findViewById(R.id.imgFullScreen);
        imageView.setScrollPosition(0, 0);

        imageView.setLayoutParams(new RelativeLayout.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT));

        imageView.setImageBitmap(bitmap);

    }
}