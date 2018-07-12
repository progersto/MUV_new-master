package com.muvit.passenger.Fragments;

import android.Manifest;
import android.animation.LayoutTransition;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.muvit.passenger.Activities.HomeActivity;
import com.muvit.passenger.Activities.Step2Activity;
import com.muvit.passenger.Adapters.HomeAdapter;
import com.muvit.passenger.Adapters.SubCarTypeAdapter;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.GeoLocation.activity.GeocoderHelper;
import com.muvit.passenger.GeoLocation.adapter.PlaceAutocompleteAdapter;
import com.muvit.passenger.GeoLocation.logger.Log;
import com.muvit.passenger.Models.CarTypePOJO;
import com.muvit.passenger.Models.CarsItem;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.Models.SubCarTypeItem;
import com.muvit.passenger.Models.SubCarTypePOJO;
import com.muvit.passenger.Models.UserLocationPOJO;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.GPSTracker;
import com.muvit.passenger.Utils.KeyboardUtils;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.location.places.Place.TYPE_COUNTRY;

/**
 * Created by nct119 on 28/10/16.
 */

public class HomeFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1; // 1 second
    protected GoogleApiClient mGoogleApiClient;
    GoogleMap googleMap;
    LatLng homeLocation, workLocation;
    boolean isHomeLocation = false, isWorkLocation = false;
    Dialog dialog;
    private RecyclerView recyclerView;
    private RecyclerView rvSubType;
    private LinearLayoutManager layoutManager;
    private LinearLayoutManager carTypelayoutManager;
    private ArrayList<CarsItem> arrayList;
    private ArrayList<SubCarTypeItem> caryTypeList;
    private HomeAdapter adapter;
    private SubCarTypeAdapter subCarTypeAdapter;
    private ImageView imgPrev, imgNext, imgSubPrev, imgSubNext, imgLocation;
    private String url;
    private ArrayList<String> params;
    private ArrayList<String> values;
    private PlaceAutocompleteAdapter mAdapter;
    private AutoCompleteTextView mAutoCompleteView;
    private Geocoder geocoder;
    private Marker marker;
    private LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));
    private LatLng selectedLatLng = new LatLng(0.0, 0.0);
    private LinearLayout subCarLayout, carLayout;
    private LocationManager locationManager;
    private Timer mTimer1;

    private TimerTask mTt1;
    private Handler mTimerHandler = new Handler();
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                android.util.Log.e("HomeLocationActivity", "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            } else {
                //selectedLatLng = places.get(0).getLatLng();
                //setMarker(places.get(0).getLatLng());
                if (marker != null) {
                    marker.remove();
                }
                selectedLatLng = places.get(0).getLatLng();
                //place marker where user just clicked
                try {
                    marker = googleMap.addMarker(new MarkerOptions().position(places.get(0).getLatLng()).title("Pick Up")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pick_up_marker)));
                    moveToCurrentLocation(places.get(0).getLatLng());
                    KeyboardUtils.hideSoftKeyboard(getActivity());
                } catch (NullPointerException npe) {
                    npe.printStackTrace();
                }
            }
            places.release();
        }
    };
    private AdapterView.OnItemClickListener
            mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i("HomeFragment", "Autocomplete item selected: " + item.description);

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            //Toast.makeText(getActivity(), "Clicked: " + item.description, Toast.LENGTH_SHORT).show();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(rootView);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getBaseContext()).addApi(Places.GEO_DATA_API).build();
        mGoogleApiClient.connect();

        startTimer();

        locationManager = (LocationManager) getActivity()
                .getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

        geocoder = new Geocoder(getActivity(), Locale.getDefault());
        mAutoCompleteView.setOnItemClickListener(mAutocompleteClickListener);
        AutocompleteFilter.Builder builder = new AutocompleteFilter.Builder();
        builder.setTypeFilter(TYPE_COUNTRY);

        //AutocompleteFilter filter = builder.setCountry("in").build();
        AutocompleteFilter filter = builder.build();
        mAdapter = new PlaceAutocompleteAdapter(getContext(), android.R.layout.simple_list_item_1,
                mGoogleApiClient, BOUNDS_GREATER_SYDNEY, filter);
        mAutoCompleteView.setAdapter(mAdapter);

        arrayList = new ArrayList<CarsItem>();
        caryTypeList = new ArrayList<SubCarTypeItem>();
        adapter = new HomeAdapter(getActivity(), arrayList, new HomeAdapter.SelectCarCallback() {
            @Override
            public void onCarSelected(CarsItem carsItem) {
                KeyboardUtils.hideSoftKeyboard(getActivity());
                if (carsItem.isSelected) {
                    if (!mAutoCompleteView.getText().toString().trim().isEmpty() || !mAutoCompleteView.getText().toString().trim().equalsIgnoreCase("")) {
                        getSubCarType(String.valueOf(carsItem.getId()));
                    } else {
                        Toast.makeText(getActivity(), "Please enter location to continue", Toast.LENGTH_SHORT).show();
                        adapter.resetSelection();
                        adapter.notifyDataSetChanged();
                        caryTypeList.clear();
                        subCarTypeAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        subCarTypeAdapter = new SubCarTypeAdapter(getActivity(), caryTypeList, new SubCarTypeAdapter.SelectCarCallback() {
            @Override
            public void onCarSelected(SubCarTypeItem subCarTypeItem) {
                KeyboardUtils.hideSoftKeyboard(getActivity());
                if (!mAutoCompleteView.getText().toString().isEmpty()) {
                    Intent i = new Intent(getActivity(), Step2Activity.class);
                    i.putExtra("lat", selectedLatLng.latitude);
                    i.putExtra("long", selectedLatLng.longitude);
                    i.putExtra("location", mAutoCompleteView.getText().toString());
                    i.putExtra("carItemId", adapter.getSelectedCar().getId());
                    i.putExtra("subCarTypeId", subCarTypeItem.getId());
                    startActivity(i);
                }
            }
        });
        recyclerView.setAdapter(adapter);
        rvSubType.setAdapter(subCarTypeAdapter);

        imgNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView,
                            null, layoutManager.findLastVisibleItemPosition() + 1);
                } catch (Exception e) {

                }

            }
        });

        imgPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView,
                            null, layoutManager.findFirstVisibleItemPosition() - 1);
                } catch (Exception e) {
                }

            }
        });


        imgSubNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    rvSubType.getLayoutManager().smoothScrollToPosition(rvSubType,
                            null,
                            carTypelayoutManager.findLastVisibleItemPosition() + 1);
                } catch (Exception e) {

                }

            }
        });

        imgSubPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    rvSubType.getLayoutManager().smoothScrollToPosition(rvSubType,
                            null,
                            carTypelayoutManager.findFirstVisibleItemPosition() - 1);
                } catch (Exception e) {
                }

            }
        });

        imgLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserLocation();
            }
        });
        getCarTypes();
        return rootView;
    }

    private void initViews(View rootView) {


        recyclerView = rootView.findViewById(R.id.recyclerView);
        rvSubType = rootView.findViewById(R.id.rvSubType);
        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        carTypelayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        rvSubType.setLayoutManager(carTypelayoutManager);
        rvSubType.setHasFixedSize(true);
        mAutoCompleteView = rootView.findViewById(R.id.autocomplete_places);
        imgPrev = rootView.findViewById(R.id.imgPrev);
        imgNext = rootView.findViewById(R.id.imgNext);
        imgSubPrev = rootView.findViewById(R.id.imgSubPrev);
        imgSubNext = rootView.findViewById(R.id.imgSubNext);
        imgLocation = rootView.findViewById(R.id.imgLocation);
        subCarLayout = rootView.findViewById(R.id.subCarLayout);
        carLayout = rootView.findViewById(R.id.carLayout);

        try {
            carLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            subCarLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            subCarLayout.getLayoutTransition().setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setUpMap();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("HomeFragment", "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        Toast.makeText(getContext(),
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {

            EventBus.getDefault().unregister(this);
        } catch (Exception e) {

        }
        try {

            mGoogleApiClient.disconnect();
        } catch (Exception e) {

        }

    }

    public void setUpMap() {
        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentByTag("mapFragment");
        if (mapFragment == null) {
            mapFragment = new SupportMapFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.mapFragmentContainer, mapFragment, "mapFragment");
            ft.commit();
            fm.executePendingTransactions();
        }
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap1) {
                try {
                    googleMap = googleMap1;
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng point) {
                            if (!mAutoCompleteView.isPopupShowing()) {
                                setMarker(point);
                            }
                            KeyboardUtils.hideSoftKeyboard(getActivity());

                        }
                    });
                    // googleMap.setMyLocationEnabled(true);
                    GPSTracker gpsTracker = new GPSTracker(getActivity());
                    Location location = gpsTracker.getLocation();
                    if (!gpsTracker.canGetLocation()) {
                        if (!PrefsUtil.isStartGPSShowing) {
                            gpsTracker.showSettingsAlert();
                        }
                    }
                    if (location.getLatitude() == 9999999 || location.getLongitude() == 9999999) {
                        //Toast.makeText(getActivity(), "Location not available", Toast.LENGTH_SHORT).show();

                    } else {
                        selectedLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        setMarker(selectedLatLng);
                    }
                    /*Geocoder geocoder = new Geocoder(getActivity());
                    List<Address> addresses  = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                    Log.e("UserProfileFragment", "onMapReady: "+addresses.get(0).getCountryName());*/

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
                    //googleMap.addMarker(markerOptions);
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(markerOptions.getPosition());

                    LatLngBounds bounds = builder.build();
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));
                    googleMap.moveCamera(CameraUpdateFactory.zoomTo(18));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getCarTypes() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.usergetcartype;
        ArrayList<String> params = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        new ParseJSON(getActivity(), url, params, values, CarTypePOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    CarTypePOJO resultObj = (CarTypePOJO) obj;
                    if (resultObj.isStatus()) {
                        arrayList.addAll(resultObj.getCars());
                    }
                    adapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    public void getUserLocation() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.usergetlocation;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(getActivity()).readInt("uId")));
        new ParseJSON(getActivity(), url, params, values, UserLocationPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    UserLocationPOJO resultObj = (UserLocationPOJO) obj;
                    if (!TextUtils.isEmpty(resultObj.getUserLocation().get(0).getHomeLocation())) {
                        isHomeLocation = true;
                        homeLocation = new LatLng(Double.parseDouble(resultObj.getUserLocation().get(0).getHomeLat()), Double.parseDouble(resultObj.getUserLocation().get(0).getHomeLong()));
                    } else {
                        isHomeLocation = false;
                    }

                    if (!TextUtils.isEmpty(resultObj.getUserLocation().get(0).getWorkLocation())) {
                        isWorkLocation = true;
                        workLocation = new LatLng(Double.parseDouble(resultObj.getUserLocation().get(0).getWorkLat()), Double.parseDouble(resultObj.getUserLocation().get(0).getWorkLong()));
                    } else {
                        isWorkLocation = false;
                    }
                    showLocationDialog();

                } else {
                    Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    public void setMarker(final LatLng point) {

        Runnable newthread = new Runnable() {

            @Override
            public void run() {
                try {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    selectedLatLng = point;
                                    Location temp = new Location(LocationManager.GPS_PROVIDER);
                                    temp.setLatitude(point.latitude);
                                    temp.setLongitude(point.longitude);
                                    GeocoderHelper gHelper = new GeocoderHelper();
                                    gHelper.fetchAddress(getActivity(), temp, mAutoCompleteView, mAutocompleteClickListener);

                                    //remove previously placed Marker
                                    if (marker != null) {
                                        marker.remove();
                                    }

                                    //place marker where user just clicked
                                    try {
                                        marker = googleMap.addMarker(new MarkerOptions().position(point).title("Pick Up")
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pick_up_marker)));
                                    } catch (NullPointerException npe) {
                                        npe.printStackTrace();
                                    }
                                    moveToCurrentLocation(selectedLatLng);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Cannot get address from location", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    e.printStackTrace();
                }
            }

        };

        Thread t = new Thread(newthread);
        t.start();
    }

    private void moveToCurrentLocation(LatLng currentLocation) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
        //googleMap.animateCamera(CameraUpdateFactory.zoomTo(18),2000,null);
        // Zoom in, animating the camera.
        //googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        //googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);


    }

    private void getSubCarType(String carTypeId) {
        caryTypeList.clear();
        //subCarTypeAdapter.notifyDataSetChanged();
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.getsubcartypes;
        ArrayList<String> params = new ArrayList<>();
        params.add("carTypeId");
        ArrayList<String> values = new ArrayList<>();
        values.add(carTypeId);
        new ParseJSON(getActivity(), url, params, values, SubCarTypePOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    SubCarTypePOJO resultObj = (SubCarTypePOJO) obj;
                    caryTypeList.addAll(resultObj.getSubCarType());
                    subCarTypeAdapter.notifyDataSetChanged();
                    subCarTypeAdapter.resetSelection();
                    subCarLayout.setVisibility(View.VISIBLE);

                } else {
                    Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    private void showLocationDialog() {
        dialog = new Dialog(getActivity()
        );
        try {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog.setContentView(R.layout.dialog_user_location);
        dialog.setCancelable(false);

        TextView txtClose = (TextView) dialog.findViewById(R.id.txtClose);
        TextView txtHome = (TextView) dialog.findViewById(R.id.txtHome);
        TextView txtWork = (TextView) dialog.findViewById(R.id.txtWork);

        if (!isHomeLocation) {
            txtHome.setText("No Home Location Set");
        }

        if (!isWorkLocation) {
            txtWork.setText("No Work Location Set");
        }

        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        txtHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isHomeLocation) {
                    setMarker(homeLocation);
                    dialog.dismiss();
                } else {
                    //Toast.makeText(getActivity(), "You have not set Home location", Toast.LENGTH_SHORT).show();
                }


            }
        });

        txtWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWorkLocation) {
                    setMarker(workLocation);
                    dialog.dismiss();
                } else {
                    //Toast.makeText(getActivity(), "You have not set Work location", Toast.LENGTH_SHORT).show();
                }


            }
        });

        // now that the dialog is set up, it's time to show it
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onStart() {
        super.onStart();
        android.util.Log.e("HomeFragment", "onStart: ");
        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {

        }

    }

    @Override
    public void onStop() {
        super.onStop();
        android.util.Log.e("HomeFragment", "onStart: ");
        stopTimer();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {

        if (event.getType().equalsIgnoreCase("cancelride") || event.getType().equalsIgnoreCase("rideInfo")) {
            android.util.Log.e("HomeFragment", "onMessageEvent: ");
            try {

                adapter.resetSelection();
                adapter.notifyDataSetChanged();
                caryTypeList.clear();
                subCarTypeAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity() instanceof HomeActivity){
            ((HomeActivity)getActivity()).showToolbar();
            ((HomeActivity)getActivity()).hideToolbarTiltle();
            ((HomeActivity)getActivity()).turnActionBarIconBlack();
        }


        startTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        startTimer();
    }

    @Override
    public void onLocationChanged(Location location) {
        setMarker(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void stopTimer() {
        if (mTimer1 != null) {
            mTimer1.cancel();
            mTimer1.purge();
        }
    }

    private void startTimer() {
        mTimer1 = new Timer();
        mTt1 = new TimerTask() {
            public void run() {
                mTimerHandler.post(new Runnable() {
                    public void run() {
                        GPSTracker gps = new GPSTracker(getActivity());
                        if (gps.isGPSEnabled) {
                            if (gps.getLatitude() == 9999999 || gps.getLatitude() == 0
                                    || gps.getLongitude() == 9999999 || gps.getLongitude() == 0) {
                                if (!(PrefsUtil.isLocationNotFoundShowing || PrefsUtil.isStartGPSShowing)) {
                                    final Dialog d = new Dialog(getActivity(), android.R.style.Theme_Light_NoTitleBar);
                                    d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    d.setContentView(R.layout.dialog_location_not_found);
                                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                                    lp.copyFrom(d.getWindow().getAttributes());
                                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                                    lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                                    d.getWindow().setAttributes(lp);
                                    TextView txtRetry = (TextView) d.findViewById(R.id.txtRetry);
                                    txtRetry.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            PrefsUtil.isLocationNotFoundShowing = false;
                                            d.dismiss();
                                        }
                                    });
                                    d.setCancelable(true);
                                    d.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            PrefsUtil.isLocationNotFoundShowing = false;
                                        }
                                    });
                                    PrefsUtil.isLocationNotFoundShowing = true;
                                    d.show();
                                }
                            } else {
                                if (selectedLatLng != null) {
                                    /*Location l = new Location(LocationManager.GPS_PROVIDER);
                                    l.setLatitude(selectedLatLng.latitude);
                                    l.setLongitude(selectedLatLng.longitude);
                                    if (!(gps.getLocation() == l)) {*/
                                    if (!(gps.getLatitude() == selectedLatLng.latitude
                                            && gps.getLongitude() == selectedLatLng.longitude)) {
                                        onLocationChanged(gps.getLocation());
                                    }
                                } else {
                                    onLocationChanged(gps.getLocation());
                                }
                            }
                        } else {
                            if (!PrefsUtil.isStartGPSShowing) {
                                if (PrefsUtil.dialogStartGPS != null) {
                                    PrefsUtil.dialogStartGPS.dismiss();
                                    PrefsUtil.isStartGPSShowing = false;
                                }
                                gps.showSettingsAlert();
                            }
                        }
                    }
                });
            }
        };

        mTimer1.schedule(mTt1, 1000, 60000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 777) {
            if (PrefsUtil.dialogStartGPS != null) {
                PrefsUtil.dialogStartGPS.dismiss();
                PrefsUtil.isStartGPSShowing = false;
            }
            if (PrefsUtil.isStartGPSShowing) {
                PrefsUtil.isStartGPSShowing = false;
            }
        }
    }
}
