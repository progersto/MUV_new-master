package com.muvit.passenger.Activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.firebase.iid.FirebaseInstanceId;
import com.koushikdutta.ion.bitmap.Transform;
import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Fragments.AboutFragment;
import com.muvit.passenger.Fragments.AccountSettingsFragment;
import com.muvit.passenger.Fragments.HelpFragment;
import com.muvit.passenger.Fragments.HomeFragment;
import com.muvit.passenger.Fragments.InfoFragment;
import com.muvit.passenger.Fragments.MyTripsFragment;
import com.muvit.passenger.Fragments.NotificationsFragment;
import com.muvit.passenger.Fragments.UserProfileFragment;
import com.muvit.passenger.Fragments.WalletFragment;
import com.muvit.passenger.GeoLocation.activity.GeocoderHelper;
import com.muvit.passenger.GeoLocation.adapter.PlaceAutocompleteAdapter;
import com.muvit.passenger.GeoLocation.logger.Log;
import com.muvit.passenger.Models.CommonPOJO;
import com.muvit.passenger.Models.ConfirmRidePOJO;
import com.muvit.passenger.Models.DefaultPaymentMethodPOJO;
import com.muvit.passenger.Models.FareEstimateItem;
import com.muvit.passenger.Models.FareEstimatePOJO;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.Models.UserLocationPOJO;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.ImgUtils;
import com.muvit.passenger.Utils.KeyboardUtils;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.koushikdutta.ion.Ion;
import com.muvit.passenger.database.AppDatabase;
import com.muvit.passenger.database.Card;
import com.muvit.passenger.database.CardDao;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class Step2Activity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {


    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    public ActionBarDrawerToggle toggle;
    FragmentManager fragmentManager;
    BroadcastReceiver mMessageReceiver;
    private Disposable disposable;
    private CardDao cardDao;

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));
    //    protected GoogleApiClient mGoogleApiClient;
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
    private AdapterView.OnItemClickListener
            mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i("Step2Activity", "Autocomplete item selected: " + item.description);

//            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
//            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            //Toast.makeText(Step2Activity.this, "Clicked: " + item.description, Toast.LENGTH_SHORT).show();
            Log.i("Step2Activity", "Called getPlaceById to get Place details for " + item.placeId);
        }
    };
    private ImageView car_img_fare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step2);
        initViews();
        initInternetReceiver();
        fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(getListener());
        //getDefaultPaymentMethod();
        defaultPaymentMethod = "c";
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, 0 /*clientId*/, this)
//                .addApi(Places.GEO_DATA_API)
//                .addConnectionCallbacks()
//                .addOnConnectionFailedListener()
//                .build();
//
//        mGoogleApiClient.connect();
//


        /*imgWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgWallet.setImageResource(R.drawable.wallet_profile);
                imgCash.setImageResource(R.drawable.cash_gray);
                btnBooknRide.setBackgroundColor(getResources().getColor(R.color.yellowColor));
                btnBooknRide.setTypeface(null, Typeface.BOLD);
                defaultPaymentMethod = "w";
            }
        });*/
        //imgCash.setImageResource(R.drawable.cash);
        //btnBooknRide.setBackgroundColor(getResources().getColor(R.color.yellowColor));
        // btnBooknRide.setTypeface(null, Typeface.BOLD);

        /*imgCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgWallet.setImageResource(R.drawable.wallet_gray);
                imgCash.setImageResource(R.drawable.cash);
                btnBooknRide.setBackgroundColor(getResources().getColor(R.color.yellowColor));
                btnBooknRide.setTypeface(null, Typeface.BOLD);
                defaultPaymentMethod = "c";
            }
        });*/

//        btnBooknRide.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                /*intent = new Intent(Step2Activity.this, RideInformationActivity.class);
//                startActivity(intent);*/
//                if (!TextUtils.isEmpty(mAutoCompleteView.getText().toString())) {
//                    getFareSummary(false);
//                } else {
//                    Toast.makeText(Step2Activity.this, "Please Select Drop off Location" +
//                            "", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

