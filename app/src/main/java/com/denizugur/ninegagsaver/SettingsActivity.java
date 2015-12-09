package com.denizugur.ninegagsaver;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    private final static int FILE_CODE = 0;
    private static String gags_dir = null;
    private static String str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        try {
            assert actionBar != null;
            actionBar.setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        File _dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + File.separator + "gags");
        gags_dir = _dir.toString();

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsPreferenceFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        try {
            new File(str);
            Intent i = new Intent(this, HomeCardActivity.class);
            startActivity(i);
            finish();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getString(R.string.choose_folder_toast), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                try {
                    new File(str);
                    Intent i = new Intent(this, HomeCardActivity.class);
                    startActivity(i);
                    finish();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.choose_folder_toast), Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class SettingsPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Preference chooseFolder = findPreference("chooseFolder");
            str = prefs.getString("path", null);
            try {
                assert str != null;
                new File(str);
                chooseFolder.setSummary(prefs.getString("path", null));
            } catch (Exception e) {
                chooseFolder.setSummary(getActivity().getString(R.string.na));
            }

            chooseFolder.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = preference.getIntent();

                    i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                    i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                    i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                    i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                    startActivityForResult(i, FILE_CODE);
                    return true;
                }
            });

            Preference clearGags = findPreference("clearGags");
            clearGags.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    File dir = new File(gags_dir);
                    if (dir.isDirectory()) {
                        String[] children = dir.list();
                        for (String aChildren : children) {
                            new File(dir, aChildren).delete();
                        }
                    }
                    Toast.makeText(getActivity(), getString(R.string.clear_confirm), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                File folder = new File(uri.getPath());

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("path", folder.toString());
                editor.apply();

                str = folder.toString();

                Preference editTextPref = findPreference("chooseFolder");
                editTextPref.setSummary(folder.toString());
            }
        }
    }
}