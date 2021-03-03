package com.savvy.talya;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jsibbold.zoomage.ZoomageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.savvy.talya.Databases.BluePrintDetailsReaderContract;
import com.savvy.talya.Databases.PlotsReaderContract;
import com.savvy.talya.Models.BluePrint;
import com.savvy.talya.Models.BluePrintDetail;
import com.savvy.talya.Models.BluePrint_information;
import com.savvy.talya.Models.Plot;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.gridlayout.widget.GridLayout;

public class BluePrint_profile extends AppCompatActivity {
    BluePrint bluePrint;
    DisplayImageOptions options;
    ArrayList<Plot> plots;
    String plan_id;
    private PopupWindow mPopupWindow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_print_profile);
        init();
    }

    private void init() {
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        bluePrint = (BluePrint) Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).getBundle("blueprint")).getSerializable("blueprint");
        plan_id = bluePrint.getId();
        getDetailsFromLocal();
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.logoyellow)
                .showImageOnFail(R.drawable.logoyellow)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(20))
                .build();
        findViewById(R.id.map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(bluePrint.getMaps_url()));
                startActivity(browserIntent);
            }
        });
        setInfo();
        getPlotsFromLocal();
        ScrollToBottom();
    }

    private void ScrollToBottom() {
        final NestedScrollView scrollView = findViewById(R.id.layoutN);
        findViewById(R.id.bottom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //((NestedScrollView)(findViewById(R.id.mainScroll))).fullScroll(View.FOCUS_DOWN);
                View lastChild = scrollView.getChildAt(scrollView.getChildCount() - 1);
                int bottom = lastChild.getBottom() + scrollView.getPaddingBottom();
                int sy = scrollView.getScrollY();
                int sh = scrollView.getHeight();
                int delta = bottom - (sy + sh);

                scrollView.smoothScrollBy(0, delta);
            }
        });


    }

    private void openDetailsPopup(final String url, String name) throws URISyntaxException {
        boolean selected = false;
        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        // Inflate the custom layout/view
        View customView = inflater.inflate(R.layout.image_popup, null);

        // Initialize a new instance of popup window
        mPopupWindow = new PopupWindow(
                customView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );

        // Set an elevation value for popup window
        // Call requires API level 21
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        // Get a reference for the custom view close button
        ((TextView) customView.findViewById(R.id.name)).setText(name);
        ImageView closeButton = customView.findViewById(R.id.close);
        ZoomageView imageZoom = customView.findViewById(R.id.myZoomageView);
        ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .writeDebugLogs()
                .build();
        imageLoader.init(config);
        imageLoader.displayImage(url, imageZoom);
        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                mPopupWindow.dismiss();
            }
        });


        View layout = findViewById(R.id.layout);

        // Finally, show the popup window at the center location of root relative layout
        mPopupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
    }

    private void setInfo() {
        ((TextView) findViewById(R.id.title)).setText(bluePrint.getName());
        ((TextView) findViewById(R.id.note)).setText(bluePrint.getNote());
        ((TextView) findViewById(R.id.plot_total)).setText(bluePrint.getPlot_total());
        ((TextView) findViewById(R.id.plot_rest)).setText(bluePrint.getPlot_rest());
        ((TextView) findViewById(R.id.plot_sold)).setText(bluePrint.getPlot_sold());
        try {
            if (Integer.parseInt(bluePrint.getPlot_rest()) == 0) {
                findViewById(R.id.available_plots).setVisibility(View.GONE);
            }
        } catch (Exception e) {

        }


        if (!(bluePrint.getImage().isEmpty())) {
            System.out.println("image=" + bluePrint.getImage());
            ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                    .writeDebugLogs()
                    .build();
            imageLoader.init(config);
            imageLoader.displayImage(bluePrint.getImage(), (ImageView) findViewById(R.id.image), options);
            // popupImage((ImageView) findViewById(R.id.image),bluePrint.getImage());

            findViewById(R.id.image).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        openDetailsPopup(bluePrint.getImage(), bluePrint.getName());
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                }
            });

        }
        // setContentView(webView);

    }
    public void getDetailsFromLocal() {
        BluePrintDetailsReaderContract.FeedReaderDbHelper dbHelper = new BluePrintDetailsReaderContract.FeedReaderDbHelper(getApplicationContext());

        SQLiteDatabase db = dbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                BluePrintDetailsReaderContract.FeedEntry.GId,
                BluePrintDetailsReaderContract.FeedEntry.type_id,
                BluePrintDetailsReaderContract.FeedEntry.plan_id,
                BluePrintDetailsReaderContract.FeedEntry.text
        };


