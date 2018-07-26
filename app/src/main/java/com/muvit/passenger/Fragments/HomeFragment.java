package com.muvit.passenger.Fragments;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.support.v7.app.AlertDialog;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.koushikdutta.ion.Ion;
import com.muvit.passenger.Activities.AddCardActivity;
import com.muvit.passenger.Activities.DepositFundActivity;
import com.muvit.passenger.Activities.HomeActivity;
import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.AsyncTask.SetUpAdress;
import com.muvit.passenger.GeoLocation.activity.GeocoderHelper;
import com.muvit.passenger.GeoLocation.adapter.PlaceAutocompleteAdapter;
import com.muvit.passenger.GeoLocation.logger.Log;
import com.muvit.passenger.Models.ConfirmRidePOJO;
import com.muvit.passenger.Models.DefaultPaymentMethodPOJO;
import com.muvit.passenger.Models.FareEstimateItem;
import com.muvit.passenger.Models.FareEstimatePOJO;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.Models.UserLocationPOJO;
import com.muvit.passenger.Models.WalletDetailPOJO;
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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.muvit.passenger.database.AppDatabase;
import com.muvit.passenger.database.Card;
import com.muvit.passenger.database.CardDao;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by nct119 on 28/10/16.
 */

public class HomeFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {

    //    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
//            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));
    protected GoogleApiClient mGoogleApiClient;
    Dialog promoDialog, fareDialog, paymentMethodDialog;
    private ProgressDialog prd;
    RelativeLayout cancelBtn, okBtn, cancelFare, btnCash, btnWallet, btnCard;
    TextView fareView, startLocation, endLocation, car_name;

    String carTypeId = "3";
    String subCarTypeId = "2";
    FareEstimateItem fareSummaryItem;
    String defaultPaymentMethod = "w";
    TextSwitcher txtSwitcherMessage;
    LatLng homeLocation, workLocation, pickUpPoint, dropOffPoint;
    boolean isHomeLocation = false, isWorkLocation = false;
    Dialog dialog;
    private Toolbar toolbar;
    private TextView txtTitle;
    private ImageView imgWallet, dashLine, imgLocation, imgNav, close_anim;
    LinearLayout imgCash_btn, txtFareEstimate;
    RelativeLayout promo_btn;
    private Button btnBooknRide;
    private PopupWindow popupWindow;

