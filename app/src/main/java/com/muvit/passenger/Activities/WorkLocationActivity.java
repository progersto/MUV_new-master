package com.muvit.passenger.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.GeoLocation.activity.GeocoderHelper;
import com.muvit.passenger.GeoLocation.adapter.PlaceAutocompleteAdapter;
import com.muvit.passenger.Models.CommonPOJO;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.GPSTracker;
import com.muvit.passenger.Utils.KeyboardUtils;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
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

public class WorkLocationActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private Toolbar toolbar;
    private TextView txtTitle;
    private MapView mapView;
    private GoogleMap map;
    private Geocoder geocoder;
    private Marker marker;
    private Button btnSaveLocation;


    protected GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mAdapter;
    private AutoCompleteTextView mAutoCompleteView;
    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));
    private LatLng selectedLatLng = new LatLng(0.0,0.0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_location);

        initViews();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /*clientId*/, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        mAutoCompleteView.setOnItemClickListener(mAutocompleteClickListener);
        mAdapter = new PlaceAutocompleteAdapter(this, android.R.layout.simple_list_item_1,
                mGoogleApiClient, BOUNDS_GREATER_SYDNEY, null);
        mAutoCompleteView.setAdapter(mAdapter);

        btnSaveLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //Log.e("WorkLocation", "Lat & Long : ");
                if (!TextUtils.isEmpty(mAutoCompleteView.getText().toString())) {
                    setWorkLocation();
                }else {
                    Toast.makeText(WorkLocationActivity.this, "Please select location", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void initViews() {
        setupToolbar();
        new KeyboardUtils().setupUI(findViewById(R.id.activity_user_edit_profile), WorkLocationActivity.this);
        btnSaveLocation = (Button) findViewById(R.id.btnSaveLocation);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this, Locale.getDefault());
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng point) {
                        setMarker(point);
                    }
                });
                try {
                    if (getIntent().getBooleanExtra("dataAvailable",false)) {
                        selectedLatLng = new LatLng(Double.parseDouble(getIntent().getStringExtra("lat")),Double.parseDouble(getIntent().getStringExtra("long")));

                        setMarker(selectedLatLng);
                    }else {
                        GPSTracker gpsTracker = new GPSTracker(WorkLocationActivity.this);
                        Location location = gpsTracker.getLocation();
                        if (!gpsTracker.canGetLocation()) {
                            if(!PrefsUtil.isStartGPSShowing) {
                                gpsTracker.showSettingsAlert();
                            }
                        }
                        if (location.getLatitude() == 9999999 || location.getLongitude() == 9999999) {
                            //Toast.makeText(getActivity(), "Location not available", Toast.LENGTH_SHORT).show();

                        } else {
                            selectedLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            setMarker(selectedLatLng);
                        }
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
        txtTitle.setText(R.string.work_location_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAutoCompleteView = (AutoCompleteTextView) findViewById(R.id.autocomplete_places);
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

    private AdapterView.OnItemClickListener
            mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i("WorkLocationActivity", "Autocomplete item selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Toast.makeText(WorkLocationActivity.this, "Clicked: " + item.description, Toast.LENGTH_SHORT).show();
            Log.i("WorkLocationActivity", "Called getPlaceById to get Place details for " + item.placeId);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e("WorkLocationActivity", "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }else {
                //selectedLatLng = places.get(0).getLatLng();
                setMarker(places.get(0).getLatLng());
            }
            places.release();
        }
    };

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("WorkLocationActivity", "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    public void setWorkLocation(){
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        params.add("locationType");
        params.add("workLocation");
        params.add("workLat");
        params.add("workLong");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(this).readInt("uId")));
        values.add("work");
        values.add(mAutoCompleteView.getText().toString());
        values.add(String.valueOf(selectedLatLng.latitude));
        values.add(String.valueOf(selectedLatLng.longitude));
        new ParseJSON(WorkLocationActivity.this, WebServiceUrl.ServiceUrl+WebServiceUrl.usereditlocation, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    CommonPOJO resultObj = (CommonPOJO) obj;
                    Toast.makeText(WorkLocationActivity.this, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK,resultIntent);
                    finish();
                    //String loginObj = (String) obj;
                }else {
                    Toast.makeText(WorkLocationActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    public void setMarker(final LatLng point){
        /*try {
            List<Address> addresses = new ArrayList<>();
            try {
                addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            android.location.Address address = addresses.get(0);

            if (address != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i) + ", ");
                }
                selectedLatLng = point;
                mAutoCompleteView.setText(sb.toString());
                //Toast.makeText(WorkLocationActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
            }

            //remove previously placed Marker
            if (marker != null) {
                marker.remove();
            }

            //place marker where user just clicked
            marker = map.addMarker(new MarkerOptions().position(point).title("Marker")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pick_up_marker)));
            moveToCurrentLocation(selectedLatLng);
        } catch (Exception e) {
            Toast.makeText(WorkLocationActivity.this, "Cannot get address from clicked location", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }*/

        Runnable newthread = new Runnable() {

            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            selectedLatLng = point;
                            Location temp = new Location(LocationManager.GPS_PROVIDER);
                            temp.setLatitude(point.latitude);
                            temp.setLongitude(point.longitude);
                            GeocoderHelper gHelper = new GeocoderHelper();
                            gHelper.fetchAddress(WorkLocationActivity.this, temp,
                                    mAutoCompleteView, mAutocompleteClickListener);

                            //remove previously placed Marker
                            if (marker != null) {
                                marker.remove();
                            }

                            //place marker where user just clicked
                            marker = map.addMarker(new MarkerOptions().position(point).title("Work")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pick_up_marker)));
                            moveToCurrentLocation(selectedLatLng);
                        }
                    });


                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WorkLocationActivity.this,
                                    "Cannot get address from location",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    e.printStackTrace();
                }
            }

        };

        Thread t = new Thread(newthread);
        t.start();
    }

    private void moveToCurrentLocation(LatLng currentLocation)
    {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 777) {
            if(PrefsUtil.dialogStartGPS != null) {
                PrefsUtil.dialogStartGPS.dismiss();
                PrefsUtil.isStartGPSShowing = false;
            }
            if(PrefsUtil.isStartGPSShowing){
                PrefsUtil.isStartGPSShowing = false;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(PrefsUtil.dialogStartGPS != null){
            PrefsUtil.dialogStartGPS.dismiss();
            PrefsUtil.dialogStartGPS = null;
            PrefsUtil.isStartGPSShowing = false;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection")) {
                if (event.getMessage().equalsIgnoreCase("disconnected")) {
                    if (!(new ConnectionCheck().isNetworkConnected(WorkLocationActivity.this))) {
                        Log.e("RideDetailActivity", "disconnected");
                        if (!ApplicationController.isOnline) {
                            if (PrefsUtil.isInternetConnectedShowing) {
                                if (PrefsUtil.dialogInternetConnected != null) {
                                    PrefsUtil.dialogInternetConnected.dismiss();
                                    PrefsUtil.isInternetConnectedShowing = false;
                                }
                            }
                            final Dialog d = new Dialog(WorkLocationActivity.this,
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