// How you want the results sorted in the resulting Cursor
        String sortOrder =
                BluePrintDetailsReaderContract.FeedEntry.GId + " DESC";

        String selection = BluePrintDetailsReaderContract.FeedEntry.plan_id + " = ? ";
        String[] selectionArgs = {plan_id};

        Cursor cursor = db.query(
                BluePrintDetailsReaderContract.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        List<BluePrintDetail> details = new ArrayList<>();

        while (cursor.moveToNext()) {
            BluePrintDetail detail = new BluePrintDetail();
            String type_id = cursor.getString(
                    cursor.getColumnIndexOrThrow(BluePrintDetailsReaderContract.FeedEntry.type_id));
            String text = cursor.getString(
                    cursor.getColumnIndexOrThrow(BluePrintDetailsReaderContract.FeedEntry.text));
            detail.setType_id(String.valueOf(type_id));
            detail.setText(text);
            System.out.println(type_id);
            details.add(detail);
        }
        cursor.close();
        getData(details);
    }

    private void getData(List<BluePrintDetail> details) {
        ArrayList<BluePrint_information> advantages = new ArrayList<>();
        ArrayList<BluePrint_information> location = new ArrayList<>();
        ArrayList<BluePrint_information> spaces = new ArrayList<>();
        for (int y = 0; y < details.size(); y++) {
            BluePrint_information bluePrint_information = new BluePrint_information();
            switch (Integer.parseInt(details.get(y).getType_id())) {
                case 3: {
                    bluePrint_information.setInfo(details.get(y).getText());
                    location.add(bluePrint_information);
                }
                break;
                case 2: {
                    bluePrint_information.setInfo(details.get(y).getText());
                    advantages.add(bluePrint_information);
                }
                break;
                case 4: {
                    bluePrint_information.setInfo(details.get(y).getText());
                    spaces.add(bluePrint_information);
                }
                break;
            }
        }
        setLocationListView(location);
        setSpacesListView(spaces);
        setAdvantageListView(advantages);
    }

    private void setLocationListView(ArrayList<BluePrint_information> arrayList) {
        final BluePrint_profile.Details_list_adapter adapter = new BluePrint_profile.Details_list_adapter(arrayList, getApplicationContext(), R.layout.list_items);
        ((ListView) (findViewById(R.id.blueprint_details_include).findViewById(R.id.location_list))).setAdapter(adapter);
    }

    private void setSpacesListView(ArrayList<BluePrint_information> arrayList) {
        final BluePrint_profile.Details_list_adapter adapter = new BluePrint_profile.Details_list_adapter(arrayList, getApplicationContext(), R.layout.list_items);
        ((ListView) (findViewById(R.id.blueprint_details_include).findViewById(R.id.spaces_list))).setAdapter(adapter);
    }

    private void setAdvantageListView(ArrayList<BluePrint_information> arrayList) {
        final BluePrint_profile.Details_list_adapter adapter = new BluePrint_profile.Details_list_adapter(arrayList, getApplicationContext(), R.layout.list_items);
        ((ListView) (findViewById(R.id.blueprint_details_include).findViewById(R.id.advantage_list))).setAdapter(adapter);
    }

    private void initGrid(ArrayList sections) {
        System.out.println("LENGTH=" + sections.size());
        int length = sections.size();
        final int columnNo = 4;
        int rowNo = 0;
        if (length % 4 != 0) {
            length = length + 1;
        }
        rowNo = length / 4;
        final GridLayout section_grid = findViewById(R.id.section_grid);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                section_grid.removeAllViewsInLayout();
            }
        });
        plots = new ArrayList<>();
        final int finalRowNo = rowNo;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                section_grid.setColumnCount(columnNo);
                section_grid.setRowCount(finalRowNo);
            }
        });


        for (int x = 0, c = 0, r = 0; x < length; x++, c++) {
            System.out.println("inside");

            if (c == columnNo) {
                c = 0;
                r++;
            }
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View section_card = inflater.inflate(R.layout.plot_card, null);

            final CardView section_cardView = section_card.findViewById(R.id.section_cardView);
            section_cardView.setId(getID("1", x));
            TextView plot_cat = section_cardView.findViewById(R.id.plot_cat);
            plot_cat.setId(getID("3", x));
            TextView plot_no = section_cardView.findViewById(R.id.plot_no);
            plot_no.setId(getID("4", x));
            Plot section = null;
            if (x < sections.size()) {
                section = (Plot) sections.get(x);
                plot_cat.setText(getCatString(section.getCat()));
                plot_no.setText(section.getPlot_no());
                section_cardView.setTag(x);
                plots.add(section);
            } else section_cardView.setVisibility(View.INVISIBLE);
            //icon.setImageURI(Uri.parse(section.getString("img")));

            section_cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openPlotProfile(Integer.parseInt(view.getTag().toString()));
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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // section_grid.setLayoutParams(gridParam);
                    section_grid.addView(section_cardView);
                }
            });
        }
    }
    private int getID(String id, int number) {
        String s = id + number;
        return Integer.parseInt(s);
    }
    public void getPlotsFromLocal() {
        PlotsReaderContract.FeedReaderDbHelper dbHelper = new PlotsReaderContract.FeedReaderDbHelper(getApplicationContext());

        SQLiteDatabase db = dbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                PlotsReaderContract.FeedEntry.GId,
                PlotsReaderContract.FeedEntry.plot_no,
                PlotsReaderContract.FeedEntry.plot_area,
                PlotsReaderContract.FeedEntry.cat,
                PlotsReaderContract.FeedEntry.meter_price,
                PlotsReaderContract.FeedEntry.price_sdg,
                PlotsReaderContract.FeedEntry.blueprint_id,
                PlotsReaderContract.FeedEntry.price_usd,
                PlotsReaderContract.FeedEntry.status,
                PlotsReaderContract.FeedEntry.status_id,
                PlotsReaderContract.FeedEntry.status_description,
                PlotsReaderContract.FeedEntry.allowOffer,
                PlotsReaderContract.FeedEntry.basic,
                PlotsReaderContract.FeedEntry.total,
                PlotsReaderContract.FeedEntry.extra_quantity,
                PlotsReaderContract.FeedEntry.extra_amount
        };


