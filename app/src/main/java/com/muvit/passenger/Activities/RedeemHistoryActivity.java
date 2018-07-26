package com.muvit.passenger.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.muvit.passenger.Adapters.RedeemHistoryAdapter;
import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.Models.RedeemHistoryItem;
import com.muvit.passenger.Models.RedeemHistoryPOJO;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;


public class RedeemHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<RedeemHistoryItem> arrayList;
    private RedeemHistoryAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private int lastCount = 0;
    private Toolbar toolbar;
    private TextView txtTitle;

    /*pagination vars start*/
    private boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    int total_records = 0;
    /*pagination vars end*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem_history);

        initViews();
        getHistory(true,"0");

    }

    private void initViews() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        arrayList = new ArrayList<RedeemHistoryItem>();
        adapter = new RedeemHistoryAdapter(RedeemHistoryActivity.this, arrayList);
        recyclerView.setAdapter(adapter);
        setupToolbar();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) //check for scroll down
                {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisiblesItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            loading = false;
                            if (arrayList.size() < total_records) {
                                getHistory(false, String.valueOf(arrayList.size()));
                            }
                            //Do pagination.. i.e. fetch new data
                        }
                    }
                }
            }
        });
    }

    public void getHistory(final boolean clearFlag, String lastCount) {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.getredeemhistory;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        params.add("lastCount");
        params.add("totalRecords");

        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(this).readInt("uId")));
        values.add(lastCount);
        values.add(String.valueOf("10"));

        new ParseJSON(this, url, params, values, RedeemHistoryPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    if (clearFlag) {
                        arrayList.clear();
                    }
                    RedeemHistoryPOJO redeemHistory = (RedeemHistoryPOJO) obj;
                    arrayList.addAll(redeemHistory.getRedeemHistory());
                    total_records = redeemHistory.getTotalRecords();
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(RedeemHistoryActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.redeem_history_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection")) {
                if (event.getMessage().equalsIgnoreCase("disconnected")) {
                    if (!(new ConnectionCheck().isNetworkConnected(RedeemHistoryActivity.this))) {
                        Log.e("RideDetailActivity", "disconnected");
                        if (!ApplicationController.isOnline) {
                            if (PrefsUtil.isInternetConnectedShowing) {
                                if (PrefsUtil.dialogInternetConnected != null) {
                                    PrefsUtil.dialogInternetConnected.dismiss();
                                    PrefsUtil.isInternetConnectedShowing = false;
                                }
                            }
                            final Dialog d = new Dialog(RedeemHistoryActivity.this,
                                    android.R.style.Theme_Light_NoTitleBar);
                            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            d.setContentView(R.layout.dialog_no_internet_with_finish);
                            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                            lp.copyFrom(d.getWindow().getAttributes());
                            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                            d.getWindow().setAttributes(lp);
                            TextView txtRetry = (TextView) d.findViewById(R.id.txtRetry);
                            txtRetry.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    PrefsUtil.isNoInternetShowing = false;
                                    d.dismiss();
                                    System.exit(0);
                                }
                            });
                            d.setCancelable(true);
                            d.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    PrefsUtil.isLocationNotFoundShowing = false;
                                }
                            });
                            PrefsUtil.isNoInternetShowing = true;
                            PrefsUtil.dialogNoInternet = d;
                            d.show();
                            ApplicationController.isOnline = false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
}
