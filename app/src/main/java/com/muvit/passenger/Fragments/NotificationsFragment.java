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
import android.widget.Toast;

import com.muvit.passenger.Activities.Step2Activity;
import com.muvit.passenger.Adapters.NotificationsAdapter;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.Models.Notification;
import com.muvit.passenger.Models.NotificationPOJO;
import com.muvit.passenger.Models.Response;
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
import java.io.FileWriter;
import java.util.ArrayList;

public class NotificationsFragment extends Fragment {

    private RecyclerView rvNotifications;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Notification> arrayList;
    private NotificationsAdapter adapter;

    /*pagination vars start*/
    private boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    int page = 1;
    int total_records = 0;
    /*pagination vars end*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notifications, container, false);
        intiView(rootView);

        if (new ConnectionCheck().isNetworkConnected(getActivity())) {
            getNotifications(true,"0");
        } else {
            getOfflineNotifications();
        }
        return rootView;
    }

    private void intiView(View rootView) {
        rvNotifications = (RecyclerView) rootView.findViewById(R.id.rvNotifications);
        layoutManager = new LinearLayoutManager(getContext());
        rvNotifications.setLayoutManager(layoutManager);
        rvNotifications.setHasFixedSize(true);
        arrayList = new ArrayList<Notification>();
        adapter = new NotificationsAdapter(getContext(), arrayList);
        rvNotifications.setAdapter(adapter);

        rvNotifications.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                                getNotifications(false,String.valueOf(arrayList.size()));
                            }
                            //Do pagination.. i.e. fetch new data
                        }
                    }
                }
            }
        });
    }


    public void getNotifications(final boolean clearFlag, String lastCount){
        String url= WebServiceUrl.ServiceUrl+WebServiceUrl.getnotification;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        params.add("lastCount");
        params.add("totalRecords");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(getActivity()).readInt("uId")));
        values.add(lastCount);
        values.add("10");
        new ParseJSON(getActivity(), url, params, values, NotificationPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    NotificationPOJO resultObj = (NotificationPOJO) obj;
                    total_records = resultObj.getTotalRecords();
                    if (clearFlag) {
                        arrayList.clear();
                    }
                    arrayList.addAll(resultObj.getNotifications());
                    adapter.notifyDataSetChanged();

                }else {
                    Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                }
                loading = true;

            }
        });
    }

    @Override
    public void onResume() {
        if(getActivity() instanceof Step2Activity){
            ((Step2Activity)getActivity()).showToolbar();
            ((Step2Activity)getActivity()).showToolbarTiltle("Notificatoins");
            ((Step2Activity)getActivity()).turnActionBarIconWhite();
        }
        super.onResume();
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
                    getNotifications(true,"0");
                }else if (event.getMessage().equalsIgnoreCase("disconnected")){
                    getOfflineNotifications();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getOfflineNotifications() {
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
            arrayList.addAll(item.getOfflineDataModel().get(0).getGetnotification().getDataAns());
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("TAG", "Error in Reading: " + e.getLocalizedMessage());
        }
    }
}
