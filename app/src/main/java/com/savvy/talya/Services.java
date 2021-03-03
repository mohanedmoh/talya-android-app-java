package com.savvy.talya;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.savvy.talya.Adapters.TabAdapter;

import java.util.Objects;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Services#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Services extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private View view;
    SharedPreferences shared;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Services() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Services.
     */
    // TODO: Rename and change types and number of parameters
    public static Services newInstance(String param1, String param2) {
        Services fragment = new Services();
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

    @SuppressLint("SetTextI18n")
    private void init() {
        shared = getActivity().getSharedPreferences("com.savvy.talya", Context.MODE_PRIVATE);

        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                changeTitle(position);
            }

            @Override
            public void onPageSelected(int position) {
                changeTitle(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout = view.findViewById(R.id.tabLayout);
        adapter = new TabAdapter(getFragmentManager());
        adapter.addFragment(new SubServices(), getResources().getString(R.string.services));
        adapter.addFragment(new BluePrints(), getResources().getString(R.string.blueprint));
        adapter.addFragment(new Offers(), getResources().getString(R.string.offers));
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        try {
            Objects.requireNonNull(getActivity()).getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        TabLayout.Tab tab = tabLayout.getTabAt(2);
        assert tab != null;
        tab.select();
        // ((TextView)view.findViewById(R.id.welcome_name)).setText(getResources().getString(R.string.offers_title));
    }

    private void changeTitle(int position) {
        switch (position) {
            case 0: {
                ((TextView) view.findViewById(R.id.welcome_name)).setText(getResources().getString(R.string.services_title));
            }
            break;
            case 1: {
                ((TextView) view.findViewById(R.id.welcome_name)).setText(getResources().getString(R.string.blueprint_title));
            }
            break;
            case 2: {
                ((TextView) view.findViewById(R.id.welcome_name)).setText(getResources().getString(R.string.offers_title));
            }
            break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_services, container, false);
        init();
        return view;
    }
}