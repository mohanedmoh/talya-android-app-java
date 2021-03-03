package com.savvy.talya;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fxn.pix.Pix;
import com.savvy.talya.Databases.BluePrintsReaderContract;
import com.savvy.talya.Network.Iokihttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class registeration_form extends AppCompatActivity {
    Iokihttp iokihttp;
    SharedPreferences shared;
    boolean inside_talya = true;
    ArrayList<String> images;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration_form);
        init();
    }

    public File saveImage(final Context context, final String imageData, String imageName) throws IOException {
        final File file = new File(getApplicationContext().getCacheDir(), imageName);
        file.createNewFile();
        OutputStream os1 = new BufferedOutputStream(new FileOutputStream(file));

        File imageString1 = new File(imageData);
        Bitmap bitmap1 = new BitmapDrawable(getApplicationContext().getResources(), imageString1.getAbsolutePath()).getBitmap();
        bitmap1.compress(Bitmap.CompressFormat.JPEG, 70, os1);
        os1.close();
        return file;
    }

    private void emptyForm() {
        ((EditText) findViewById(R.id.land_numE)).setText("");
        ((Spinner) findViewById(R.id.blueprint_nameS)).setSelected(false);
        ((EditText) findViewById(R.id.blueprint_nameE)).setText("");
        ((Spinner) findViewById(R.id.reg_type)).setSelected(false);
        ((TextView) findViewById(R.id.image1)).setText("");
        ((TextView) findViewById(R.id.image2)).setText("");
        ((TextView) findViewById(R.id.image3)).setText("");

    }

    private void init() {
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        shared = getSharedPreferences("com.savvy.talya", Context.MODE_PRIVATE);
        iokihttp = new Iokihttp();
        fillReg_types();
        ((Spinner) findViewById(R.id.reg_type)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) viewSpinners();
                else viewEditTexts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        getBluePrintsFromLocal();
        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    validate();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.attach_files).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCam();
            }
        });
    }

    private void register(ArrayList<String> images) throws IOException {
        File[] files = new File[images.size()];
        for (int x = 0; x < images.size(); x++) {
            System.out.println("FILE PATH =" + images.get(x));
            files[x] = saveImage(getApplicationContext(), images.get(x), "image" + x);
            // files[x].createNewFile(
        }


        String land_num = ((EditText) findViewById(R.id.land_numE)).getText().toString();
        String plan_id = ((StringWithTag) (((Spinner) findViewById(R.id.blueprint_nameS)).getSelectedItem())).key;
        String plan_name = ((EditText) findViewById(R.id.blueprint_nameE)).getText().toString();
        int reg_type = (((Spinner) findViewById(R.id.reg_type)).getSelectedItemPosition()) + 1;
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        try {
            subJSON.put("type", reg_type);
            subJSON.put("user_id", shared.getString("user_id", "0"));
            subJSON.put("plan_id", plan_id);
            subJSON.put("plan_name", plan_name);
            subJSON.put("plot_no", land_num);
            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getApplicationContext())) {
            showLoading();

            iokihttp.uploadImage(json.toString(), files, getString(R.string.url) + "landRegistration", shared.getString("token", ""), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    hideLoading();
                    System.out.println("FAIL" + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                        }
                    });
                    getBluePrintsFromLocal();
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    hideLoading();
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        try {
                            JSONObject resJSON = new JSONObject(responseStr);
                            if (Integer.parseInt(resJSON.get("error").toString()) == 1) {
                                if (Integer.parseInt(resJSON.get("code").toString()) == 1) {
                                    try {
                                        emptyForm();
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), getString(R.string.successfull_sent), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } catch (Exception e) {
                                        System.out.println("Ex=" + e);
                                    }
                                }
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                getBluePrintsFromLocal();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Response=" + responseStr);

                    } else {
                        getBluePrintsFromLocal();
                        System.out.println("Response=" + "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj" + response.toString());
                    }
                }

            });
        } else {
        }
    }

    private void openCam() {
        Pix.start(registeration_form.this, 101, 3);
    }

    private void validate() throws IOException {
        String land_name = ((EditText) findViewById(R.id.land_numE)).getText().toString();
        String plan_id = ((StringWithTag) (((Spinner) findViewById(R.id.blueprint_nameS)).getSelectedItem())).key;
        String blueprintE = ((EditText) findViewById(R.id.blueprint_nameE)).getText().toString();
        int reg_type = ((Spinner) findViewById(R.id.reg_type)).getSelectedItemPosition();

        if (land_name.isEmpty() || (reg_type == 0 && plan_id.isEmpty()) || (reg_type == 1 && blueprintE.isEmpty())) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.fill_error), Toast.LENGTH_LONG).show();
                }
            });
        } else if (images == null || images.isEmpty() || images.size() == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.upload_image_error), Toast.LENGTH_LONG).show();
                }
            });
        } else {

            register(images);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            images = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            setImages(images);
            //System.out.println("FFFFFFFFFFFFFFFFFFFFFf");
            // changeImage(profile_image.get(0), R.id.profile_image);

        } else {
            hideLoading();
        }
    }
    public void getBluePrintsFromLocal() {
        BluePrintsReaderContract.FeedReaderDbHelper dbHelper = new BluePrintsReaderContract.FeedReaderDbHelper(getApplicationContext());

        SQLiteDatabase db = dbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                BluePrintsReaderContract.FeedEntry.GId,
                BluePrintsReaderContract.FeedEntry.name,
        };


