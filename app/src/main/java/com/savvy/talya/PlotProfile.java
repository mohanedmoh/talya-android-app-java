package com.savvy.talya;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fxn.pix.Pix;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.savvy.talya.Databases.OffersReaderContract;
import com.savvy.talya.Models.Offer;
import com.savvy.talya.Models.Plot;
import com.savvy.talya.Models.PlotPayament;
import com.savvy.talya.Network.Iokihttp;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class PlotProfile extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    public DatePickerDialog dpd;
    public Calendar now;
    Plot plot;
    Offer offer;
    SharedPreferences shared;
    String date = "";
    private static final int MAKE_CALL_PERMISSION_REQUEST_CODE = 1;
    Iokihttp iokihttp;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    ArrayList<String> images;
    boolean fromProfile = false;


    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot_profile);
        try {
            init();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void show() {
        if (shared.contains("client_id")) {
            findViewById(R.id.clientForm).setVisibility(View.VISIBLE);
        }
        if (shared.contains("agent_id")) {
            findViewById(R.id.agentForm).setVisibility(View.VISIBLE);
            findViewById(R.id.incentive_plot).setVisibility(View.VISIBLE);
            fillIncentives();
        }
    }

    private void fillIncentives() {
        TextView plot_no, plot_name, total_incentive, extra_quantity, extra_amount, basic;
        total_incentive = findViewById(R.id.total_incentive);
        extra_quantity = findViewById(R.id.extra_quantity);
        extra_amount = findViewById(R.id.extra_amount);
        basic = findViewById(R.id.basic);

        total_incentive.setText(plot.getTotal());
        extra_quantity.setText(plot.getExtra_quantity());
        extra_amount.setText(plot.getExtra_amount());
        basic.setText(plot.getBasic());


    }

    private void init() throws JSONException {
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        iokihttp = new Iokihttp();
        shared = getSharedPreferences("com.savvy.talya", Context.MODE_PRIVATE);
        plot = (Plot) Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).getBundle("bundle")).getSerializable("plot");
        offer = new Offer();
        getOffersFromLocal();
        now = Calendar.getInstance();

        dpd = DatePickerDialog.newInstance(
                PlotProfile.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        findViewById(R.id.date).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });
        findViewById(R.id.sendAgent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAgent();
            }
        });
        findViewById(R.id.calculate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateCal();
            }
        });
        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
            }
        });
        show();
        if (getIntent().getExtras().containsKey("client") && getIntent().getExtras().getBoolean("client")) {
            fromClientProfile();
        } else if (getIntent().getExtras().containsKey("agent") && getIntent().getExtras().getBoolean("agent")) {
            fromAgentProfile();
        }
        callButtons();
        findViewById(R.id.attach_files).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCam();
            }
        });
        findViewById(R.id.sendFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    validateImages();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        hideOffer();
        if (fromProfile) hideRegister();
    }

    private void emptyForm() {
        ((TextView) findViewById(R.id.image1)).setText("");
        ((TextView) findViewById(R.id.image2)).setText("");
        ((TextView) findViewById(R.id.image3)).setText("");

    }

    private void validateImages() throws IOException {

        if (images == null || images.isEmpty() || images.size() == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.upload_image_error), Toast.LENGTH_LONG).show();
                }
            });
        } else {

            attach(images);
        }
    }

    private void attach(ArrayList<String> images) throws IOException {
        File[] files = new File[images.size()];
        for (int x = 0; x < images.size(); x++) {
            System.out.println("FILE PATH =" + images.get(x));
            files[x] = saveImage(getApplicationContext(), images.get(x), "image" + x);
            // files[x].createNewFile(
        }
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        try {
            subJSON.put("user_id", shared.getString("user_id", "0"));
            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getApplicationContext())) {
            showLoading();

            iokihttp.uploadImage(json.toString(), files, getString(R.string.url) + "paymentProof", shared.getString("token", ""), new Callback() {
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
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Response=" + responseStr);

                    } else {
                        System.out.println("Response=" + "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj" + response.toString());
                    }
                }

            });
        } else {
        }
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

    private void openCam() {
        Pix.start(PlotProfile.this, 101, 3);
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

    private void callButtons() {
        findViewById(R.id.call1).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                call(getResources().getString(R.string.sells_call_phone1));
            }
        });
        findViewById(R.id.call2).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                call(getResources().getString(R.string.sells_call_phone2));
            }
        });
        findViewById(R.id.call3).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                call(getResources().getString(R.string.sells_call_phone3));
            }
        });
        findViewById(R.id.call4).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                call(getResources().getString(R.string.sells_call_phone4));
            }
        });
    }

    private void fromAgentProfile() {
        fromProfile = true;
        findViewById(R.id.payments_list_linear).setVisibility(View.GONE);
        findViewById(R.id.clientForm).setVisibility(View.GONE);
        findViewById(R.id.agentForm).setVisibility(View.GONE);
        findViewById(R.id.calculate_linear).setVisibility(View.GONE);
        findViewById(R.id.contact_us).setVisibility(View.GONE);
        findViewById(R.id.registerR).setVisibility(View.GONE);
        findViewById(R.id.upgradeR).setVisibility(View.GONE);
        findViewById(R.id.resellR).setVisibility(View.GONE);
        findViewById(R.id.attachFileLinear).setVisibility(View.GONE);
        findViewById(R.id.investment_chartR).setVisibility(View.GONE);
        findViewById(R.id.incentive_plot).setVisibility(View.GONE);
        setExtraPlotInfo();
        // makeInvestment_bar(plot.getInvestments());

    }

    private void fromClientProfile() {
        fromProfile = true;
        findViewById(R.id.payments_list_linear).setVisibility(View.VISIBLE);
        findViewById(R.id.clientForm).setVisibility(View.GONE);
        findViewById(R.id.agentForm).setVisibility(View.GONE);
        findViewById(R.id.calculate_linear).setVisibility(View.GONE);
        // findViewById(R.id.contact_us).setVisibility(View.GONE);
        findViewById(R.id.incentive_plot).setVisibility(View.GONE);
        findViewById(R.id.investment_chartR).setVisibility(View.VISIBLE);

        try {
            JSONArray investments = new JSONArray(plot.getInvestments());
            makeInvestment_bar(investments);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (Integer.parseInt(plot.getStatus_id()) <= 3)
            findViewById(R.id.attachFileLinear).setVisibility(View.VISIBLE);
        setList(plot.getPlotPayament());
        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), registeration_form.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.upgrade).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                landUpgrade();
            }
        });
        findViewById(R.id.resell).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                landResell();
            }
        });
        setExtraPlotInfo();
    }

    private void setExtraPlotInfo() {
        findViewById(R.id.employee_linear).setVisibility(View.VISIBLE);
        findViewById(R.id.current_price_linear).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.plot_current_meter_price)).setText(plot.getCurrent_meter_price().equals("null") ? "" : plot.getCurrent_meter_price() + getResources().getString(R.string.sdg));
        ((TextView) findViewById(R.id.plot_current_price)).setText(plot.getCurrent_price().equals("null") ? "" : plot.getCurrent_price() + getResources().getString(R.string.sdg));
        ((TextView) findViewById(R.id.action_employee)).setText(plot.getEmp_action().equals("null") ? "" : plot.getEmp_action());
        ((TextView) findViewById(R.id.follower_employee)).setText(plot.getEmp_follower().equals("null") ? "" : plot.getEmp_follower());
        ((TextView) findViewById(R.id.total_price_t)).setText(getResources().getString(R.string.price_in_purchase));
        ((TextView) findViewById(R.id.meter_price_t)).setText(getResources().getString(R.string.meter_price_in_purchase));


    }

    private void landResell() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(PlotProfile.this, R.style.MyDialogTheme);

        builder.setMessage(getString(R.string.continue_insure))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        JSONObject json = new JSONObject();
                        JSONObject subJSON = new JSONObject();
                        try {
                            subJSON.put("plot_id", plot.getId());
                            subJSON.put("user_id", shared.getString("user_id", ""));

                            json.put("data", subJSON);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        showLoading();
                        if (iokihttp.isNetworkConnected(getApplicationContext())) {
                            iokihttp.post(getString(R.string.url) + "landResell", shared.getString("token", ""), json.toString(), new Callback() {
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

                                            if (Integer.parseInt(resJSON.get("error").toString()) == 1) {
                                                emptyFields();
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), getString(R.string.successfull_sent), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
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
                                        System.out.println("Response=" + "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj" + response.toString());
                                    }
                                }
                            });
                        }
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();

        dialog.show();


    }
    private void landUpgrade() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(PlotProfile.this, R.style.MyDialogTheme);

        builder.setMessage(getString(R.string.continue_insure))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        JSONObject json = new JSONObject();
                        JSONObject subJSON = new JSONObject();
                        try {
                            subJSON.put("plot_id", plot.getId());
                            subJSON.put("user_id", shared.getString("user_id", ""));

                            json.put("data", subJSON);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        showLoading();
                        if (iokihttp.isNetworkConnected(getApplicationContext())) {
                            iokihttp.post(getString(R.string.url) + "landUpgrade", shared.getString("token", ""), json.toString(), new Callback() {
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

                                            if (Integer.parseInt(resJSON.get("error").toString()) == 1) {
                                                emptyFields();
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), getString(R.string.successfull_sent), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
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
                                        System.out.println("Response=" + "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj" + response.toString());
                                    }
                                }
                            });
                        }
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();

        dialog.show();
    }
    private void validate() {
        String name = ((EditText) findViewById(R.id.name)).getText().toString();
        String email = ((EditText) findViewById(R.id.email)).getText().toString();
        String message = ((EditText) findViewById(R.id.message)).getText().toString();

        if (name.isEmpty() || message.isEmpty() || (!isEmailValid(email) && !email.isEmpty())) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.fill_error), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            sendClient(name, email, message);
        }
    }
    private void emptyFields() {
        ((EditText) findViewById(R.id.name)).setText("");
        ((EditText) findViewById(R.id.email)).setText("");
        ((EditText) findViewById(R.id.message)).setText("");
        ((TextView) findViewById(R.id.date)).setText(getResources().getString(R.string.date_choose));
    }

    private void sendClient(String name, String email, String message) {
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();

        try {
            subJSON.put("type_id", 1);
            subJSON.put("plot_id", plot.getId());
            subJSON.put("user_id", shared.getString("user_id", ""));
            subJSON.put("title", name);
            subJSON.put("email", email);
            subJSON.put("description", message);

            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        showLoading();
        if (iokihttp.isNetworkConnected(getApplicationContext())) {
            iokihttp.post(getString(R.string.url) + "landContact", shared.getString("token", ""), json.toString(), new Callback() {
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

                            if (Integer.parseInt(resJSON.get("error").toString()) == 1) {
                                emptyFields();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), getString(R.string.successfull_sent), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
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
                        System.out.println("Response=" + "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj" + response.toString());
                    }
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void call(String phone) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, MAKE_CALL_PERMISSION_REQUEST_CODE);
        System.out.println("call");
        if (checkPermission(Manifest.permission.CALL_PHONE)) {
            String dial = "tel:" + phone;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(PlotProfile.this, "Permission Call Phone denied", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void sendAgent() {
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        String note = ((EditText) findViewById(R.id.note)).getText().toString();
        if (date.isEmpty()) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), getString(R.string.date_error), Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        try {
            subJSON.put("type_id", 1);
            subJSON.put("plot_id", plot.getId());
            subJSON.put("user_id", shared.getString("user_id", ""));
            subJSON.put("to_date", date);
            subJSON.put("note", note);

            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        showLoading();
        if (iokihttp.isNetworkConnected(getApplicationContext())) {
            iokihttp.post(getString(R.string.url) + "landReserve", shared.getString("token", ""), json.toString(), new Callback() {
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

                            if (Integer.parseInt(resJSON.get("error").toString()) == 1) {
                                emptyFields();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), getString(R.string.successfull_sent), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
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
                        System.out.println("Response=" + "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj" + response.toString());
                    }
                }
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void setInfo(Offer offer) {
        ((TextView) findViewById(R.id.plot_space)).setText(plot.getPlot_area());
        ((TextView) findViewById(R.id.plot_meter_price)).setText(plot.getMeter_price().equals("null") ? "" : plot.getMeter_price() + getResources().getString(R.string.sdg));
        ((TextView) findViewById(R.id.plot_cat)).setText(plot.getCat().equals("NULL") || plot.getCat().equals("\\N") ? "" : plot.getCat());
        ((TextView) findViewById(R.id.plot_cash_price)).setText(plot.getPrice_sdg().equals("null") ? "" : plot.getPrice_sdg() + getResources().getString(R.string.sdg));
        String offerS = "";
        if (!offer.getAdvance().isEmpty())
            offerS = offer.getAdvance() + "%" + getResources().getString(R.string.advance);
        if (!offer.getAdded_value().isEmpty())
            offerS += " " + offer.getAdded_value() + "%" + getResources().getString(R.string.added_value);
        if (offer.getId().isEmpty()) {
            findViewById(R.id.calculate_linear).setVisibility(View.GONE);
        }
        if (offerS.isEmpty()) {
            findViewById(R.id.offer_info_linear).setVisibility(View.GONE);
        }
        ((TextView) findViewById(R.id.plot_offer)).setText(offerS);
        if (plot.getStatus().equals("null") || plot.getStatus().isEmpty()) {
            findViewById(R.id.status_linear).setVisibility(View.GONE);
        }
        if (plot.getStatus_description().equals("null") || plot.getStatus_description().isEmpty()) {
            findViewById(R.id.status_desc_linear).setVisibility(View.GONE);
        }
        ((TextView) findViewById(R.id.plot_status)).setText(plot.getStatus());
        ((TextView) findViewById(R.id.statud_desc)).setText(plot.getStatus_description());

        if (plot.getStatus().equals("متعسرة")) {
            ((TextView) findViewById(R.id.plot_status)).setTextColor(getResources().getColor(R.color.red));
        }
        ((TextView) findViewById(R.id.plot_space)).setText(plot.getPlot_area());
        if (!plot.getPrice_usd().isEmpty() && !plot.getPrice_usd().equals("null")) {
            findViewById(R.id.usd_priceL).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.usd_price)).setText(plot.getPrice_usd().equals("null") ? "" : plot.getPrice_usd() + getResources().getString(R.string.usd));
            ((TextView) findViewById(R.id.plot_meter_price)).setText(plot.getMeter_price().equals("null") ? "" : plot.getMeter_price() + getResources().getString(R.string.usd));

        }
    }

    private void makeInvestment_bar(JSONArray table) throws JSONException {
        String title = "";// getResources().getString(R.string.sells_no);
        final BarChart barChart = findViewById(R.id.investment_chart);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                barChart.setVisibility(View.VISIBLE);
            }
        });
        List<BarEntry> entries = new ArrayList<>();
        String[] names = new String[table.length()];
        Float[] values = new Float[table.length()];
        float sum = 0f;
        for (int x = 0; x < names.length; x++) {
            JSONObject jsonArray = table.getJSONObject(x);
            names[x] = jsonArray.getString("title");
            values[x] = Float.parseFloat(jsonArray.getString("amount"));
            sum = sum + values[x];
        }

        for (int i = 0; i < names.length; i++) {
            float value = 1;
            try {
                // if (sum == 0) sum = 1;
                value = values[i];//(values[i] / sum) * 100;
            } catch (ArithmeticException a) {

            }
            entries.add(new BarEntry(i, value));
        }
        BarDataSet set = new BarDataSet(entries, title);
        BarData data = new BarData(set);
        data.setValueTextSize(10);
        data.setValueTextColor(R.color.white);
        set.setColors(getResources().getColor(R.color.bar_yellow_color));
        barChart.setData(data);
        data.setValueTextColor(Color.WHITE);
        data.setBarWidth(0.8f);
        final String[] quarters = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            quarters[i] = names[i];
        }
        IAxisValueFormatter formatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return quarters[(int) value];
            }

            // we don't draw numbers, so no decimal digits needed
        };

        XAxis xAxis = barChart.getXAxis();
        YAxis yAxisL = barChart.getAxisLeft();
        YAxis yAxisR = barChart.getAxisRight();
        yAxisR.setEnabled(false);
        yAxisL.setEnabled(false);

        yAxisL.setTextColor(getResources().getColor(R.color.white));
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setTextColor(getResources().getColor(R.color.primary_text));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(formatter);
        Legend l = barChart.getLegend();
        l.setEnabled(false);

        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);
        barChart.setTouchEnabled(true);
        barChart.animateY(3000);
        barChart.invalidate();
    }

    private void validateCal() {
        String advance = ((EditText) findViewById(R.id.advanceForm)).getText().toString();
        String month_no = ((EditText) findViewById(R.id.month_no)).getText().toString();
        //int reg_type = ((Spinner) findViewById(R.id.reg_type)).getSelectedItemPosition();
        try {
            if (advance.isEmpty() || month_no.isEmpty()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.fill_error), Toast.LENGTH_LONG).show();
                    }
                });
            } else if (Integer.parseInt(advance) < Integer.parseInt(offer.getAdvance())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.percentage_more_error), Toast.LENGTH_LONG).show();
                    }
                });
            } else if (Integer.parseInt(month_no) > Integer.parseInt(offer.getMonth_no())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.month_error), Toast.LENGTH_LONG).show();
                    }
                });
            } else if (Integer.parseInt(advance) >= 100) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.percentage_error), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                calculate();
            }
        } catch (Exception e) {

        }
    }

    private void calculate() {
        String advance = ((EditText) findViewById(R.id.advanceForm)).getText().toString();
        String month_no = ((EditText) findViewById(R.id.month_no)).getText().toString();
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        try {
            subJSON.put("month_no", month_no);
            subJSON.put("advance", advance);
            subJSON.put("plot_id", plot.getId());
            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getApplicationContext())) {
            showLoading();

            iokihttp.post(getString(R.string.url) + "calculate", shared.getString("token", ""), json.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    hideLoading();
                    System.out.println("FAIL");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
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
                                    fillCalculations(subresJSON);
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
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Response=" + responseStr);

                    } else {
                        hideLoading();
                        System.out.println("Response=" + "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj" + response.toString());
                    }
                }

            });
        } else {
        }
    }

    @SuppressLint("SetTextI18n")
    private void fillCalculations(JSONObject jsonObject) throws JSONException {
        findViewById(R.id.recieved_calculations).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.price_after)).setText(jsonObject.getString("plot_price"));
        Float percentage = Float.parseFloat(((EditText) findViewById(R.id.advanceForm)).getText().toString()) / 100;
        Float price = Float.parseFloat(plot.getPrice_sdg().isEmpty() ? plot.getPrice_usd() : plot.getPrice_sdg());
        ((TextView) findViewById(R.id.advance_after)).setText(price * percentage + "");
        ((TextView) findViewById(R.id.price_per_month_after)).setText(jsonObject.getString("monthly"));
        ((TextView) findViewById(R.id.added_value_after)).setText(jsonObject.getString("added_value"));

    }

    public void getOffersFromLocal() {
        OffersReaderContract.FeedReaderDbHelper dbHelper = new OffersReaderContract.FeedReaderDbHelper(getApplicationContext());

        SQLiteDatabase db = dbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                OffersReaderContract.FeedEntry.GId,
                OffersReaderContract.FeedEntry.start_date,
                OffersReaderContract.FeedEntry.end_date,
                OffersReaderContract.FeedEntry.currency,
                OffersReaderContract.FeedEntry.currency_name,
                OffersReaderContract.FeedEntry.advance,
                OffersReaderContract.FeedEntry.added_value,
                OffersReaderContract.FeedEntry.month_no,
                OffersReaderContract.FeedEntry.plan_id,
                OffersReaderContract.FeedEntry.plan_name,
                OffersReaderContract.FeedEntry.type,
        };
