package com.savvy.talya.Firebase;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by mohaned on 3/13/2017.
 */

public class SharedPreManager {
    private static final String SHARED_PREF_NAME = "FCMSharedPref";
    private static final String TAG_TOKEN = "token";

    public static SharedPreManager mInstance;
    private static Context mCtx;

    public SharedPreManager(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPreManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPreManager(context);
        }
        return mInstance;
    }

    //this method will save the device token to shared preferences
    public boolean saveDeviceToken(String token) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TAG_TOKEN, token);
        editor.apply();
        return true;
    }

    //this method will fetch the device token from shared preferences
    public String getDeviceToken() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        System.out.println("shared device token = " + sharedPreferences.getString(TAG_TOKEN, null));
        return sharedPreferences.getString(TAG_TOKEN, null);
    }
}
