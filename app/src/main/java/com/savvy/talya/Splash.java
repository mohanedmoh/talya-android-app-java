package com.savvy.talya;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.savvy.talya.Network.Iokihttp;

import androidx.appcompat.app.AppCompatActivity;
import me.leolin.shortcutbadger.ShortcutBadger;

public class Splash extends AppCompatActivity {
    private static int splash_time_out = 1000;
    SharedPreferences shared;
    Iokihttp iokihttp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        shared = this.getSharedPreferences("com.savvy.talya", Context.MODE_PRIVATE);
        iokihttp = new Iokihttp();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                check();
            }
        }, splash_time_out);
        clearNotifications();
    }

    private void clearNotifications() {
        shared.edit().putInt("count", 0).apply();
        ShortcutBadger.removeCount(getApplicationContext()); //for 1.1.4+

    }

    private void check() {
        boolean login = shared.getBoolean("login", false);
        Intent i;
        if (login) {
            i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            finish();
        } else {
            i = new Intent(getApplicationContext(), Login.class);
            startActivity(i);
            finish();
        }
    }
}