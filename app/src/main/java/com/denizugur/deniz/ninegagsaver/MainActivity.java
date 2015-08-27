package com.denizugur.deniz.ninegagsaver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    public final static String GAG_TITLE = "com.denizugur.deniz.ninegagsaver.GAG_TITLE";
    private final static int EXIT_APP = 0;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent);
            }
        } else {
            Intent i = new Intent(this, HomeCardActivity.class);
            startActivity(i);
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
                        .theme(Theme.DARK)
                        .show();

                final fetchGAG f = new fetchGAG();
                f.setURL(gagURL);
                try {
                    f.fetch();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int screenHeight = size.y;

                if (f.getHeightImage() > screenHeight) {
                    Log.w(getLocalClassName(), "Long" + " Screen@" + screenHeight + " Image@" + f.getHeightImage());
                } else if (f.getHeightImage() == 0) {
                    Log.w(getLocalClassName(), "No GIF allowed here");
                    new MaterialDialog.Builder(this)
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
                } else {
                    Log.w(getLocalClassName(), "Short" + " Screen@" + screenHeight + " Image@" + f.getHeightImage());
                }

                DownloadManager mgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                if (f.getPhotoURL() != null) {
                    Uri downloadUri = Uri.parse(f.getPhotoURL());
                    DownloadManager.Request request = new DownloadManager.Request(
                            downloadUri);

                    request.setAllowedNetworkTypes(
                            DownloadManager.Request.NETWORK_WIFI
                                    | DownloadManager.Request.NETWORK_MOBILE)
                            .setAllowedOverRoaming(false).setTitle("9GAG Photo")
                            .setDescription("Something useful. No, really.")
                            .setNotificationVisibility(0)
                            .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "GAG.png");

                    mgr.enqueue(request);

                    BroadcastReceiver onComplete = new BroadcastReceiver() {
                        public void onReceive(Context context, Intent intent) {
                            Log.d("Main", "Finished");
                            Intent intentImage = new Intent(getApplicationContext(), DisplayReceivedImage.class);
                            intentImage.putExtra(GAG_TITLE, f.getTitle());
                            md.dismiss();
                            unregisterReceiver(this);
                            startActivityForResult(intentImage, EXIT_APP);
                        }
                    };

                    registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                }
            } else {
                new MaterialDialog.Builder(this)
                        .content("Share is from undefined source. This app is only for 9GAG.")
                        .positiveText("Ok")
                        .theme(Theme.DARK)
                        .show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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