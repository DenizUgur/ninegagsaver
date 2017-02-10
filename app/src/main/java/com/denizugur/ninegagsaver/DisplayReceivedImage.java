package com.denizugur.ninegagsaver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.denizugur.helpers.BitmapCacher;
import com.denizugur.helpers.WriteObjectSP;
import com.denizugur.helpers.fetchGAG;
import com.facebook.samples.zoomable.ZoomableDraweeView;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

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
import static com.denizugur.ninegagsaver.MainActivity.PHOTO_URL;


public class DisplayReceivedImage extends AppCompatActivity implements View.OnClickListener {

    String gagTitle = "";
    String newGagTitle = "";
    String gagURL = null;
    String photo_id = null;
    private Context context;
    private Integer customPercent = 5;
    private BitmapCacher bc;

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
        String photoURL = intent.getStringExtra(PHOTO_URL);

        setContentView(R.layout.activity_display_recieved_image);

        final FloatingActionMenu fam = (FloatingActionMenu) findViewById(R.id.fam);
        FloatingActionButton save = (FloatingActionButton) findViewById(R.id.save);
        FloatingActionButton share = (FloatingActionButton) findViewById(R.id.share);
        FloatingActionButton changeTitle = (FloatingActionButton) findViewById(R.id.changeTitle);
        final FloatingActionButton changeSize = (FloatingActionButton) findViewById(R.id.changeSize);

        DiscreteSeekBar seekbar = (DiscreteSeekBar) findViewById(R.id.seekBar);
        final RelativeLayout sbc = (RelativeLayout) findViewById(R.id.seekBarContainer);
        sbc.setVisibility(View.INVISIBLE);

        ZoomableDraweeView zdv = (ZoomableDraweeView) findViewById(R.id.zoomableDV);
        bc = new BitmapCacher(this, zdv);

        changeSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sbc.setVisibility(View.VISIBLE);
                changeSize.setEnabled(false);
                fam.close(true);
            }
        });

        seekbar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                customPercent = value;
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
                Toast.makeText(context,
                        "Changes will take effect after releasing seek bar.",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                bc.reprocess(customPercent);
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
                        .input("Title", gagTitle, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                if (input.length() == 0) {
                                    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                    newGagTitle = "";
                                } else {
                                    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                    newGagTitle = input.toString();
                                }
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                if (!newGagTitle.equals("")) {
                                    bc.reprocess(newGagTitle);
                                } else {
                                    bc.noprocess();
                                }
                                fam.close(true);
                            }
                        }).show();
            }
        });

        save.setOnClickListener(this);
        share.setOnClickListener(this);

        bc.setUri(Uri.parse(photoURL));
        bc.process(gagTitle, customPercent); //TODO: Zoom not working on zdv
    }

    public void onClick(View v) {

        switch (v.getId()) {

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

        String format = prefs.getString("image_format", "0");

        class FORMAT {
            public String ext;
            public Bitmap.CompressFormat bitmap_format;
            public Integer q;

            public void setString(String x) {
                this.ext = x;
            }

            public void setBitmapFormat(Bitmap.CompressFormat x) {
                this.bitmap_format = x;
            }

            public void setQuality(Integer x) {
                this.q = x;
            }
        }

        final FORMAT f = new FORMAT();

        if (format.equals("0")) {
            f.setString(".png");
            f.setBitmapFormat(Bitmap.CompressFormat.PNG);
            f.setQuality(100);
        } else {
            f.setString(".jpeg");
            f.setBitmapFormat(Bitmap.CompressFormat.JPEG);
            f.setQuality(80);
        }

        assert file_path != null;
        final File dir = new File(file_path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Thread normal_save = new Thread(new Runnable() {
            @Override
            public void run() {
                pushObjectToSP();

                String modifiedTitle = gagTitle.replaceAll(" ", "-");
                File file = new File(dir, modifiedTitle + f.ext);

                FileOutputStream outo = null;
                try {
                    outo = new FileOutputStream(file);
                    bc.getBitmap(false).compress(f.bitmap_format, f.q, outo);
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
                File dir_app = new File(directory_gags + File.separator + photo_id + ".png");
                FileOutputStream outi = null;

                try {
                    outi = new FileOutputStream(dir_app);
                    bc.getBitmap(false).compress(f.bitmap_format, f.q, outi);
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

        normal_save.start();
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
                final File dir_app = new File(directory_gags + File.separator + photo_id + ".png");

                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(dir_app);
                    bc.getBitmap(false).compress(Bitmap.CompressFormat.PNG, 100, out);
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
                photo_id +
                ".png");

        Uri file_uri = new Uri.Builder()
                .scheme("file")
                .path(file_path.toString())
                .build();

        try {
            Gson gson = new Gson();
            JsonParser jsonParser = new JsonParser();

            gagInfo gi = new gagInfo();
            gi.setTitle(title);
            gi.setLikes(likes);
            gi.setComments(comments);
            gi.setSaved_Date(saved_date);
            gi.setPhotoId(photo_id);
            gi.setFile_Path(file_uri.toString());

            Log.d("wwwwww", file_uri.toString());

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
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                new File(dir, aChildren).delete();
            }
        }
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }
}