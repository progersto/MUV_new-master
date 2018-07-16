package com.muvit.passenger.Fragments;

import android.Manifest;
import android.animation.LayoutTransition;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.MapFragment;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.koushikdutta.ion.Ion;
import com.muvit.passenger.Activities.HomeActivity;
import com.muvit.passenger.Activities.Step2Activity;
import com.muvit.passenger.Adapters.HomeAdapter;
import com.muvit.passenger.Adapters.SubCarTypeAdapter;
import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.GeoLocation.activity.GeocoderHelper;
import com.muvit.passenger.GeoLocation.adapter.PlaceAutocompleteAdapter;
import com.muvit.passenger.GeoLocation.logger.Log;
import com.muvit.passenger.Models.CarTypePOJO;
import com.muvit.passenger.Models.CarsItem;
import com.muvit.passenger.Models.ConfirmRidePOJO;
import com.muvit.passenger.Models.DefaultPaymentMethodPOJO;
import com.muvit.passenger.Models.FareEstimateItem;
import com.muvit.passenger.Models.FareEstimatePOJO;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.Models.SubCarTypeItem;
import com.muvit.passenger.Models.SubCarTypePOJO;
import com.muvit.passenger.Models.UserLocationPOJO;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.location.places.Place.TYPE_COUNTRY;

/**
 * Created by nct119 on 28/10/16.
 */

public class HomeFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
//            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));
    protected GoogleApiClient mGoogleApiClient;
    Dialog promoDialog, fareDialog;
    RelativeLayout cancelBtn, okBtn, cancelFare;
    TextView fareView, startLocation, endLocation, car_name;

    LatLng fromLat;
    String carTypeId = "0";
    FareEstimateItem fareSummaryItem;
    String defaultPaymentMethod = "w";
    String subCarTypeId = "";
    TextSwitcher txtSwitcherMessage;
    LatLng homeLocation, workLocation;
    boolean isHomeLocation = false, isWorkLocation = false;
    Dialog dialog;
    private Toolbar toolbar;
    private TextView txtTitle, txtSource;
    private ImageView imgWallet, dashLine, imgLocation, close_anim;
    LinearLayout imgCash, txtFareEstimate;
    RelativeLayout promo_btn;
    private Button btnBooknRide;
    private PopupWindow popupWindow;
    private GoogleMap map;
    private Marker marker;
    private LatLng selectedLatLng = new LatLng(0.0, 0.0);
    private Geocoder geocoder;
    private RelativeLayout layoutOverlay;
    private PlaceAutocompleteAdapter mAdapter;
    private AutoCompleteTextView mAutoCompleteView;
    private Marker pickupMarker;
        private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e("Step2Activity", "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            } else {
                //remove previously placed Marker
                if (marker != null) {
                    marker.remove();
                }
                selectedLatLng = places.get(0).getLatLng();
                //place marker where user just clicked
                marker = map.addMarker(new MarkerOptions().position(places.get(0).getLatLng()).title("Drop Off")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.drop_off_marker)));
                moveToCurrentLocation();
            }
            places.release();
        }
    };
//    private AdapterView.OnItemClickListener
//            mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
//            final String placeId = String.valueOf(item.placeId);
//            Log.i("Step2Activity", "Autocomplete item selected: " + item.description);
//
//            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
//            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
//
//            //Toast.makeText(Step2Activity.this, "Clicked: " + item.description, Toast.LENGTH_SHORT).show();
//            Log.i("Step2Activity", "Called getPlaceById to get Place details for " + item.placeId);
//        }
//    };
    private ImageView car_img_fare;


    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1; // 1 second
    GoogleMap googleMap;
    private RecyclerView recyclerView;
    private RecyclerView rvSubType;
    private LinearLayoutManager layoutManager;
    private LinearLayoutManager carTypelayoutManager;
    private ArrayList<CarsItem> arrayList;
    private ArrayList<SubCarTypeItem> caryTypeList;
    private HomeAdapter adapter;
    private SubCarTypeAdapter subCarTypeAdapter;
    private ImageView imgPrev, imgNext, imgSubPrev, imgSubNext;
    private String url;
    private ArrayList<String> params;
    private ArrayList<String> values;
    private LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));
    private LinearLayout subCarLayout, carLayout;
    private LocationManager locationManager;
    private Timer mTimer1;

    private TimerTask mTt1;
    private Handler mTimerHandler = new Handler();
