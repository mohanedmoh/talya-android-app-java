package com.savvy.talya;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.savvy.talya.Models.News;

import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;

public class News_Profile extends AppCompatActivity {
    SharedPreferences shared;
    News singleNew;
    DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news__profile);
        init();
    }
    private void init() {
        try {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        shared = getSharedPreferences("com.savvy.talya", Context.MODE_PRIVATE);
        singleNew = (News) Objects.requireNonNull(getIntent().getExtras()).getBundle("new").getSerializable("new");
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.logoyellow)
                .showImageOnFail(R.drawable.logoyellow)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(20))
                .build();
        fillProfile(singleNew);
    }

    private void fillProfile(News singleNew) {
        ((TextView) findViewById(R.id.title)).setText(singleNew.getTitle());
        ((TextView) findViewById(R.id.date)).setText(singleNew.getDate());
        ((TextView) findViewById(R.id.description)).setText(singleNew.getDescription());
        //((ImageView)findViewById(R.id.image))
        if (!(singleNew.getImage_url().isEmpty())) {
            System.out.println("image=" + singleNew.getImage_url());
            ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                    .writeDebugLogs()
                    .build();
            imageLoader.init(config);
            imageLoader.displayImage(singleNew.getImage_url(), (ImageView) findViewById(R.id.image), options);
        }
    }
}