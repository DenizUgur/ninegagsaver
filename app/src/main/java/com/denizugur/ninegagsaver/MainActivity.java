package com.denizugur.ninegagsaver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListener;
import com.thin.downloadmanager.ThinDownloadManager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends Activity {

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

    public boolean checkOnlineState() {
        ConnectivityManager CManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo NInfo = CManager.getActiveNetworkInfo();
        return NInfo != null && NInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main_splash);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String str = prefs.getString("path", null);

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            try {
                assert str != null;
                new File(str);
                if ("text/plain".equals(type) && checkOnlineState()) {
                    handleSendText(intent);
                } else {
                    new MaterialDialog.Builder(this)
                            .title(R.string.sorry)
                            .content(R.string.no_internet)
                            .positiveText(R.string.go_back)
                            .theme(Theme.DARK)
                            .cancelable(false)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    System.exit(0);
                                }
                            })
                            .show();
                }
            } catch (Exception e) {
                Intent i = new Intent(this, SettingsActivity.class);
                Toast.makeText(getApplicationContext(), getString(R.string.choose_folder_toast), Toast.LENGTH_LONG).show();
                startActivity(i);
                finish();
            }
        } else try {
            assert str != null;
            new File(str);
            Intent i = new Intent(this, HomeCardActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        } catch (Exception e) {
            Intent i = new Intent(this, SettingsActivity.class);
            Toast.makeText(getApplicationContext(), getString(R.string.choose_folder_toast), Toast.LENGTH_LONG).show();
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

                final fetchGAG f = new fetchGAG();
                f.setURL(gagURL);

                new Thread(new Runnable() {
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
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new MaterialDialog.Builder(context)
                                            .title(R.string.sorry)
                                            .content(R.string.no_gif)
                                            .positiveText(R.string.go_back)
                                            .theme(Theme.DARK)
                                            .cancelable(false)
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
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

                        Uri downloadUri = Uri.parse(f.getPhotoURL());
                        Uri destinationUri = Uri.parse(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/downloads/GAG.png");
                        DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                                .setRetryPolicy(new DefaultRetryPolicy())
                                .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                                .setDownloadListener(new DownloadStatusListener() {
                                    @Override
                                    public void onDownloadComplete(int id) {
                                        Log.d(String.valueOf(id), "Finished");
                                        Intent intentImage = new Intent(context, DisplayReceivedImage.class);
                                        intentImage.putExtra(GAG_TITLE, f.getTitle());
                                        intentImage.putExtra(GAG_URL, f.getURL());
                                        startActivityForResult(intentImage, EXIT_APP);
                                    }

                                    @Override
                                    public void onDownloadFailed(int id, int errorCode, String errorMessage) {
                                        new MaterialDialog.Builder(context)
                                                .title(R.string.download_failed_title)
                                                .content(R.string.download_failed)
                                                .positiveText(R.string.go_back)
                                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                    @Override
                                                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                                        System.exit(0);
                                                    }
                                                }).show();
                                    }

                                    @Override
                                    public void onProgress(int id, long totalBytes, long downlaodedBytes, int progress) {}
                                });

                        ThinDownloadManager downloadManager = new ThinDownloadManager();
                        downloadManager.add(downloadRequest);
                    }
                }).start();
            }
        }
    }
}