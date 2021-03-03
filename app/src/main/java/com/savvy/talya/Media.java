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
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.savvy.talya.Databases.NewsReaderContract;
import com.savvy.talya.Models.News;
import com.savvy.talya.Network.Iokihttp;

import java.util.ArrayList;
import java.util.Objects;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Media#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Media extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    View view;
    Iokihttp iokihttp;
    SharedPreferences shared;
    private ArrayList<News> news;
    DisplayImageOptions options;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Media() {
        // Required empty public constructor
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
        news = new ArrayList<>();
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
            final View section_card = inflater.inflate(R.layout.news_card, null);

            final CardView section_cardView = section_card.findViewById(R.id.section_cardView);
            section_cardView.setId(getID("1", x));
            final ImageView icon = section_cardView.findViewById(R.id.section_icon);
            icon.setId(getID("2", x));
            TextView lable = section_cardView.findViewById(R.id.section_label);
            lable.setId(getID("3", x));
            TextView date = section_cardView.findViewById(R.id.section_date);
            date.setId(getID("4", x));
            News section = null;
            if (x < sections.size()) {
                section = (News) sections.get(x);
                lable.setText(section.getTitle());
                date.setText(section.getDate());
                if (!(section.getImage_url().isEmpty())) {
                    final ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance
                    ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(Objects.requireNonNull(getContext()))
                            .writeDebugLogs()
                            .build();
                    imageLoader.init(config);
                    final News finalSection = section;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                imageLoader.displayImage(finalSection.getImage_url(), icon, options);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                section_cardView.setTag(x);

                news.add(section);
            } else section_cardView.setVisibility(View.INVISIBLE);

            //icon.setImageURI(Uri.parse(section.getString("img")));

            section_cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openNewsProfile(Integer.parseInt(view.getTag().toString()));
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
            final int finalX = x;
            System.out.println("outside" + finalX);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // section_grid.setLayoutParams(gridParam);
                    section_grid.addView(section_cardView);
                }
            });
        }
    }

    private void openNewsProfile(int tag) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("new", news.get(tag));
        Intent intent = new Intent(getContext(), News_Profile.class);
        intent.putExtra("new", bundle);
        startActivityForResult(intent, 111);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Media.
     */
    // TODO: Rename and change types and number of parameters
    public static Media newInstance(String param1, String param2) {
        Media fragment = new Media();
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

    private void init() {
        try {
            Objects.requireNonNull(getActivity()).getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        shared = getActivity().getSharedPreferences("com.savvy.talya", Context.MODE_PRIVATE);
        iokihttp = new Iokihttp();
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.logoyellow)
                .showImageOnFail(R.drawable.logoyellow)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(20))
                .build();
        try {
            getNewsFromLocal();
        } catch (Exception e) {
            System.out.println("dddd" + e);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_media, container, false);
            init();
        return view;
    }

    private int getID(String id, int number) {
        String s = id + number;
        return Integer.parseInt(s);
    }

    public void getNewsFromLocal() {
        NewsReaderContract.FeedReaderDbHelper dbHelper = new NewsReaderContract.FeedReaderDbHelper(getContext());

        SQLiteDatabase db = dbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                NewsReaderContract.FeedEntry.GId,
                NewsReaderContract.FeedEntry.title,
                NewsReaderContract.FeedEntry.description,
                NewsReaderContract.FeedEntry.date,
                NewsReaderContract.FeedEntry.url_image,
        };


// How you want the results sorted in the resulting Cursor
        String sortOrder =
                NewsReaderContract.FeedEntry._ID + " ASC";

        Cursor cursor = db.query(
                NewsReaderContract.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        ArrayList<News> news = new ArrayList<>();
        while (cursor.moveToNext()) {
            News singleNew = new News();
            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(NewsReaderContract.FeedEntry.GId));
            String title = cursor.getString(
                    cursor.getColumnIndexOrThrow(NewsReaderContract.FeedEntry.title));
            String description = cursor.getString(
                    cursor.getColumnIndexOrThrow(NewsReaderContract.FeedEntry.description));
            String date = cursor.getString(
                    cursor.getColumnIndexOrThrow(NewsReaderContract.FeedEntry.date));
            String url_image = cursor.getString(
                    cursor.getColumnIndexOrThrow(NewsReaderContract.FeedEntry.url_image));
            singleNew.setId(String.valueOf(id));
            singleNew.setTitle(title);
            singleNew.setDescription(description);
            singleNew.setDate(date);
            singleNew.setImage_url(url_image);
            news.add(singleNew);
        }
        cursor.close();
        initGrid(news);
    }
}