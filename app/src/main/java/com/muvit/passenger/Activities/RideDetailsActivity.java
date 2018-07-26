package com.muvit.passenger.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Models.CommonPOJO;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.Models.RideDetail;
import com.muvit.passenger.Models.RideDetailPOJO;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.ImgUtils;
import com.muvit.passenger.Utils.KeyboardUtils;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.Transform;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class RideDetailsActivity extends AppCompatActivity {
    Dialog postNotiDialog;
    ScrollView sv;
    ImageView back_btn,cancelBtn;
   // private Toolbar toolbar;
    private TextView txtTitle,total_top;
    private LinearLayout layoutFareDetails,layoutCash,layoutWallet;
    private String tripId = "0";
    private RelativeLayout layoutMap;
    private ImageView imgProfile, imgCarType;
    private TextView txtStatus, txtPickUp, txtDropOff, txtName, txtMobileNo, txtCarName, txtTotalRatings,
            txtAvgRatings, txtCarType, txtCarNo, txtDate, txtBaseFare, txtFare, txtExtraKm, txtKm, txtTimeTaken,
            txtTimePerMin, txtTotal, txtTotalKm, txtByCash, txtByWallet, txtReview, txtReportDriver;
    private RatingBar ratingMyReview;
    private Button btnSubmit;
    private EditText edtDescription;
    private boolean fromSummary = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_details);
        tripId = String.valueOf(getIntent().getIntExtra("tripId", 0));
        try {
            fromSummary = getIntent().getBooleanExtra("from",false);
        }catch (Exception e){
            fromSummary = false;
            e.printStackTrace();
        }
        initViews();
        getTripDetail();


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeReview();
            }
        });
    }

    private void initViews() {
        initNotiDialog();
        setupToolbar();
        layoutFareDetails = (LinearLayout) findViewById(R.id.topLayout);
        layoutFareDetails.invalidate();
        layoutFareDetails.bringToFront();
        dialog = new Dialog(RideDetailsActivity.this);
    }

    private void setupToolbar() {
//        toolbar = (Toolbar) findViewById(R.id.toolBar);
//        txtTitle = (TextView) findViewById(R.id.txtTitle);
//        txtTitle.setText(R.string.ride_details_title);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        back_btn = (ImageView) findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        total_top = (TextView) findViewById(R.id.total_top);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        txtPickUp = (TextView) findViewById(R.id.txtPickUp);
        txtDropOff = (TextView) findViewById(R.id.txtDropOff);
        txtName = (TextView) findViewById(R.id.txtName);
        txtMobileNo = (TextView) findViewById(R.id.txtMobileNo);
        txtCarName = (TextView) findViewById(R.id.txtCarName);
        txtTotalRatings = (TextView) findViewById(R.id.txtTotalRatings);
        txtAvgRatings = (TextView) findViewById(R.id.txtAvgRatings);
        txtCarType = (TextView) findViewById(R.id.txtCarType);
        txtCarNo = (TextView) findViewById(R.id.txtCarNo);
        txtDate = (TextView) findViewById(R.id.txtDate);
        txtBaseFare = (TextView) findViewById(R.id.txtBaseFare);
        txtFare = (TextView) findViewById(R.id.txtFare);
        txtExtraKm = (TextView) findViewById(R.id.txtExtraKm);
        txtKm = (TextView) findViewById(R.id.txtKm);
        txtTimeTaken = (TextView) findViewById(R.id.txtTimeTaken);
        txtTimePerMin = (TextView) findViewById(R.id.txtTimePerMin);
        txtTotal = (TextView) findViewById(R.id.txtTotal);
        txtTotalKm = (TextView) findViewById(R.id.txtTotalKm);
        txtByCash = (TextView) findViewById(R.id.txtByCash);
        txtByWallet = (TextView) findViewById(R.id.txtByWallet);
        txtReview = (TextView) findViewById(R.id.txtReview);
        txtReportDriver = (TextView) findViewById(R.id.txtReportDriver);
        edtDescription = (EditText) findViewById(R.id.edtDescription);
        imgProfile = (ImageView) findViewById(R.id.imgProfile);
        imgCarType = (ImageView) findViewById(R.id.imgCarType);
        layoutMap = (RelativeLayout) findViewById(R.id.layoutMap);
        ratingMyReview = (RatingBar) findViewById(R.id.ratingMyReview);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        layoutCash = (LinearLayout) findViewById(R.id.layoutCash);
        layoutWallet = (LinearLayout) findViewById(R.id.layoutWallet);
        txtReportDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReportDriverDialog("1");
            }
        });
        new KeyboardUtils().setupUI(findViewById(R.id.activity_ride_details), RideDetailsActivity.this);
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

    private void initNotiDialog() {
        // final View view = getLayoutInflater().inflate(R.layout.promo_dialog, null);
        postNotiDialog = new Dialog(this);
        postNotiDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        postNotiDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        postNotiDialog.setContentView(R.layout.post_notification_dialog);
       cancelBtn = (ImageView) postNotiDialog.findViewById(R.id.cancel_noti);

//        okBtn = (RelativeLayout) promoDialog.findViewById(R.id.ok_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postNotiDialog.dismiss();
            }
        });
