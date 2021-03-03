package com.savvy.talya;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.savvy.talya.Models.Incentives;
import com.savvy.talya.Models.Plot;
import com.savvy.talya.Models.PlotPayament;
import com.savvy.talya.Network.Iokihttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Profile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Profile extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    double total_deserved = 0;

    View view, client_profile, agent_profile;
    Iokihttp iokihttp;
    SharedPreferences shared;

    private void init() {
        view.findViewById(R.id.signout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signout();
            }
        });
        getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        shared = getActivity().getSharedPreferences("com.savvy.talya", Context.MODE_PRIVATE);
        iokihttp = new Iokihttp();
        client_profile = view.findViewById(R.id.client_profile);
        agent_profile = view.findViewById(R.id.agent_profile);
        changeLoadingText();
        showIfExist();
        getProfile();
        setInfo();
        ScrollToBottom();
    }

    private void changeLoadingText() {
        if (shared.contains("client_id")) {
            ((TextView) view.findViewById(R.id.loading_text)).setText(getResources().getString(R.string.client_loading));
        }
        if (shared.contains("agent_id")) {
            ((TextView) view.findViewById(R.id.loading_text)).setText(getResources().getString(R.string.agent_loading));
        }
    }

    private void ScrollToBottom() {
        final NestedScrollView scrollView = view.findViewById(R.id.layout);
        view.findViewById(R.id.bottom).setOnClickListener(new View.OnClickListener() {
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

    public void showLoading() {
        final View main = view.findViewById(R.id.layout);
        final View loading = view.findViewById(R.id.loading);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.setClickable(false);

                main.setVisibility(View.GONE);

                loading.setVisibility(View.VISIBLE);
            }
        });
    }

    private void signout() {
        shared.edit().remove("user_id").apply();
        shared.edit().remove("client_id").apply();
        shared.edit().remove("agent_id").apply();
        shared.edit().putBoolean("login", false).apply();
        try {
            Objects.requireNonNull(getContext()).deleteDatabase("FeedReader.db");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(getContext(), Login.class);
        startActivity(intent);
        Objects.requireNonNull(getActivity()).finish();

    }

    public void hideLoading() {
        final View main = view.findViewById(R.id.layout);
        final View loading = view.findViewById(R.id.loading);
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    main.setClickable(true);

                    main.setVisibility(View.VISIBLE);

                    loading.setVisibility(View.GONE);
                    view.findViewById(R.id.bottom).setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception e) {

        }
    }

    public Profile() {
        // Required empty public constructor
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Profile.
     */
    // TODO: Rename and change types and number of parameters
    public static Profile newInstance(String param1, String param2) {
        Profile fragment = new Profile();
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
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        init();
        return view;
    }
    private void getProfile() {
        JSONObject json = new JSONObject();
        JSONObject subJSON = new JSONObject();
        try {
            subJSON.put("client_id", shared.getString("client_id", ""));
            subJSON.put("agent_id", shared.getString("agent_id", ""));
            subJSON.put("user_id", shared.getString("user_id", ""));
            json.put("data", subJSON);
            System.out.println("JSON=" + json.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (iokihttp.isNetworkConnected(getContext())) {
            showLoading();

            iokihttp.post(getString(R.string.url) + "getProfile", shared.getString("token", ""), json.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    hideLoading();
                    System.out.println("FAIL");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), getString(R.string.server_error), Toast.LENGTH_SHORT).show();
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
                                    final JSONObject subresJSON = resJSON.getJSONObject("data");
                                    if (subresJSON.has("Plots")) {
                                        createPlotList(subresJSON.getJSONArray("Plots"));
                                    }
                                    if (subresJSON.has("Customers")) {
                                        createClientList(subresJSON.getJSONArray("Customers"));
                                    } else {
                                        agent_profile.findViewById(R.id.agent_linear).setVisibility(View.GONE);
                                    }
                                    if (subresJSON.has("Sales_Total")) {
                                        makeSalles_no_bar(subresJSON.getJSONArray("Sales_Total"));
                                    }
                                    if (subresJSON.has("Sales_Size")) {
                                        makeSalles_size_bar(subresJSON.getJSONArray("Sales_Size"));
                                    }
                                    if (subresJSON.has("Incentives")) {
                                        createIncentivesList(subresJSON.getJSONArray("Incentives"));
                                    } else {
                                        agent_profile.findViewById(R.id.incentive_linear).setVisibility(View.GONE);
                                    }

                                }
                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("ExceptionEEE=" + e);
                        }
                        System.out.println("Response=" + responseStr);

                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

            });
        } else {
        }
    }
    private void createPlotList(JSONArray plots) throws JSONException {
        ArrayList<Plot> plotsArray = new ArrayList<>();
        for (int x = 0; x < plots.length(); x++) {
            Plot plot = new Plot();
            JSONObject jsonObject = plots.getJSONObject(x);

            plot.setId(jsonObject.getString("plot_id"));
            plot.setCat(jsonObject.getString("cat"));
            plot.setPlot_area(jsonObject.getString("plot_area"));
            plot.setPlot_no(jsonObject.getString("plot_no"));
            plot.setMeter_price(jsonObject.getString("meter_price"));
            plot.setPrice_sdg(jsonObject.getString("price_sdg"));
            plot.setPrice_usd(jsonObject.getString("price_usd"));
            plot.setStatus(jsonObject.getString("plot_status"));
            plot.setStatus_id(jsonObject.getString("plot_status_id"));
            plot.setStatus_description(jsonObject.getString("plot_status_desc"));
            plot.setAllowOffer("0");
            plot.setIsRegistered(jsonObject.getString("isRegistered"));
            plot.setBlueprint_id(jsonObject.getString("plan_id"));
            plot.setBlueprint_name(jsonObject.getString("plan_name"));
            plot.setCurrent_meter_price(jsonObject.getString("current_meter_price"));
            plot.setCurrent_price(jsonObject.getString("current_price"));
            plot.setEmp_action(jsonObject.getString("emp_action"));
            plot.setEmp_follower(jsonObject.getString("emp_follower"));

            JSONArray jsonArray = plots.getJSONObject(x).getJSONArray("payments");
            System.out.println("SIZE OF JSON IN PROFILE=" + jsonArray.length());
            plot.setInvestments(jsonObject.getString("Investment"));
            ArrayList<PlotPayament> plotPayaments = new ArrayList<>();
            System.out.println("PLOT_ID" + plot.getId());
            for (int y = 0; y < jsonArray.length(); y++) {
                PlotPayament plotPayament = new PlotPayament();
                plotPayament.setPayment_id(jsonArray.getJSONObject(y).getString("payment_id"));
                plotPayament.setPayment_no(jsonArray.getJSONObject(y).getString("payment_no"));
                plotPayament.setPayment_date(jsonArray.getJSONObject(y).getString("payment_date"));
                plotPayament.setOp_id(jsonArray.getJSONObject(y).getString("op_id"));
                plotPayament.setPayment_amount(jsonArray.getJSONObject(y).getString("payment_ammount"));
                plotPayament.setChaque_no(jsonArray.getJSONObject(y).getString("chaque_no"));
                plotPayament.setChaque_bank(jsonArray.getJSONObject(y).getString("chaque_bank"));
                plotPayament.setPaid(jsonArray.getJSONObject(y).getString("paid"));
                plotPayament.setRecipt(jsonArray.getJSONObject(y).getString("recipt"));
                plotPayament.setRecipt_date(jsonArray.getJSONObject(y).getString("recipt_date"));
                plotPayament.setNotification(jsonArray.getJSONObject(y).getString("notification"));
                plotPayament.setReason(jsonArray.getJSONObject(y).getString("reason"));
                plotPayament.setCanceled(jsonArray.getJSONObject(y).getString("Canceled"));
                plotPayament.setOp_type(jsonArray.getJSONObject(y).getString("op_type"));
                plotPayament.setRateID(jsonArray.getJSONObject(y).getString("rateID"));
                plotPayaments.add(plotPayament);
            }
            plot.setPlotPayament(plotPayaments);
            plotsArray.add(plot);
        }
        setList(plotsArray);
    }
    private void createClientList(JSONArray plots) throws JSONException {
        ArrayList<Plot> plotsArray = new ArrayList<>();
        for (int x = 0; x < plots.length(); x++) {
            Plot plot = new Plot();
            JSONObject jsonObject = plots.getJSONObject(x);
            System.out.println("index=" + x + "plot_id" + jsonObject.getString("plot_id"));

            plot.setId(jsonObject.getString("plot_id"));
            plot.setCat(jsonObject.getString("cat"));
            plot.setPlot_area(jsonObject.getString("plot_area"));
            plot.setPlot_no(jsonObject.getString("plot_no"));
            plot.setMeter_price(jsonObject.getString("meter_price"));
            plot.setPrice_sdg(jsonObject.getString("price_sdg"));
            plot.setPrice_usd(jsonObject.getString("price_usd"));
            plot.setStatus(jsonObject.getString("plot_status"));
            plot.setStatus_id(jsonObject.getString("plot_status_id"));
            plot.setStatus_description(jsonObject.getString("plot_status_desc"));
            //plot.setAllowOffer(jsonObject.getString("allowOffer"));
            plot.setIsRegistered(jsonObject.getString("isRegistered"));
            plot.setBlueprint_id(jsonObject.getString("plan_id"));
            plot.setBlueprint_name(jsonObject.getString("plan_name"));
            plot.setClient_id(jsonObject.getString("client_id"));
            plot.setClient_name(jsonObject.getString("client_name"));
            plot.setCurrent_meter_price(jsonObject.getString("current_meter_price"));
            plot.setCurrent_price(jsonObject.getString("current_price"));
            plot.setEmp_action(jsonObject.getString("emp_action"));
            plot.setEmp_follower(jsonObject.getString("emp_follower"));
            plotsArray.add(plot);
        }
        setClientList(plotsArray);
    }
    public void setClientList(final ArrayList<Plot> plots) {
        System.out.println("11");
        final ListView listView = agent_profile.findViewById(R.id.agent_list);
        System.out.println("22++++" + plots.size());
        if (plots.size() > 0) {
            final Profile.client_list_adapter adapter = new Profile.client_list_adapter(plots, getContext());

            try {
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Plot dataModel = plots.get(position);
                        openPlotProfileAsAgent(dataModel);
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    agent_profile.findViewById(R.id.agent_list).setVisibility(View.GONE);
                    agent_profile.findViewById(R.id.agent_list_no_data).setVisibility(View.VISIBLE);
                }
            });

        }

    }

    private void createIncentivesList(JSONArray incentives) throws JSONException {
        ArrayList<Incentives> incentivesArray = new ArrayList<>();
        total_deserved = 0;

        for (int x = 0; x < incentives.length(); x++) {
            Incentives incentive = new Incentives();
            JSONObject jsonObject = incentives.getJSONObject(x);

            incentive.setOp_price(jsonObject.getString("op_price"));
            incentive.setPlot_no(getResources().getString(R.string.plot_no) + " " + jsonObject.getString("plot_no"));
            incentive.setPlot_name(jsonObject.getString("plan_name"));
            incentive.setTotal(jsonObject.getString("Total"));
            incentive.setDeserved(jsonObject.getString("Deserved"));
            try {
                total_deserved += Double.parseDouble(jsonObject.getString("Deserved"));
            } catch (Exception e) {
                System.out.println("Ex=" + e);
            }
            incentive.setExtra_quantity(jsonObject.getString("ExtraQuantity"));
            incentive.setExtra_amount(jsonObject.getString("ExtraAmount"));
            incentive.setBasic(jsonObject.getString("Basic"));
            incentivesArray.add(incentive);
        }
        ((TextView) (agent_profile.findViewById(R.id.total_deserved))).setText(total_deserved + " " + getResources().getString(R.string.sdg));
        setIncentivesList(incentivesArray);
    }

    public void setIncentivesList(final ArrayList<Incentives> incentives) {
        final ListView listView = agent_profile.findViewById(R.id.incentives_list);
        if (incentives.size() > 0) {
            final Profile.incentives_list_adapter adapter = new Profile.incentives_list_adapter(incentives, getContext());

            try {
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Incentives dataModel = incentives.get(position);
                        //openPlotProfileAsAgent(dataModel);
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    agent_profile.findViewById(R.id.incentives_list).setVisibility(View.GONE);
                    agent_profile.findViewById(R.id.incentives_list_no_data).setVisibility(View.VISIBLE);
                }
            });

        }

    }
    public void setList(final ArrayList<Plot> plots) {
        System.out.println("11");
        final ListView listView = client_profile.findViewById(R.id.plot_list);
        System.out.println("22");
        if (plots.size() > 0) {
            final Profile.plot_list_adapter adapter = new Profile.plot_list_adapter(plots, getContext());

            try {
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Plot dataModel = plots.get(position);
                        openPlotProfileAsClient(dataModel);
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
            client_profile.findViewById(R.id.plot_list).setVisibility(View.GONE);
            client_profile.findViewById(R.id.plot_list_no_data).setVisibility(View.VISIBLE);
        }

    }
    private void makeSalles_size_bar(JSONArray table) throws JSONException {
        String title = "";//getResources().getString(R.string.sells_size);
        final BarChart barChart = view.findViewById(R.id.sells_size);
        getActivity().runOnUiThread(new Runnable() {
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

        // data.setValueFormatter(new PercentFormatter());
        set.setColors(getResources().getColor(R.color.bar_yellow_color));
        barChart.setData(data);
        data.setValueTextColor(Color.TRANSPARENT);
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
        //xAxis.setEnabled(false);
        yAxisL.setTextColor(getResources().getColor(R.color.white));
        // yAxisR.setTextColor(getResources().getColor(R.color.white));

        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setTextColor(getResources().getColor(R.color.primary_text));
        // xAxis.setLabelRotationAngle(90F);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(formatter);
        Legend l = barChart.getLegend();
        l.setEnabled(false);
        //   l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        // l.setTextSize(19.0f);
        //l.setTextColor(Color.WHITE);
        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);
        barChart.setTouchEnabled(true);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                barChart.animateY(3000);
            }
        });
        barChart.invalidate();
    }
    private void makeSalles_no_bar(JSONArray table) throws JSONException {
        String title = "";// getResources().getString(R.string.sells_no);
        final BarChart barChart = view.findViewById(R.id.sells_no);
        getActivity().runOnUiThread(new Runnable() {
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
        // data.setValueFormatter(new PercentFormatter());
        set.setColors(getResources().getColor(R.color.bar_gray_color));
        barChart.setData(data);
        data.setValueTextColor(Color.TRANSPARENT);
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

        //xAxis.setEnabled(false);

        yAxisL.setTextColor(getResources().getColor(R.color.white));
        //  yAxisR.setTextColor(getResources().getColor(R.color.white));
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setTextColor(getResources().getColor(R.color.primary_text));
        // xAxis.setLabelRotationAngle(90F);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(formatter);
        Legend l = barChart.getLegend();
        l.setEnabled(false);
        //l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        //l.setTextSize(19.0f);
        //l.setTextColor(Color.WHITE);
        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);
        barChart.setTouchEnabled(true);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                barChart.animateY(3000);
            }
        });

        barChart.invalidate();
    }
    private void showIfExist() {
        if (shared.contains("client_id")) {
            client_profile.setVisibility(View.VISIBLE);
        }
        if (shared.contains("agent_id")) {
            agent_profile.setVisibility(View.VISIBLE);
        }

    }
    private void openPlotProfileAsClient(Plot plot) {
        Intent intent = new Intent(getContext(), PlotProfile.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("plot", plot);
        intent.putExtra("bundle", bundle);
        intent.putExtra("client", true);
        startActivityForResult(intent, 101);
    }
    private void openPlotProfileAsAgent(Plot plot) {
        Intent intent = new Intent(getContext(), PlotProfile.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("plot", plot);
        intent.putExtra("bundle", bundle);
        intent.putExtra("agent", true);

        startActivityForResult(intent, 101);
    }
    private void setInfo() {
        ((TextView) view.findViewById(R.id.name)).setText(shared.getString("user_name", ""));
        ((TextView) view.findViewById(R.id.phone)).setText(shared.getString("phone", ""));

    }
    public class plot_list_adapter extends ArrayAdapter<Plot> implements View.OnClickListener {
        private final Context mContext;
        private int lastPosition = -1;
        private Profile.plot_list_adapter.ViewHolder viewHolder;


        public plot_list_adapter(ArrayList<Plot> data, Context context) {
            super(context, R.layout.plot_profile_card, data);
            this.mContext = context;

        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            final Plot dataModel = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            // view lookup cache stored in tag

            final View result;
            if (convertView == null) {


                viewHolder = new Profile.plot_list_adapter.ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.plot_profile_card, parent, false);

                viewHolder.plot_no = convertView.findViewById(R.id.plot_no);
                // viewHolder.datetime = convertView.findViewById(R.id.date_status);
                viewHolder.blueprint_name = convertView.findViewById(R.id.blueprint_name);
                viewHolder.status = convertView.findViewById(R.id.plot_status);


                result = convertView;


                System.out.println("Position=" + position);
                convertView.setTag(viewHolder);


            } else {
                viewHolder = (Profile.plot_list_adapter.ViewHolder) convertView.getTag();
                result = convertView;
            }
            assert dataModel != null;
            viewHolder.plot_no.setText(dataModel.getPlot_no());
            viewHolder.status.setText(dataModel.getStatus());
            if (dataModel.getStatus().equals("متعسرة")) {
                viewHolder.status.setTextColor(getResources().getColor(R.color.red));
            } else {
                viewHolder.status.setTextColor(getResources().getColor(R.color.primary_text));
            }
            viewHolder.blueprint_name.setText(dataModel.getBlueprint_name());


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
            TextView plot_no, blueprint_name, status;


        }


    }

    public class incentives_list_adapter extends ArrayAdapter<Incentives> implements View.OnClickListener {
        private final Context mContext;
        private int lastPosition = -1;
        private Profile.incentives_list_adapter.ViewHolder viewHolder;


        public incentives_list_adapter(ArrayList<Incentives> data, Context context) {
            super(context, R.layout.incentives_card, data);
            this.mContext = context;

        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            final Incentives dataModel = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            // view lookup cache stored in tag

            final View result;
            if (convertView == null) {


                viewHolder = new Profile.incentives_list_adapter.ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.incentives_card, parent, false);

                viewHolder.plot_no = convertView.findViewById(R.id.plot_no);
                viewHolder.plot_name = convertView.findViewById(R.id.blueprint_name);
                viewHolder.deserved = convertView.findViewById(R.id.deserved);
                viewHolder.total_incentive = convertView.findViewById(R.id.total_incentive);
                viewHolder.extra_quantity = convertView.findViewById(R.id.extra_quantity);
                viewHolder.extra_amount = convertView.findViewById(R.id.extra_amount);
                viewHolder.basic = convertView.findViewById(R.id.basic);

                result = convertView;


                System.out.println("Position=" + position);
                convertView.setTag(viewHolder);


            } else {
                viewHolder = (Profile.incentives_list_adapter.ViewHolder) convertView.getTag();
                result = convertView;
            }
            assert dataModel != null;
            viewHolder.plot_no.setText(dataModel.getPlot_no());
            viewHolder.plot_name.setText(dataModel.getPlot_name());
            viewHolder.deserved.setText(dataModel.getDeserved());
            viewHolder.total_incentive.setText(dataModel.getTotal());
            viewHolder.extra_quantity.setText(dataModel.getExtra_quantity());
            viewHolder.extra_amount.setText(dataModel.getExtra_amount());
            viewHolder.basic.setText(dataModel.getBasic());

            lastPosition = position;

            return convertView;
        }

        @Override
        public void onClick(View v) {

        }

        private class ViewHolder {
            TextView plot_no, plot_name, total, total_incentive, deserved, extra_quantity, extra_amount, basic;


        }


    }

    public class client_list_adapter extends ArrayAdapter<Plot> implements View.OnClickListener {
        private final Context mContext;
        private int lastPosition = -1;
        private Profile.client_list_adapter.ViewHolder viewHolder;


        public client_list_adapter(ArrayList<Plot> data, Context context) {
            super(context, R.layout.client_card, data);
            this.mContext = context;

        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            final Plot dataModel = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            // view lookup cache stored in tag

            final View result;
            if (convertView == null) {


                viewHolder = new Profile.client_list_adapter.ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.client_card, parent, false);

                viewHolder.plot_no = convertView.findViewById(R.id.plot_no);
                // viewHolder.datetime = convertView.findViewById(R.id.date_status);
                viewHolder.blueprint_name = convertView.findViewById(R.id.blueprint_name);
                viewHolder.status = convertView.findViewById(R.id.status);
                viewHolder.client_name = convertView.findViewById(R.id.name);


                result = convertView;


                System.out.println("Position=" + position);
                convertView.setTag(viewHolder);


            } else {
                viewHolder = (Profile.client_list_adapter.ViewHolder) convertView.getTag();
                result = convertView;
            }
            assert dataModel != null;
            viewHolder.plot_no.setText(dataModel.getPlot_no());
            viewHolder.status.setText(dataModel.getStatus());
            viewHolder.blueprint_name.setText(dataModel.getBlueprint_name());
            viewHolder.client_name.setText(dataModel.getClient_name());

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
            TextView plot_no, blueprint_name, status, client_name;


        }


    }
}