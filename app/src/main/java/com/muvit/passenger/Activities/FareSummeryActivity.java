package com.muvit.passenger.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Models.CommonPOJO;
import com.muvit.passenger.Models.FareSummaryPOJO;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.KeyboardUtils;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class FareSummeryActivity extends AppCompatActivity {

    String rideId, driverId, carId, timeInMinutes;
    private Toolbar toolbar;
    private TextView txtTitle, txtCarName, txtCarType, txtPickUp, txtDropOff, txtBaseFare, txtBaseKm, txtExtraKmFare, txtExtraKmRate, txtTimeTaken,
            txtTimeRate, txtTotalAmount, txtTotalKm, txtCash, txtWallet;
    private EditText edtDescription;
    private Button btnSubmit;
    private RatingBar ratingDriver;
    private LinearLayout activity_fare_summery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fare_summery);
        try {
            rideId = getIntent().getStringExtra("rideId");
        } catch (Exception e) {
            e.printStackTrace();
        }
        initViews();
        fareSummary();
        PrefsUtil.with(getApplicationContext()).write("lastSendId", "");
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeReview();
            }
        });
    }

    private void initViews() {
        setupToolbar();
        txtCarName = (TextView) findViewById(R.id.txtCarName);
        txtCarType = (TextView) findViewById(R.id.txtCarType);
        txtPickUp = (TextView) findViewById(R.id.txtPickUp);
        txtDropOff = (TextView) findViewById(R.id.txtDropOff);
        txtBaseFare = (TextView) findViewById(R.id.txtBaseFare);
        txtBaseKm = (TextView) findViewById(R.id.txtBaseKm);
        txtExtraKmFare = (TextView) findViewById(R.id.txtExtraKmFare);
        txtExtraKmRate = (TextView) findViewById(R.id.txtExtraKmRate);
        txtTimeTaken = (TextView) findViewById(R.id.txtTimeTaken);
        txtTimeRate = (TextView) findViewById(R.id.txtTimeRate);
        txtTotalAmount = (TextView) findViewById(R.id.txtTotalAmount);
        txtTotalKm = (TextView) findViewById(R.id.txtTotalKm);
        txtCash = (TextView) findViewById(R.id.txtCash);
        txtWallet = (TextView) findViewById(R.id.txtWallet);

        edtDescription = (EditText) findViewById(R.id.edtDescription);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        ratingDriver = (RatingBar) findViewById(R.id.ratingDriver);

        new KeyboardUtils().setupUI(findViewById(R.id.activity_fare_summery), FareSummeryActivity.this);
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.fare_summery_title);
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

    private void fareSummary() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.getfaresummery;
        ArrayList<String> params = new ArrayList<>();
        params.add("rideId");
        ArrayList<String> values = new ArrayList<>();
        values.add(rideId);
        new ParseJSON(this, url, params, values, FareSummaryPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    FareSummaryPOJO resultObj = (FareSummaryPOJO) obj;
                    txtCarName.setText(resultObj.getFareSummary().getCarBrand() + " " + resultObj.getFareSummary().getCarName());
                    txtCarType.setText(resultObj.getFareSummary().getCarTypeName());
                    txtPickUp.setText(resultObj.getFareSummary().getPickUpLocation());
                    txtDropOff.setText(resultObj.getFareSummary().getDropOffLocation());
                    txtBaseFare.setText(getString(R.string.currencySign) + resultObj.getFareSummary().getBaseFare().getTotalFareAmount());
                    txtBaseKm.setText("(" + resultObj.getFareSummary().getBaseFare().getPerKmAmount() + " Km)");
                    if (resultObj.getFareSummary().getExtraKm().getTotalExtraKm().contains("-")) {
                        txtExtraKmFare.setText("0");
                    } else {
                        txtExtraKmFare.setText(resultObj.getFareSummary().getExtraKm().getTotalExtraKm());
                    }

                    txtExtraKmRate.setText("(" + getString(R.string.currencySign) + resultObj.getFareSummary().getExtraKm().getPerKmPrice() + " Per Km)");
                    txtTimeTaken.setText(resultObj.getFareSummary().getTimeTaken().getTotalTime());
                    txtTimeRate.setText("(" + getString(R.string.currencySign) + resultObj.getFareSummary().getTimeTaken().getPerMinFareAmount() + ") Per min");
                    txtTotalAmount.setText(getString(R.string.currencySign) + resultObj.getFareSummary().getFinalAmount().getFinalTotalRidePrice());
                    txtTotalKm.setText("(" + resultObj.getFareSummary().getFinalAmount().getTotalKm() + " Km)");


                    if (!resultObj.getFareSummary().getFinalAmount().getPayByCash().equalsIgnoreCase("N/A")) {
                        if (resultObj.getFareSummary().getFinalAmount().getPayByCash().equalsIgnoreCase("0.00") || resultObj.getFareSummary().getFinalAmount().getPayByCash().equalsIgnoreCase("0")) {
                            txtCash.setVisibility(View.GONE);
                        } else {
                            txtCash.setText(getString(R.string.currencySign) + resultObj.getFareSummary().getFinalAmount().getPayByCash());
                        }

                    } else {
                        txtCash.setText(resultObj.getFareSummary().getFinalAmount().getPayByCash());
                    }

                    if (!resultObj.getFareSummary().getFinalAmount().getPayByWallet().equalsIgnoreCase("N/A")) {
                        /*if (resultObj.getFareSummary().getFinalAmount().getPayByWallet()
                                .equalsIgnoreCase("0.00")
                                || resultObj.getFareSummary().getFinalAmount().getPayByWallet()
                                .equalsIgnoreCase("0")) {
                            txtWallet.setVisibility(View.GONE);
                        }else {*/
                        txtWallet.setText(getString(R.string.currencySign) + resultObj.getFareSummary().getFinalAmount().getPayByWallet());
                        /*}*/

                    } else {
                        txtWallet.setText(resultObj.getFareSummary().getFinalAmount().getPayByWallet());
                    }
                } else {
                    Toast.makeText(FareSummeryActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }


    private void placeReview() {
        boolean result = true;
        if (edtDescription.getText().toString().isEmpty()) {
            edtDescription.setError("Please enter review");
            result = false;
        }
        if (result) {
            String url = WebServiceUrl.ServiceUrl + WebServiceUrl.useraddfeedback;
            ArrayList<String> params = new ArrayList<>();
            params.add("userId");
            params.add("rideId");
            params.add("ratting");
            params.add("comment");
            ArrayList<String> values = new ArrayList<>();
            values.add(String.valueOf(PrefsUtil.with(this).readInt("uId")));
            values.add(rideId);
            values.add(String.valueOf(ratingDriver.getRating()));
            values.add(edtDescription.getText().toString());
            new ParseJSON(this, url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
                @Override
                public void onResult(boolean status, Object obj) {
                    if (status) {
                        CommonPOJO resultObj = (CommonPOJO) obj;
                        Toast.makeText(FareSummeryActivity.this, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(FareSummeryActivity.this, RideDetailsActivity.class);
                        i.putExtra("tripId", Integer.parseInt(rideId));
                        i.putExtra("fromSummary", true);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(FareSummeryActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                    }


                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection")) {
                if (event.getMessage().equalsIgnoreCase("disconnected")) {
                    if (!(new ConnectionCheck().isNetworkConnected(FareSummeryActivity.this))) {
                        Log.e("RideDetailActivity", "disconnected");
                        if (!ApplicationController.isOnline) {
                            if (PrefsUtil.isInternetConnectedShowing) {
                                if (PrefsUtil.dialogInternetConnected != null) {
                                    PrefsUtil.dialogInternetConnected.dismiss();
                                    PrefsUtil.isInternetConnectedShowing = false;
                                }
                            }
                            final Dialog d = new Dialog(FareSummeryActivity.this,
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