// How you want the results sorted in the resulting Cursor
        String sortOrder =
                OffersReaderContract.FeedEntry.GId + " DESC";

        String selection = OffersReaderContract.FeedEntry.plan_id + " = ? ";
        String[] selectionArgs = {plot.getBlueprint_id()};
        System.out.println("ID=" + plot.getBlueprint_id());
        if (plot.getBlueprint_id() != null) {
            Cursor cursor = db.query(
                    OffersReaderContract.FeedEntry.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );
            ArrayList<Offer> offers = new ArrayList<>();

            while (cursor.moveToNext()) {
                Offer offer = new Offer();
                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow(OffersReaderContract.FeedEntry.GId));

                String currency = cursor.getString(
                        cursor.getColumnIndexOrThrow(OffersReaderContract.FeedEntry.currency));
                String currency_name = cursor.getString(
                        cursor.getColumnIndexOrThrow(OffersReaderContract.FeedEntry.currency_name));
                String advance = cursor.getString(
                        cursor.getColumnIndexOrThrow(OffersReaderContract.FeedEntry.advance));
                String added_value = cursor.getString(
                        cursor.getColumnIndexOrThrow(OffersReaderContract.FeedEntry.added_value));
                String month_no = cursor.getString(
                        cursor.getColumnIndexOrThrow(OffersReaderContract.FeedEntry.month_no));
                String plan_id = cursor.getString(
                        cursor.getColumnIndexOrThrow(OffersReaderContract.FeedEntry.plan_id));
                String plan = cursor.getString(
                        cursor.getColumnIndexOrThrow(OffersReaderContract.FeedEntry.plan_name));
                String type = cursor.getString(
                        cursor.getColumnIndexOrThrow(OffersReaderContract.FeedEntry.type));

                offer.setId(String.valueOf(id));
                offer.setCurrency(currency);
                offer.setCurrency_name(currency_name);
                offer.setAdvance(advance);
                offer.setAdded_value(added_value);
                offer.setMonth_no(month_no);
                offer.setPlan_id(plan_id);
                offer.setPlan_name(plan);
                offer.setType(type);
                if (Integer.parseInt(type) == 1) offers.add(offer);
            }
            cursor.close();
            System.out.println("SIZE OFFERS=" + offers.size());
            if (offers.size() > 0) {
                offer = offers.get(0);
                if (!fromProfile)
                    findViewById(R.id.calculator).setVisibility(View.VISIBLE);
            } else hideOffer();
            setInfo(offer);
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

    public void setList(final ArrayList<PlotPayament> plotPayaments) {
        System.out.println("11");
        final ListView listView = findViewById(R.id.payments_list);
        System.out.println("SIZE=" + plotPayaments.size());
        System.out.println("22");
        if (plotPayaments.size() > 0) {
            final PlotProfile.payment_list_adapter adapter = new PlotProfile.payment_list_adapter(plotPayaments, getApplicationContext());

            try {
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        PlotPayament dataModel = plotPayaments.get(position);
                        //   openPlotProfile(dataModel);
                        //getPackagesMeals(dataModel);
                        //openHallsDetails("", "", dataModel);

                    }
                });
                runOnUiThread(new Runnable() {
                    public void run() {
                        listView.setAdapter(adapter);
                    }
                });
            } catch (Exception e) {

            }
        } else {
            findViewById(R.id.payments_list).setVisibility(View.GONE);
            findViewById(R.id.payments_list_no_data).setVisibility(View.VISIBLE);
        }

    }

    private void hideOffer() {
        System.out.println("ALLOW= " + plot.getAllowOffer());
        if (Integer.parseInt(plot.getAllowOffer()) == 0) {
            findViewById(R.id.offer_info_linear).setVisibility(View.INVISIBLE);
        }
    }

    private void hideRegister() {
        if (Integer.parseInt(plot.getIsRegistered()) == 1) {
            findViewById(R.id.registerR).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
        ((TextView) (findViewById(R.id.date))).setText(date);
    }

    public class payment_list_adapter extends ArrayAdapter<PlotPayament> implements View.OnClickListener {
        private final Context mContext;
        private int lastPosition = -1;
        private PlotProfile.payment_list_adapter.ViewHolder viewHolder;


        public payment_list_adapter(ArrayList<PlotPayament> data, Context context) {
            super(context, R.layout.payment_card, data);
            this.mContext = context;

        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            final PlotPayament dataModel = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            // view lookup cache stored in tag

            final View result;
            if (convertView == null) {


                viewHolder = new PlotProfile.payment_list_adapter.ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.payment_card, parent, false);

                viewHolder.payment_date = convertView.findViewById(R.id.payment_date);
                viewHolder.payment_amount = convertView.findViewById(R.id.payment_amount);
                viewHolder.recipt_date = convertView.findViewById(R.id.recipt_date);
                viewHolder.payment_method = convertView.findViewById(R.id.payment_method);
                viewHolder.payment_no = convertView.findViewById(R.id.payment_no);
                viewHolder.payment_status = convertView.findViewById(R.id.payment_status);
                viewHolder.payment_comments = convertView.findViewById(R.id.payment_comments);


                result = convertView;


                System.out.println("Position=" + position);
                convertView.setTag(viewHolder);


            } else {
                viewHolder = (PlotProfile.payment_list_adapter.ViewHolder) convertView.getTag();
                result = convertView;
            }
            assert dataModel != null;
            viewHolder.payment_date.setText(dataModel.getPayment_date());
            viewHolder.payment_amount.setText(dataModel.getPayment_amount());
            viewHolder.recipt_date.setText(dataModel.getRecipt_date());
            viewHolder.payment_method.setText("");
            viewHolder.payment_no.setText(dataModel.getRecipt());
            viewHolder.payment_status.setText(dataModel.getPaid().equals("true") ? getResources().getString(R.string.paid) : getResources().getString(R.string.not_paid));
            viewHolder.payment_comments.setText("");


            //   viewHolder.datetime.setText(dataModel.getDate());


            lastPosition = position;

          /*  viewHolder.txtLocation.setText(dataModel.getArea());
            viewHolder.txtPrice.setText(dataModel.getPrice());
            viewHolder.txtType.setText(dataModel.getType());
          */
            // new LoadImageTask(this).execute(dataModel.getImage());
            return convertView;
        }

        @Override
        public void onClick(View v) {

        }

        private class ViewHolder {
            TextView payment_date, payment_amount, recipt_date, payment_method, payment_no, payment_status, payment_comments;


        }


    }


}