    private LatLng selectedLatLng = new LatLng(0.0, 0.0);
    private Geocoder geocoder;
    private RelativeLayout layoutOverlay;
    private PlaceAutocompleteAdapter mAdapterPickUp;
    private PlaceAutocompleteAdapter mAdapterDropOff;
    private AutoCompleteTextView txtSource, mAutoCompleteView;
    private Marker pickupMarker;
    private Marker dropOffMarker;
    private ImageView car_img_fare;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1; // 1 second
    GoogleMap googleMap;
    private LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));
    private LocationManager locationManager;
    private Timer mTimer1;
    private Disposable disposable;
    private CardDao cardDao;
    private TimerTask mTt1;
    private SetUpAdress setUpAdressPickupMarker = new SetUpAdress() {
        @Override
        public void setupAdress(String adress) {
            txtSource.setText(adress);
            txtSource.dismissDropDown();

            //remove previously placed Marker
            if (pickupMarker != null) {
                pickupMarker.remove();
            }

            pickupMarker = googleMap.addMarker(new MarkerOptions().position(pickUpPoint).title(adress)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pick_up_marker)));
            pickupMarker.showInfoWindow();
            if (dropOffMarker != null) {
                moveToCurrentLocationRoute();
            } else
                moveToCurrentLocation(pickUpPoint);
        }
    };

    private SetUpAdress setUpAdressDropOfMarker = new SetUpAdress() {
        @Override
        public void setupAdress(String adress) {
            mAutoCompleteView.setText(adress);
            mAutoCompleteView.dismissDropDown();

            //remove previously placed Marker
            if (dropOffMarker != null) {
                dropOffMarker.remove();
            }

            //place marker where user just clicked
            dropOffMarker = googleMap.addMarker(new MarkerOptions().position(dropOffPoint).title(adress)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.drop_off_marker)));
            if (pickupMarker != null) {
                moveToCurrentLocationRoute();
            } else
                moveToCurrentLocation(dropOffPoint);
        }
    };

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            pickUpPoint = new LatLng(location.getLatitude(), location.getLongitude());
            setPickupMarker(pickUpPoint, "onLocationChanged");
            locationManager.removeUpdates(this);
            if (prd != null)
                prd.dismiss();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
    //    private int totalWallet;
    private Handler mTimerHandler = new Handler();

    private AdapterView.OnItemClickListener mAutocompleteDropOffClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapterDropOff.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i("HomeFragment", "Autocomplete item selected: " + item.description);

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdateDropOffPlaceDetailsCallback);
        }
    };

    private AdapterView.OnItemClickListener mAutocompletePickUpClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapterPickUp.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i("HomeFragment", "Autocomplete item selected: " + item.description);

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePickUpPlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdateDropOffPlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e("Step2Activity", "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            } else {
                dropOffPoint = places.get(0).getLatLng();
                setDropOffMarker(dropOffPoint);
            }
            places.release();
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePickUpPlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e("Step2Activity", "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            } else {
                pickUpPoint = places.get(0).getLatLng();
                setPickupMarker(pickUpPoint, "Callback");
            }
            places.release();
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
                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);

        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        getDefaultPaymentMethod();
        defaultPaymentMethod = "c";

        mAdapterPickUp = new PlaceAutocompleteAdapter(getContext(), android.R.layout.simple_list_item_1,
                mGoogleApiClient, BOUNDS_GREATER_SYDNEY, null);

        mAdapterDropOff = new PlaceAutocompleteAdapter(getContext(), android.R.layout.simple_list_item_1,
                mGoogleApiClient, BOUNDS_GREATER_SYDNEY, null);

        mAutoCompleteView.setOnItemClickListener(mAutocompleteDropOffClickListener);
        mAutoCompleteView.setAdapter(mAdapterDropOff);

        txtSource.setOnItemClickListener(mAutocompletePickUpClickListener);
        txtSource.setAdapter(mAdapterPickUp);

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
        imgNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prd = new ProgressDialog(getContext(), R.style.DialogTheme);
                prd.setTitle("Loading...");
                prd.setMessage("Please Wait While Loading");
                prd.setCancelable(false);
                prd.show();

                locationManager = (LocationManager) getActivity()
                        .getSystemService(Context.LOCATION_SERVICE);

                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
            }
        });

        return rootView;
    }//onCreateView


    private void initViewsNew(View rootView) {
        setupToolbar(rootView);
        imgWallet = rootView.findViewById(R.id.imgWallet);
        imgCash_btn = rootView.findViewById(R.id.card_btn);
        btnBooknRide = rootView.findViewById(R.id.btnBooknRide);
        promo_btn = (RelativeLayout) rootView.findViewById(R.id.promo_btn);
        initPromoDialog();
        initPaymentMethodDialog();
        initEstimate();
        promo_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // showPromoDialog();
                promoDialog.show();
            }
        });
        imgCash_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // showPromoDialog();
                paymentMethodDialog.show();
            }
        });


        txtFareEstimate = rootView.findViewById(R.id.txtFareEstimate);
        txtSource = rootView.findViewById(R.id.txtSource);
        txtSource.setMovementMethod(new ScrollingMovementMethod());
        dashLine = rootView.findViewById(R.id.dashLine);
        imgLocation = rootView.findViewById(R.id.imgLocation);
        imgNav = rootView.findViewById(R.id.imgNav);
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


    private void setupToolbar(View rootView) {
        toolbar = (Toolbar) rootView.findViewById(R.id.toolBar);
        txtTitle = (TextView) rootView.findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.step2_title);
        //todo пока коментим толбар
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
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
    }//initPromoDialog


    private void initPaymentMethodDialog() {
        AppDatabase db = ApplicationController.getInstance().getDatabase();
        cardDao = db.cardDao();

        paymentMethodDialog = new Dialog(getContext());
        paymentMethodDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        paymentMethodDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        paymentMethodDialog.setContentView(R.layout.payment_dialog);
        btnCash = (RelativeLayout) paymentMethodDialog.findViewById(R.id.btnCash);
        btnWallet = (RelativeLayout) paymentMethodDialog.findViewById(R.id.btnWallet);
        btnCard = (RelativeLayout) paymentMethodDialog.findViewById(R.id.btnCard);
        btnCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                PrefsUtil.with(getContext()).write("payment_method", "c");
                defaultPaymentMethod = "c";
                Toast toast = Toast.makeText(getContext(), "Selected method of payment in cash", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                paymentMethodDialog.dismiss();
            }
        });
        btnWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                PrefsUtil.with(getContext()).write("payment_method", "w");
                defaultPaymentMethod = "w";
                Toast toast = Toast.makeText(getContext(), "Selected method of payment from the wallet", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                paymentMethodDialog.dismiss();
            }
        });
        btnCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //запрашиваем карты
                getListAllCards();
                paymentMethodDialog.dismiss();
            }
        });
        paymentMethodDialog.setCancelable(true);
    }//initPaymentMethodDialog


    public void getListAllCards() {
        disposable = cardDao.getListCards()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listAccounts -> {
                    disposable.dispose();
                    checkCard(listAccounts);
                });
    }//getListAllCards


    private void checkCard(List<Card> listCards) {
        if (listCards.size() > 0) {
            // идем в  активность пополнения счета
            Intent intent = new Intent(getContext(), DepositFundActivity.class);
            intent.putExtra("numCard", listCards.get(0).getNumberCard());
            intent.putExtra("mm", listCards.get(0).getMonth());
            intent.putExtra("yy", listCards.get(0).getYear());
            intent.putExtra("cvv", listCards.get(0).getCvv());
            startActivity(intent);
        } else {
            // открываем добавление карты в базу
            startActivity(new Intent(getContext(), AddCardActivity.class));
        }//if
    }//checkCard


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
    }//initEstimate

    private void popupEstimateNew(FareEstimateItem fareSummaryItem) {
        startLocation.setText(fareSummaryItem.getPickUpLocation());
        endLocation.setText(fareSummaryItem.getDropOffLocation());
        car_name.setText(fareSummaryItem.getCarTypeName());
        Ion.with(car_img_fare)
                .error(R.drawable.rides)
                .load(WebServiceUrl.carUrl + fareSummaryItem.getCarTypeImage());
        fareView.setText(fareSummaryItem.getFinalEstimatedTotal());
        fareDialog.show();
    }//popupEstimateNew

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
                                dropOffPoint = point;
                                setDropOffMarker(point);
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
                        pickUpPoint = selectedLatLng;
                        setPickupMarker(pickUpPoint, "setUpMap");
                        moveToCurrentLocation1(pickUpPoint);

                        carTypeId = String.valueOf(3);
                        subCarTypeId = String.valueOf(2);
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


    public void setPickupMarker(LatLng point, String location) {
        try {
            Location temp = new Location(LocationManager.GPS_PROVIDER);
            temp.setLatitude(point.latitude);
            temp.setLongitude(point.longitude);
            GeocoderHelper gHelper = new GeocoderHelper();
            gHelper.fetchAddress1(getActivity(), temp, setUpAdressPickupMarker);//вписываем адрес
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDropOffMarker(final LatLng point) {
        try {
            selectedLatLng = point;
            Location temp = new Location(LocationManager.GPS_PROVIDER);
            temp.setLatitude(point.latitude);
            temp.setLongitude(point.longitude);
            GeocoderHelper gHelper = new GeocoderHelper();
            gHelper.fetchAddress1(getActivity(), temp, setUpAdressDropOfMarker);//вписываем адрес
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        values.add(String.valueOf(pickUpPoint.latitude));
        values.add(String.valueOf(pickUpPoint.longitude));
        values.add(String.valueOf(selectedLatLng.latitude));
        values.add(String.valueOf(selectedLatLng.longitude));
        new ParseJSON(getContext(), url, params, values, FareEstimatePOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    FareEstimatePOJO resultObj = (FareEstimatePOJO) obj;
                    fareSummaryItem = resultObj.getFareSummary().get(0);
                    if (openDialog) {
                        popupEstimateNew(fareSummaryItem);//open dialod
                    } else {
                        if (!TextUtils.isEmpty(mAutoCompleteView.getText().toString())) {
                            if (defaultPaymentMethod.equals("c")) {
                                confirmRide();
                            } else {
                                getTotalWallet(fareSummaryItem);  //get total from wallet
                            }
                        } else {
                            Toast.makeText(getContext(), "Please choose drop off location", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(getContext(), (String) obj, Toast.LENGTH_SHORT).show();
                }
            }
        });
        txtFareEstimate.setClickable(true);
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
        values.add(String.valueOf(pickUpPoint.latitude));
        values.add(String.valueOf(pickUpPoint.longitude));
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
        if (defaultPaymentMethod.equals("w")) {
            values.add("w");//wallet
        } else {
            values.add("c");//cash
        }
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
//                        imgCash_btn.setImageResource(R.drawable.cash_gray);
//                        btnBooknRide.setBackgroundColor(getResources().getColor(R.color.yellowColor));
//                        btnBooknRide.setTypeface(null, Typeface.BOLD);
                    } else {
                        imgWallet.setImageResource(R.drawable.wallet_gray);
//                        imgCash_btn.setImageResource(R.drawable.cash);
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


    public void getTotalWallet(FareEstimateItem fareSummaryItem) {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.getuserwalletdetails;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(getActivity()).readInt("uId")));
        new ParseJSON(getActivity(), url, params, values, WalletDetailPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    WalletDetailPOJO resultObj = (WalletDetailPOJO) obj;
                    if (!resultObj.getWallet().get(0).getCurrenctBalance().isEmpty()) {
                        int totalWallet = Integer.parseInt(resultObj.getWallet().get(0).getCurrenctBalance());

                        int FareDistanceCharges = Integer.parseInt(fareSummaryItem.getFareDistanceCharges().toString());
                        if (totalWallet < FareDistanceCharges) {
                            Toast toast = Toast.makeText(getContext(), "There are not enough funds on your account", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        } else
                            confirmRide();
                    }
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
                    dropOffPoint = homeLocation;
                    setDropOffMarker(homeLocation);
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
                    dropOffPoint = workLocation;
                    setDropOffMarker(workLocation);
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


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        stopTimer();
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

    private void moveToCurrentLocation(LatLng currentLocation) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(18), 2000, null);
//         Zoom in, animating the camera.
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
//         Zoom out to zoom level 10, animating with a duration of 2 seconds.
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }

    private void moveToCurrentLocationRoute() {
        try {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            //the include method will calculate the min and max bound.
            builder.include(pickupMarker.getPosition());
            builder.include(dropOffMarker.getPosition());

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        locationManager.removeUpdates(locationListener);
    }


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
//                                if (selectedLatLng != null) {
//                                    /*Location l = new Location(LocationManager.GPS_PROVIDER);
//                                    l.setLatitude(selectedLatLng.latitude);
//                                    l.setLongitude(selectedLatLng.longitude);
//                                    if (!(gps.getLocation() == l)) {*/
//                                    if (!(gps.getLatitude() == selectedLatLng.latitude
//                                            && gps.getLongitude() == selectedLatLng.longitude)) {
//                                        onLocationChanged(gps.getLocation());
//                                    }
//                                } else {
//                                    onLocationChanged(gps.getLocation());
//                                }
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
