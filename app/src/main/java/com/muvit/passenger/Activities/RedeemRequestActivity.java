package com.muvit.passenger.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Models.CurrentBalancePOJO;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.Models.RedeemRequestPOJO;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.KeyboardUtils;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class RedeemRequestActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView txtTitle;
    private TextView txtAvailableBal;

    private Button btnSubmit;

    private EditText edtEmail, edtAmount, edtDescription;
    double currentBalance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem_request);
        initViews();
        getBalance();
        try {
            edtEmail.setText(PrefsUtil.with(RedeemRequestActivity.this).readString("paypalEmail"));
        }catch (Exception e){
            e.printStackTrace();
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateViews()) {
                    redeemRequest();
                }
            }
        });
    }

    private void initViews() {
        setupToolbar();
        txtAvailableBal = (TextView) findViewById(R.id.txtAvailableBal);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtAmount = (EditText) findViewById(R.id.edtAmount);
        edtDescription = (EditText) findViewById(R.id.edtDescription);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        new KeyboardUtils().setupUI(findViewById(R.id.activity_redeem_request), RedeemRequestActivity.this);
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.redeem_request_title);
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

    public void getBalance() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.getuserbalance;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");

        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(this).readInt("uId")));

        new ParseJSON(this, url, params, values, CurrentBalancePOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    CurrentBalancePOJO resultObj = (CurrentBalancePOJO) obj;
                    txtAvailableBal.setText(getString(R.string.currencySign) + resultObj.getBalance().get(0).getCurrenctBalance());
                    currentBalance = Double.parseDouble(resultObj.getBalance().get(0).getCurrenctBalance());
                } else {
                    Toast.makeText(RedeemRequestActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateViews() {
        boolean result = true;
        if (edtEmail.getText().toString().isEmpty()) {
            edtEmail.setError("Please enter your Email");
            edtEmail.requestFocus();
            result = false;
        }
        if (edtAmount.getText().toString().isEmpty()) {
            edtAmount.setError("Please enter amount");
            edtAmount.requestFocus();
            result = false;
        }
        if (edtDescription.getText().toString().isEmpty()) {
            edtDescription.setError("Please enter description");
            edtDescription.requestFocus();
            result = false;
        }

        if (result) {
            if (!Patterns.EMAIL_ADDRESS.matcher(edtEmail.getText().toString()).matches()) {
                edtEmail.setError("Enter Valid E-mail");
                edtEmail.requestFocus();
                result = false;
            }
            if (currentBalance<Double.parseDouble(edtAmount.getText().toString())){
                edtAmount.setError("Redeem amount must be smaller than current balance");
                edtAmount.requestFocus();
                result = false;
            }
        }

        return result;
    }

    private void redeemRequest() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.userredeemrequest;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        params.add("emailAddress");
        params.add("amount");
        params.add("description");

        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(this).readInt("uId")));
        values.add(edtEmail.getText().toString());
        values.add(edtAmount.getText().toString());
        values.add(edtDescription.getText().toString());

        new ParseJSON(this, url, params, values, RedeemRequestPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    RedeemRequestPOJO resultObj = (RedeemRequestPOJO) obj;
                    Toast.makeText(RedeemRequestActivity.this, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RedeemRequestActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }


    private void validateRedeemRequest(){

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection")) {
                if (event.getMessage().equalsIgnoreCase("disconnected")) {
                    if (!(new ConnectionCheck().isNetworkConnected(RedeemRequestActivity.this))) {
                        Log.e("RideDetailActivity", "disconnected");
                        if (!ApplicationController.isOnline) {
                            if (PrefsUtil.isInternetConnectedShowing) {
                                if (PrefsUtil.dialogInternetConnected != null) {
                                    PrefsUtil.dialogInternetConnected.dismiss();
                                    PrefsUtil.isInternetConnectedShowing = false;
                                }
                            }
                            final Dialog d = new Dialog(RedeemRequestActivity.this,
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
