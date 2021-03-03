package com.savvy.talya;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.savvy.talya.Databases.BluePrintsReaderContract;
import com.savvy.talya.Databases.OffersReaderContract;
import com.savvy.talya.Models.BluePrint;
import com.savvy.talya.Models.Offer;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Offers#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Offers extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    View view;
    private static final int MAKE_CALL_PERMISSION_REQUEST_CODE = 1;


    public Offers() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Offers.
     */
    // TODO: Rename and change types and number of parameters
    public static Offers newInstance(String param1, String param2) {
        Offers fragment = new Offers();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
        view = inflater.inflate(R.layout.fragment_offers, container, false);
        init();
        return view;
    }

    private void callButtons() {
        view.findViewById(R.id.call1).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                call(getResources().getString(R.string.sells_call_phone1));
            }
        });
        view.findViewById(R.id.call2).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                call(getResources().getString(R.string.sells_call_phone2));
            }
        });
        view.findViewById(R.id.call3).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                call(getResources().getString(R.string.sells_call_phone3));
            }
        });
        view.findViewById(R.id.call4).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                call(getResources().getString(R.string.sells_call_phone4));
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void call(String phone) {
        ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.CALL_PHONE}, MAKE_CALL_PERMISSION_REQUEST_CODE);
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

    private void init() {
        getOffersFromLocal();
        callButtons();
    }

    public void getOffersFromLocal() {
        OffersReaderContract.FeedReaderDbHelper dbHelper = new OffersReaderContract.FeedReaderDbHelper(getContext());

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

        Cursor cursor = db.query(
                OffersReaderContract.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        ArrayList<Offer> current_offers = new ArrayList<>();
        ArrayList<Offer> old_offers = new ArrayList<>();

        while (cursor.moveToNext()) {
            Offer offer = new Offer();
            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(OffersReaderContract.FeedEntry.GId));
            String start_date = cursor.getString(
                    cursor.getColumnIndexOrThrow(OffersReaderContract.FeedEntry.start_date));
            String end_date = cursor.getString(
                    cursor.getColumnIndexOrThrow(OffersReaderContract.FeedEntry.end_date));
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
            offer.setStart_date(start_date);
            offer.setEnd_date(end_date);
            offer.setCurrency(currency);
            offer.setCurrency_name(currency_name);
            offer.setAdvance(advance);
            offer.setAdded_value(added_value);
            offer.setMonth_no(month_no);
            offer.setPlan_id(plan_id);
            offer.setPlan_name(plan);
            offer.setType(type);
            if (Integer.parseInt(type) == 1) current_offers.add(offer);
            else old_offers.add(offer);

            //.add(faq);
        }
        cursor.close();
        setCurrentList(current_offers);
        setOldList(old_offers);
    }

    public void setCurrentList(final ArrayList<Offer> offers) {
        if (offers.size() > 0) {
            System.out.println("11");
            final ListView listView = view.findViewById(R.id.current_offers_list);
            System.out.println("22");
            final Offers.Offer_list_adapter adapter = new Offers.Offer_list_adapter(offers, getContext(), R.layout.current_offer_card);
            try {
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Offer dataModel = offers.get(position);
                        if (Integer.parseInt(dataModel.getType()) == 1)
                            getBluePrintFromLocal(dataModel.getPlan_id());
                        //getPackagesMeals(dataModel);
                        //openHallsDetails("", "", dataModel);

                    }
                });
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        listView.setAdapter(adapter);
                    }
                });
            } catch (Exception e) {

            }
        } else {
            view.findViewById(R.id.current_offers_list).setVisibility(View.GONE);
            view.findViewById(R.id.current_offer_no_data).setVisibility(View.VISIBLE);
        }
    }

    public void getBluePrintFromLocal(String plan_id) {
        BluePrintsReaderContract.FeedReaderDbHelper dbHelper = new BluePrintsReaderContract.FeedReaderDbHelper(getContext());

        SQLiteDatabase db = dbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                BluePrintsReaderContract.FeedEntry.GId,
                BluePrintsReaderContract.FeedEntry.name,
                BluePrintsReaderContract.FeedEntry.image_url,
                BluePrintsReaderContract.FeedEntry.note,
                BluePrintsReaderContract.FeedEntry.map_url,
                BluePrintsReaderContract.FeedEntry.plot_total,
                BluePrintsReaderContract.FeedEntry.plot_rest,
                BluePrintsReaderContract.FeedEntry.plot_sold,
        };


