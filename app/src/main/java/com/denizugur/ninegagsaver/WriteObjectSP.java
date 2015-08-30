package com.denizugur.ninegagsaver;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;
/**
 * Created by deniz on 28/08/15.
 */

/**
 * Allows the reading/writing of an object from/to a file
 * inside the application's /data directory. Allows for
 * more efficient storing of objects such as arrays instead
 * of within SharedPreferences
 *
 * @author Isaac Whitfield
 * @version 31/07/2013
 */
public class WriteObjectSP {

    public static final String GAGS = "com.denizugur.ninegagsaver.gags";
    private Context parent;
    private List<String> list;

    public WriteObjectSP(Context c){
        parent = c;
    }

    public void writeObject(JSONObject inputObject, String id){
        SharedPreferences.Editor prefsEditor = parent.getSharedPreferences(GAGS, Context.MODE_PRIVATE).edit();

        Gson gson = new Gson();
        String json = gson.toJson(inputObject);
        prefsEditor.putString(id, json);
        prefsEditor.apply();

        Log.d("ADDED OBJECT TO SP", "ID: " + id);
    }
}