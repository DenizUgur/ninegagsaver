package com.denizugur.ninegagsaver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeCardActivity extends AppCompatActivity {

    public static final String GAGS = "com.denizugur.ninegagsaver.gags";
    public List<gagInfo> list;

    @NonNull
    public static String getVersionName(@NonNull Context context) {
        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();
        String versionName;
        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            versionName = info.versionName;

            // Make the info part of version name a bit smaller.
            if (versionName.indexOf('-') >= 0) {
                versionName = versionName.replaceFirst("\\-", "<small>-") + "</small>";
            }
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "N/A";
        }

        return versionName;
    }

    /**
     * Cards, Comments
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (prefsCheck()) {
            RelativeLayout relativeLayout = new RelativeLayout(this);
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);

            TextView tv = new TextView(this);
            tv.setText("There is no saved gag, go save some to see them here");
            tv.setTextSize(30);
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(Color.GRAY);

            FloatingActionButton fab = new FloatingActionButton(this);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(rlp);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            params.addRule(Gravity.RIGHT);
            fab.setImageResource(R.drawable.fab_add);
            fab.setColorNormalResId(R.color.primary_color);
            fab.setColorPressedResId(R.color.primary_color);
            fab.setColorRippleResId(R.color.primary_color_600);
            fab.setLayoutParams(params);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            tv.setLayoutParams(lp);
            relativeLayout.addView(tv);
            relativeLayout.addView(fab);

            setContentView(relativeLayout, rlp);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customPhoto();
                }
            });

        } else {
            setContentView(R.layout.activity_home_card);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customPhoto();
                }
            });

            RecyclerView recList = (RecyclerView) findViewById(R.id.cardList);
            recList.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            recList.setLayoutManager(llm);

            getList();

            gagAdapter ca = new gagAdapter(list);
            recList.setAdapter(ca);
        }
    }

    private void customPhoto() {
        Intent i = new Intent(this, DisplayReceivedImage.class);
        i.putExtra("isCustom", true);
        startActivity(i);
    }

    private Boolean prefsCheck() {
        File directory_gags = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + "gags");
        if (!directory_gags.exists()) {
            directory_gags.mkdir();
        }
        File null_object = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + "gags" + File.separator + "null");
        File[] contents = directory_gags.listFiles();
        if (contents.length == 0) {
            SharedPreferences prefs = getSharedPreferences(GAGS, 0);
            prefs.edit().clear().apply();
            return true;
        } else if (null_object.exists()) {
            for (File content : contents) {
                if (content.equals(null_object)) {
                    null_object.delete();
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private void getList() {

        SharedPreferences prefs = getSharedPreferences(GAGS, MODE_PRIVATE);

        Map<String, ?> allEntries = prefs.getAll();
        list = new ArrayList<>();
        JSONObject obj;
        String keyObject;
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String entryValue = entry.getValue().toString();
            keyObject = entry.getKey();

            try {
                JSONObject entryObject = new JSONObject(entryValue);
                obj = entryObject.getJSONObject("nameValuePairs");
                gagInfo gi = new gagInfo();
                    Log.d("JSON", "Processing... " + keyObject);

                    try {
                        gi.title = obj.getString("title");
                        gi.likes = obj.getString("likes");
                        gi.comments = obj.getString("comments");
                        gi.saved_date = obj.getString("saved_date");
                        gi.file_path = obj.getString("file_path");
                        gi.photoId = obj.getString("photoId");

                        list.add(gi);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.remove(keyObject);
                        editor.apply();
                    }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_card, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            finish();
            return true;
        } else if (id == R.id.action_about) {
            String app_name = getResources().getString(R.string.app_name);
            new MaterialDialog.Builder(this)
                    .iconRes(R.drawable.ic_action_about_white)
                    .title(app_name + " " + getVersionName(this))
                    .content(Html.fromHtml(getString(R.string.about_body)))
                    .negativeText(R.string.close)
                    .show();
        return true;
        }

        return super.onOptionsItemSelected(item);
    }
}