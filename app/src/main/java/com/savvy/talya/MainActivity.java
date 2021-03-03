package com.savvy.talya;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.savvy.talya.Databases.BluePrintDetailsReaderContract;
import com.savvy.talya.Databases.BluePrintsReaderContract;
import com.savvy.talya.Databases.FAQsReaderContract;
import com.savvy.talya.Databases.NewsReaderContract;
import com.savvy.talya.Databases.OffersReaderContract;
import com.savvy.talya.Databases.PlotsReaderContract;
import com.savvy.talya.Network.Iokihttp;
import com.ss.bottomnavigation.BottomNavigation;
import com.ss.bottomnavigation.events.OnSelectedItemChangeListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import me.leolin.shortcutbadger.ShortcutBadger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import per.wsj.library.AndRatingBar;

public class MainActivity extends AppCompatActivity {
    private FragmentTransaction transaction;
    Iokihttp iokihttp;
    SharedPreferences shared;
    View layout;
    private PopupWindow mPopupWindow;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        clearNotifications();
    }

    private void clearNotifications() {
        shared.edit().putInt("count", 0).apply();
        ShortcutBadger.removeCount(getApplicationContext()); //for 1.1.4+

    }

    private void saveBluePrints(JSONArray blueprints) throws JSONException {
        BluePrintsReaderContract.FeedReaderDbHelper dbHelper = new BluePrintsReaderContract.FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int deletedRows = db.delete(BluePrintsReaderContract.FeedEntry.TABLE_NAME, null, null);
        } catch (SQLException e) {
            dbHelper.onCreate(db);
        }
// Create a new map of values, where column names are the keys
        for (int x = 0; x < blueprints.length(); x++) {
            JSONObject blueprint = blueprints.getJSONObject(x);
            ContentValues values = new ContentValues();
            values.put(BluePrintsReaderContract.FeedEntry.GId, blueprint.getString("id"));
            values.put(BluePrintsReaderContract.FeedEntry.name, blueprint.getString("name"));
            values.put(BluePrintsReaderContract.FeedEntry.image_url, blueprint.getString("img"));
            values.put(BluePrintsReaderContract.FeedEntry.map_url, blueprint.getString("GoogleUrl"));
            values.put(BluePrintsReaderContract.FeedEntry.note, blueprint.getString("notes"));
            values.put(BluePrintsReaderContract.FeedEntry.plot_total, blueprint.getString("plots_total"));
            values.put(BluePrintsReaderContract.FeedEntry.plot_rest, blueprint.getString("plots_rest"));
            values.put(BluePrintsReaderContract.FeedEntry.plot_sold, blueprint.getString("plots_sold"));

// Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(BluePrintsReaderContract.FeedEntry.TABLE_NAME, null, values);
        }
    }

    private void savePlots(JSONArray plots) throws JSONException {
        PlotsReaderContract.FeedReaderDbHelper dbHelper = new PlotsReaderContract.FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int deletedRows = db.delete(PlotsReaderContract.FeedEntry.TABLE_NAME, null, null);
        } catch (SQLException e) {
            dbHelper.onCreate(db);
        }