//
//        okBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                promoDialog.dismiss();
//            }
//        });

        postNotiDialog.setCancelable(false);

    }

    public void getTripDetail() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.usertripdetails;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        params.add("tripId");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(this).readInt("uId")));
        values.add(tripId);
        new ParseJSON(RideDetailsActivity.this, url, params, values, RideDetailPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    RideDetailPOJO resultObj = (RideDetailPOJO) obj;
                    if (resultObj.isStatus()) {
                        final RideDetail rideDetail = resultObj.getRideDetail();
                        if (rideDetail.getStatus().equalsIgnoreCase("w")) {
                            txtStatus.setText("Waiting");
                        } else if (rideDetail.getStatus().equalsIgnoreCase("s")) {
                            txtStatus.setText("Started");
                        } else if (rideDetail.getStatus().equalsIgnoreCase("c")) {
                            txtStatus.setText("Completed");
                        } else if (rideDetail.getStatus().equalsIgnoreCase("r")) {
                            txtStatus.setText("Rejected");
                        } else {
                            txtStatus.setText("Pending");
                        }

                        txtPickUp.setText(rideDetail.getPickUpLocation());
                        txtDropOff.setText(rideDetail.getDropOffLocation());
                        txtName.setText(rideDetail.getDriverFirstName() + " " + rideDetail.getDriverLastName());
                        txtMobileNo.setText(rideDetail.getMobileNo());
                        txtAvgRatings.setText(String.valueOf(rideDetail.getAvgRatting()));
                        txtTotalRatings.setText(String.valueOf(rideDetail.getTotalRating()) + " Ratings");
                        txtCarName.setText(rideDetail.getBrandName() + " " + rideDetail.getCarName());
                        txtCarType.setText(rideDetail.getTypeName());
                        txtCarNo.setText(rideDetail.getCarNumber());
                        txtDate.setText(rideDetail.getRideDateTime());
                        //txtBaseFare.setText(rideDetail.getFareDistanceCharges());

                        if (!TextUtils.isEmpty(rideDetail.getFareDistanceCharges())) {
                            txtBaseFare.setText(getString(R.string.currencySign)  + rideDetail.getFareDistanceCharges());
                        }else {
                            txtBaseFare.setText("");
                        }


                        if (!TextUtils.isEmpty(rideDetail.getFareDistance())) {
                            txtFare.setText("(" + rideDetail.getFareDistance() + " Km)");
                        }else {
                            txtFare.setText("");
                        }

                        txtExtraKm.setText(String.valueOf(rideDetail.getExtraDistance()));


                        if (!TextUtils.isEmpty(String.valueOf(rideDetail.getFareAdditionalChargesPerKm()))) {
                            txtKm.setText("(" + getString(R.string.currencySign) + String.valueOf(rideDetail.getFareAdditionalChargesPerKm()) + " Per Km)");
                        }else {
                            txtKm.setText("");
                        }


                        if (rideDetail.getTotalTime().equalsIgnoreCase("N/A")) {
                            txtTimeTaken.setText(rideDetail.getTotalTime());
                        }else {
                            txtTimeTaken.setText(rideDetail.getTotalTime() + " min");
                        }
                        //txtTimeTaken.setText(rideDetail.getTotalTime() + " min");
                        //txtTimePerMin.setText(String.valueOf(rideDetail.getPerMinCharges()));
                        if (!TextUtils.isEmpty(rideDetail.getPerMinCharges())) {
                            txtTimePerMin.setText("(" + getString(R.string.currencySign) + rideDetail.getPerMinCharges() + " Per Min)");
                        }else {
                            txtTimePerMin.setText("");
                        }
                        if (!rideDetail.getTotalCharges().equalsIgnoreCase("N/A")) {
                            txtTotal.setText(getString(R.string.currencySign) +rideDetail.getTotalCharges());
                            total_top.setText(getString(R.string.currencySign) +rideDetail.getTotalCharges());
                        }else {
                            txtTotal.setText(rideDetail.getTotalCharges());
                            total_top.setText(rideDetail.getTotalCharges());
                        }

                        if (!TextUtils.isEmpty(rideDetail.getTotalDistance())) {
                            txtTotalKm.setText("(" + rideDetail.getTotalDistance() + ")");
                        }else {
                            txtTotalKm.setText("");
                        }


                        if (!rideDetail.getPayByCash().equalsIgnoreCase("N/A")) {
                            if (rideDetail.getPayByCash().equalsIgnoreCase("0.00") || rideDetail.getPayByCash().equalsIgnoreCase("0")) {
                                layoutCash.setVisibility(View.GONE);
                            }else {
                                txtByCash.setText(getString(R.string.currencySign) + rideDetail.getPayByCash());
                            }
                        }else {
                            txtByCash.setText(rideDetail.getPayByCash());
                        }

                        if (!rideDetail.getPayByWallet().equalsIgnoreCase("N/A")) {
                            if (rideDetail.getPayByWallet().equalsIgnoreCase("0.00") || rideDetail.getPayByWallet().equalsIgnoreCase("0")) {
                                layoutWallet.setVisibility(View.GONE);
                            }else {
                                txtByWallet.setText(getString(R.string.currencySign) + rideDetail.getPayByWallet());
                            }
                        }else {
                            txtByWallet.setText(rideDetail.getPayByWallet());
                        }



                        //txtByWallet.setText(getString(R.string.currencySign) + rideDetail.getPayByWallet());

                        if (rideDetail.getStatus().equalsIgnoreCase("c")) {
                            if (rideDetail.getFeedback().equalsIgnoreCase("N/A")) {
                                edtDescription.setVisibility(View.VISIBLE);
                                ratingMyReview.setIsIndicator(false);
                                btnSubmit.setVisibility(View.VISIBLE);
                                txtReview.setVisibility(View.GONE);
                            }else {
                                ratingMyReview.setIsIndicator(true);
                                edtDescription.setVisibility(View.GONE);
                                txtReview.setText(rideDetail.getFeedback());
                                btnSubmit.setVisibility(View.GONE);
                                txtReview.setVisibility(View.VISIBLE);
                            }
                        }else {


                            txtReview.setVisibility(View.VISIBLE);
                            txtReview.setText(rideDetail.getFeedback());
                        }



                        layoutMap.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(RideDetailsActivity.this, TripDetailMapctivity.class);
                                i.putExtra("dataAvailable", true);
                                i.putExtra("PickUpLat", rideDetail.getPickUpLat());
                                i.putExtra("PickUpLong", rideDetail.getPickUpLong());
                                i.putExtra("PickUpLocation", rideDetail.getPickUpLocation());
                                i.putExtra("DropOffLat", rideDetail.getDropOffLat());
                                i.putExtra("DropOffLong", rideDetail.getDropOffLong());
                                i.putExtra("DropOffLocation", rideDetail.getDropOffLocation());
                                startActivity(i);
                            }
                        });

                        //imgProfile
                        Ion.with(imgProfile)
                                .transform(new Transform() {
                                    @Override
                                    public Bitmap transform(Bitmap b) {
                                        return ImgUtils.createCircleBitmap(b);
                                    }

                                    @Override
                                    public String key() {
                                        return null;
                                    }
                                })
                                .error(R.drawable.no_image)
                                .load(WebServiceUrl.profileUrl + rideDetail.getDriverProfileImage());


                        Ion.with(imgCarType)
                                .error(R.drawable.rides)
                                .load(WebServiceUrl.carUrl + rideDetail.getTypeImage());

                        txtReportDriver.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showReportDriverDialog(rideDetail.getDriverId());
                            }
                        });

                        try {
                            if (!rideDetail.getRating().equalsIgnoreCase("N/A")){
                                ratingMyReview.setRating(Float.parseFloat(rideDetail.getRating()));
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }


                    }

                    sv.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(RideDetailsActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void reportDriver(String driverId,String subject,String desc) {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.reportdriver;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        params.add("driverId");
        params.add("subject");
        params.add("desc");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(this).readInt("uId")));
        values.add(driverId);
        values.add(subject);
        values.add(desc);
        new ParseJSON(RideDetailsActivity.this, url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    CommonPOJO resultObj = (CommonPOJO) obj;
                    Toast.makeText(RideDetailsActivity.this, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(RideDetailsActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    Dialog dialog;
    private void showReportDriverDialog(final String driverId) {

        try {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog.setContentView(R.layout.dialog_report_driver);
        dialog.setCancelable(false);

        TextView txtClose = (TextView) dialog.findViewById(R.id.txtClose);
        TextView txtDone = (TextView) dialog.findViewById(R.id.txtDone);
        final EditText edtSubject = (EditText) dialog.findViewById(R.id.edtSubject);
        final EditText edtDescription = (EditText) dialog.findViewById(R.id.edtDescription);

        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        txtDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean validationResult = true;
                if (edtSubject.getText().toString().length() == 0) {
                    edtSubject.setError("Subject cannot be empty.");
                    edtSubject.requestFocus();
                    validationResult = false;
                }
                if (edtDescription.getText().toString().length() == 0) {
                    edtDescription.setError("Description cannot be empty.");
                    edtDescription.requestFocus();
                    validationResult = false;
                }

                if (validationResult) {
                    reportDriver(driverId,edtSubject.getText().toString(),edtDescription.getText().toString());
                }
            }
        });

        // now that the dialog is set up, it's time to show it
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }


    private void placeReview() {
        boolean result = true;
        if (edtDescription.getText().toString().isEmpty()) {
            edtDescription.setError("Please enter review");
            result = false;
        }
        if (result) {

            postNotiDialog.show();
            String url = WebServiceUrl.ServiceUrl + WebServiceUrl.useraddfeedback;
            ArrayList<String> params = new ArrayList<>();
            params.add("userId");
            params.add("rideId");
            params.add("ratting");
            params.add("comment");
            ArrayList<String> values = new ArrayList<>();
            values.add(String.valueOf(PrefsUtil.with(this).readInt("uId")));
            values.add(tripId);
            values.add(String.valueOf(ratingMyReview.getRating()));
            values.add(edtDescription.getText().toString());
            new ParseJSON(this, url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
                @Override
                public void onResult(boolean status, Object obj) {
                    if (status) {
                        CommonPOJO resultObj = (CommonPOJO) obj;
                        Toast.makeText(RideDetailsActivity.this, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                        getTripDetail();
                    } else {
                        Toast.makeText(RideDetailsActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                    }


                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (fromSummary) {
            Intent i = new Intent(this, HomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //i.putExtra("rideId", dataAns.getString("rideId"));
            // i.putExtra("driverId", dataAns.getString("driverId"));
            // i.putExtra("distanceInMeter", dataAns.getString("distanceInMeter"));
            startActivity(i);
        }
        super.onBackPressed();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection")) {
                if (event.getMessage().equalsIgnoreCase("disconnected")) {
                    if (!(new ConnectionCheck().isNetworkConnected(RideDetailsActivity.this))) {
                        Log.e("RideDetailActivity", "disconnected");
                        if (!ApplicationController.isOnline) {
                            if (PrefsUtil.isInternetConnectedShowing) {
                                if (PrefsUtil.dialogInternetConnected != null) {
                                    PrefsUtil.dialogInternetConnected.dismiss();
                                    PrefsUtil.isInternetConnectedShowing = false;
                                }
                            }
                            final Dialog d = new Dialog(RideDetailsActivity.this,
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
