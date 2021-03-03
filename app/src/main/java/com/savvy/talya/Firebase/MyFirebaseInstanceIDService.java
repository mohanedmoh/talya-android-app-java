package com.savvy.talya.Firebase;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;

/**
 * Created by mohaned on 3/13/2017.
 */


public class MyFirebaseInstanceIDService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onNewToken(String s) {

        //Getting registration token

        //Displaying token on logcat
        Log.d(TAG, "Refreshed token: " + s);

        //calling the method store token and passing token
        storeToken(s);
    }

    private void storeToken(String token) {
        //we will save the token in sharedpreferences later
        System.out.println("in instance class  = " + token);
        SharedPreManager.getInstance(this).saveDeviceToken(token);
    }
}