// Create a new map of values, where column names are the keys
        for (int x = 0; x < plots.length(); x++) {
            JSONObject plot = plots.getJSONObject(x);
            ContentValues values = new ContentValues();
            values.put(PlotsReaderContract.FeedEntry.GId, plot.getString("ID"));
            values.put(PlotsReaderContract.FeedEntry.plot_no, plot.getString("plot_no"));
            values.put(PlotsReaderContract.FeedEntry.plot_area, plot.getString("plot_area"));
            values.put(PlotsReaderContract.FeedEntry.cat, plot.getString("cat"));
            values.put(PlotsReaderContract.FeedEntry.blueprint_id, plot.getString("plan_id"));
            values.put(PlotsReaderContract.FeedEntry.meter_price, plot.getString("meter_price"));
            values.put(PlotsReaderContract.FeedEntry.price_sdg, plot.getString("price_sdg"));
            values.put(PlotsReaderContract.FeedEntry.price_usd, plot.getString("price_usd"));
            values.put(PlotsReaderContract.FeedEntry.status, plot.getString("plot_status"));
            values.put(PlotsReaderContract.FeedEntry.status_id, plot.getString("plot_status_id"));
            values.put(PlotsReaderContract.FeedEntry.status_description, plot.getString("plot_status_desc"));
            // System.out.println("ALLOW ="+plot.getString("allowOffer"));
            values.put(PlotsReaderContract.FeedEntry.allowOffer, plot.getString("allowOffer"));
            //    values.put(PlotsReaderContract.FeedEntry.isRegistered, plot.getString("isRegistered"));
            values.put(PlotsReaderContract.FeedEntry.basic, plot.getString("Basic"));
            values.put(PlotsReaderContract.FeedEntry.total, plot.getString("Total"));
            values.put(PlotsReaderContract.FeedEntry.extra_amount, plot.getString("ExtraAmount"));
            values.put(PlotsReaderContract.FeedEntry.extra_quantity, plot.getString("ExtraQuantity"));

// Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(PlotsReaderContract.FeedEntry.TABLE_NAME, null, values);
        }
    }

    private void getMainData() {
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        try {
            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getApplicationContext())) {
            showLoading();

            iokihttp.get(getString(R.string.url) + "startup", shared.getString("token", ""), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    hideLoading();
                    System.out.println("FAIL");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        try {

                            JSONObject resJSON = new JSONObject(responseStr);
                            if (Integer.parseInt(resJSON.get("error").toString()) == 1) {
                                if (Integer.parseInt(resJSON.get("code").toString()) == 1) {
                                    final JSONObject subresJSON = resJSON.getJSONObject("data");

                                    saveNews(subresJSON.getJSONArray("Articels"));
                                    saveFaqs(subresJSON.getJSONArray("FAQS"));
                                    saveBluePrints(subresJSON.getJSONArray("Plans"));
                                    saveDetails(subresJSON.getJSONArray("Details"));
                                    savePlots(subresJSON.getJSONArray("Plots"));
                                    saveOffers(subresJSON.getJSONObject("Offers").getJSONArray("Current"), subresJSON.getJSONObject("Offers").getJSONArray("Old"));
                                    hideLoading();

                                } else {
                                    hideLoading();
                                }
                            } else {
                                hideLoading();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Exception=" + e);
                        }
                        System.out.println("Response=" + responseStr);

                    } else {
                        hideLoading();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

            });
        } else {
        }
    }

    private void saveDetails(JSONArray details) throws JSONException {
        BluePrintDetailsReaderContract.FeedReaderDbHelper dbHelper = new BluePrintDetailsReaderContract.FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int deletedRows = db.delete(BluePrintDetailsReaderContract.FeedEntry.TABLE_NAME, null, null);
        } catch (SQLException e) {
            dbHelper.onCreate(db);
        }
// Create a new map of values, where column names are the keys
        for (int x = 0; x < details.length(); x++) {
            JSONObject detail = details.getJSONObject(x);
            ContentValues values = new ContentValues();
            values.put(BluePrintDetailsReaderContract.FeedEntry.GId, detail.getString("id"));
            values.put(BluePrintDetailsReaderContract.FeedEntry.plan_id, detail.getString("PlanID"));
            values.put(BluePrintDetailsReaderContract.FeedEntry.type_id, detail.getString("TypeID"));
            values.put(BluePrintDetailsReaderContract.FeedEntry.text, detail.getString("TypeText"));

// Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(BluePrintDetailsReaderContract.FeedEntry.TABLE_NAME, null, values);
        }
    }

    private void saveFaqs(JSONArray faqs) throws JSONException {
        FAQsReaderContract.FeedReaderDbHelper dbHelper = new FAQsReaderContract.FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int deletedRows = db.delete(FAQsReaderContract.FeedEntry.TABLE_NAME, null, null);
        } catch (SQLException e) {
            dbHelper.onCreate(db);
        }
