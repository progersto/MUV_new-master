package com.muvit.passenger.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.muvit.passenger.Activities.Step2Activity;
import com.muvit.passenger.Adapters.MyTripsAdapter;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Models.DummyItems;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.Models.MyRidesPOJO;
import com.muvit.passenger.Models.Response;
import com.muvit.passenger.Models.RidesItem;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class MyTripsFragment extends Fragment {

    private RecyclerView rvMyTrip;
    private DummyItems dummyItems;
    private ArrayList<RidesItem> arrayList;
    private MyTripsAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    Spinner spinner;

    /*pagination vars start*/
    private boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    int total_records = 0;
    /*pagination vars end*/


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_trips, container, false);
        intiView(rootView);


       // getTripDetails();
        return rootView;
    }

    private void myTripList() {

    }

    private void intiView(View rootView) {
        rvMyTrip = (RecyclerView) rootView.findViewById(R.id.rvMyTrip);
        layoutManager = new LinearLayoutManager(getContext());
        rvMyTrip.setLayoutManager(layoutManager);
        rvMyTrip.setHasFixedSize(true);
        arrayList = new ArrayList<RidesItem>();
        adapter = new MyTripsAdapter(getContext(), arrayList);
        rvMyTrip.setAdapter(adapter);


        spinner = (Spinner) rootView.findViewById(R.id.spinner);
        List<String> filters = new ArrayList<String>();
        filters.add("All");
        filters.add("Completed");
        filters.add("Pending");
        filters.add("Waiting");
        filters.add("Started");
        filters.add("Rejected");
        filters.add("Expired");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), R.layout.custom_yellow_spinner, filters);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        // Spinner click listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (new ConnectionCheck().isNetworkConnected(getActivity())) {
                    getTripDetails(true,"0");
                } else {
                    getOfflineTripDetails();
                }

                //getOfflineTripDetails();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        rvMyTrip.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) //check for scroll down
                {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisiblesItems = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            loading = false;
                            if (arrayList.size() < total_records) {
                                if (new ConnectionCheck().isNetworkConnected(getActivity())) {
                                    getTripDetails(false,String.valueOf(arrayList.size()));
                                } else {
                                    getOfflineTripDetails();
                                }

                            }
                            //Do pagination.. i.e. fetch new data
                        }
                    }
                }
            }
        });

    }

    public void getTripDetails(final boolean clearFlag, String lastCount) {
        String tripStatus = "";
        switch (spinner.getSelectedItemPosition()) {
            case 0:
                tripStatus = "";
                break;
            case 1:
                tripStatus = "c";
                break;
            case 2:
                tripStatus = "p";
                break;
            case 3:
                tripStatus = "w";
                break;
            case 4:
                tripStatus = "s";
                break;
            case 5:
                tripStatus = "r";
                break;
            case 6:
                tripStatus = "e";
                break;
        }
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.getusertriplist;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        params.add("tripstatus");
        params.add("lastCount");
        params.add("totalRecords");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(getActivity()).readInt("uId")));
        values.add(tripStatus);
        values.add(lastCount);
        values.add("10");
        new ParseJSON(getActivity(), url, params, values, MyRidesPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    MyRidesPOJO resultObj = (MyRidesPOJO) obj;
                    total_records = resultObj.getTotalRecords();
                    if (clearFlag) {
                        arrayList.clear();
                    }

                    arrayList.addAll(resultObj.getRides());
                    adapter.notifyDataSetChanged();
                } else {
                    arrayList.clear();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                }
                loading = true;

            }
        });
    }


    public void getOfflineTripDetails() {
        spinner.setVisibility(View.GONE);
        try {
            File f = new File(getActivity().getFilesDir().getPath() + "/" + "offline.json");
            //check whether file exists
            FileInputStream is = new FileInputStream(f);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String s = new String(buffer);
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("M/d/yy hh:mm a"); //Format of our JSON dates
            Gson gson = gsonBuilder.create();
            Response item = new Response();
            item = gson.fromJson(s,Response.class);
            arrayList.clear();
            arrayList.addAll(item.getOfflineDataModel().get(0).getGetusertriplist().getDataAns());
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("TAG", "Error in Reading: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity() instanceof Step2Activity){
            ((Step2Activity)getActivity()).showToolbar();
            ((Step2Activity)getActivity()).showToolbarTiltle("My Trips");
            ((Step2Activity)getActivity()).turnActionBarIconWhite();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection")){
                if (event.getMessage().equalsIgnoreCase("connected")) {
                    getTripDetails(true,"0");
                }else if (event.getMessage().equalsIgnoreCase("disconnected")){
                    getOfflineTripDetails();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
