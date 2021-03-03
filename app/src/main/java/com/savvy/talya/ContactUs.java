package com.savvy.talya;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.savvy.talya.Databases.FAQsReaderContract;
import com.savvy.talya.Models.FAQs;
import com.savvy.talya.Network.Iokihttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContactUs#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactUs extends Fragment {

    View view;
    SharedPreferences shared;
    private Iokihttp okhttp;
    public static String FACEBOOK_URL = "https://www.facebook.com/Talya.properties";
    public static String FACEBOOK_PAGE_ID = "271164176314167";
    public static String youtubeURL = "https://www.youtube.com/user/talyaproperties";
    private static final int MAKE_CALL_PERMISSION_REQUEST_CODE = 1;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ContactUs() {
        // Required empty public constructor
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContactUs.
     */
    // TODO: Rename and change types and number of parameters
    public static ContactUs newInstance(String param1, String param2) {
        ContactUs fragment = new ContactUs();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private void init() {
        try {
            Objects.requireNonNull(getActivity()).getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        okhttp = new Iokihttp();
        shared = getActivity().getSharedPreferences("com.savvy.talya", Context.MODE_PRIVATE);
        view.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
            }
        });
        view.findViewById(R.id.facebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFB();
            }
        });
        view.findViewById(R.id.twitter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTwitter();
            }
        });
        view.findViewById(R.id.youtube).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openYoutube();
            }
        });
        view.findViewById(R.id.insta).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openInsta();
            }
        });
        view.findViewById(R.id.call1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call(Objects.requireNonNull(getActivity()).getResources().getString(R.string.call_phone));
            }
        });
        try {
            getFaqsFromLocal();
        } catch (Exception e) {

        }
    }

    private void validate() {
        String subject = ((EditText) view.findViewById(R.id.subject)).getText().toString();
        String name = ((EditText) view.findViewById(R.id.name)).getText().toString();
        String email = ((EditText) view.findViewById(R.id.email)).getText().toString();
        String message = ((EditText) view.findViewById(R.id.message)).getText().toString();
        int selected_type_id = ((RadioGroup) (view.findViewById(R.id.types_group))).getCheckedRadioButtonId();
        String selected_type = (selected_type_id == R.id.messageR ? "1" : "2");

        if (subject.isEmpty() || name.isEmpty() || message.isEmpty() || (!isEmailValid(email) && !email.isEmpty())) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), getResources().getString(R.string.fill_error), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            send(subject, name, email, message, selected_type);
        }
    }

    private void send(String subject, String name, String email, String message, String type) {
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();

        try {
            subJSON.put("user_id", shared.getString("user_id", ""));
            subJSON.put("subject", subject);
            subJSON.put("name", name);
            subJSON.put("email", email);
            subJSON.put("type", type);
            subJSON.put("message", message);

            json.put("data", subJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        showLoading();
        if (okhttp.isNetworkConnected(getContext())) {
            okhttp.post(getString(R.string.url) + "contactUs", shared.getString("token", ""), json.toString(), new Callback() {
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
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getContext(), getString(R.string.successfull_sent), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
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
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, MAKE_CALL_PERMISSION_REQUEST_CODE);
        System.out.println("call");
        if (checkPermission(Manifest.permission.CALL_PHONE)) {
            String dial = "tel:" + phone;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), "Permission Call Phone denied", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void emptyFields() {
        ((EditText) view.findViewById(R.id.subject)).setText("");
        ((EditText) view.findViewById(R.id.name)).setText("");
        ((EditText) view.findViewById(R.id.email)).setText("");
        ((EditText) view.findViewById(R.id.message)).setText("");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_contact_us, container, false);
        init();
        return view;
    }
    public void showLoading() {
        final View main = view.findViewById(R.id.layout);
        final ProgressBar loading = view.findViewById(R.id.main_loading);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.setClickable(false);

                main.setVisibility(View.GONE);

                loading.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hideLoading() {
        final View main = view.findViewById(R.id.layout);
        final ProgressBar loading = view.findViewById(R.id.main_loading);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.setClickable(true);

                main.setVisibility(View.VISIBLE);

                loading.setVisibility(View.GONE);
            }
        });
    }

    public void getFaqsFromLocal() {
        FAQsReaderContract.FeedReaderDbHelper dbHelper = new FAQsReaderContract.FeedReaderDbHelper(getContext());

        SQLiteDatabase db = dbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                FAQsReaderContract.FeedEntry.GId,
                FAQsReaderContract.FeedEntry.question,
                FAQsReaderContract.FeedEntry.answer
        };


// How you want the results sorted in the resulting Cursor
        String sortOrder =
                FAQsReaderContract.FeedEntry.GId + " DESC";

        Cursor cursor = db.query(
                FAQsReaderContract.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        ArrayList<FAQs> faqs = new ArrayList<>();
        while (cursor.moveToNext()) {
            FAQs faq = new FAQs();
            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(FAQsReaderContract.FeedEntry.GId));
            String question = cursor.getString(
                    cursor.getColumnIndexOrThrow(FAQsReaderContract.FeedEntry.question));
            String answer = cursor.getString(
                    cursor.getColumnIndexOrThrow(FAQsReaderContract.FeedEntry.answer));

            faq.setId(String.valueOf(id));
            faq.setQuestion(question);
            faq.setAnswer(answer);

            faqs.add(faq);
        }
        cursor.close();
        setList(faqs);
    }

    public void setList(ArrayList<FAQs> faqs) {
        System.out.println("11");
        final ListView listView = view.findViewById(R.id.faq_list);
        System.out.println("22");

        final faqs_list_adapter adapter = new ContactUs.faqs_list_adapter(faqs, getContext());

        try {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    listView.setAdapter(adapter);
                }
            });
        } catch (Exception e) {

        }


    }

    public class faqs_list_adapter extends ArrayAdapter<FAQs> implements View.OnClickListener {
        private Context mContext;
        private int lastPosition = -1;
        private ViewHolder viewHolder;


        public faqs_list_adapter(ArrayList<FAQs> data, Context context) {
            super(context, R.layout.faq_card, data);
            System.out.println("DATAMODELS INSIDE ADAPTER=" + data.size());
            this.mContext = context;

        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            final FAQs dataModel = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            // view lookup cache stored in tag

            final View result;
            if (convertView == null) {


                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.faq_card, parent, false);

                viewHolder.question = convertView.findViewById(R.id.question);
                // viewHolder.datetime = convertView.findViewById(R.id.date_status);
                viewHolder.answer = convertView.findViewById(R.id.answer);


                result = convertView;


                System.out.println("Position=" + position);
                convertView.setTag(viewHolder);


            } else {
                viewHolder = (ViewHolder) convertView.getTag();
                result = convertView;
            }
            assert dataModel != null;
            viewHolder.question.setText(dataModel.getQuestion());
            viewHolder.answer.setText(dataModel.getAnswer());

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
            TextView answer, question;


        }


    }

    private void openInsta() {
        try {
            // mediaLink is something like "https://instagram.com/p/6GgFE9JKzm/" or
            // "https://instagram.com/_u/sembozdemir"
            Uri uri = Uri.parse("https://instagram.com/talyaproperties29?igshid=iv9ru0x7um13");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);

            intent.setPackage("com.instagram.android");
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            //Log.e(TAG, e.getMessage());
        }
    }

    private void openTwitter() {
        Intent intent = null;
        try {
            // get the Twitter app if possible
            getActivity().getPackageManager().getPackageInfo("com.twitter.android", 0);
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=" + "2501619848"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } catch (Exception e) {
            // no Twitter app, revert to browser
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/TalyaProperties"));
        }
        this.startActivity(intent);
    }

    private void openFB() {
        try {
            getContext().getPackageManager().getPackageInfo("com.facebook.katana", 0);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/" + FACEBOOK_PAGE_ID));
            startActivity(intent);
        } catch (Exception e) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOK_URL));
            startActivity(intent);
        }
    }

    private void openWeb() {

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.savvy-technology.com"));
        startActivity(intent);
    }

    private void openYoutube() {
        Intent intent = null;
        try {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage("com.google.android.youtube");
            intent.setData(Uri.parse(youtubeURL));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(youtubeURL));
            startActivity(intent);
        }
    }
}