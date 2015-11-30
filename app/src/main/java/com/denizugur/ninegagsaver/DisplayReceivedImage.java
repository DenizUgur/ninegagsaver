package com.denizugur.ninegagsaver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.nononsenseapps.filepicker.FilePickerActivity;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.denizugur.ninegagsaver.MainActivity.GAG_TITLE;
import static com.denizugur.ninegagsaver.MainActivity.GAG_URL;

public class DisplayReceivedImage extends AppCompatActivity implements View.OnClickListener {

    String gagTitle = "";
    String newGagTitle = "";
    Bitmap photo;
    Bitmap newBitmap = null;
    String gagURL = null;
    String photo_id = null;
    Boolean isCustom;
    private int PICK_IMAGE_REQUEST = 1;
    private int FILE_CODE = 0;
    private Context context;
    private int customPercent = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
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
        gagURL = intent.getStringExtra(GAG_URL);
        isCustom = getIntent().getBooleanExtra("isCustom", false);

        setContentView(R.layout.activity_display_recieved_image);

        final FloatingActionMenu fam = (FloatingActionMenu) findViewById(R.id.fam);
        FloatingActionButton save = (FloatingActionButton) findViewById(R.id.save);
        FloatingActionButton share = (FloatingActionButton) findViewById(R.id.share);
        FloatingActionButton changeTitle = (FloatingActionButton) findViewById(R.id.changeTitle);
        FloatingActionButton changeSize = (FloatingActionButton) findViewById(R.id.changeSize);
        final TouchImageView mImageView =  (TouchImageView) findViewById(R.id.imageViewPhoto);

        DiscreteSeekBar seekbar = (DiscreteSeekBar) findViewById(R.id.seekBar);
        final RelativeLayout sbc = (RelativeLayout) findViewById(R.id.seekBarContainer);
        sbc.setVisibility(View.INVISIBLE);

        changeSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sbc.setVisibility(View.VISIBLE);
            }
        });

        seekbar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                customPercent = value;
                process(photo, mImageView, true);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                sbc.setVisibility(View.INVISIBLE);
                fam.close(true);
            }
        });

        changeTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(context)
                        .title("Change Title Text")
                        .theme(Theme.DARK)
                        .cancelable(false)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .alwaysCallInputCallback()
                        .negativeText("Cancel")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                gagTitle = newGagTitle;
                                process(photo, mImageView, false);
                            }
                        })
                        .input("Title", gagTitle, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                if (input.length() == 0) {
                                    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                    newGagTitle = "";
                                    mImageView.setImageBitmap(photo);
                                } else {
                                    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                    newGagTitle = input.toString();
                                }
                            }
                        }).show();
            }
        });

        save.setOnClickListener(this);
        share.setOnClickListener(this);

        if (isCustom) {
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            i.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(i, PICK_IMAGE_REQUEST);
        } else {
            File photoLocal = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + "downloads" + File.separator + "GAG.png");
            photo = BitmapFactory.decodeFile(String.valueOf(photoLocal));
            mImageView.setImageBitmap(photo);
            process(photo, mImageView, false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            Cursor cursor = null;
            try {
                String[] proj = {MediaStore.Images.Media.DATA};
                cursor = this.getContentResolver().query(uri, proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();

                String file = cursor.getString(column_index);
                photo = BitmapFactory.decodeFile(file);

                TouchImageView mImageView = (TouchImageView) findViewById(R.id.imageViewPhoto);
                mImageView.setImageBitmap(photo);

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            final TouchImageView mImageView = (TouchImageView) findViewById(R.id.imageViewPhoto);
            new MaterialDialog.Builder(this)
                    .title("Set title for your image")
                    .content("This text will be show on top the image")
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .theme(Theme.DARK)
                    .cancelable(false)
                    .alwaysCallInputCallback()
                    .input("9gag is awesome...", "", new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            if (input.length() == 0) {
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                gagTitle = "";
                                mImageView.setImageBitmap(photo);
                            } else {
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                gagTitle = input.toString();
                                mImageView.setImageBitmap(photo);
                                process(photo, mImageView, false);
                            }
                        }
                    })
                    .negativeText("Back")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                            finish();
                        }
                    })
                    .show();
        } else if (requestCode == FILE_CODE && resultCode == RESULT_OK) {
            final String modifiedTitleCustom = gagTitle.replaceAll(" ", "-");
            final Uri uri = data.getData();

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    File file = new File(uri.getPath(), modifiedTitleCustom + ".png");

                    FileOutputStream outo = null;
                    try {
                        outo = new FileOutputStream(file);
                        newBitmap.compress(Bitmap.CompressFormat.PNG, 100, outo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (outo != null) {
                                outo.close();
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
            Toast.makeText(getApplicationContext(), "Photo saved to" + uri.getPath(), Toast.LENGTH_LONG).show();
        } else {
            finish();
        }
    }

    private void process(Bitmap bitmap, TouchImageView mImageView, Boolean customSizePercent) {

        try {
            Bitmap.Config config = bitmap.getConfig();
            if (config == null) {
                config = Bitmap.Config.ARGB_8888;
            }

            newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);
            Canvas newCanvas = new Canvas(newBitmap);

            newCanvas.drawBitmap(bitmap, 0, 0, null);

            if (customSizePercent) {
                drawText(newCanvas, bitmap, customPercent, false);
            } else if (gagTitle.length() <= 15) {
                drawText(newCanvas, bitmap, 5, true);
            } else {
                drawText(newCanvas, bitmap, 5, false);
            }

            mImageView.setImageBitmap(newBitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawText(Canvas canvas, Bitmap bitmap, int percent, Boolean isShort) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isGradient = prefs.getBoolean("gradientKey", true);

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
        }

        int pL = bitmap.getWidth() / 100;
        int pT = bitmap.getHeight() / 100;

        if (isShort) {

            TextPaint paintText = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paintText.setColor(Color.WHITE);
            paintText.setTextSize(size);
            paintText.setStyle(Paint.Style.FILL_AND_STROKE);
            paintText.setShadowLayer(3f, 3f, 3f, Color.BLACK);

            Rect rectText = new Rect();
            paintText.getTextBounds(gagTitle, 0, gagTitle.length(), rectText);

            if (isGradient) {
                int GRADIENT_HEIGHT = rectText.height();

                Paint paint = new Paint();
                LinearGradient shader = new LinearGradient(0, 0, 0, GRADIENT_HEIGHT, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
                paint.setShader(shader);
                canvas.drawRect(0, 0, newBitmap.getWidth(), GRADIENT_HEIGHT, paint);
            }

            canvas.drawText(gagTitle, pL, rectText.height() + pT / 2, paintText);
        } else {

            TextPaint paintText = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            paintText.setColor(Color.WHITE);
            paintText.setTextSize(size);
            paintText.setStyle(Paint.Style.FILL_AND_STROKE);
            paintText.setShadowLayer(3f, 3f, 3f, Color.BLACK);

            StaticLayout mTextLayout = new StaticLayout(
                    gagTitle,
                    paintText,
                    canvas.getWidth(),
                    Layout.Alignment.ALIGN_NORMAL,
                    1.0f,
                    0.0f,
                    false);

            if (isGradient) {
                int GRADIENT_HEIGHT = mTextLayout.getHeight();

                Paint paint = new Paint();
                LinearGradient shader = new LinearGradient(0, 0, 0, GRADIENT_HEIGHT, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
                paint.setShader(shader);
                canvas.drawRect(0, 0, newBitmap.getWidth(), GRADIENT_HEIGHT, paint);
            }

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

        final FloatingActionButton save = (FloatingActionButton) findViewById(R.id.save);
        save.setEnabled(false);
        save.setIndeterminate(true);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String file_path = prefs.getString("path", null);

        final File dir = new File(file_path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Thread normal_save = new Thread(new Runnable() {
            @Override
            public void run() {
                pushObjectToSP();

                String modifiedTitle = gagTitle.replaceAll(" ", "-");
                File file = new File(dir, modifiedTitle + ".png");

                FileOutputStream outo = null;
                try {
                    outo = new FileOutputStream(file);
                    newBitmap.compress(Bitmap.CompressFormat.PNG, 100, outo);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outo != null) {
                            outo.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                File directory_gags = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + "gags");
                if (!directory_gags.exists()) {
                    directory_gags.mkdirs();
                }
                File dir_app = new File(directory_gags + File.separator + photo_id);
                FileOutputStream outi = null;

                try {
                    outi = new FileOutputStream(dir_app);
                    newBitmap.compress(Bitmap.CompressFormat.PNG, 100, outi);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outi != null) {
                            outi.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(file));
                sendBroadcast(intent);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        save.setIndeterminate(false);
                        Toast.makeText(getApplicationContext(), "Gag saved successfully", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        if (isCustom) {
            Intent i = new Intent(context, FilePickerActivity.class);

            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
            i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

            save.setIndeterminate(false);
            startActivityForResult(i, FILE_CODE);
        } else {
            normal_save.start();
        }
    }

    public void sharePhoto() {

        final FloatingActionButton share = (FloatingActionButton) findViewById(R.id.share);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        share.setEnabled(false);
        share.setIndeterminate(true);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                pushObjectToSP();

                File directory_gags = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + "gags");
                if (!directory_gags.exists()) {
                    directory_gags.mkdirs();
                }
                final File dir_app = new File(directory_gags + File.separator + photo_id);

                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(dir_app);
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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        share.setEnabled(true);
                        share.setIndeterminate(false);
                    }
                });

                Uri uri = Uri.fromFile(dir_app);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("image/*");

                Boolean boo = prefs.getBoolean("useTitleAsMessage", false);

                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
                if (boo) {
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, gagTitle);
                } else {
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
                }
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(intent, "Share Gag to..."));
            }
        });
        t.start();
    }

    private void pushObjectToSP() {

        WriteObjectSP wosp = new WriteObjectSP(this);

        fetchGAG f = new fetchGAG();

        f.setURL(gagURL);
        f.fetch();

        String title = f.getTitle();
        String likes = f.getLikes();
        String comments = f.getComments();
        photo_id = f.getID();

        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String saved_date = formatter.format(today);

                File file_path = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) +
                        File.separator +
                        "gags" +
                        File.separator +
                        photo_id);

        try {
            Gson gson = new Gson();
            JsonParser jsonParser = new JsonParser();

            gagInfo gi = new gagInfo();
            gi.setTitle(title);
            gi.setLikes(likes);
            gi.setComments(comments);
            gi.setSaved_Date(saved_date);
            gi.setPhotoId(photo_id);
            gi.setFile_Path(String.valueOf(file_path));

            String jsonStr = jsonParser.parse(gson.toJson(gi)).toString();
            JSONObject JSONObject = new JSONObject(jsonStr);

            wosp.writeObject(JSONObject, photo_id);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + "downloads");
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (String aChildren : children) {
                new File(dir, aChildren).delete();
            }
        }

        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    @Override
    public void onDestroy() {
        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + "downloads");
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (String aChildren : children) {
                new File(dir, aChildren).delete();
            }
        }
        super.onDestroy();
    }
}