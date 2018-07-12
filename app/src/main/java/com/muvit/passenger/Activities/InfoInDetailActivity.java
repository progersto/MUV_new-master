package com.muvit.passenger.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.AsyncTask.GetAsyncTask;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.Models.WebContentPOJO;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class InfoInDetailActivity extends AppCompatActivity {

    private WebView infoWebView;
    private ProgressDialog prd;
    private Uri.Builder builder;
    private GetAsyncTask getAsyncTask;
    private String pId;
    private String pageTitle;
    private Toolbar toolbar;
    private TextView txtTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_in_detail);

       setupToolbar();
        infoWebView = (WebView) findViewById(R.id.infoWebView);

        try {
            pId = String.valueOf(getIntent().getIntExtra("pId",0));
            pageTitle = getIntent().getStringExtra("pageTitle");
        }catch (Exception e){
            e.printStackTrace();
        }

        ConnectionCheck cd = new ConnectionCheck();
        boolean isInternetAvailable = cd.isNetworkConnected(this);
        if (isInternetAvailable) {
        } else {
            try {
                new ConnectionCheck().showconnectiondialog(this).show();
            } catch (Exception e){
                e.printStackTrace();
            }

        }
        getWebData();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                this.finish();
        }
        return (super.onOptionsItemSelected(menuItem));
    }
    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText(getIntent().getStringExtra("pageTitle"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    private void getWebData(){
        String url= WebServiceUrl.ServiceUrl+WebServiceUrl.getcms;
        ArrayList<String> params = new ArrayList<>();
                params.add("userType");
                params.add("cmsId");
                params.add("cmsConstant");
                ArrayList<String> values = new ArrayList<>();
                values.add("u");
                values.add(pId);
                values.add(pageTitle);
                new ParseJSON(this, url, params, values, WebContentPOJO.class, new ParseJSON.OnResultListner() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        if (status) {
                            WebContentPOJO resultObj = (WebContentPOJO) obj;
                            infoWebView.loadData(resultObj.getWebContent().get(0).getHtmlContent(), "text/html", "UTF-8");
                        }else {
                            Toast.makeText(InfoInDetailActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                        }
        
        
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection")) {
                if (event.getMessage().equalsIgnoreCase("disconnected")) {
                    if (!(new ConnectionCheck().isNetworkConnected(InfoInDetailActivity.this))) {
                        Log.e("RideDetailActivity", "disconnected");
                        if (!ApplicationController.isOnline) {
                            if (PrefsUtil.isInternetConnectedShowing) {
                                if (PrefsUtil.dialogInternetConnected != null) {
                                    PrefsUtil.dialogInternetConnected.dismiss();
                                    PrefsUtil.isInternetConnectedShowing = false;
                                }
                            }
                            final Dialog d = new Dialog(InfoInDetailActivity.this,
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