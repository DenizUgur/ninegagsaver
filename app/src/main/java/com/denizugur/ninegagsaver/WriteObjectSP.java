package com.denizugur.ninegagsaver;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

public class WriteObjectSP {

    public static final String GAGS = "com.denizugur.ninegagsaver.gags";
    private Context parent;

    public WriteObjectSP(Context c) {
        parent = c;
    }

    public void writeObject(JSONObject inputObject, String id) {
        SharedPreferences.Editor prefsEditor = parent.getSharedPreferences(GAGS, Context.MODE_PRIVATE).edit();

        Gson gson = new Gson();
        String json = gson.toJson(inputObject);
        prefsEditor.putString(id, json);
        prefsEditor.apply();

        Log.d("ADDED OBJECT TO SP", "ID: " + id);
    }
}