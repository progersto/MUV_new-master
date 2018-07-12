package com.muvit.passenger.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import com.muvit.passenger.Models.CommonPOJO;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.Models.RideInfo;
import com.muvit.passenger.Models.RideInfoPOJO;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.ImgUtils;
import com.muvit.passenger.Utils.KeyboardUtils;
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
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static com.muvit.passenger.WebServices.WebServiceUrl.waitingUrl;

public class RideInformationActivity extends AppCompatActivity {

    WebSocketClient mWebSocketClient;
    JSONObject obj;
//    private Toolbar toolbar;
    private TextView txtTitle;
    private TextView txtDriverName, txtRatings, txtTotalRatings, txtCarName, txtCarType, txtEstArrivalTime, txtCarNo, txtContactNo, txtMinFareRate,
            txtMinFare, txtKmFare, txtPerKmCharges, txtTimeCharges;
    private GoogleMap map;
    private Button btnCancelTrip, btnTrackDrive;
    private String rideId = "1";
    private ImageView imgDriverImage, imgPanic,back_btn;
    private LatLng pickupLatLng = new LatLng(0.0, 0.0);
    private LatLng dropoffLatLng = new LatLng(0.0, 0.0);
    private Marker pickupMarker;
    private Marker dropoffMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_information);
        initViews();

        setUpMap();
        try {
            rideId = getIntent().getStringExtra("rideId");
        } catch (Exception e) {
            e.printStackTrace();
        }
        connectWebSocket();
        getRideInfo();

        btnCancelTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RideInformationActivity.this);
                builder.setMessage("Are you sure you want cancel this ride?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                cancelTrip();

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                // Create the AlertDialog object and return it
                builder.create();
                builder.show();

            }
        });

        btnTrackDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RideInformationActivity.this, DriverTrackingActivity.class);
                i.putExtra("rideId", rideId);
                startActivity(i);
                mWebSocketClient.close();
                finish();
                /*try {
                    if (!obj.getBoolean("status")) {
                        Toast.makeText(RideInformationActivity.this, "Please wait for " + obj.getString("wait") + " minutes", Toast.LENGTH_SHORT).show();
                    }else {
                        Intent i = new Intent(RideInformationActivity.this,DriverTrackingActivity.class);
                        i.putExtra("rideId",rideId);
                        startActivity(i);
                        mWebSocketClient.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }*/

            }
        });


    }

    private void initViews() {
        back_btn = (ImageView) findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        txtDriverName = (TextView) findViewById(R.id.txtDriverName);
        txtRatings = (TextView) findViewById(R.id.txtRatings);
        txtTotalRatings = (TextView) findViewById(R.id.txtTotalRatings);
        txtCarName = (TextView) findViewById(R.id.txtCarName);
        txtCarType = (TextView) findViewById(R.id.txtCarType);
        txtEstArrivalTime = (TextView) findViewById(R.id.txtEstArrivalTime);
        txtCarNo = (TextView) findViewById(R.id.txtCarNo);
        txtContactNo = (TextView) findViewById(R.id.txtContactNo);
        txtMinFare = (TextView) findViewById(R.id.txtMinFare);
        txtMinFareRate = (TextView) findViewById(R.id.txtMinFareRate);
        txtKmFare = (TextView) findViewById(R.id.txtKmFare);
        txtPerKmCharges = (TextView) findViewById(R.id.txtPerKmCharges);
        txtTimeCharges = (TextView) findViewById(R.id.txtTimeCharges);
        imgDriverImage = (ImageView) findViewById(R.id.txtDriverImage);
        imgPanic = (ImageView) findViewById(R.id.imgPanic);
        btnCancelTrip = (Button) findViewById(R.id.btnCancelTrip);
        btnTrackDrive = (Button) findViewById(R.id.btnTrackDrive);
        imgPanic.setVisibility(View.GONE);
        new KeyboardUtils().setupUI(findViewById(R.id.activity_ride_information), RideInformationActivity.this);
       // setupToolbar();
    }

    private void setupToolbar() {
//        toolbar = (Toolbar) findViewById(R.id.toolBar);
//        txtTitle = (TextView) findViewById(R.id.txtTitle);
//        txtTitle.setText(R.string.ride_information_title);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    /*private void getPanicNumber(){
        String url= WebServiceUrl.ServiceUrl+WebServiceUrl.getpanicnumber;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(this).readInt("uId")));
        new ParseJSON(this, url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
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
                }else {
                    Toast.makeText(RideInformationActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    private void setPanicRide(){
        String url= WebServiceUrl.ServiceUrl+WebServiceUrl.setpanicride;
        ArrayList<String> params = new ArrayList<>();
        params.add("rideId");
        ArrayList<String> values = new ArrayList<>();
        values.add(rideId);
        new ParseJSON(this, url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    final PanicDataPOJO resultObj = (PanicDataPOJO) obj;
                    imgPanic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", resultObj.getPanicData().get(0).getUserPanicNo(), null));
                            startActivity(intent);
                        }
                    });
                }else {
                    Toast.makeText(RideInformationActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }*/

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
                    RideInfo rideInfo = resultObj.getRideInfo();
                    txtDriverName.setText(rideInfo.getDriverName());
                    txtRatings.setText(String.valueOf(rideInfo.getAvgRatting()));
                    txtTotalRatings.setText(String.valueOf(rideInfo.getTotalRatting()) + " Ratings");
                    txtCarName.setText(rideInfo.getBrandName() + " " + rideInfo.getCarName());
                    txtCarType.setText(rideInfo.getTypeName());
                    txtEstArrivalTime.setText(rideInfo.getDriverArrivelTime());
                    txtCarNo.setText(rideInfo.getCarNumber());
                    txtContactNo.setText(rideInfo.getDriverContact());
                    txtMinFare.setText(getString(R.string.currencySign) + rideInfo.getMinFareKmRate());
                    txtMinFareRate.setText("Min Fare (" + rideInfo.getMinFareKm() + " Km)");
                    txtKmFare.setText("Per km charge after  (" + rideInfo.getMinFareKm() + " Km)");
                    txtPerKmCharges.setText(getString(R.string.currencySign) + rideInfo.getExtraFareKmRate());
                    txtTimeCharges.setText(getString(R.string.currencySign) + String.valueOf(rideInfo.getPerMinRate()));

                    if (rideInfo.getIsLongRide().equalsIgnoreCase("y")) {
                        btnTrackDrive.setVisibility(View.GONE);
                    } else {
                        btnTrackDrive.setVisibility(View.VISIBLE);
                    }

                    Ion.with(imgDriverImage)
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
                            .load(WebServiceUrl.profileUrl + rideInfo.getDriverProfileImage());

                    try {
                        pickupLatLng = new LatLng(Double.parseDouble(rideInfo.getPickUpLat()), Double.parseDouble(rideInfo.getPickUpLong()));
                        dropoffLatLng = new LatLng(Double.parseDouble(rideInfo.getDropOffLat()), Double.parseDouble(rideInfo.getDropOffLong()));

                        setPickupMarker(pickupLatLng, rideInfo.getPickUpLocation());
                        setDropoffMarker(dropoffLatLng, rideInfo.getDropOffLocation());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //getPanicNumber();

                } else {
                    Toast.makeText(RideInformationActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
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

    private void cancelTrip() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.cancelride;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        params.add("userType");
        params.add("rideId");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(this).readInt("uId")));
        values.add("u");
        values.add(rideId);
        new ParseJSON(this, url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    CommonPOJO resultObj = (CommonPOJO) obj;
                    Toast.makeText(RideInformationActivity.this, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                    try {
                        mWebSocketClient.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    finish();
                } else {
                    Toast.makeText(RideInformationActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI(waitingUrl);
            // uri = new URI("ws://162.144.134.38:5000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.e("Websocket", "Opened");
                mWebSocketClient.send(rideId);
                //mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                Log.e("Websocket", "onMessage " + s);

                try{

                }catch (Exception e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            obj = new JSONObject(message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Log.e("TripTrackingActivity", "message : "+message );
                        /*responseArray.add(message);

                      */
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.e("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.e("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
        //new ParserTask().execute(pathString);
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
            Toast.makeText(RideInformationActivity.this, "Cannot get address from clicked location", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(RideInformationActivity.this, "Cannot get address from clicked location", Toast.LENGTH_SHORT).show();
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
                || event.getType().equalsIgnoreCase("rideInfo")
                || event.getType().equalsIgnoreCase("ridestarted")) {
            try {
                finish();
                mWebSocketClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (event.getType().equalsIgnoreCase("connection")) {
            if (event.getMessage().equalsIgnoreCase("disconnected")) {
                if (!(new ConnectionCheck().isNetworkConnected(RideInformationActivity.this))) {
                    Log.e("RideDetailActivity", "disconnected");
                    if (!ApplicationController.isOnline) {
                        if (PrefsUtil.isInternetConnectedShowing) {
                            if (PrefsUtil.dialogInternetConnected != null) {
                                PrefsUtil.dialogInternetConnected.dismiss();
                                PrefsUtil.isInternetConnectedShowing = false;
                            }
                        }
                        final Dialog d = new Dialog(RideInformationActivity.this,
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
}