//    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
//        @Override
//        public void onResult(PlaceBuffer places) {
//            if (!places.getStatus().isSuccess()) {
//                android.util.Log.e("HomeLocationActivity", "Place query did not complete. Error: " + places.getStatus().toString());
//                places.release();
//                return;
//            } else {
//                //selectedLatLng = places.get(0).getLatLng();
//                //setMarker(places.get(0).getLatLng());
//                if (marker != null) {
//                    marker.remove();
//                }
//                selectedLatLng = places.get(0).getLatLng();
//                //place marker where user just clicked
//                try {
//                    marker = googleMap.addMarker(new MarkerOptions().position(places.get(0).getLatLng()).title("Pick Up")
//                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pick_up_marker)));
//                    moveToCurrentLocation(places.get(0).getLatLng());
//                    KeyboardUtils.hideSoftKeyboard(getActivity());
//                } catch (NullPointerException npe) {
//                    npe.printStackTrace();
//                }
//            }
//            places.release();
//        }
//    };
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
        initViewsNew(rootView);
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
//        mAutoCompleteView.setOnItemClickListener(mAutocompleteClickListener);
//        AutocompleteFilter.Builder builder = new AutocompleteFilter.Builder();
//        builder.setTypeFilter(TYPE_COUNTRY);
//
//        //AutocompleteFilter filter = builder.setCountry("in").build();
//        AutocompleteFilter filter = builder.build();
//        mAdapter = new PlaceAutocompleteAdapter(getContext(), android.R.layout.simple_list_item_1,
//                mGoogleApiClient, BOUNDS_GREATER_SYDNEY, filter);
//        mAutoCompleteView.setAdapter(mAdapter);
//
//        arrayList = new ArrayList<CarsItem>();
//        caryTypeList = new ArrayList<SubCarTypeItem>();
//        adapter = new HomeAdapter(getActivity(), arrayList, new HomeAdapter.SelectCarCallback() {
//            @Override
//            public void onCarSelected(CarsItem carsItem) {
//                KeyboardUtils.hideSoftKeyboard(getActivity());
//                if (carsItem.isSelected) {
//                    if (!mAutoCompleteView.getText().toString().trim().isEmpty() || !mAutoCompleteView.getText().toString().trim().equalsIgnoreCase("")) {
//                        getSubCarType(String.valueOf(carsItem.getId()));
//                    } else {
//                        Toast.makeText(getActivity(), "Please enter location to continue", Toast.LENGTH_SHORT).show();
//                        adapter.resetSelection();
//                        adapter.notifyDataSetChanged();
//                        caryTypeList.clear();
//                        subCarTypeAdapter.notifyDataSetChanged();
//                    }
//                }
//            }
//        });
//
//        subCarTypeAdapter = new SubCarTypeAdapter(getActivity(), caryTypeList, new SubCarTypeAdapter.SelectCarCallback() {
//            @Override
//            public void onCarSelected(SubCarTypeItem subCarTypeItem) {
//                KeyboardUtils.hideSoftKeyboard(getActivity());
//                if (!mAutoCompleteView.getText().toString().isEmpty()) {
//                    Intent i = new Intent(getActivity(), Step2Activity.class);
//                    i.putExtra("lat", selectedLatLng.latitude);
//                    i.putExtra("long", selectedLatLng.longitude);
//                    i.putExtra("location", mAutoCompleteView.getText().toString());
//                    i.putExtra("carItemId", adapter.getSelectedCar().getId());
//                    i.putExtra("subCarTypeId", subCarTypeItem.getId());
//                    startActivity(i);
//                }
//            }
//        });
//        recyclerView.setAdapter(adapter);
//        rvSubType.setAdapter(subCarTypeAdapter);
//
//        imgNext.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView,
//                            null, layoutManager.findLastVisibleItemPosition() + 1);
//                } catch (Exception e) {
//
//                }
//
//            }
//        });
//
//        imgPrev.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView,
//                            null, layoutManager.findFirstVisibleItemPosition() - 1);
//                } catch (Exception e) {
//                }
//
//            }
//        });
//
//
//        imgSubNext.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    rvSubType.getLayoutManager().smoothScrollToPosition(rvSubType,
//                            null,
//                            carTypelayoutManager.findLastVisibleItemPosition() + 1);
//                } catch (Exception e) {
//
//                }
//
//            }
//        });
//
//        imgSubPrev.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    rvSubType.getLayoutManager().smoothScrollToPosition(rvSubType,
//                            null,
//                            carTypelayoutManager.findFirstVisibleItemPosition() - 1);
//                } catch (Exception e) {
//                }
//
//            }
//        });
//
//        imgLocation.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getUserLocation();
//            }
//        });
//        getCarTypes();


