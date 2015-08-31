package com.denizugur.ninegagsaver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    public final static String GAG_TITLE = "com.denizugur.ninegagsaver.GAG_TITLE";
    public final static String GAG_URL = "com.denizugur.ninegagsaver.GAG_URL";
    private final static int EXIT_APP = 0;
    private Context context;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EXIT_APP && resultCode == RESULT_CANCELED) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            Boolean boo = prefs.getBoolean("backTo9gag", true);
            if (boo) {
                finish();
            } else {
                Intent i = new Intent(this, HomeCardActivity.class);
                startActivity(i);
                finish();
            }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String str = prefs.getString("path", null);

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            try {
                new File(str);
                if ("text/plain".equals(type) && isNetworkConnected()) {
                    handleSendText(intent);
                } else {
                        new MaterialDialog.Builder(this)
                                .title("Sorry")
                                .content("No Internet Connection")
                                .positiveText("Go Back")
                                .theme(Theme.DARK)
                                .cancelable(false)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        System.exit(0);
                                    }
                                })
                                .show();
                }
            } catch (Exception e) {
                Intent i = new Intent(this, SettingsActivity.class);
                Toast.makeText(getApplicationContext(), getString(R.string.choose_folder_toast), Toast.LENGTH_LONG).show();
                startActivity(i);
            }
        } else {
            try {
                new File(str);
                Intent i = new Intent(this, HomeCardActivity.class);
                startActivity(i);
            } catch (Exception e) {
                Intent i = new Intent(this, SettingsActivity.class);
                Toast.makeText(getApplicationContext(), getString(R.string.choose_folder_toast), Toast.LENGTH_LONG).show();
                startActivity(i);
            }
            finish();
        }
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            if (sharedText.contains("9gag")) {
                String gagURL = null;
                String[] splitted = sharedText.split("http://");
                try {
                    URL url = new URL("http://" + splitted[1]);
                    gagURL = url.getPath();
                    gagURL = "http://9gag.com" + gagURL;
                    Log.d("URL", gagURL);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                final MaterialDialog md = new MaterialDialog.Builder(this)
                        .title("Loading")
                        .content("Please wait...")
                        .progress(true, 0)
                        .cancelable(false)
                        .theme(Theme.DARK)
                        .show();

                final fetchGAG f = new fetchGAG();
                f.setURL(gagURL);

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        f.fetch();

                        Display display = getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);
                        int screenHeight = size.y;

                        if (f.getHeightImage() > screenHeight) {
                            Log.w(getLocalClassName(), "Long" + " Screen@" + screenHeight + " Image@" + f.getHeightImage());
                        } else if (f.getHeightImage() == 0) {
                            Log.w(getLocalClassName(), "No GIF allowed here");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new MaterialDialog.Builder(context)
                                            .title("Sorry")
                                            .content("No GIF Allowed")
                                            .positiveText("Go Back")
                                            .theme(Theme.DARK)
                                            .cancelable(false)
                                            .callback(new MaterialDialog.ButtonCallback() {
                                                @Override
                                                public void onPositive(MaterialDialog dialog) {
                                                    md.dismiss();
                                                    System.exit(0);
                                                }
                                            })
                                            .show();
                                }
                            });
                        } else {
                            Log.w(getLocalClassName(), "Short" + " Screen@" + screenHeight + " Image@" + f.getHeightImage());
                        }

                        File directory_download = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + "downloads");
                        if (!directory_download.exists()) {
                            directory_download.mkdirs();
                        }

                        DownloadManager mgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                        if (f.getPhotoURL() != null) {
                            Uri downloadUri = Uri.parse(f.getPhotoURL());
                            DownloadManager.Request request = new DownloadManager.Request(
                                    downloadUri);

                            request.setAllowedNetworkTypes(
                                    DownloadManager.Request.NETWORK_WIFI
                                            | DownloadManager.Request.NETWORK_MOBILE)
                                    .setAllowedOverRoaming(false).setTitle(getString(R.string.download_title))
                                    .setDescription(getString(R.string.download_description))
                                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                                    .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "/downloads/GAG.png");

                            mgr.enqueue(request);

                            BroadcastReceiver onComplete = new BroadcastReceiver() {
                                public void onReceive(Context context, Intent intent) {
                                    Log.d("Main", "Finished");
                                    Intent intentImage = new Intent(context, DisplayReceivedImage.class);
                                    intentImage.putExtra(GAG_TITLE, f.getTitle());
                                    intentImage.putExtra(GAG_URL, f.getURL());
                                    md.dismiss();
                                    unregisterReceiver(this);
                                    startActivityForResult(intentImage, EXIT_APP);
                                }
                            };

                            registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                        }
                    }
                });
                t.start();
            }
        }
    }
}