// How you want the results sorted in the resulting Cursor
        String sortOrder =
                PlotsReaderContract.FeedEntry._ID + " ASC";

        String selection = PlotsReaderContract.FeedEntry.blueprint_id + " = ? ";
        String[] selectionArgs = {plan_id};

        Cursor cursor = db.query(
                PlotsReaderContract.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        ArrayList<Plot> plots = new ArrayList<>();

        while (cursor.moveToNext()) {
            Plot plot = new Plot();
            String id = cursor.getString(
                    cursor.getColumnIndexOrThrow(PlotsReaderContract.FeedEntry.GId));
            String plot_no = cursor.getString(
                    cursor.getColumnIndexOrThrow(PlotsReaderContract.FeedEntry.plot_no));
            String plot_area = cursor.getString(
                    cursor.getColumnIndexOrThrow(PlotsReaderContract.FeedEntry.plot_area));
            String cat = cursor.getString(
                    cursor.getColumnIndexOrThrow(PlotsReaderContract.FeedEntry.cat));
            String meter_price = cursor.getString(
                    cursor.getColumnIndexOrThrow(PlotsReaderContract.FeedEntry.meter_price));
            String price_sdg = cursor.getString(
                    cursor.getColumnIndexOrThrow(PlotsReaderContract.FeedEntry.price_sdg));
            String price_usd = cursor.getString(
                    cursor.getColumnIndexOrThrow(PlotsReaderContract.FeedEntry.price_usd));
            String status = cursor.getString(
                    cursor.getColumnIndexOrThrow(PlotsReaderContract.FeedEntry.status));
            String blueprint_id = cursor.getString(
                    cursor.getColumnIndexOrThrow(PlotsReaderContract.FeedEntry.blueprint_id));
            String allowOffer = cursor.getString(
                    cursor.getColumnIndexOrThrow(PlotsReaderContract.FeedEntry.allowOffer));
            String status_id = cursor.getString(
                    cursor.getColumnIndexOrThrow(PlotsReaderContract.FeedEntry.status_id));
            String status_description = cursor.getString(
                    cursor.getColumnIndexOrThrow(PlotsReaderContract.FeedEntry.status_description));
            String basic = cursor.getString(
                    cursor.getColumnIndexOrThrow(PlotsReaderContract.FeedEntry.basic));
            String total = cursor.getString(
                    cursor.getColumnIndexOrThrow(PlotsReaderContract.FeedEntry.total));
            String extra_amount = cursor.getString(
                    cursor.getColumnIndexOrThrow(PlotsReaderContract.FeedEntry.extra_amount));
            String extra_quantity = cursor.getString(
                    cursor.getColumnIndexOrThrow(PlotsReaderContract.FeedEntry.extra_quantity));

            plot.setId(id);
            plot.setCat(cat);
            plot.setPlot_area(plot_area);
            plot.setPlot_no(plot_no);
            plot.setMeter_price(meter_price);
            plot.setPrice_sdg(price_sdg);
            plot.setPrice_usd(price_usd);
            plot.setStatus(status);
            plot.setBlueprint_id(blueprint_id);
            plot.setAllowOffer(allowOffer);
            plot.setStatus_description(status_description);
            plot.setStatus_id(status_id);
            plot.setBasic(basic);
            plot.setTotal(total);
            plot.setExtra_amount(extra_amount);
            plot.setExtra_quantity(extra_quantity);

            plots.add(plot);
        }
        cursor.close();
        initGrid(plots);
    }

    private String getCatString(String cat) {
        if (cat.equalsIgnoreCase("A") || cat.equalsIgnoreCase("A+")) {
            return getResources().getString(R.string.special_plot);
        } else return getResources().getString(R.string.normal_plot);
    }

    private void openPlotProfile(int id) {
        Intent intent = new Intent(getApplicationContext(), PlotProfile.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("plot", plots.get(id));
        intent.putExtra("bundle", bundle);
        startActivity(intent);
    }

    public class Details_list_adapter extends ArrayAdapter<BluePrint_information> implements View.OnClickListener {
        private final Context mContext;
        private int lastPosition = -1;
        private BluePrint_profile.Details_list_adapter.ViewHolder viewHolder;


        public Details_list_adapter(ArrayList<BluePrint_information> data, Context context, int layout) {
            super(context, layout, data);
            //  System.out.println("DATAMODELS INSIDE ADAPTER=" + data.size());
            this.mContext = context;

        }

        @SuppressLint("SetTextI18n")
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            final BluePrint_information dataModel = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            // view lookup cache stored in tag

            final View result;
            if (convertView == null) {


                viewHolder = new BluePrint_profile.Details_list_adapter.ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());

                convertView = inflater.inflate(R.layout.list_items, parent, false);


                viewHolder.info = convertView.findViewById(R.id.expandedListItems);


                result = convertView;


                System.out.println("Position=" + position);
                convertView.setTag(viewHolder);


            } else {
                viewHolder = (BluePrint_profile.Details_list_adapter.ViewHolder) convertView.getTag();
                result = convertView;
            }
            assert dataModel != null;
            viewHolder.info.setText(dataModel.getInfo());


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
            TextView info;
        }
    }

}