//        initViewsNew();
        getDefaultPaymentMethod();
        defaultPaymentMethod = "c";

        mAutoCompleteView.setOnItemClickListener(mAutocompleteClickListener);
        mAdapter = new PlaceAutocompleteAdapter(getContext(), android.R.layout.simple_list_item_1,
                mGoogleApiClient, BOUNDS_GREATER_SYDNEY, null);
        mAutoCompleteView.setAdapter(mAdapter);

        btnBooknRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*intent = new Intent(Step2Activity.this, RideInformationActivity.class);
                startActivity(intent);*/
                if (!TextUtils.isEmpty(mAutoCompleteView.getText().toString())) {
                    getFareSummary(false);
                } else {
                    Toast.makeText(getContext(), "Please Select Drop off Location" +
                            "", Toast.LENGTH_SHORT).show();
                }
            }
        });

        txtFareEstimate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(mAutoCompleteView.getText().toString())) {
                    txtFareEstimate.setClickable(false);
                    getFareSummary(true);
                } else {
                    Toast.makeText(getContext(), "Please Select Drop off Location" +
                            "", Toast.LENGTH_SHORT).show();
                }

            }
        });
        setUpMap();
        imgLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserLocation();
            }
        });

        return rootView;
    }//onCreateView


    private void initViewsNew(View rootView) {
        setupToolbar(rootView);
        imgWallet = rootView.findViewById(R.id.imgWallet);
        imgCash = rootView.findViewById(R.id.card_btn);
        btnBooknRide = rootView.findViewById(R.id.btnBooknRide);
        promo_btn = (RelativeLayout) rootView.findViewById(R.id.promo_btn);
        initPromoDialog();
        initEstimate();
        promo_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // showPromoDialog();
                promoDialog.show();
            }
        });


        txtFareEstimate = rootView.findViewById(R.id.txtFareEstimate);
        txtSource = rootView.findViewById(R.id.txtSource);
        txtSource.setMovementMethod(new ScrollingMovementMethod());
        dashLine = rootView.findViewById(R.id.dashLine);
        imgLocation = rootView.findViewById(R.id.imgLocation);
        layoutOverlay = rootView.findViewById(R.id.layoutOverlayAnim);
        close_anim = (ImageView) rootView.findViewById(R.id.close_anim);
        close_anim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutOverlay.setVisibility(View.GONE);
            }
        });

        txtSwitcherMessage = rootView.findViewById(R.id.txtSwitcherMessage);
        txtSwitcherMessage.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                // TODO Auto-generated method stub
                // create a TextView
                TextView t = new TextView(getContext());
                // set the gravity of text to top and center horizontal
                t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                t.setTextColor(Color.WHITE);
                t.setTextSize(15);
                return t;
            }
        });

        txtFareEstimate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fareDialog.show();
            }
        });

        txtSwitcherMessage.setInAnimation(getContext(), android.R.anim.slide_in_left);
        txtSwitcherMessage.setOutAnimation(getContext(), android.R.anim.slide_out_right);
        geocoder = new Geocoder(getContext(), Locale.getDefault());
        mAutoCompleteView = rootView.findViewById(R.id.autocomplete_places);
        dashLine.bringToFront();
        dashLine.invalidate();
        new KeyboardUtils().setupUI(rootView.findViewById(R.id.activity_step2), getActivity());

        final long period = 3000;
        final int[] message_counter = {0};
        final String[] messageArray = {getResources().getString(R.string.wait_message_1),
                getResources().getString(R.string.wait_message_2),
                getResources().getString(R.string.wait_message_3),
                getResources().getString(R.string.wait_message_4)};
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (message_counter[0] >= 4) {
                            message_counter[0] = 0;
                        }
                        txtSwitcherMessage.setText(messageArray[message_counter[0]]);
                    }
                });
                message_counter[0] = (message_counter[0] + 1);
            }
        }, 0, period);
    }


    //    public void setPickupMarker(LatLng point, String location) {
