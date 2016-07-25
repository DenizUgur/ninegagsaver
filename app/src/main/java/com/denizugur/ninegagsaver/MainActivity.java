package com.denizugur.ninegagsaver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.denizugur.helpers.fetchGAG;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends Activity {

    public final static String GAG_TITLE = "com.denizugur.ninegagsaver.GAG_TITLE";
    public final static String GAG_URL = "com.denizugur.ninegagsaver.GAG_URL";
    public final static String FILE_EXT = "com.denizugur.ninegagsaver.FILE_EXT";
    private final static String TAG = "MainActivity";
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

    private static boolean checkOnlineState(Context context) {
        ConnectivityManager CManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
                if ("text/plain".equals(type) && checkOnlineState(this)) {
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

                new Thread(new Runnable() { //TODO: GIF implementation via video_download()
                    @Override
                    public void run() {
                        try {
                            f.fetch();
                            if (f.isGIF) throw new Exception("GIF is not possible to process.");
                            img_download();
                        } catch (Exception e) {
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
                        }
                    }

                    private void img_download() {
                        File directory_download = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + "downloads");
                        if (!directory_download.exists()) {
                            directory_download.mkdirs();
                        }

                        Uri downloadUri = Uri.parse(f.getPhotoURL());
                        final String extension = String.valueOf(downloadUri).substring(String.valueOf(downloadUri).lastIndexOf("."));
                        final File destinationFile = new File(directory_download + "/GAG" + extension);

                        DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                                .setRetryPolicy(new DefaultRetryPolicy())
                                .setDestinationURI(Uri.fromFile(destinationFile)).setPriority(DownloadRequest.Priority.HIGH)
                                .setStatusListener(new DownloadStatusListenerV1() {
                                    @Override
                                    public void onDownloadComplete(DownloadRequest downloadRequest) {
                                        Log.d(String.valueOf(downloadRequest), "Finished");
                                        Intent intentImage = new Intent(context, DisplayReceivedImage.class);
                                        intentImage.putExtra(GAG_TITLE, f.getTitle());
                                        intentImage.putExtra(FILE_EXT, extension);
                                        intentImage.putExtra(GAG_URL, f.getURL());
                                        startActivityForResult(intentImage, EXIT_APP);
                                    }

                                    @Override
                                    public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {
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
                                    public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {}
                                });

                        ThinDownloadManager downloadManager = new ThinDownloadManager();
                        downloadManager.add(downloadRequest);
                    }
                }).start();
            }
        }
    }
}