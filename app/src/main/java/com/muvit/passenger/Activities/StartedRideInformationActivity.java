package com.muvit.passenger.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.GeoLocation.activity.GeocoderHelper2;
import com.muvit.passenger.Models.CommonPOJO;
import com.muvit.passenger.Models.FareSummaryPOJO;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.Models.PanicDataPOJO;
import com.muvit.passenger.Models.RideInfo;
import com.muvit.passenger.Models.RideInfoPOJO;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.GPSTracker;
import com.muvit.passenger.Utils.GPSTrackerForUpdate;
import com.muvit.passenger.Utils.ImgUtils;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.Transform;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class StartedRideInformationActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView txtTitle;
    private TextView txtDriverName, txtRatings, txtTotalRatings, txtCarName, txtCarType, txtCarNo, txtContactNo, txtMinFareRate,
            txtMinFare, txtKmFare, txtPerKmCharges, txtTimeCharges;
    private GoogleMap map;
    private Button btnTrackTrip, btnCancelTrip;
    private String rideId;

    private ImageView imgDriverImage, imgCar, imgPanic;
    private LatLng pickupLatLng = new LatLng(0.0, 0.0);
    private LatLng dropoffLatLng = new LatLng(0.0, 0.0);
    private Marker pickupMarker;
    private Marker dropoffMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_started_ride_info);
        initViews();
        try {
            rideId = getIntent().getStringExtra("rideId");
        } catch (Exception e) {
            //rideId = "319";
            e.printStackTrace();
        }
        setUpMap();
        getRideInfo();

        btnCancelTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final GPSTrackerForUpdate gpsupdate = new GPSTrackerForUpdate(
                        StartedRideInformationActivity.this);
                if (!(gpsupdate.getLatitude() == 9999999 || gpsupdate.getLongitude() == 9999999)) {
                    GeocoderHelper2 ghelper = new GeocoderHelper2(
                            StartedRideInformationActivity.this, gpsupdate.getLatitude(),
                            gpsupdate.getLongitude(), new GeocoderHelper2.onGetAddress() {
                        @Override
                        public void onSuccess(String address) {
                            Log.e("IF ADD SUCCESS : ", address);
                            fareSummary(String.valueOf(gpsupdate.getLatitude()),
                                    String.valueOf(gpsupdate.getLongitude()), address);
                        }

                        @Override
                        public void onFail() {
                            Log.e("IF ADD FAIL : ", "");
                            fareSummary(String.valueOf(gpsupdate.getLatitude()),
                                    String.valueOf(gpsupdate.getLongitude()), "");
                        }
                    });
                } else {
                    final GPSTracker gps = new GPSTracker(
                            StartedRideInformationActivity.this);
                    if (gps.canGetLocation()) {
                        GeocoderHelper2 ghelper = new GeocoderHelper2(
                                StartedRideInformationActivity.this, gps.getLatitude(),
                                gps.getLongitude(), new GeocoderHelper2.onGetAddress() {
                            @Override
                            public void onSuccess(String address) {
                                Log.e("ELSE ADD SUCCESS : ", address);
                                fareSummary(String.valueOf(gps.getLatitude()),
                                        String.valueOf(gps.getLongitude()), address);
                            }

                            @Override
                            public void onFail() {
                                Log.e("ELSE ADD FAIL : ", "");
                                fareSummary(String.valueOf(gps.getLatitude()),
                                        String.valueOf(gps.getLongitude()), "");
                            }
                        });
                    } else {
                        Log.e("Can't call service", "Location Not Found");
                    }
                }
            }
        });
    }

    private void initViews() {
        txtDriverName = (TextView) findViewById(R.id.txtDriverName);
        txtRatings = (TextView) findViewById(R.id.txtRatings);
        txtTotalRatings = (TextView) findViewById(R.id.txtTotalRatings);
        txtCarName = (TextView) findViewById(R.id.txtCarName);
        txtCarType = (TextView) findViewById(R.id.txtCarType);
        txtCarNo = (TextView) findViewById(R.id.txtCarNo);
        txtContactNo = (TextView) findViewById(R.id.txtContactNo);
        txtMinFare = (TextView) findViewById(R.id.txtMinFare);
        txtMinFareRate = (TextView) findViewById(R.id.txtMinFareRate);
        txtKmFare = (TextView) findViewById(R.id.txtKmFare);
        txtPerKmCharges = (TextView) findViewById(R.id.txtPerKmCharges);
        txtTimeCharges = (TextView) findViewById(R.id.txtTimeCharges);
        imgDriverImage = (ImageView) findViewById(R.id.txtDriverImage);
        imgPanic = (ImageView) findViewById(R.id.imgPanic);
        btnTrackTrip = (Button) findViewById(R.id.btnTrackTrip);
        btnCancelTrip = (Button) findViewById(R.id.btnCancelTrip);
        setupToolbar();
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.ride_information_title);
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

    private void getRideInfo() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.userrideinfo;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        params.add("rideId");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(this).readInt("uId")));
        values.add(rideId);
        new ParseJSON(this, url, params, values, RideInfoPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    RideInfoPOJO resultObj = (RideInfoPOJO) obj;
                    final RideInfo rideInfo = resultObj.getRideInfo();
                    txtDriverName.setText(rideInfo.getDriverName());
                    txtRatings.setText(String.valueOf(rideInfo.getAvgRatting()));
                    txtTotalRatings.setText(String.valueOf(rideInfo.getTotalRatting()) + " Ratings");
                    txtCarName.setText(rideInfo.getBrandName() + " " + rideInfo.getCarName());
                    txtCarType.setText(rideInfo.getTypeName());
                    txtCarNo.setText(rideInfo.getCarNumber());
                    txtContactNo.setText(rideInfo.getDriverContact());
                    txtMinFare.setText(getString(R.string.currencySign) + rideInfo.getMinFareKmRate());
                    txtMinFareRate.setText("Min Fare (" + rideInfo.getMinFareKm() + " Km)");
                    txtKmFare.setText("Per km charge after (" + rideInfo.getMinFareKm() + " Km)");
                    txtPerKmCharges.setText(getString(R.string.currencySign) + rideInfo.getExtraFareKmRate());
                    txtTimeCharges.setText(getString(R.string.currencySign) + String.valueOf(rideInfo.getPerMinRate()));

                    btnTrackTrip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(
                                    StartedRideInformationActivity.this,
                                    TripTrackingActivity.class);
                            i.putExtra("rideId", rideId);
                            startActivity(i);
                        }
                    });
                    if (rideInfo.getIsLongRide().equalsIgnoreCase("y")) {
                        btnTrackTrip.setVisibility(View.GONE);
                    } else {
                        btnTrackTrip.setVisibility(View.VISIBLE);
                    }
                    Ion.with(imgDriverImage)
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
                            .load(WebServiceUrl.profileUrl + rideInfo.getDriverProfileImage());

                    getPanicNumber();

                    try {
                        pickupLatLng = new LatLng(Double.parseDouble(rideInfo.getPickUpLat()), Double.parseDouble(rideInfo.getPickUpLong()));
                        dropoffLatLng = new LatLng(Double.parseDouble(rideInfo.getDropOffLat()), Double.parseDouble(rideInfo.getDropOffLong()));

                        setPickupMarker(pickupLatLng, rideInfo.getPickUpLocation());
                        setDropoffMarker(dropoffLatLng, rideInfo.getDropOffLocation());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(StartedRideInformationActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setUpMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng point) {
                        //setMarker(point);
                    }
                });
            }
        });
    }

    private void getPanicNumber() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.getpanicnumber;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(this).readInt("uId")));
        new ParseJSON(this, url, params, values, PanicDataPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    final PanicDataPOJO resultObj = (PanicDataPOJO) obj;
                    imgPanic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", resultObj.getPanicData().get(0).getUserPanicNo(), null));
                            startActivity(intent);
                            setPanicRide();
                        }
                    });
                } else {
                    Toast.makeText(StartedRideInformationActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    private void setPanicRide() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.setpanicride;
        ArrayList<String> params = new ArrayList<>();
        params.add("rideId");
        ArrayList<String> values = new ArrayList<>();
        values.add(rideId);
        new ParseJSON(this, url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    /*final PanicDataPOJO resultObj = (PanicDataPOJO) obj;
                    imgPanic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", resultObj.getPanicData().get(0).getUserPanicNo(), null));
                            startActivity(intent);
                        }
                    });*/
                } else {
                    Toast.makeText(StartedRideInformationActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    public void setPickupMarker(LatLng point, String location) {
        try {

            //remove previously placed Marker
            if (pickupMarker != null) {
                pickupMarker.remove();
            }

            //place marker where user just clicked
            pickupMarker = map.addMarker(new MarkerOptions().position(point).title(location)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pick_up_marker)));
            pickupMarker.showInfoWindow();
            //moveToCurrentLocation(dropoffLatLng);
        } catch (Exception e) {
            Toast.makeText(StartedRideInformationActivity.this, "Cannot get address from clicked location", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    public void setDropoffMarker(LatLng point, String location) {
        try {


            //remove previously placed Marker
            if (dropoffMarker != null) {
                dropoffMarker.remove();
            }

            //place marker where user just clicked
            dropoffMarker = map.addMarker(new MarkerOptions().position(point).title(location)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.drop_off_marker)));
            dropoffMarker.showInfoWindow();
            moveToCurrentLocation(dropoffLatLng);
        } catch (Exception e) {
            Toast.makeText(StartedRideInformationActivity.this, "Cannot get address from clicked location", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void moveToCurrentLocation(LatLng currentLocation) {
        /*map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);*/

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

//the include method will calculate the min and max bound.
        builder.include(pickupMarker.getPosition());
        builder.include(dropoffMarker.getPosition());

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDimensionPixelSize(R.dimen.map_height);
        int padding = (int) (width * 0.11); // offset from edges of the map 12% of screen

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

        map.animateCamera(cu);


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
        if (event.getType().equalsIgnoreCase("cancelride")
                || event.getType().equalsIgnoreCase("fareEstimate")) {
            try {
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (event.getType().equalsIgnoreCase("connection")) {
            if (event.getMessage().equalsIgnoreCase("disconnected")) {
                if (!(new ConnectionCheck().isNetworkConnected(StartedRideInformationActivity.this))) {
                    Log.e("RideDetailActivity", "disconnected");
                    if (!ApplicationController.isOnline) {
                        if (PrefsUtil.isInternetConnectedShowing) {
                            if (PrefsUtil.dialogInternetConnected != null) {
                                PrefsUtil.dialogInternetConnected.dismiss();
                                PrefsUtil.isInternetConnectedShowing = false;
                            }
                        }
                        final Dialog d = new Dialog(StartedRideInformationActivity.this,
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
    }

    private void fareSummary(String serviceLatitude, String serviceLongitude, String address) {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.faresummery;
        ArrayList<String> params = new ArrayList<>();
        params.add("rideId");
        params.add("dropLat");
        params.add("dropLong");
        params.add("dropLocation");
        params.add("isCompletedRide");
        ArrayList<String> values = new ArrayList<>();
        values.add(getIntent().getStringExtra("rideId"));
        values.add(serviceLatitude);
        values.add(serviceLongitude);
        values.add(address);
        values.add("n");
        new ParseJSON(this, url, params, values, FareSummaryPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    FareSummaryPOJO resultObj = (FareSummaryPOJO) obj;
                    Toast.makeText(StartedRideInformationActivity.this, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                    /*txtCarName.setText(resultObj.getFareSummary().getCarBrand() + " " + resultObj.getFareSummary().getCarName());
                    txtCarType.setText(resultObj.getFareSummary().getCarTypeName());
                    txtPickUp.setText(resultObj.getFareSummary().getPickUpLocation());
                    txtDropOff.setText(resultObj.getFareSummary().getDropOffLocation());
                    txtBaseFare.setText(getString(R.string.currencySign) + resultObj.getFareSummary().getBaseFare().getTotalFareAmount());
                    txtBaseKm.setText("(" + resultObj.getFareSummary().getBaseFare().getPerKmAmount() + " Km)");
                    txtExtraKmFare.setText(resultObj.getFareSummary().getExtraKm().getTotalExtraKm());
                    txtExtraKmRate.setText("("+ getString(R.string.currencySign)  + resultObj.getFareSummary().getExtraKm().getPerKmPrice() + " Per Km)");
                    txtTimeTaken.setText(resultObj.getFareSummary().getTimeTaken().getTotalTime());
                    txtTimeRate.setText("("+ getString(R.string.currencySign) + resultObj.getFareSummary().getTimeTaken().getPerMinFareAmount() + ") Per min");
                    txtTotalAmount.setText(getString(R.string.currencySign)+resultObj.getFareSummary().getFinalAmount().getFinalTotalRidePrice());
                    txtTotalKm.setText("("+resultObj.getFareSummary().getFinalAmount().getTotalKm()+ " Km)");

                    if (!resultObj.getFareSummary().getFinalAmount().getPayByCash().equalsIgnoreCase("N/A")) {
                        if (resultObj.getFareSummary().getFinalAmount().getPayByCash().equalsIgnoreCase("0.00") || resultObj.getFareSummary().getFinalAmount().getPayByCash().equalsIgnoreCase("0")) {
                            txtCash.setVisibility(View.GONE);
                        }else {
                            txtCash.setText(getString(R.string.currencySign) + resultObj.getFareSummary().getFinalAmount().getPayByCash());
                        }

                    }else {
                        txtCash.setText(resultObj.getFareSummary().getFinalAmount().getPayByCash());
                    }

                    if (!resultObj.getFareSummary().getFinalAmount().getPayByWallet().equalsIgnoreCase("N/A")) {
                        if (resultObj.getFareSummary().getFinalAmount().getPayByWallet().equalsIgnoreCase("0.00") || resultObj.getFareSummary().getFinalAmount().getPayByWallet().equalsIgnoreCase("0")) {
                            txtWallet.setVisibility(View.GONE);
                        }else {
                            txtWallet.setText(getString(R.string.currencySign) + resultObj.getFareSummary().getFinalAmount().getPayByWallet());
                        }

                    }else {
                        txtWallet.setText(resultObj.getFareSummary().getFinalAmount().getPayByWallet());
                    }
                    //txtCash.setText(getString(R.string.currencySign)+resultObj.getFareSummary().getFinalAmount().getPayByCash());
                    //txtWallet.setText(getString(R.string.currencySign)+resultObj.getFareSummary().getFinalAmount().getPayByWallet());
                    PrefsUtil.with(FareSummeryActivity.this).write("isInRide",false);
                    PrefsUtil.with(FareSummeryActivity.this).write("isRideStarted",false);
                    PrefsUtil.with(FareSummeryActivity.this).write("startUpdateLatLongWithRideId",false);*/
                } else {
                    Toast.makeText(StartedRideInformationActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

}
