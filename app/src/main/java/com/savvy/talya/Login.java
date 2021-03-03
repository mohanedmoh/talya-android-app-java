package com.savvy.talya;

import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.goodiebag.pinview.Pinview;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hbb20.CountryCodePicker;
import com.savvy.talya.Network.Iokihttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

//import com.google.firebase.iid.FirebaseInstanceId;

public class Login extends AppCompatActivity {
    SharedPreferences shared;
    boolean doubleBackToExitPressedOnce = false;
    int exist = 1;
    View phone_include;
    private boolean main = true;
    private Iokihttp okhttp;
    private Button verify, verify_pin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();

    }

    private void init() {
        //getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        okhttp = new Iokihttp();
        //avi=findViewById(R.id.avi);
        verify = findViewById(R.id.btn_send);
        verify_pin = findViewById(R.id.verify_pin);
        phone_include = findViewById(R.id.phoneLayout);
        verify_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verify_pin();
            }
        });
        phone_include.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateLogin()) {
                    send();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getString(R.string.fill_error), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        shared = this.getSharedPreferences("com.savvy.talya", Context.MODE_PRIVATE);
        phone_include.findViewById(R.id.create_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCreateAccount();
            }
        });
        //    hideKeyboard();
        // ((Pinview)(findViewById(R.id.otcLayout).findViewById(R.id.pinView))).setcolo
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);

    }

    private boolean validateLogin() {
        String phone = ((EditText) phone_include.findViewById(R.id.phone)).getText().toString();
        return !phone.isEmpty() && phone.length() >= 8;
    }

    protected void send() {

        EditText mPhoneEdit = phone_include.findViewById(R.id.phone);
        final String[] phone = new String[1];
        phone[0] = validate();
        if (phone[0] == null) {
            mPhoneEdit.requestFocus();
            mPhoneEdit.setError(getString(R.string.label_error_incorrect_phone));
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(Login.this, R.style.MyDialogTheme);

        builder.setMessage(getString(R.string.insure_dialog) + "\n" + " " + phone[0] + "\n" + getString(R.string.is_this_ok))
                .setPositiveButton(R.string.continueS, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        showLoading();
                        String country_code = ((CountryCodePicker) phone_include.findViewById(R.id.ccp)).getSelectedCountryCode();
                        JSONObject json = new JSONObject();
                        JSONObject subJSON = new JSONObject();
                        try {
                            subJSON.put("country_code", country_code);
                            subJSON.put("phone", phone[0]);

                            json.put("data", subJSON);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (okhttp.isNetworkConnected(getApplicationContext())) {
                            okhttp.post(getString(R.string.url) + "sendOTP", shared.getString("token", ""), json.toString(), new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    System.out.println("FAIL");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                                        }
                                    });
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
                                                if (Integer.parseInt(resJSON.get("code").toString()) == 1) {
                                                    final JSONObject subresJSON = new JSONObject(resJSON.getString("data"));
                                                    if (subresJSON.has("Master")) {
                                                        shared.edit().putString("user_id", subresJSON.getJSONObject("Master").getString("id")).apply();
                                                        shared.edit().putString("user_name", subresJSON.getJSONObject("Master").getString("name")).apply();
                                                        shared.edit().putString("phone", subresJSON.getJSONObject("Master").getString("phone")).apply();
                                                        /*runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    Toast.makeText(getApplicationContext(), subresJSON.getJSONObject("Master").getString("verification_code"), Toast.LENGTH_LONG).show();
                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });*/
                                                    }
                                                    if (subresJSON.has("Client") && !subresJSON.get("Client").toString().isEmpty() && !subresJSON.get("Client").toString().equals("null")) {
                                                        shared.edit().putString("client_id", subresJSON.getJSONObject("Client").getString("client_id")).apply();
                                                    }
                                                    if (subresJSON.has("Agent") && !subresJSON.get("Agent").toString().isEmpty() && !subresJSON.get("Agent").toString().equals("null")) {
                                                        shared.edit().putString("agent_id", subresJSON.getJSONObject("Agent").getString("agent_id")).apply();
                                                    }
                                                    openpinLayout();
                                                } else if (Integer.parseInt(resJSON.get("code").toString()) == 2) {
                                                    hideLoading();
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Toast.makeText(getApplicationContext(), getString(R.string.number_not_found), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            } else {
                                                hideLoading();
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
                .setNegativeButton(R.string.edit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();

        dialog.show();


    }

    protected String validate() {
        String phone = ((EditText) phone_include.findViewById(R.id.phone)).getText().toString();
        return phone;
    }

    protected void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    protected void showKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void openpinLayout() {
        final View phoneLayout = findViewById(R.id.phoneLayout);
        final View otcLayout = findViewById(R.id.otcLayout);
        animateLayout(phoneLayout, otcLayout);
    }

    private void openCreateAccount() {
        Intent intent = new Intent(getApplicationContext(), Signup.class);
        startActivity(intent);
        finish();
    }

    public void animateLayout(final View before, final View after) {
        Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                System.out.println("SHOW " + after.getId());
                before.setVisibility(View.GONE);
                after.setVisibility(View.VISIBLE);
                //  finalAfter.animate().alpha(1f).setDuration(700);

                main = false;


            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };
        before.animate().alpha(0f).setDuration(700).setListener(animatorListener);
    }

    @Override
    public void onBackPressed() {
        if (main) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                finish();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getString(R.string.press_back), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 3000);

        } else {
            final View phoneLayout = findViewById(R.id.phoneLayout);
            final View otcLayout = findViewById(R.id.otcLayout);


            phoneLayout.setVisibility(View.VISIBLE);
            otcLayout.setVisibility(View.GONE);

            main = true;
            phoneLayout.setAlpha(1f);
        }
    }
    public void openMain() {
        shared.edit().putBoolean("login", true).apply();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void verify_pin() {
        //String token = FirebaseInstanceId.getInstance().getToken();
        String token = FirebaseInstanceId.getInstance().getToken();

        Pinview pin = findViewById(R.id.pinView);
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        if (pin.getValue().isEmpty() || pin.getValue().length() < 4) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.fill_error), Toast.LENGTH_LONG).show();
            return;
        }
        try {
            System.out.println("PIN=" + pin.getValue());
            subJSON.put("user_id", shared.getString("user_id", ""));
            subJSON.put("token", token);
            subJSON.put("pin", pin.getValue());
            json.put("data", subJSON);
            System.out.println("lfllflfl" + json.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        showLoading();
        if (okhttp.isNetworkConnected(getApplicationContext())) {
            okhttp.post(getString(R.string.url) + "verifyOTP", shared.getString("token", ""), json.toString(), new Callback() {
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
                                shared.edit().putString("token", resJSON.getJSONObject("data").getString("token")).apply();
                                openMain();
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
                        System.out.println("Response=" + "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj");
                    }
                }
            });
        }
    }

    public void showLoading() {
        final View main = findViewById(R.id.login_layout);
        final ProgressBar loading = findViewById(R.id.login_loading);
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
        final View main = findViewById(R.id.login_layout);
        final ProgressBar loading = findViewById(R.id.login_loading);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.setClickable(true);

                main.setVisibility(View.VISIBLE);

                loading.setVisibility(View.GONE);
            }
        });
    }

}