// Create a new map of values, where column names are the keys
        for (int x = 0; x < faqs.length(); x++) {
            JSONObject faq = faqs.getJSONObject(x);
            ContentValues values = new ContentValues();
            values.put(FAQsReaderContract.FeedEntry.GId, faq.getString("id"));
            values.put(FAQsReaderContract.FeedEntry.question, faq.getString("question"));
            values.put(FAQsReaderContract.FeedEntry.answer, faq.getString("answer"));
            System.out.println("values=" + values.toString());

// Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(FAQsReaderContract.FeedEntry.TABLE_NAME, null, values);
        }
    }

    private void saveOffers(JSONArray current_offers, JSONArray old_offers) throws JSONException {
        OffersReaderContract.FeedReaderDbHelper dbHelper = new OffersReaderContract.FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int deletedRows = db.delete(OffersReaderContract.FeedEntry.TABLE_NAME, null, null);
        } catch (SQLException e) {
            dbHelper.onCreate(db);
        }
// Create a new map of values, where column names are the keys
        for (int x = 0; x < current_offers.length(); x++) {
            System.out.println("JSONARRAY=" + current_offers.length());
            JSONArray innerJsonArray = current_offers.getJSONArray(x);

            for (int y = 0; y < innerJsonArray.length(); y++) {

                JSONObject offer = innerJsonArray.getJSONObject(y);

                ContentValues values = new ContentValues();
                values.put(OffersReaderContract.FeedEntry.GId, offer.getString("offer_id"));
                values.put(OffersReaderContract.FeedEntry.start_date, offer.getString("start_date"));
                values.put(OffersReaderContract.FeedEntry.end_date, offer.getString("end_date"));
                values.put(OffersReaderContract.FeedEntry.currency, offer.getString("currency"));
                values.put(OffersReaderContract.FeedEntry.currency_name, offer.getString("currency_name"));
                values.put(OffersReaderContract.FeedEntry.advance, offer.getString("advance"));
                values.put(OffersReaderContract.FeedEntry.added_value, offer.getString("added_value"));
                values.put(OffersReaderContract.FeedEntry.month_no, offer.getString("month_no"));
                values.put(OffersReaderContract.FeedEntry.plan_id, offer.getString("plan_id"));
                values.put(OffersReaderContract.FeedEntry.plan_name, offer.getString("plan_name"));
                values.put(OffersReaderContract.FeedEntry.type, 1);

// Insert the new row, returning the primary key value of the new row
                long newRowId = db.insert(OffersReaderContract.FeedEntry.TABLE_NAME, null, values);
            }
        }
        for (int x = 0; x < old_offers.length(); x++) {
            JSONArray innerJsonArray = old_offers.getJSONArray(x);

            for (int y = 0; y < innerJsonArray.length(); y++) {

                JSONObject offer = innerJsonArray.getJSONObject(y);

                ContentValues values = new ContentValues();
                values.put(OffersReaderContract.FeedEntry.GId, offer.getString("offer_id"));
                values.put(OffersReaderContract.FeedEntry.start_date, offer.getString("start_date"));
                values.put(OffersReaderContract.FeedEntry.end_date, offer.getString("end_date"));
                values.put(OffersReaderContract.FeedEntry.currency, offer.getString("currency"));
                values.put(OffersReaderContract.FeedEntry.currency_name, offer.getString("currency_name"));
                values.put(OffersReaderContract.FeedEntry.advance, offer.getString("advance"));
                values.put(OffersReaderContract.FeedEntry.added_value, offer.getString("added_value"));
                values.put(OffersReaderContract.FeedEntry.month_no, offer.getString("month_no"));
                values.put(OffersReaderContract.FeedEntry.plan_id, offer.getString("plan_id"));
                values.put(OffersReaderContract.FeedEntry.plan_name, offer.getString("plan_name"));
                values.put(OffersReaderContract.FeedEntry.type, 2);

// Insert the new row, returning the primary key value of the new row
                long newRowId = db.insert(OffersReaderContract.FeedEntry.TABLE_NAME, null, values);
            }
        }

    }

    private void saveNews(JSONArray news) throws JSONException {
        NewsReaderContract.FeedReaderDbHelper dbHelper = new NewsReaderContract.FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int deletedRows = db.delete(NewsReaderContract.FeedEntry.TABLE_NAME, null, null);
        } catch (SQLException e) {
            dbHelper.onCreate(db);
        }