// How you want the results sorted in the resulting Cursor
        String sortOrder =
                BluePrintsReaderContract.FeedEntry.GId + " DESC";

        String selection = BluePrintsReaderContract.FeedEntry.GId + " = ? ";
        String[] selectionArgs = {plan_id};

        Cursor cursor = db.query(
                BluePrintsReaderContract.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        ArrayList<BluePrint> bluePrints = new ArrayList<>();
        while (cursor.moveToNext()) {
            BluePrint bluePrint = new BluePrint();
            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(BluePrintsReaderContract.FeedEntry.GId));
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(BluePrintsReaderContract.FeedEntry.name));
            String image = cursor.getString(
                    cursor.getColumnIndexOrThrow(BluePrintsReaderContract.FeedEntry.image_url));
            String note = cursor.getString(
                    cursor.getColumnIndexOrThrow(BluePrintsReaderContract.FeedEntry.note));
            String map_url = cursor.getString(
                    cursor.getColumnIndexOrThrow(BluePrintsReaderContract.FeedEntry.map_url));
            String plot_total = cursor.getString(
                    cursor.getColumnIndexOrThrow(BluePrintsReaderContract.FeedEntry.plot_total));
            String plot_rest = cursor.getString(
                    cursor.getColumnIndexOrThrow(BluePrintsReaderContract.FeedEntry.plot_rest));
            String plot_sold = cursor.getString(
                    cursor.getColumnIndexOrThrow(BluePrintsReaderContract.FeedEntry.plot_sold));
            bluePrint.setId(String.valueOf(id));
            bluePrint.setName(name);
            bluePrint.setImage(image);
            bluePrint.setNote(note);
            bluePrint.setMaps_url(map_url);
            bluePrint.setPlot_total(plot_total);
            bluePrint.setPlot_rest(plot_rest);
            bluePrint.setPlot_sold(plot_sold);
            bluePrints.add(bluePrint);
        }
        cursor.close();
        if (bluePrints.size() > 0)
            openBluePrintProfile(bluePrints.get(0));
        else
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), getResources().getString(R.string.no_profile), Toast.LENGTH_LONG).show();
                }
            });
    }

    private void openBluePrintProfile(BluePrint bluePrint) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("blueprint", bluePrint);
        Intent intent = new Intent(getContext(), BluePrint_profile.class);
        intent.putExtra("blueprint", bundle);
        startActivityForResult(intent, 111);
    }

    public void setOldList(ArrayList<Offer> offers) {
        System.out.println("11");
        final ListView listView = view.findViewById(R.id.old_offers_list);
        System.out.println("22");
        final Offers.Offer_list_adapter adapter = new Offers.Offer_list_adapter(offers, getContext(), R.layout.old_offer_card);
        try {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    listView.setAdapter(adapter);
                }
            });
        } catch (Exception e) {

        }
    }

    public class Offer_list_adapter extends ArrayAdapter<Offer> implements View.OnClickListener {
        private final Context mContext;
        private int lastPosition = -1;
        private Offers.Offer_list_adapter.ViewHolder viewHolder;


        public Offer_list_adapter(ArrayList<Offer> data, Context context, int layout) {
            super(context, layout, data);
            //  System.out.println("DATAMODELS INSIDE ADAPTER=" + data.size());
            this.mContext = context;

        }

        @SuppressLint("SetTextI18n")
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            final Offer dataModel = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            // view lookup cache stored in tag

            final View result;
            if (convertView == null) {


                viewHolder = new Offers.Offer_list_adapter.ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                if (Integer.parseInt(dataModel.getType()) == 1)
                    convertView = inflater.inflate(R.layout.current_offer_card, parent, false);
                else convertView = inflater.inflate(R.layout.old_offer_card, parent, false);


                viewHolder.blueprint_name = convertView.findViewById(R.id.blueprint_name);
                viewHolder.currency_name = convertView.findViewById(R.id.currency_name);
                viewHolder.date = convertView.findViewById(R.id.date);
                viewHolder.added_value = convertView.findViewById(R.id.added_value);
                viewHolder.month_no = convertView.findViewById(R.id.month_no);
                viewHolder.advance = convertView.findViewById(R.id.advance);


                result = convertView;


                System.out.println("Position=" + position);
                convertView.setTag(viewHolder);


            } else {
                viewHolder = (Offers.Offer_list_adapter.ViewHolder) convertView.getTag();
                result = convertView;
            }
            assert dataModel != null;
            viewHolder.blueprint_name.setText(dataModel.getPlan_name());
            viewHolder.currency_name.setText(dataModel.getCurrency_name().equals("null") || dataModel.getCurrency_name().isEmpty() ? getResources().getString(R.string.sdg) : dataModel.getCurrency_name());
            viewHolder.date.setText(dataModel.getStart_date() + " - " + dataModel.getEnd_date());
            viewHolder.added_value.setText(dataModel.getAdded_value() + "%");
            viewHolder.month_no.setText(dataModel.getMonth_no());
            viewHolder.advance.setText(dataModel.getAdvance() + "%");

            //viewHolder.answer.setText(dataModel.getAnswer());

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
            TextView blueprint_name, currency_name, date, added_value, month_no, advance;


        }


    }

}