//        try {
//            List<Address> addresses = new ArrayList<>();
//            try {
//                addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            Address address = addresses.get(0);
//
//            if (address != null) {
//                StringBuilder sb = new StringBuilder();
//                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
//                    sb.append(address.getAddressLine(i) + " ");
//                }
//                //Toast.makeText(WorkLocationActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
//            }
//
//            //remove previously placed Marker
//            if (pickupMarker != null) {
//                pickupMarker.remove();
//            }
//
//            //place marker where user just clicked
//            pickupMarker = map.addMarker(new MarkerOptions().position(point).title(location)
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pick_up_marker)));
//            pickupMarker.showInfoWindow();
//            //moveToCurrentLocation(dropoffLatLng);
//            moveToCurrentLocation1(point);
//            //moveToCurrentLocation(point);
//        } catch (Exception e) {
//            // Toast.makeText(Step2Activity.this, "Cannot get address from clicked location", Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//        }
//    }
    public void setPickupMarker(LatLng point, String location) {
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
            pickupMarker = googleMap.addMarker(new MarkerOptions().position(point).title(location)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pick_up_marker)));
            pickupMarker.showInfoWindow();
            //moveToCurrentLocation(dropoffLatLng);
            moveToCurrentLocation1(point);
            //moveToCurrentLocation(point);
        } catch (Exception e) {
            // Toast.makeText(Step2Activity.this, "Cannot get address from clicked location", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    private void setupToolbar(View rootView) {
        toolbar = (Toolbar) rootView.findViewById(R.id.toolBar);
        txtTitle = (TextView) rootView.findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.step2_title);
        //todo пока коментим толбар
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //todo пока коментим
                //   onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void initPromoDialog() {
        // final View view = getLayoutInflater().inflate(R.layout.promo_dialog, null);
        promoDialog = new Dialog(getContext());
        promoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        promoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        promoDialog.setContentView(R.layout.promo_dialog);
        cancelBtn = (RelativeLayout) promoDialog.findViewById(R.id.cancel_btn);
        okBtn = (RelativeLayout) promoDialog.findViewById(R.id.ok_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promoDialog.dismiss();
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promoDialog.dismiss();
            }
        });

        promoDialog.setCancelable(false);

    }

    private void initEstimate() {
        // final View view = getLayoutInflater().inflate(R.layout.promo_dialog, null);
        fareDialog = new Dialog(getContext());
        fareDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        fareDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        fareDialog.setContentView(R.layout.estimate_dialog);
        car_img_fare = (ImageView) fareDialog.findViewById(R.id.car_img_fare);
        cancelFare = (RelativeLayout) fareDialog.findViewById(R.id.cancel_fare);
        car_name = (TextView) fareDialog.findViewById(R.id.car_name);

        cancelFare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fareDialog.dismiss();
            }
        });


        fareView = fareDialog.findViewById(R.id.fare);
        startLocation = fareDialog.findViewById(R.id.src_addr);
        endLocation = fareDialog.findViewById(R.id.destination_addr);

        fareDialog.setCancelable(false);

    }

    private void popupEstimateNew(FareEstimateItem fareSummaryItem) {
        startLocation.setText(fareSummaryItem.getPickUpLocation());
        endLocation.setText(fareSummaryItem.getDropOffLocation());
        car_name.setText(fareSummaryItem.getCarTypeName());
        Ion.with(car_img_fare)
                .error(R.drawable.rides)
                .load(WebServiceUrl.carUrl + fareSummaryItem.getCarTypeImage());
        fareView.setText(fareSummaryItem.getFareDistanceCharges());
        fareDialog.show();

    }

    private void popupFareEstimate(FareEstimateItem fareSummaryItem) {
        View popUpView = getLayoutInflater().inflate(R.layout.popup_fare_estimate, null);
        popupWindow = new PopupWindow(popUpView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

        // Creation of popup
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        popupWindow.showAtLocation(popUpView, Gravity.CENTER, 0, 0);
        txtFareEstimate.setClickable(true);
        TextView txtClose = (TextView) popUpView.findViewById(R.id.txtClose);
        TextView txtCarType = (TextView) popUpView.findViewById(R.id.txtCarType);
        TextView txtPickUp = (TextView) popUpView.findViewById(R.id.txtPickUp);
        TextView txtDropOff = (TextView) popUpView.findViewById(R.id.txtDropOff);
        TextView txtMinFareKm = (TextView) popUpView.findViewById(R.id.txtMinFareKm);
        TextView txtMinFare = (TextView) popUpView.findViewById(R.id.txtMinFare);
        TextView txtExtraCharges = (TextView) popUpView.findViewById(R.id.txtExtraCharges);
        TextView txtPerKmCharges = (TextView) popUpView.findViewById(R.id.txtPerKmCharges);
        TextView txtExtraTime = (TextView) popUpView.findViewById(R.id.txtExtraTime);
        TextView txtTimeCharges = (TextView) popUpView.findViewById(R.id.txtTimeCharges);
        TextView txtTotalAmount = (TextView) popUpView.findViewById(R.id.txtTotalAmount);
        LinearLayout llTotalEstimation = (LinearLayout) popUpView.findViewById(R.id.llTotalEstimation);

        ImageView imgCar = (ImageView) popUpView.findViewById(R.id.imgCar);
        Ion.with(imgCar)
                .error(R.drawable.rides)
                .load(WebServiceUrl.carUrl + fareSummaryItem.getCarTypeImage());
        txtCarType.setText(fareSummaryItem.getCarTypeName());
        txtPickUp.setText(fareSummaryItem.getPickUpLocation());
        txtDropOff.setText(fareSummaryItem.getDropOffLocation());
        txtMinFareKm.setText("Min Fare (" + fareSummaryItem.getFareDistance() + "Km)");
        txtMinFare.setText(getString(R.string.currencySign) + fareSummaryItem.getFareDistanceCharges());

        /*uncomment it for only estimation and per km and min charge on fare estimation*/
        txtExtraCharges.setText("Per Km Charges after (" + fareSummaryItem.getFareDistance() + "Km)");
        txtPerKmCharges.setText(getString(R.string.currencySign) + fareSummaryItem.getFareAdditionalCharges());
        txtTimeCharges.setText(getString(R.string.currencySign) + String.valueOf(fareSummaryItem.getTimeChargesPerMin()));
        llTotalEstimation.setVisibility(View.GONE);


        /*uncomment it for final calculation on fare estimation*/
        /*txtExtraCharges.setText("Extra Km (" + fareSummaryItem.getEstimateExtraKm() + "Km)");
        txtPerKmCharges.setText(getString(R.string.currencySign) + fareSummaryItem.getTotalExtraCharges());
        txtExtraTime.setText("Extra Time (" + fareSummaryItem.getEstimateTime() + "Min)");
        txtTimeCharges.setText(getString(R.string.currencySign) + fareSummaryItem.getEstmatedTimeCharges());
        txtTotalAmount.setText(getString(R.string.currencySign) + fareSummaryItem.getFinalEstimatedTotal());
        llTotalEstimation.setVisibility(View.VISIBLE);*/

        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Step2Activity", "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        Toast.makeText(getContext(),
                "Could not connect to server",
                Toast.LENGTH_SHORT).show();
    }

    public void setUpMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);
