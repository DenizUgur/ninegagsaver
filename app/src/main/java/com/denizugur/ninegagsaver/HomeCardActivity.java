package com.denizugur.ninegagsaver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.facebook.drawee.backends.pipeline.Fresco;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeCardActivity extends AppCompatActivity {

    public static final String GAGS = "com.denizugur.ninegagsaver.gags";
    public List<gagInfo> list;
    private Context context;
    private gagAdapter ca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        Fresco.initialize(this);

        VersionCheck vs = new VersionCheck(this);
        if (vs.firstRun()) {
            int accentColor = ThemeSingleton.get().widgetColor;
            if (accentColor == 0)
                accentColor = ContextCompat.getColor(this, R.color.accent_color);
            ChangelogDialog.create(false, accentColor)
                    .show(getSupportFragmentManager(), "changelog");
        }

        if (prefsCheck(this)) {
            setContentView(R.layout.activity_home_card_empty);
        } else {
            setContentView(R.layout.activity_home_card);
        }

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.setEnabled(false);
                customPhoto();
                fab.setEnabled(true);
            }
        });

        final RecyclerView recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        list = new ArrayList<>();
        gagInfo gi = new gagInfo();
        list.add(gi.setEmpty());
        recList.setAdapter(new gagAdapter(list, context));

        SwipeableRecyclerViewTouchListener swipeTouchListener =
                new SwipeableRecyclerViewTouchListener(recList,
                        new SwipeableRecyclerViewTouchListener.SwipeListener() {
                            @Override
                            public boolean canSwipe(int position) {
                                return true;
                            }

                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (final int position : reverseSortedPositions) {
                                    final gagInfo gi = list.get(position);
                                    ca.notifyItemRemoved(position);
                                    list.remove(position);
                                    ca.notifyDataSetChanged();

                                    CoordinatorLayout crdLayout = (CoordinatorLayout) findViewById(R.id.fabContainer);

                                    Snackbar.make(crdLayout, "Gag successfully removed.", Snackbar.LENGTH_LONG)
                                            .setAction("Undo", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    populateRecyclerView(recList);
                                                    ca = new gagAdapter(list, context);
                                                    recList.setAdapter(ca);
                                                    ca.notifyDataSetChanged();
                                                }
                                            })
                                            .setCallback(new Snackbar.Callback() {
                                                @Override
                                                public void onDismissed(Snackbar snackbar, int event) {
                                                    super.onDismissed(snackbar, event);
                                                    if (event != DISMISS_EVENT_ACTION) {
                                                        SharedPreferences.Editor editor = context.getSharedPreferences(GAGS, Context.MODE_PRIVATE).edit();
                                                        editor.remove(gi.photoId).apply();

                                                        File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + "gags");
                                                        if (dir.isDirectory()) {
                                                            File file = new File(dir + File.separator + gi.photoId);
                                                            file.delete();
                                                        }

                                                        if (position == 0 && list.size() < 1) {
                                                            finish();
                                                            Intent i = new Intent(HomeCardActivity.this, HomeCardActivity.class);
                                                            startActivity(i);
                                                        }
                                                    }
                                                }
                                            }).show();
                                }
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                for (final int position : reverseSortedPositions) {
                                    final gagInfo gi = list.get(position);
                                    ca.notifyItemRemoved(position);
                                    list.remove(position);
                                    ca.notifyDataSetChanged();

                                    CoordinatorLayout crdLayout = (CoordinatorLayout) findViewById(R.id.fabContainer);

                                    Snackbar.make(crdLayout, "Gag successfully removed.", Snackbar.LENGTH_LONG)
                                            .setAction("Undo", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    populateRecyclerView(recList);
                                                    ca = new gagAdapter(list, context);
                                                    recList.setAdapter(ca);
                                                    ca.notifyDataSetChanged();
                                                }
                                            })
                                            .setCallback(new Snackbar.Callback() {
                                                @Override
                                                public void onDismissed(Snackbar snackbar, int event) {
                                                    super.onDismissed(snackbar, event);
                                                    if (event != DISMISS_EVENT_ACTION) {
                                                        SharedPreferences.Editor editor = context.getSharedPreferences(GAGS, Context.MODE_PRIVATE).edit();
                                                        editor.remove(gi.photoId).apply();

                                                        File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + "gags");
                                                        if (dir.isDirectory()) {
                                                            File file = new File(dir + File.separator + gi.photoId);
                                                            file.delete();
                                                        }

                                                        if (position == 0 && list.size() < 1) {
                                                            finish();
                                                            Intent i = new Intent(HomeCardActivity.this, HomeCardActivity.class);
                                                            startActivity(i);
                                                        }
                                                    }
                                                }
                                            }).show();
                                }
                            }
                        });

        recList.addOnItemTouchListener(swipeTouchListener);
        populateRecyclerView(recList);
    }

    private void customPhoto() {
        Intent i = new Intent(this, DisplayReceivedImage.class);
        i.putExtra("isCustom", true);
        startActivity(i);
    }

    public Boolean prefsCheck(Context context) {
        File directory_gags = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + "gags");
        if (!directory_gags.exists()) {
            directory_gags.mkdir();
        }
        File null_object = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + "gags" + File.separator + "null");
        File[] contents = directory_gags.listFiles();
        if (contents.length == 0) {
            SharedPreferences prefs = context.getSharedPreferences(GAGS, 0);
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

    private void populateRecyclerView(final RecyclerView rView) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = getSharedPreferences(GAGS, MODE_PRIVATE);

                Map<String, ?> allEntries = prefs.getAll();
                list = new ArrayList<>();
                JSONObject obj;
                String keyObject;
                Boolean first = true;
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

                            if (first) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ca = new gagAdapter(list, context);
                                        rView.setAdapter(ca);
                                    }
                                });
                                first = false;
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ca.notifyItemInserted(list.size() - 1);
                                    }
                                });
                            }

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
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_card, menu);
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        TextView tv = new TextView(this);

        if (Build.VERSION.SDK_INT < 23) {
            tv.setTextAppearance(this, R.style.dialog_tv_style);
        } else {
            tv.setTextAppearance(R.style.dialog_tv_style);
        }
        tv.setMovementMethod(LinkMovementMethod.getInstance());

        VersionCheck vs = new VersionCheck(this);
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            finish();
            return true;
        } else if (id == R.id.action_about) {
            tv.setText(Html.fromHtml(getString(R.string.about_body)));
            new MaterialDialog.Builder(this)
                    .iconRes(R.drawable.ic_action_about_white)
                    .title(Html.fromHtml("<b>" + getResources().getString(R.string.app_name) + "</b>&nbsp;<font color='#888888'>v" + vs.getVersionName() + "</font>"))
                    .customView(tv, true)
                    .negativeText(R.string.close)
                    .show();
            return true;
        } else if (id == R.id.action_help) {
            tv.setText(Html.fromHtml(getString(R.string.help_body)));
            new MaterialDialog.Builder(this)
                    .iconRes(R.drawable.ic_action_help_white)
                    .title(getResources().getString(R.string.action_help))
                    .customView(tv, true)
                    .negativeText(R.string.close)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }
}