// How you want the results sorted in the resulting Cursor
        String sortOrder =
                BluePrintsReaderContract.FeedEntry.GId + " DESC";

        Cursor cursor = db.query(
                BluePrintsReaderContract.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        ArrayList<StringWithTag> bluePrints = new ArrayList<>();
        bluePrints.add(new StringWithTag(getResources().getString(R.string.blueprint_name), "0"));
        while (cursor.moveToNext()) {
            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(BluePrintsReaderContract.FeedEntry.GId));
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(BluePrintsReaderContract.FeedEntry.name));
            bluePrints.add(new StringWithTag(name, String.valueOf(id)));
        }
        cursor.close();
        fillBlueprints(bluePrints);
        // initGrid(bluePrints);
    }
    private void viewEditTexts() {
        //findViewById(R.id.land_numER).setVisibility(View.VISIBLE);
        findViewById(R.id.blueprint_nameER).setVisibility(View.VISIBLE);
        findViewById(R.id.blueprint_nameSR).setVisibility(View.GONE);
        // findViewById(R.id.land_numSR).setVisibility(View.GONE);
        inside_talya = false;
    }
    private void viewSpinners() {
        // findViewById(R.id.land_numER).setVisibility(View.GONE);
        findViewById(R.id.blueprint_nameER).setVisibility(View.GONE);
        findViewById(R.id.blueprint_nameSR).setVisibility(View.VISIBLE);
        //    findViewById(R.id.land_numSR).setVisibility(View.VISIBLE);
        inside_talya = true;
    }
    public void fillReg_types() {

        String[] types = getResources().getStringArray(R.array.reg_types);
        final Spinner reg_type = findViewById(R.id.reg_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item_custom, types);
        reg_type.setAdapter(adapter);
    }
    public void fillBlueprints(ArrayList bluePrints) {
        final Spinner blue_prints = findViewById(R.id.blueprint_nameS);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item_custom, bluePrints);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                blue_prints.setAdapter(adapter);
            }
        });
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
    private static class StringWithTag {
        public String key;
        public String value;

        public StringWithTag(String value, String key) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private void setImages(ArrayList<String> images) {
        ((TextView) findViewById(R.id.image1)).setText("");
        ((TextView) findViewById(R.id.image2)).setText("");
        ((TextView) findViewById(R.id.image3)).setText("");

        if (images.size() > 0) {
            ((TextView) findViewById(R.id.image1)).setText(getResources().getString(R.string.image1));
        }
        if (images.size() > 1) {
            ((TextView) findViewById(R.id.image2)).setText(getResources().getString(R.string.image2));

        }
        if (images.size() > 2) {
            ((TextView) findViewById(R.id.image3)).setText(getResources().getString(R.string.image3));

        }
    }

}