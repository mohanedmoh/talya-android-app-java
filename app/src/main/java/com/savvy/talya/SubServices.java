package com.savvy.talya;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.savvy.talya.Models.Service;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SubServices#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SubServices extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    View view;
    SharedPreferences shared;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    String[] names, description;
    Drawable[] icons;

    public SubServices() {
        // Required empty public constructor
    }

    private void init() {
        shared = getActivity().getSharedPreferences("com.savvy.talya", Context.MODE_PRIVATE);
        names = new String[]{getResources().getString(R.string.register_service), getResources().getString(R.string.upgrade), getResources().getString(R.string.republish)};
        description = new String[]{getResources().getString(R.string.register_desc), getResources().getString(R.string.upgrade_desc), getResources().getString(R.string.republish_desc)};
        icons = new Drawable[]{getResources().getDrawable(R.drawable.registration), getResources().getDrawable(R.drawable.upgrade), getResources().getDrawable(R.drawable.offers)};
        createList();
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SubServices.
     */
    // TODO: Rename and change types and number of parameters
    public static SubServices newInstance(String param1, String param2) {
        SubServices fragment = new SubServices();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_sub_services, container, false);
        init();
        return view;
    }

    private void createList() {
        ArrayList<Service> services = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            Service service = new Service();
            service.setName(names[i]);
            service.setDescription(description[i]);
            service.setIcon(icons[i]);
            services.add(service);
        }
        setList(services);
    }

    public void setList(final ArrayList<Service> services) {
        final ListView listView = view.findViewById(R.id.services_list);

        final SubServices.service_list_adapter adapter = new SubServices.service_list_adapter(services, getContext());

        try {

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Service dataModel = services.get(position);
                    if (checkIfTalyaUser()) {
                        if (position == 0) {
                            Intent intent = new Intent(getContext(), registeration_form.class);
                            startActivity(intent);
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), getResources().getString(R.string.from_profile), Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), getResources().getString(R.string.only_talya_customers), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    listView.setAdapter(adapter);
                }
            });
        } catch (Exception e) {

        }


    }

    public boolean checkIfTalyaUser() {
        return shared.contains("client_id") || shared.contains("agent_id");
    }

    public class service_list_adapter extends ArrayAdapter<Service> implements View.OnClickListener {
        private final Context mContext;
        private int lastPosition = -1;
        private SubServices.service_list_adapter.ViewHolder viewHolder;


        public service_list_adapter(ArrayList<Service> data, Context context) {
            super(context, R.layout.service_card, data);
            this.mContext = context;

        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            final Service dataModel = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            // view lookup cache stored in tag

            final View result;
            if (convertView == null) {


                viewHolder = new SubServices.service_list_adapter.ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.service_card, parent, false);

                viewHolder.name = convertView.findViewById(R.id.name);
                viewHolder.description = convertView.findViewById(R.id.description);
                viewHolder.icon = convertView.findViewById(R.id.icon);


                result = convertView;


                System.out.println("Position=" + position);
                convertView.setTag(viewHolder);


            } else {
                viewHolder = (SubServices.service_list_adapter.ViewHolder) convertView.getTag();
                result = convertView;
            }
            assert dataModel != null;
            // viewHolder.icon.setText(dataModel.getQuestion());
            viewHolder.name.setText(dataModel.getName());
            viewHolder.description.setText(dataModel.getDescription());
            viewHolder.icon.setImageDrawable(dataModel.getIcon());

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
            TextView name, description;
            ImageView icon;


        }


    }
}