// Create a new map of values, where column names are the keys
        for (int x = 0; x < news.length(); x++) {
            JSONObject singleNew = news.getJSONObject(x);
            ContentValues values = new ContentValues();
            values.put(NewsReaderContract.FeedEntry.GId, singleNew.getString("id"));
            values.put(NewsReaderContract.FeedEntry.title, singleNew.getString("title"));
            values.put(NewsReaderContract.FeedEntry.description, singleNew.getString("description"));
            values.put(NewsReaderContract.FeedEntry.date, singleNew.getString("created_at").split(" ")[0]);
            values.put(NewsReaderContract.FeedEntry.url_image, singleNew.getString("url"));
// Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(NewsReaderContract.FeedEntry.TABLE_NAME, null, values);
        }
    }

    public void showLoading() {
        final View main = findViewById(R.id.layout);
        final ProgressBar loading = findViewById(R.id.main_loading);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.setClickable(false);

                main.setVisibility(View.GONE);

                loading.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hideLoading() {
        final View main = findViewById(R.id.layout);
        final ProgressBar loading = findViewById(R.id.main_loading);
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    main.setClickable(true);

                    main.setVisibility(View.VISIBLE);

                    loading.setVisibility(View.GONE);
                }
            });
        } catch (Exception e) {

        }
    }

    private void init() {
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        shared = getSharedPreferences("com.savvy.talya", Context.MODE_PRIVATE);
        iokihttp = new Iokihttp();
        layout = findViewById(R.id.main_layout);

        getMainData();
        BottomNavigation bottomNavigation = findViewById(R.id.bottom_navigation);
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Typeface typeface = getResources().getFont(R.font.handlee);
                bottomNavigation.setTypeface(typeface);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        bottomNavigation.setOnSelectedItemChangeListener(new OnSelectedItemChangeListener() {
            @Override
            public void onSelectedItemChanged(int itemId) {
                switch (itemId) {
                    case R.id.tab_contact_us:
                        transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_fragment_containers, new ContactUs());
                        break;
                    case R.id.tab_profile:
                        transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_fragment_containers, new Profile());
                        break;
                    case R.id.tab_media:
                        transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_fragment_containers, new Media());
                        break;
                    case R.id.tab_services:
                        transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_fragment_containers, new Services());
                        break;
                }
                transaction.commit();
            }
        });
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_fragment_containers, new Services());
        transaction.commit();


        bottomNavigation.setSelectedItem(0);
    }

    @Override
    public void onBackPressed() {
        openRatePopup();
    }

    private void openRatePopup() {
        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        // Inflate the custom layout/view
        View customView = inflater.inflate(R.layout.rate_card, null);


        mPopupWindow = new PopupWindow(
                customView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        ((AndRatingBar) customView.findViewById(R.id.rating_bar)).setOnRatingChangeListener(new AndRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(AndRatingBar ratingBar, float rating) {
                sendRate(rating);
            }
        });
        // Set an elevation value for popup window
        // Call requires API level 21
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }
        mPopupWindow.dismiss();
        mPopupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
    }

    private void sendRate(float rating) {

        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();

        try {
            subJSON.put("user_id", shared.getString("user_id", ""));
            subJSON.put("rate", rating);

            json.put("data", subJSON);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        showLoading();
        if (iokihttp.isNetworkConnected(getApplicationContext())) {
            iokihttp.post(getString(R.string.url) + "verifyOTP", shared.getString("token", ""), json.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("FAIL");
                    hideLoading();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    hideLoading();
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        try {
                            JSONObject resJSON = new JSONObject(responseStr);
                            if (Integer.parseInt(resJSON.get("error").toString()) == 1 && Integer.parseInt(resJSON.get("code").toString()) == 1) {
                                finish();
                            } else {
                                finish();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Response=" + responseStr);

                    } else {
                        System.out.println("Response=" + "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj");
                    }
                }
            });
        }
    }
}