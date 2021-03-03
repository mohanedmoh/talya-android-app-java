package com.savvy.talya;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.savvy.talya.Databases.BluePrintsReaderContract;
import com.savvy.talya.Databases.OffersReaderContract;
import com.savvy.talya.Models.BluePrint;
import com.savvy.talya.Models.Offer;
import com.savvy.talya.Network.Iokihttp;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BluePrints#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BluePrints extends Fragment {


    View view;
    Iokihttp iokihttp;
    SharedPreferences shared;
    DisplayImageOptions options;
    private ArrayList<BluePrint> bluePrints;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BluePrints() {
        // Required empty public constructor
    }

    private void init() throws JSONException {
        Objects.requireNonNull(getActivity()).getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        shared = getActivity().getSharedPreferences("com.savvy.talya", Context.MODE_PRIVATE);
        iokihttp = new Iokihttp();
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.logoyellow)
                .showImageOnFail(R.drawable.logoyellow)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new CircleBitmapDisplayer(getResources().getColor(R.color.secondary_bg), 1))
                .build();

            getBluePrintsFromLocal();

    }

    private void initGrid(ArrayList sections) {

        int length = sections.size();
        final int columnNo = 2;
        int rowNo = 0;
        if (length % 2 != 0) {
            length = length + 1;
        }
        rowNo = length / 2;
        final GridLayout section_grid = view.findViewById(R.id.section_grid);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                section_grid.removeAllViewsInLayout();
            }
        });
        bluePrints = new ArrayList<>();
        final int finalRowNo = rowNo;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                section_grid.setColumnCount(columnNo);
                section_grid.setRowCount(finalRowNo);
            }
        });


        for (int x = 0, c = 0, r = 0; x < length; x++, c++) {
            if (c == columnNo) {
                c = 0;
                r++;
            }
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View section_card = inflater.inflate(R.layout.blue_print_card, null);

            final CardView section_cardView = section_card.findViewById(R.id.section_cardView);
            section_cardView.setId(getID("1", x));
            final ImageView icon = section_cardView.findViewById(R.id.section_icon);
            icon.setId(getID("2", x));
            TextView lable = section_cardView.findViewById(R.id.section_label);
            TextView alert = section_card.findViewById(R.id.alert_title);

            lable.setId(getID("3", x));
            BluePrint section = null;
            if (x < sections.size()) {
                section = (BluePrint) sections.get(x);
                lable.setText(section.getName());
                if (!(section.getImage().isEmpty())) {
                    final ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance
                    ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(Objects.requireNonNull(getContext()))
                            .writeDebugLogs()
                            .build();
                    imageLoader.init(config);
                    final BluePrint finalSection = section;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageLoader.displayImage(finalSection.getImage(), icon, options);
                        }
                    });

                }
                if (Integer.parseInt(section.getPlot_rest()) <= 0) {
                    System.out.println("INSIDE SOLDOUT");
                    alert.setVisibility(View.VISIBLE);
                    alert.setText(getResources().getString(R.string.soldout));
                } else if (hasOffer(section.getId())) {
                    System.out.println("INSIDE OFFERS");

                    alert.setVisibility(View.VISIBLE);
                    alert.setText(getResources().getString(R.string.has_offer));
                }
                section_cardView.setTag(x);
                bluePrints.add(section);
            } else section_cardView.setVisibility(View.INVISIBLE);
            // URL url = new URL(getResources().getString(R.string.imgUrl)+section.getString("image"));
            // Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            //icon.setImageBitmap(bmp);

            //icon.setImageURI(Uri.parse(section.getString("img")));
            section_cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openBluePrintProfile(view.getTag().toString());
                }
            });
            GridLayout.Spec rowSpan = GridLayout.spec(GridLayout.UNDEFINED, 1);
            GridLayout.Spec colspan = GridLayout.spec(GridLayout.UNDEFINED, 1);
            if (r == 0 && c == 0) {
                Log.e("", "spec");
                colspan = GridLayout.spec(GridLayout.UNDEFINED, 2);
                rowSpan = GridLayout.spec(GridLayout.UNDEFINED, 2);
            }
            final GridLayout.LayoutParams gridParam = new GridLayout.LayoutParams(
                    rowSpan, colspan);

            if (section_cardView.getParent() != null) {
                ((ViewGroup) section_cardView.getParent()).removeView(section_cardView); // <- fix
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    section_grid.addView(section_cardView);
                }
            });
        }
    }

    private void rotate(View view) {
        view.setRotationY(360f);
    }

    private void openBluePrintProfile(String tag) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("blueprint", bluePrints.get(Integer.parseInt(tag)));
        Intent intent = new Intent(getContext(), BluePrint_profile.class);
        intent.putExtra("blueprint", bundle);
        startActivityForResult(intent, 111);
    }

    private int getID(String id, int number) {
        String s = id + number;
        return Integer.parseInt(s);
    }

    public boolean hasOffer(String id) {
        OffersReaderContract.FeedReaderDbHelper dbHelper = new OffersReaderContract.FeedReaderDbHelper(getContext());

        SQLiteDatabase db = dbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                OffersReaderContract.FeedEntry.GId,
                OffersReaderContract.FeedEntry.type,

        };


// How you want the results sorted in the resulting Cursor
        String sortOrder =
                OffersReaderContract.FeedEntry.GId + " DESC";

        String selection = OffersReaderContract.FeedEntry.plan_id + " = ? ";
        String[] selectionArgs = {id};
        if (id != null) {
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
                int gid = cursor.getInt(
                        cursor.getColumnIndexOrThrow(OffersReaderContract.FeedEntry.GId));
                String type = cursor.getString(
                        cursor.getColumnIndexOrThrow(OffersReaderContract.FeedEntry.type));

                offer.setId(String.valueOf(gid));
                if (Integer.parseInt(type) == 1) offers.add(offer);


                //.add(faq);
            }
            cursor.close();
            System.out.println("SIZE OFFERS=" + offers.size());
            return offers.size() > 0;
        }
        return false;
    }

    public void getBluePrintsFromLocal() {
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
                BluePrintsReaderContract.FeedEntry._ID + " ASC";

        Cursor cursor = db.query(
                BluePrintsReaderContract.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
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
        initGrid(bluePrints);
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BluePrints.
     */
    // TODO: Rename and change types and number of parameters
    public static BluePrints newInstance(String param1, String param2) {
        BluePrints fragment = new BluePrints();
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
        view = inflater.inflate(R.layout.fragment_blue_prints, container, false);
        try {
            init();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }
}