//        mapFragment.getMapAsync(new OnMapReadyCallback() {
//            @Override
//            public void onMapReady(GoogleMap googleMap) {
//                map = googleMap;
//                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//                    @Override
//                    public void onMapClick(LatLng point) {
//                        setMarker(point);
//                    }
//                });
//                setPickupMarker(fromLat, txtSource.getText().toString());
//            }
//        });


//        FragmentManager fm = getChildFragmentManager();
//        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentByTag("mapFragment");
//        if (mapFragment == null) {
//            mapFragment = new SupportMapFragment();
//            FragmentTransaction ft = fm.beginTransaction();
//            ft.add(R.id.mapFragmentContainer, mapFragment, "mapFragment");
//            ft.commit();
//            fm.executePendingTransactions();
//        }
//====
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
//                                setMarker(point);
                                setMarkerOnMap(point);
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
                        try {
                            fromLat = new LatLng(selectedLatLng.latitude, selectedLatLng.longitude);
//                            txtSource.setText(mAutoCompleteView.getText().toString());
                            // TODO: 13.07.2018 пока ставим заглушку для carTypeId и subCarTypeId
//            carTypeId = String.valueOf(getIntent().getIntExtra("carItemId", 0));
//            subCarTypeId = String.valueOf(getIntent().getIntExtra("subCarTypeId", 0));
                            carTypeId = String.valueOf(3);
                            subCarTypeId = String.valueOf(2);
                        } catch (Exception e) {
                            fromLat = new LatLng(0.0, 0.0);
                            e.printStackTrace();
                        }
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

    public void setMarkerOnMap(final LatLng point) {

//        Runnable newthread = new Runnable() {
//
//            @Override
//            public void run() {
                try {
                    selectedLatLng = point;
                    Location temp = new Location(LocationManager.GPS_PROVIDER);
                    temp.setLatitude(point.latitude);
                    temp.setLongitude(point.longitude);
                    GeocoderHelper gHelper = new GeocoderHelper();
                    gHelper.fetchAddress(getActivity(), temp,
                            mAutoCompleteView, mAutocompleteClickListener);

                    //remove previously placed Marker
                    if (pickupMarker != null) {
                        pickupMarker.remove();
                    }

                    //place marker where user just clicked
                    pickupMarker = googleMap.addMarker(new MarkerOptions().position(point).title("Drop Off")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.drop_off_marker)));

                    moveToCurrentLocation();
                } catch (Exception e) {
                    Log.d("ss", e.getMessage());
//                    if (getActivity() != null) {
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(getActivity(), "Cannot get address from location", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
                    e.printStackTrace();
                }
//            }
//
//        };
//
//        Thread t = new Thread(newthread);
//        t.start();
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
                                    gHelper.fetchAddress1(getActivity(), temp, txtSource);//вписываем адрес

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


    private void moveToCurrentLocation1(LatLng currentLocation) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        // Zoom in, animating the camera.
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }

    private void getFareSummary(final boolean openDialog) {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.fareestimate;
        ArrayList<String> params = new ArrayList<>();
        params.add("carTypeId");
        params.add("subCarTypeId");
        params.add("pickupLat");
        params.add("pickupLong");
        params.add("dropoffLat");
        params.add("dropoffLong");
        ArrayList<String> values = new ArrayList<>();
        values.add(carTypeId);
        values.add(subCarTypeId);
        values.add(String.valueOf(fromLat.latitude));
        values.add(String.valueOf(fromLat.longitude));
        values.add(String.valueOf(selectedLatLng.latitude));
        values.add(String.valueOf(selectedLatLng.longitude));
        new ParseJSON(getContext(), url, params, values, FareEstimatePOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    FareEstimatePOJO resultObj = (FareEstimatePOJO) obj;
                    fareSummaryItem = resultObj.getFareSummary().get(0);
                    if (openDialog) {
                        popupEstimateNew(fareSummaryItem);
                    } else {
                        if (!TextUtils.isEmpty(mAutoCompleteView.getText().toString())) {
                            confirmRide();
                        } else {
                            Toast.makeText(getContext(), "Please choose drop off location", Toast.LENGTH_SHORT).show();
                        }

                    }
                } else {
                    Toast.makeText(getContext(), (String) obj, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void confirmRide() {
        android.util.Log.e("Step2Activity", "fareSummaryItem.getRidePathString() : " + fareSummaryItem.getRidePathString());
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.conformride;
        ArrayList<String> params = new ArrayList<>();
        params.add("carTypeId");
        params.add("custId");
        params.add("pickUpLat");
        params.add("pickUpLong");
        params.add("pickUpLocation");
        params.add("dropOffLat");
        params.add("dropOffLong");
        params.add("dropOffLocation");
        params.add("totalDistance");
        params.add("fareDistance");
        params.add("fareAdditionalKm");
        params.add("fareTime");
        params.add("fareDistanceCharges");
        params.add("fareAdditionalCharges");
        params.add("fareTimeCharges");
        params.add("totalExtraCharges");
        params.add("paymentType");
        params.add("ridePathString");
        params.add("subCarTypeId");
        params.add("isLongRide");
        final ArrayList<String> values = new ArrayList<>();
        values.add(carTypeId);
        values.add(String.valueOf(PrefsUtil.with(getContext()).readInt("uId")));
        values.add(String.valueOf(fromLat.latitude));
        values.add(String.valueOf(fromLat.longitude));
        values.add(fareSummaryItem.getPickUpLocation());
        values.add(String.valueOf(selectedLatLng.latitude));
        values.add(String.valueOf(selectedLatLng.longitude));
        values.add(fareSummaryItem.getDropOffLocation());
        values.add(String.valueOf(fareSummaryItem.getTotalDistance()));
        values.add(String.valueOf(fareSummaryItem.getFareDistance()));
        values.add(fareSummaryItem.getFareAdditionalKm());
        values.add(fareSummaryItem.getFareTime());
        values.add(fareSummaryItem.getFareDistanceCharges());
        values.add(fareSummaryItem.getFareAdditionalCharges());
        values.add(fareSummaryItem.getFareTimeCharges());
        values.add(String.valueOf(fareSummaryItem.getTotalExtraCharges()));
        values.add("c");
        values.add(fareSummaryItem.getRidePathString());
        values.add(subCarTypeId);
        values.add(fareSummaryItem.getIsLongRide());
        new ParseJSON(getContext(), url, params, values, ConfirmRidePOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    ConfirmRidePOJO resultObj = (ConfirmRidePOJO) obj;
                    PrefsUtil.with(getContext()).write("lastSendId", "");
                    Toast.makeText(getContext(), resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                    layoutOverlay.setVisibility(View.VISIBLE);

                    try {
                        FirebaseAnalytics mFirebaseAnalytics;
                        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "ride_request");
                        bundle.putString("time_stamp", String.valueOf(new Date()));
                        mFirebaseAnalytics.logEvent("ride_request", bundle);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //finish();
                } else {
                    Toast.makeText(getContext(), (String) obj, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getDefaultPaymentMethod() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.userdefaultpaymentmethod;

        ArrayList<String> params = new ArrayList<>();
        params.add("userId");

        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(getContext()).readInt("uId")));
        new ParseJSON(getContext(), url, params, values, DefaultPaymentMethodPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    DefaultPaymentMethodPOJO resultObj = (DefaultPaymentMethodPOJO) obj;
                    defaultPaymentMethod = resultObj.getPaymentMethod().get(0).getDefaultPaymentMethod();
                    if (defaultPaymentMethod.equalsIgnoreCase("w")) {
                        imgWallet.setImageResource(R.drawable.wallet_profile);
//                        imgCash.setImageResource(R.drawable.cash_gray);
//                        btnBooknRide.setBackgroundColor(getResources().getColor(R.color.yellowColor));
//                        btnBooknRide.setTypeface(null, Typeface.BOLD);
                    } else {
                        imgWallet.setImageResource(R.drawable.wallet_gray);
//                        imgCash.setImageResource(R.drawable.cash);
//                        btnBooknRide.setBackgroundColor(getResources().getColor(R.color.yellowColor));
//                        btnBooknRide.setTypeface(null, Typeface.BOLD);
                        defaultPaymentMethod = "c";
                    }
                    //Toast.makeText(Step2Activity.this, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), (String) obj, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getUserLocation() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.usergetlocation;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(getContext()).readInt("uId")));
        new ParseJSON(getContext(), url, params, values, UserLocationPOJO.class, new ParseJSON.OnResultListner() {
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
                    Toast.makeText(getContext(), (String) obj, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    private void showLocationDialog() {
        dialog = new Dialog(getContext());
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
                    // Toast.makeText(Step2Activity.this, "You have not set Home location", Toast.LENGTH_SHORT).show();
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
                    // Toast.makeText(Step2Activity.this, "You have not set Work location", Toast.LENGTH_SHORT).show();
                }


            }
        });

        // now that the dialog is set up, it's time to show it
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

//    @Override
//    public void onBackPressed() {
//        if (layoutOverlay.getVisibility() != View.VISIBLE) {
//            super.onBackPressed();
//        }
//
//    }


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
        if (event.getType().equalsIgnoreCase("cancelride")) {
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(false);
                builder.setMessage("We apologise, unfortunately all our drivers might be on trips at the moment, please try again later.");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //if user pressed "yes", then he is allowed to exit from application
                        // TODO: 13.07.2018 пока коментим finish
                        //  finish();
                    }
                });
                /*builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //if user select "No", just cancel this dialog and continue with app
                        dialog.cancel();
                    }
                });*/
                AlertDialog alert = builder.create();
                alert.show();
                //finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (event.getType().equalsIgnoreCase("rideInfo")) {
            // TODO: 13.07.2018 пока коментим finish
            //finish();
        }
        if (event.getType().equalsIgnoreCase("connection")) {
            if (event.getMessage().equalsIgnoreCase("disconnected")) {
                if (!(new ConnectionCheck().isNetworkConnected(getContext()))) {
                    android.util.Log.e("RideDetailActivity", "disconnected");
                    if (!ApplicationController.isOnline) {
                        if (PrefsUtil.isInternetConnectedShowing) {
                            if (PrefsUtil.dialogInternetConnected != null) {
                                PrefsUtil.dialogInternetConnected.dismiss();
                                PrefsUtil.isInternetConnectedShowing = false;
                            }
                        }
                        final Dialog d = new Dialog(getContext(),
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

    private void moveToCurrentLocation() {
        try {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            //the include method will calculate the min and max bound.
            builder.include(pickupMarker.getPosition());
            builder.include(marker.getPosition());

            LatLngBounds bounds = builder.build();

            int width = getResources().getDisplayMetrics().widthPixels;
            int height = (int) (getResources().getDisplayMetrics().heightPixels / 2.5);
            int padding = (int) (width * 0.13); // offset from edges of the map 12% of screen

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

            googleMap.animateCamera(cu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initViews(View rootView) {


        recyclerView = rootView.findViewById(R.id.recyclerView);
        //todo пока коментим
        //rvSubType = rootView.findViewById(R.id.rvSubType);
        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        carTypelayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        rvSubType.setLayoutManager(carTypelayoutManager);
        rvSubType.setHasFixedSize(true);
        mAutoCompleteView = rootView.findViewById(R.id.autocomplete_places);
        imgLocation = rootView.findViewById(R.id.imgLocation);
        //todo пока коментим
//        imgPrev = rootView.findViewById(R.id.imgPrev);
//        imgNext = rootView.findViewById(R.id.imgNext);
//        imgSubPrev = rootView.findViewById(R.id.imgSubPrev);
//        imgSubNext = rootView.findViewById(R.id.imgSubNext);
//        subCarLayout = rootView.findViewById(R.id.subCarLayout);
//        carLayout = rootView.findViewById(R.id.carLayout);

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

//        setUpMap();
    }

//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        Log.e("HomeFragment", "onConnectionFailed: ConnectionResult.getErrorCode() = "
//                + connectionResult.getErrorCode());
//
//        Toast.makeText(getContext(),
//                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
//                Toast.LENGTH_SHORT).show();
//    }

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

//    public void setUpMap() {
//        FragmentManager fm = getChildFragmentManager();
//        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentByTag("mapFragment");
//        if (mapFragment == null) {
//            mapFragment = new SupportMapFragment();
//            FragmentTransaction ft = fm.beginTransaction();
//            ft.add(R.id.mapFragmentContainer, mapFragment, "mapFragment");
//            ft.commit();
//            fm.executePendingTransactions();
//        }
//        mapFragment.getMapAsync(new OnMapReadyCallback() {
//            @Override
//            public void onMapReady(GoogleMap googleMap1) {
//                try {
//                    googleMap = googleMap1;
//                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                        // TODO: Consider calling
//                        //    ActivityCompat#requestPermissions
//                        // here to request the missing permissions, and then overriding
//                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                        //                                          int[] grantResults)
//                        // to handle the case where the user grants the permission. See the documentation
//                        // for ActivityCompat#requestPermissions for more details.
//                        return;
//                    }
//                    googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//                        @Override
//                        public void onMapClick(LatLng point) {
//                            if (!mAutoCompleteView.isPopupShowing()) {
//                                setMarker(point);
//                            }
//                            KeyboardUtils.hideSoftKeyboard(getActivity());
//
//                        }
//                    });
//                    // googleMap.setMyLocationEnabled(true);
//                    GPSTracker gpsTracker = new GPSTracker(getActivity());
//                    Location location = gpsTracker.getLocation();
//                    if (!gpsTracker.canGetLocation()) {
//                        if (!PrefsUtil.isStartGPSShowing) {
//                            gpsTracker.showSettingsAlert();
//                        }
//                    }
//                    if (location.getLatitude() == 9999999 || location.getLongitude() == 9999999) {
//                        //Toast.makeText(getActivity(), "Location not available", Toast.LENGTH_SHORT).show();
//
//                    } else {
//                        selectedLatLng = new LatLng(location.getLatitude(), location.getLongitude());
//                        setMarker(selectedLatLng);
//                    }
//                    /*Geocoder geocoder = new Geocoder(getActivity());
//                    List<Address> addresses  = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
//                    Log.e("UserProfileFragment", "onMapReady: "+addresses.get(0).getCountryName());*/
//
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
//                    //googleMap.addMarker(markerOptions);
//                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
//                    builder.include(markerOptions.getPosition());
//
//                    LatLngBounds bounds = builder.build();
//                    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));
//                    googleMap.moveCamera(CameraUpdateFactory.zoomTo(18));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

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

//    public void getUserLocation() {
//        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.usergetlocation;
//        ArrayList<String> params = new ArrayList<>();
//        params.add("userId");
//        ArrayList<String> values = new ArrayList<>();
//        values.add(String.valueOf(PrefsUtil.with(getActivity()).readInt("uId")));
//        new ParseJSON(getActivity(), url, params, values, UserLocationPOJO.class, new ParseJSON.OnResultListner() {
//            @Override
//            public void onResult(boolean status, Object obj) {
//                if (status) {
//                    UserLocationPOJO resultObj = (UserLocationPOJO) obj;
//                    if (!TextUtils.isEmpty(resultObj.getUserLocation().get(0).getHomeLocation())) {
//                        isHomeLocation = true;
//                        homeLocation = new LatLng(Double.parseDouble(resultObj.getUserLocation().get(0).getHomeLat()), Double.parseDouble(resultObj.getUserLocation().get(0).getHomeLong()));
//                    } else {
//                        isHomeLocation = false;
//                    }
//
//                    if (!TextUtils.isEmpty(resultObj.getUserLocation().get(0).getWorkLocation())) {
//                        isWorkLocation = true;
//                        workLocation = new LatLng(Double.parseDouble(resultObj.getUserLocation().get(0).getWorkLat()), Double.parseDouble(resultObj.getUserLocation().get(0).getWorkLong()));
//                    } else {
//                        isWorkLocation = false;
//                    }
//                    showLocationDialog();
//
//                } else {
//                    Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
//                }
//
//
//            }
//        });
//    }

//    public void setMarker(final LatLng point) {
//
//        Runnable newthread = new Runnable() {
//
//            @Override
//            public void run() {
//                try {
//                    if (getActivity() != null) {
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    selectedLatLng = point;
//                                    Location temp = new Location(LocationManager.GPS_PROVIDER);
//                                    temp.setLatitude(point.latitude);
//                                    temp.setLongitude(point.longitude);
//                                    GeocoderHelper gHelper = new GeocoderHelper();
//                                    gHelper.fetchAddress(getActivity(), temp, mAutoCompleteView, mAutocompleteClickListener);
//
//                                    //remove previously placed Marker
//                                    if (marker != null) {
//                                        marker.remove();
//                                    }
//
//                                    //place marker where user just clicked
//                                    try {
//                                        marker = googleMap.addMarker(new MarkerOptions().position(point).title("Pick Up")
//                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pick_up_marker)));
//                                    } catch (NullPointerException npe) {
//                                        npe.printStackTrace();
//                                    }
//                                    moveToCurrentLocation(selectedLatLng);
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        });
//                    }
//                } catch (Exception e) {
//                    if (getActivity() != null) {
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(getActivity(), "Cannot get address from location", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//
//                    e.printStackTrace();
//                }
//            }
//
//        };
//
//        Thread t = new Thread(newthread);
//        t.start();
//    }

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

//    private void showLocationDialog() {
//        dialog = new Dialog(getActivity()
//        );
//        try {
//            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        dialog.setContentView(R.layout.dialog_user_location);
//        dialog.setCancelable(false);
//
//        TextView txtClose = (TextView) dialog.findViewById(R.id.txtClose);
//        TextView txtHome = (TextView) dialog.findViewById(R.id.txtHome);
//        TextView txtWork = (TextView) dialog.findViewById(R.id.txtWork);
//
//        if (!isHomeLocation) {
//            txtHome.setText("No Home Location Set");
//        }
//
//        if (!isWorkLocation) {
//            txtWork.setText("No Work Location Set");
//        }
//
//        txtClose.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//
//        txtHome.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isHomeLocation) {
//                    setMarker(homeLocation);
//                    dialog.dismiss();
//                } else {
//                    //Toast.makeText(getActivity(), "You have not set Home location", Toast.LENGTH_SHORT).show();
//                }
//
//
//            }
//        });
//
//        txtWork.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isWorkLocation) {
//                    setMarker(workLocation);
//                    dialog.dismiss();
//                } else {
//                    //Toast.makeText(getActivity(), "You have not set Work location", Toast.LENGTH_SHORT).show();
//                }
//
//
//            }
//        });
//
//        // now that the dialog is set up, it's time to show it
//        dialog.show();
//        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        android.util.Log.e("HomeFragment", "onStart: ");
//        try {
//            EventBus.getDefault().register(this);
//        } catch (Exception e) {
//
//        }
//
//    }

//    @Override
//    public void onStop() {
//        super.onStop();
//        android.util.Log.e("HomeFragment", "onStart: ");
//        stopTimer();
//    }
//
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onMessageEvent(MessageEvent event) {
//
//        if (event.getType().equalsIgnoreCase("cancelride") || event.getType().equalsIgnoreCase("rideInfo")) {
//            android.util.Log.e("HomeFragment", "onMessageEvent: ");
//            try {
//
//                adapter.resetSelection();
//                adapter.notifyDataSetChanged();
//                caryTypeList.clear();
//                subCarTypeAdapter.notifyDataSetChanged();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).showToolbar();
            ((HomeActivity) getActivity()).hideToolbarTiltle();
            ((HomeActivity) getActivity()).turnActionBarIconBlack();
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
