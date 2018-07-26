package com.muvit.passenger.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.PrefsUtil;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TripDetailMapctivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView txtTitle;
    private GoogleMap map;
    private Geocoder geocoder;
    private Marker pickupMarker;
    private Marker dropoffMarker;


    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));
    private LatLng pickupLatLng = new LatLng(0.0,0.0);
    private LatLng dropoffLatLng = new LatLng(0.0,0.0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail_map);
        initViews();
    }

    private void initViews() {
        setupToolbar();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this, Locale.getDefault());
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                /*map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng point) {
                        setMarker(point);
                    }
                });*/
                try {
                    if (getIntent().getBooleanExtra("dataAvailable",false)) {
                        pickupLatLng = new LatLng(Double.parseDouble(getIntent().getStringExtra("PickUpLat")),Double.parseDouble(getIntent().getStringExtra("PickUpLong")));
                        dropoffLatLng = new LatLng(Double.parseDouble(getIntent().getStringExtra("DropOffLat")),Double.parseDouble(getIntent().getStringExtra("DropOffLong")));

                        setPickupMarker(pickupLatLng,getIntent().getStringExtra("PickUpLocation"));
                        setDropoffMarker(dropoffLatLng,getIntent().getStringExtra("DropOffLocation"));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText("Trip Map");
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

    public void setPickupMarker(LatLng point,String location){
        try {
            List<Address> addresses = new ArrayList<>();
            try {
                addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Address address = addresses.get(0);

            if (address != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i) + " ");
                }
                //Toast.makeText(WorkLocationActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
            }

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
            Toast.makeText(TripDetailMapctivity.this, "Cannot get address from clicked location", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void setDropoffMarker(LatLng point,String location){
        try {
            List<Address> addresses = new ArrayList<>();
            try {
                addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Address address = addresses.get(0);

            if (address != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i) + " ");
                }
                //Toast.makeText(WorkLocationActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
            }



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
            Toast.makeText(TripDetailMapctivity.this, "Cannot get address from clicked location", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void moveToCurrentLocation(LatLng currentLocation)
    {
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
        int height =  getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.11); // offset from edges of the map 12% of screen

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

        map.animateCamera(cu);


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection")) {
                if (event.getMessage().equalsIgnoreCase("disconnected")) {
                    if (!(new ConnectionCheck().isNetworkConnected(TripDetailMapctivity.this))) {
                        Log.e("RideDetailActivity", "disconnected");
                        if (!ApplicationController.isOnline) {
                            if (PrefsUtil.isInternetConnectedShowing) {
                                if (PrefsUtil.dialogInternetConnected != null) {
                                    PrefsUtil.dialogInternetConnected.dismiss();
                                    PrefsUtil.isInternetConnectedShowing = false;
                                }
                            }
                            final Dialog d = new Dialog(TripDetailMapctivity.this,
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