//        txtFareEstimate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (!TextUtils.isEmpty(mAutoCompleteView.getText().toString())) {
//                    txtFareEstimate.setClickable(false);
//                    getFareSummary(true);
//                } else {
//                    Toast.makeText(Step2Activity.this, "Please Select Drop off Location" +
//                            "", Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });
//        setUpMap();
//        imgLocation.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getUserLocation();
//            }
//        });
//        try {
//            fromLat = new LatLng(getIntent().getDoubleExtra("lat", 0.0), getIntent().getDoubleExtra("long", 0.0));
//            txtSource.setText(getIntent().getStringExtra("location"));
//            carTypeId = String.valueOf(getIntent().getIntExtra("carItemId", 0));
//            subCarTypeId = String.valueOf(getIntent().getIntExtra("subCarTypeId", 0));
//
//        } catch (Exception e) {
//            fromLat = new LatLng(0.0, 0.0);
//            e.printStackTrace();
//        }
        initNavigationDrawer();
        //todo пока коментим
//        txtTitle.setText(R.string.home);
        updateDisplay(new HomeFragment(), R.string.home);
    }//onCreate


    private FragmentManager.OnBackStackChangedListener getListener() {
        FragmentManager.OnBackStackChangedListener result = new FragmentManager.OnBackStackChangedListener() {
            public void onBackStackChanged() {
                FragmentManager manager = getSupportFragmentManager();

                if (manager != null) {
                    Fragment currFrag = fragmentManager.findFragmentById(R.id.containerView);
                    if (currFrag instanceof HomeFragment || currFrag instanceof NotificationsFragment || currFrag instanceof MyTripsFragment ||
                            currFrag instanceof AccountSettingsFragment) {
                        showToolbar();
                    } else {
                        hideToolbar();
                    }

                    if (currFrag instanceof NotificationsFragment || currFrag instanceof MyTripsFragment ||
                            currFrag instanceof AccountSettingsFragment
                            ) {
                        turnActionBarIconWhite();

                    } else {
                        turnActionBarIconBlack();
                    }
                    //showToolbar();
//                    MyFragment currFrag = (MyFragment) manager.findFragmentById(R.id.fragmentItem);
//
//                    currFrag.onFragmentResume();
                }
            }
        };
        return result;
    }//getListener


    private void initNavigationDrawer() {
        // txtTitle.setText(R.string.home);
        navigationView.setCheckedItem(R.id.home);
        updateDisplay(new HomeFragment(), R.string.home);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        int id = menuItem.getItemId();

                        switch (id) {
                            case R.id.home:
                                txtTitle.setText(R.string.home);
                                updateDisplay(new HomeFragment(), R.string.home);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.myProfile:
                                txtTitle.setText(R.string.my_profile);
                                updateDisplay(new UserProfileFragment(), R.string.my_profile);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.payments:
                                getListAllCards();
                                break;
                            case R.id.wallet:
                                txtTitle.setText(R.string.nav_wallet);
                                updateDisplay(new WalletFragment(), R.string.nav_wallet);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.rides:
                                txtTitle.setText(R.string.rides);
                                updateDisplay(new MyTripsFragment(), R.string.rides);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.notifications:
                                txtTitle.setText(R.string.notifications);
                                updateDisplay(new NotificationsFragment(), R.string.notifications);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.account_settings:
                                txtTitle.setText(R.string.account_settings);
                                updateDisplay(new AccountSettingsFragment(), R.string.account_settings);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.help:
                                txtTitle.setText(R.string.help);
                                updateDisplay(new HelpFragment(), R.string.help);
                                drawerLayout.closeDrawers();
                                break;

                            case R.id.invite_friends:
                                startActivity(new Intent(Step2Activity.this, InviteFriendActivity.class));
                                break;
                            case R.id.about:
                                txtTitle.setText(R.string.about);
                                updateDisplay(new AboutFragment(), R.string.about);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.info:
                                txtTitle.setText(R.string.info);
                                updateDisplay(new InfoFragment(), R.string.info);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.logout:

                                AlertDialog.Builder builder = new AlertDialog.Builder(Step2Activity.this);
                                builder.setMessage("Are you sure you want logout?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                        /*PrefsUtil.with(HomeActivity.this).clearPrefs();
                                        PrefsUtil.with(HomeActivity.this).write("isLoggedIn",false);
                                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();*/
                                                logout();
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {

                                            }
                                        });
                                // Create the AlertDialog object and return it
                                builder.create();
                                builder.show();

                                break;
                        }
                        return true;
                    }
                });
        View header = navigationView.getHeaderView(0);
        ImageView imgProfile = (ImageView) header.findViewById(R.id.imgProfile);
        //imgProfile.setImageResource(R.mipmap.ic_launcher);
        Ion.with(imgProfile)
                .error(R.drawable.no_image)
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
                .load(PrefsUtil.with(this).readString("profileImage"));
        TextView txtUserName = (TextView) header.findViewById(R.id.txtUserName);
        txtUserName.setText(PrefsUtil.with(this).readString("firstName") + " " + PrefsUtil.with(this).readString("lastName"));
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View v) {
                InputMethodManager mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mImm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                v.requestFocus();
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                InputMethodManager mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mImm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                v.requestFocus();
                super.onDrawerOpened(v);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }//initNavigationDrawer


    public void getListAllCards() {
        AppDatabase db = ApplicationController.getInstance().getDatabase();
        cardDao = db.cardDao();
        disposable = cardDao.getListCards()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listAccounts -> {
                    disposable.dispose();
                    checkCard(listAccounts);
                });
    }//getListImageObj


    private void checkCard(List<Card> listCards) {
        if (listCards.size() > 0) {
            // карта есть, смотрим детали карты
            Intent intent = new Intent(Step2Activity.this,CardDetailsActivity.class);
            intent.putExtra("card", listCards.get(0));
            startActivity(intent);
        } else {
            // карты нет, открываем сообщение о ее отсутствии
            startActivity(new Intent(Step2Activity.this, PaymentsActivity.class));
        }//if
    }//sowData


    public void updateHeader() {
        try {
            View header = navigationView.getHeaderView(0);
        /*CircleImageView imgProfile = (CircleImageView) header.findViewById(R.id.imgProfile);
        imgProfile.setImageResource(R.mipmap.ic_launcher);*/
            ImageView imgProfile = (ImageView) header.findViewById(R.id.imgProfile);
            //imgProfile.setImageResource(R.mipmap.ic_launcher);
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
                    .load(PrefsUtil.with(Step2Activity.this).readString("profileImage"));
            TextView txtUserName = (TextView) header.findViewById(R.id.txtUserName);
            txtUserName.setText(PrefsUtil.with(this).readString("firstName") + " " + PrefsUtil.with(this).readString("lastName"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//updateHeader


    private void updateDisplay(Fragment fragment, int res) {
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.containerView,
                fragment).addToBackStack(getResources().getString(res)).commit();
    }//updateDisplay


    private void logout() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.logout;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        params.add("userType");
        params.add("deviceId");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(Step2Activity.this).readInt("uId")));
        values.add("u");
        values.add(FirebaseInstanceId.getInstance().getToken());
        new ParseJSON(Step2Activity.this, url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    CommonPOJO resultObj = (CommonPOJO) obj;
                    Toast.makeText(Step2Activity.this, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                    PrefsUtil.with(Step2Activity.this).clearPrefs();
                    PrefsUtil.with(Step2Activity.this).write("isLoggedIn", false);
                    Intent intent = new Intent(Step2Activity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(Step2Activity.this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                    PrefsUtil.with(Step2Activity.this).clearPrefs();
                    PrefsUtil.with(Step2Activity.this).write("isLoggedIn", false);
                    Intent intent = new Intent(Step2Activity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }//logout


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
            pickupMarker = map.addMarker(new MarkerOptions().position(point).title(location)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pick_up_marker)));
            pickupMarker.showInfoWindow();
            //moveToCurrentLocation(dropoffLatLng);
            moveToCurrentLocation1(point);
            //moveToCurrentLocation(point);
        } catch (Exception e) {
            // Toast.makeText(Step2Activity.this, "Cannot get address from clicked location", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }//setPickupMarker


    private void initViews() {
        navigationView = findViewById(R.id.navigation_view);
        drawerLayout = findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(
                        this,
                        drawerLayout,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close) {
        };

        setupToolbar();
        imgWallet = findViewById(R.id.imgWallet);
        imgCash = findViewById(R.id.card_btn);
        btnBooknRide = findViewById(R.id.btnBooknRide);
        promo_btn = (RelativeLayout) findViewById(R.id.promo_btn);
        initPromoDialog();
        initEstimate();
        //todo пока коментим
//        promo_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // showPromoDialog();
//                promoDialog.show();
//            }
//        });

        txtFareEstimate = findViewById(R.id.txtFareEstimate);
        //todo пока коментим
//        txtSource = findViewById(R.id.txtSource);
//        txtSource.setMovementMethod(new ScrollingMovementMethod());
        dashLine = findViewById(R.id.dashLine);
        imgLocation = findViewById(R.id.imgLocation);
        layoutOverlay = findViewById(R.id.layoutOverlayAnim);
        //todo пока коментим
//        close_anim = (ImageView) findViewById(R.id.close_anim) ;
//        close_anim.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                layoutOverlay.setVisibility(View.GONE);
//            }
//        });

//        txtSwitcherMessage = findViewById(R.id.txtSwitcherMessage);
//        txtSwitcherMessage.setFactory(new ViewSwitcher.ViewFactory() {
//
//            public View makeView() {
//                // TODO Auto-generated method stub
//                // create a TextView
//                TextView t = new TextView(Step2Activity.this);
//                // set the gravity of text to top and center horizontal
//                t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
//                t.setTextColor(Color.WHITE);
//                t.setTextSize(15);
//                return t;
//            }
//        });

//        txtFareEstimate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                fareDialog.show();
//            }
//        });

//        txtSwitcherMessage.setInAnimation(Step2Activity.this, android.R.anim.slide_in_left);
//        txtSwitcherMessage.setOutAnimation(Step2Activity.this, android.R.anim.slide_out_right);
        geocoder = new Geocoder(this, Locale.getDefault());
        mAutoCompleteView = findViewById(R.id.autocomplete_places);
//        dashLine.bringToFront();
//        dashLine.invalidate();
        new KeyboardUtils().setupUI(findViewById(R.id.activity_step2), Step2Activity.this);

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
                        //todo пока коментим
//                        txtSwitcherMessage.setText(messageArray[message_counter[0]]);
                    }
                });
                message_counter[0] = (message_counter[0] + 1);
            }
        }, 0, period);
    }//initViews


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


    private void initPromoDialog() {
        // final View view = getLayoutInflater().inflate(R.layout.promo_dialog, null);
        promoDialog = new Dialog(this);
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
        fareDialog = new Dialog(this);
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

        Toast.makeText(this,
                "Could not connect to server",
                Toast.LENGTH_SHORT).show();
    }

    public void setUpMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);
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
                setPickupMarker(fromLat, txtSource.getText().toString());
            }
        });

    }

    public void setMarker(final LatLng point) {
        Runnable newthread = new Runnable() {

            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                selectedLatLng = point;
                                Location temp = new Location(LocationManager.GPS_PROVIDER);
                                temp.setLatitude(point.latitude);
                                temp.setLongitude(point.longitude);
                                GeocoderHelper gHelper = new GeocoderHelper();
                                gHelper.fetchAddress(Step2Activity.this, temp,
                                        mAutoCompleteView, mAutocompleteClickListener);

                                //remove previously placed Marker
                                if (marker != null) {
                                    marker.remove();
                                }

                                //place marker where user just clicked
                                marker = map.addMarker(new MarkerOptions().position(point).title("Drop Off")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.drop_off_marker)));
                                moveToCurrentLocation();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Step2Activity.this, "Cannot get address from location", Toast.LENGTH_SHORT).show();
                        }
                    });

                    e.printStackTrace();
                }
            }

        };

        Thread t = new Thread(newthread);
        t.start();
    }

    private void moveToCurrentLocation1(LatLng currentLocation) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
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
        new ParseJSON(this, url, params, values, FareEstimatePOJO.class, new ParseJSON.OnResultListner() {
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
                            Toast.makeText(Step2Activity.this, "Please choose drop off location", Toast.LENGTH_SHORT).show();
                        }

                    }
                } else {
                    Toast.makeText(Step2Activity.this, (String) obj, Toast.LENGTH_SHORT).show();
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
        values.add(String.valueOf(PrefsUtil.with(this).readInt("uId")));
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
        new ParseJSON(this, url, params, values, ConfirmRidePOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    ConfirmRidePOJO resultObj = (ConfirmRidePOJO) obj;
                    PrefsUtil.with(Step2Activity.this).write("lastSendId", "");
                    Toast.makeText(Step2Activity.this, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                    layoutOverlay.setVisibility(View.VISIBLE);

                    try {
                        FirebaseAnalytics mFirebaseAnalytics;
                        mFirebaseAnalytics = FirebaseAnalytics.getInstance(Step2Activity.this);
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "ride_request");
                        bundle.putString("time_stamp", String.valueOf(new Date()));
                        mFirebaseAnalytics.logEvent("ride_request", bundle);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //finish();
                } else {
                    Toast.makeText(Step2Activity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getDefaultPaymentMethod() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.userdefaultpaymentmethod;

        ArrayList<String> params = new ArrayList<>();
        params.add("userId");

        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(this).readInt("uId")));
        new ParseJSON(this, url, params, values, DefaultPaymentMethodPOJO.class, new ParseJSON.OnResultListner() {
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
                    Toast.makeText(Step2Activity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getUserLocation() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.usergetlocation;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(Step2Activity.this).readInt("uId")));
        new ParseJSON(Step2Activity.this, url, params, values, UserLocationPOJO.class, new ParseJSON.OnResultListner() {
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
                    Toast.makeText(Step2Activity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void showLocationDialog() {
        dialog = new Dialog(Step2Activity.this);
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


    private void initInternetReceiver(){
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (new ConnectionCheck().isNetworkConnected(context)) {
                    android.util.Log.e("HomeActivity", "connected");
                    if(!ApplicationController.isOnline) {
                        if (PrefsUtil.isNoInternetShowing) {
                            if (PrefsUtil.dialogNoInternet != null) {
                                PrefsUtil.dialogNoInternet.dismiss();
                                PrefsUtil.isNoInternetShowing = false;
                            }
                        }
                        final Dialog d = new Dialog(context, android.R.style.Theme_Light_NoTitleBar);
                        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        d.setContentView(R.layout.dialog_connected);
                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                        lp.copyFrom(d.getWindow().getAttributes());
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                        d.getWindow().setAttributes(lp);
                        TextView txtRetry = (TextView) d.findViewById(R.id.txtRetry);
                        txtRetry.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PrefsUtil.isInternetConnectedShowing = false;
                                d.dismiss();
                            }
                        });
                        d.setCancelable(true);
                        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                PrefsUtil.isInternetConnectedShowing = false;
                            }
                        });
                        PrefsUtil.isInternetConnectedShowing = true;
                        PrefsUtil.dialogInternetConnected = d;
                        d.show();
                        ApplicationController.isOnline = true;
                    }
                    EventBus.getDefault().post(new MessageEvent("connection","connected"));
                } else {
                    //new ConnectionCheck().showDialogWithMessage(context, getString(R.string.sync_data_message)).show();
                    android.util.Log.e("Step2Activity", "disconnected");
                    if(ApplicationController.isOnline) {
                        if (PrefsUtil.isInternetConnectedShowing) {
                            if (PrefsUtil.dialogInternetConnected != null) {
                                PrefsUtil.dialogInternetConnected.dismiss();
                                PrefsUtil.isInternetConnectedShowing = false;
                            }
                        }
                        final Dialog d = new Dialog(context, android.R.style.Theme_Light_NoTitleBar);
                        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        d.setContentView(R.layout.dialog_no_internet);
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
                            }
                        });
                        d.setCancelable(true);
                        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                PrefsUtil.isNoInternetShowing = false;
                            }
                        });
                        PrefsUtil.isNoInternetShowing = true;
                        PrefsUtil.dialogNoInternet = d;
                        d.show();
                        ApplicationController.isOnline = false;
                    }
                    EventBus.getDefault().post(new MessageEvent("connection","disconnected"));
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
//        if (layoutOverlay.getVisibility() != View.VISIBLE) {
//            super.onBackPressed();
//        }
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        }else {

            if ((getSupportFragmentManager().getBackStackEntryCount() - 1) >= 0) {
                getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        try {
                            //Fragment fragment = getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getBackStackEntryCount() - 1);
                            FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1);
                            String tag = backEntry.getName();
                            txtTitle.setText(tag);
                            // fragment.onResume();
                        } catch (Exception e) {

                        }
                    }
                });
            }
            if (getSupportFragmentManager().getBackStackEntryCount() - 1 == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setMessage("Do you really want to Exit?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //if user pressed "yes", then he is allowed to exit from application
                        finish();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //if user select "No", just cancel this dialog and continue with app
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                getSupportFragmentManager().popBackStack();

            }
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

    @Override
    protected void onResume() {
        super.onResume();
        if(PrefsUtil.dialogStartGPS != null){
            PrefsUtil.dialogStartGPS.dismiss();
            PrefsUtil.dialogStartGPS = null;
            PrefsUtil.isStartGPSShowing = false;
        }
        try {
            updateHeader();
            InputMethodManager mImm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            mImm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            registerReceiver(mMessageReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.getType().equalsIgnoreCase("cancelride")) {
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setMessage("We apologise, unfortunately all our drivers might be on trips at the moment, please try again later.");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //if user pressed "yes", then he is allowed to exit from application
                        finish();
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
            finish();
        }
        if (event.getType().equalsIgnoreCase("connection")) {
            if (event.getMessage().equalsIgnoreCase("disconnected")) {
                if (!(new ConnectionCheck().isNetworkConnected(Step2Activity.this))) {
                    android.util.Log.e("RideDetailActivity", "disconnected");
                    if (!ApplicationController.isOnline) {
                        if (PrefsUtil.isInternetConnectedShowing) {
                            if (PrefsUtil.dialogInternetConnected != null) {
                                PrefsUtil.dialogInternetConnected.dismiss();
                                PrefsUtil.isInternetConnectedShowing = false;
                            }
                        }
                        final Dialog d = new Dialog(Step2Activity.this,
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

            map.animateCamera(cu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setupToolbar() {
        toolbar = findViewById(R.id.toolBar);

        txtTitle = findViewById(R.id.txtTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    public void showToolbarTiltle(String title) {
        txtTitle.setText(title);
        txtTitle.setVisibility(View.VISIBLE);
    }

    public void hideToolbarTiltle() {
        //txtTitle.setText(title);
        txtTitle.setVisibility(View.GONE);
    }

    public void turnActionBarIconWhite() {
        toggle = new ActionBarDrawerToggle
                (
                        this,
                        drawerLayout,
                        toolbar,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close
                ) {
        };
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        toggle.setHomeAsUpIndicator(R.drawable.ic_hamburger_white);


        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        changeBackgroundToolBar(R.color.dull_golden);
    }

    public void turnActionBarIconBlack() {
        toggle = new ActionBarDrawerToggle
                (
                        this,
                        drawerLayout,
                        toolbar,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close
                ) {
        };
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        toggle.setHomeAsUpIndicator(R.drawable.ic_hamburger_black);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.light_black));
        changeBackgroundToolBar(R.color.transparent);
    }


    public void hideToolbar() {
        toolbar.setVisibility(View.GONE);
    }

    public void showToolbar() {
        toolbar.setVisibility(View.VISIBLE);

    }

    public void changeBackgroundToolBar(int id) {
        toolbar.setBackgroundColor(ContextCompat.getColor(this, id));
    }
}
