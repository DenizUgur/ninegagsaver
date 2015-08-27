package com.denizugur.deniz.ninegagsaver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.github.clans.fab.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.denizugur.deniz.ninegagsaver.MainActivity.*;

public class DisplayReceivedImage extends AppCompatActivity implements View.OnClickListener {

    public final static String APP_PREFS = "com.denizugur.deniz.advanced9gagphotosaver.PREFS";
    String gagTitle = null;
    Bitmap photo = null;
    Bitmap newBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        gagTitle = intent.getStringExtra(GAG_TITLE);

        setContentView(R.layout.activity_display_recieved_image);

        File photoLocal = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "GAG.png");
        photo = BitmapFactory.decodeFile(String.valueOf(photoLocal));

        ImageView mImageView = (ImageView) findViewById(R.id.imageViewPhoto);
        mImageView.setImageBitmap(photo);

        FloatingActionButton save = (FloatingActionButton) findViewById(R.id.save);
        FloatingActionButton share = (FloatingActionButton) findViewById(R.id.share);

        save.setOnClickListener(this);
        share.setOnClickListener(this);

        process(photo, mImageView);
    }

    private void process(Bitmap bitmap, ImageView mImageView) {

        try {
            Bitmap.Config config = bitmap.getConfig();
            if (config == null) {
                config = Bitmap.Config.ARGB_8888;
            }

            newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);
            Canvas newCanvas = new Canvas(newBitmap);

            newCanvas.drawBitmap(bitmap, 0, 0, null);

            Display display = getWindowManager().getDefaultDisplay();
            Point sizeScreen = new Point();
            display.getSize(sizeScreen);
            int screenHeight = sizeScreen.y;

            Log.d("TEXT LENGHT", String.valueOf(gagTitle.length()));

            if (gagTitle.length() <= 15) {
                if (bitmap.getHeight() < (screenHeight / 2)) {
                    drawText(newCanvas, bitmap, 10, true);
                } else {
                    drawText(newCanvas, bitmap, 5, true);
                }
            } else {
                drawText(newCanvas, bitmap, 5, false);
            }

            mImageView.setImageBitmap(newBitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    lenght < {
        < height /2 or > height /2
        }
    lenght > {
        < height /2 or > height /2

        if (> height /2) {
                math for correct height
            }
        }
     */

    private void drawText(Canvas canvas, Bitmap bitmap, int percent, Boolean isShort) {

        int size = 0;
        int smallDummyHeight = 0;
        while (smallDummyHeight < ((bitmap.getHeight() / 100) * percent)) {
            TextPaint paintText = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            size = size + 1;
            paintText.setTextSize(size);
            paintText.setStyle(Paint.Style.FILL_AND_STROKE);
            paintText.setShadowLayer(3f, 3f, 3f, Color.BLACK);

            Rect smallDummyRect = new Rect();
            paintText.getTextBounds(gagTitle, 0, gagTitle.length(), smallDummyRect);

            smallDummyHeight = smallDummyRect.height();
            Log.d("TEXT SIZE", "Text Height: " + String.valueOf(smallDummyHeight) + " Bitmap Height: " + bitmap.getHeight());
        }

        if (isShort) {
            Log.d("ALERT", "short " + gagTitle.length());

            TextPaint paintText = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paintText.setColor(Color.WHITE);
            paintText.setTextSize(size);
            paintText.setStyle(Paint.Style.FILL_AND_STROKE);
            paintText.setShadowLayer(3f, 3f, 3f, Color.BLACK);

            Rect rectText = new Rect();
            paintText.getTextBounds(gagTitle, 0, gagTitle.length(), rectText);


            int GRADIENT_HEIGHT = rectText.height();

            Paint paint = new Paint();
            LinearGradient shader = new LinearGradient(0, 0, 0, GRADIENT_HEIGHT, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
            paint.setShader(shader);
            canvas.drawRect(0, 0, newBitmap.getWidth(), GRADIENT_HEIGHT, paint);

            canvas.drawText(gagTitle, 0, 0, paintText);
        } else {
            Log.d("ALERT", "long " + gagTitle.length());

            TextPaint paintText = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paintText.setColor(Color.WHITE);
            paintText.setTextSize(size);
            paintText.setStyle(Paint.Style.FILL_AND_STROKE);
            paintText.setShadowLayer(3f, 3f, 3f, Color.BLACK);

            int pL = bitmap.getWidth() / 100;
            int pT = bitmap.getHeight() / 100;

            StaticLayout mTextLayout = new StaticLayout(
                    gagTitle,
                    paintText,
                    canvas.getWidth(),
                    Layout.Alignment.ALIGN_NORMAL,
                    1.0f,
                    0.0f,
                    false);

            int GRADIENT_HEIGHT = mTextLayout.getHeight();

            Paint paint = new Paint();
            LinearGradient shader = new LinearGradient(0, 0, 0, GRADIENT_HEIGHT, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
            paint.setShader(shader);
            canvas.drawRect(0, 0, newBitmap.getWidth(), GRADIENT_HEIGHT, paint);

            canvas.save();
            canvas.translate(pL, pT);
            mTextLayout.draw(canvas);
            canvas.restore();

        }
    }

    public void onClick(View v) {

        switch(v.getId()) {

            case R.id.save:
                savePhoto();
                break;

            case R.id.share:
                sharePhoto();
                break;
        }
    }

    public void savePhoto() {

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        String file_path = prefs.getString("path", null);

        final File dir = new File(file_path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String modifiedTitle = gagTitle.replaceAll(" ", "-");
                File file = new File(dir, modifiedTitle + ".png");

                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(file);
                    newBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(file));
                sendBroadcast(intent);
            }
        });

        t.start();

        Toast.makeText(getApplicationContext(), "Gag saved successfully", Toast.LENGTH_SHORT).show();

    }

    public void sharePhoto() {

        final File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "tmp");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fOut;
                try {
                    fOut = new FileOutputStream(file);
                    newBitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                    fOut.flush();
                    fOut.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, gagTitle);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Share Gag to..."));

    }

    @Override
    public void onBackPressed() {
        File dir = new File(String.valueOf(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)));
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for(String aChildren : children) new File(dir, aChildren).delete();
        }

        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    @Override
    public void onDestroy() {
        File dir = new File(String.valueOf(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)));
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for(String aChildren : children) new File(dir, aChildren).delete();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_recieved_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }
}