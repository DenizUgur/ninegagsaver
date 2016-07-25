package com.denizugur.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

public class VersionCheck {

    private static final String VERSION_KEY = "VERSION_KEY";
    private static final String NO_VERSION = "";
    private Context context;
    private String lastVersion;
    private String thisVersion;

    public VersionCheck(Context context) {
        this.context = context;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        this.lastVersion = sp.getString(VERSION_KEY, NO_VERSION);
        this.thisVersion = getVersionName();
    }

    public boolean firstRun() {
        return !this.lastVersion.equals(this.thisVersion);
    }

    public String getVersionName() {
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
        updateVersionInPreferences(versionName);
        return versionName;
    }

    public static String getVersionName(Context context) {
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

    protected void updateVersionInPreferences(String version) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString(VERSION_KEY, version);
        editor.apply();
    }
}