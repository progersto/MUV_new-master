package com.muvit.passenger.Activities;
//working till 19th Jan 2017

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.Models.LocationModel;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.DirectionsJSONParser;
import com.muvit.passenger.Utils.PrefsUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.muvit.passenger.WebServices.WebServiceUrl.trackingUrl;


public class DriverTrackingActivity extends AppCompatActivity {
    private GoogleMap map;
    ArrayList<String> responseArray = new ArrayList<>();
    boolean animationFinished = true;
    int animationCounter = 0;
    int lastIndex = 0;
    ArrayList<LocationModel> locationArray = new ArrayList<>();
    String strDriverLat = "", strDriverLong = "", strPassengerLat = "", strPassengerLong = "", strCarLat = "", strCarLong = "", pathString = "", timeLapse = "";
    MarkerOptions diverOptions, passengerOtions, carOptions;
    Marker driverMarker, passengerMarker, carMarker;
    LatLng driverPoint, passengerPoint, lastCarPoint;
    boolean FIRST_TIME = true;
    //long  rotationTime = 0;
    float currentRotation = 0;
    private WebSocketClient mWebSocketClient;
    private String rideId;
    double lastLat, lastLong, oldBearing;
    JSONArray via;
    String lastSendId = "";
    float totalDistance = 0;
    Location previousLatLong;
    long startTime = System.currentTimeMillis();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_driver);
        Intent i = getIntent();
        try {
            rideId = i.getExtras().getString("rideId");
        } catch (Exception e) {
            e.printStackTrace();
        }
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps_fragment);
        fm.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                if (ActivityCompat.checkSelfPermission(DriverTrackingActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DriverTrackingActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                map = googleMap;
                map.animateCamera(CameraUpdateFactory.zoomTo(1));

                map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        /*Log.e("Here", " in the condition" + map.getCameraPosition().bearing);
                        try {
                            if (carMarker!=null) {
                                float rot = map.getCameraPosition().bearing
                                carMarker.setRotation(-rot > 180 ? rot / 2 : rot);
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }*/

                    }
                });
                connectWebSocket();
            }
        });


    }

    private void connectWebSocket() {
        URI uri;
        try {
            // TODO: 7/12/16 Change URL
            uri = new URI(trackingUrl);
            // uri = new URI("ws://162.144.134.38:5000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.e("Websocket", "Opened");
                String messageString;
                //Log.e("DriverTrackingActivity", "onOpen " + PrefsUtil.with(DriverTrackingActivity.this).readString("lastSendId"));
                if (TextUtils.isEmpty(PrefsUtil.with(DriverTrackingActivity.this).readString("lastSendId"))) {
                    messageString = rideId + "&&&" + "0";
                } else {
                    messageString = rideId + "&&&" + PrefsUtil.with(DriverTrackingActivity.this).readString("lastSendId");
                }
                mWebSocketClient.send(messageString);
                //mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                //Log.e("Websocket", "onMessage " + s);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject obj = new JSONObject(message);
                            Log.e("DriverTrackingActivity", "Message Received");
                            if (obj.getBoolean("status")) {
                                if (!obj.getBoolean("stop")) {
                                    responseArray.add(obj.toString());
                                    //Log.e("DriverTrackingActivity", "Response Size : " + responseArray.size());
                                    DriverTrackingActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            if (animationFinished) {
                                                if (animationCounter <= responseArray.size() - 1) {
                                                    decodeResult(responseArray.get(animationCounter));
                                                }
                                            }

                                        }
                                    });
                                } else {
                                    mWebSocketClient.close();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.e("Websocket", "Closed " + b + "int : " + i);
                if (i != -1) {
                    if (!backPressed) {
                        connectWebSocket();
                    }
                }


            }

            @Override
            public void onError(Exception e) {
                Log.e("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }


    private void decodeResult(String message) {
        lastIndex = 0;
        locationArray = new ArrayList<>();
        locationArray.clear();
        JSONObject j1;
        startTime = System.currentTimeMillis();


        try {
            j1 = new JSONObject(message);
            strDriverLat = j1.getString("driverLat");
            strDriverLong = j1.getString("driverLong");
            strPassengerLat = j1.getString("passengerLat");
            strPassengerLong = j1.getString("passengerLong");
            strCarLat = j1.getString("carLat");
            strCarLong = j1.getString("carLong");
            pathString = j1.getString("pathString");
            //Total time of via array
            timeLapse = j1.getString("timeLapse");
            lastSendId = j1.getString("lastSendId");
            //Log.e("DriverTrackingActivity", "j1.getString(lastSendId): " + j1.getString("lastSendId"));
            PrefsUtil.with(DriverTrackingActivity.this).write("lastSendId", lastSendId);
            //Log.e("DriverTrackingActivity", "decodeResult: " + lastSendId);
            //via = new JSONArray("[{\"lat\":22.28592090182224,\"long\":\"70.79294214381845\"},{\"lat\":22.28628070122583,\"long\":\"70.79276536642698\"}]");
            via = j1.getJSONArray("via");
            for (int i = 0; i < via.length(); i++) {
                LocationModel locationModal = new LocationModel();
                locationModal.setLat(via.getJSONObject(i).getString("lat"));
                locationModal.setLongitude(via.getJSONObject(i).getString("long"));


                if (i > 0) {
                    Location currentLoc = new Location("Service Provider");
                    currentLoc.setLatitude(Double.parseDouble(locationModal.getLat()));
                    currentLoc.setLongitude(Double.parseDouble(locationModal.getLongitude()));

                    Location oldLocation = new Location("Service Provider");
                    oldLocation.setLatitude(Double.parseDouble(locationArray.get(i - 1).getLat()));
                    oldLocation.setLongitude(Double.parseDouble(locationArray.get(i - 1).getLongitude()));

                    locationModal.setDistance(String.valueOf(currentLoc.distanceTo(oldLocation)));

                } else {
                    locationModal.setDistance("0");
                }

                locationArray.add(locationModal);
            }

            double lapse = Integer.parseInt(timeLapse) - 4000;
            timeLapse = String.valueOf(lapse);
            Log.e("TimeCalculation", "timeLapse: " + timeLapse);


            if (locationArray.size() > 2) {
                Location currentLoc = new Location("Service Provider");
                currentLoc.setLatitude(Double.parseDouble(locationArray.get(0).getLat()));
                currentLoc.setLongitude(Double.parseDouble(locationArray.get(0).getLongitude()));

                Location oldLocation = new Location("Service Provider");
                oldLocation.setLatitude(Double.parseDouble(locationArray.get(locationArray.size() - 1).getLat()));
                oldLocation.setLongitude(Double.parseDouble(locationArray.get(locationArray.size() - 1).getLongitude()));
                totalDistance = currentLoc.distanceTo(oldLocation);
            }


            Log.e("DriverTrackingActivity", "Via : " + via.toString() + "size : " + via.length());
            JSONObject responseObj = new JSONObject(responseArray.get(responseArray.size() - 1).toString());
            JSONArray responseVia = responseObj.getJSONArray("via");
            //Log.e("DriverTrackingActivity", "Resonse Array" + responseVia.toString());

            if (FIRST_TIME) {
                drawInitialPath();
            } else {
                if (animationCounter <= responseArray.size() - 1) {
                    if (via.length() > 0) {
                        lastCarPoint = new LatLng((Double.parseDouble(locationArray.get(lastIndex).getLat())), Double.parseDouble(locationArray.get(lastIndex).getLongitude()));
                        carMarker.remove();
                        carOptions = new MarkerOptions();
                        carOptions.position(lastCarPoint);
                        carOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi));
                        carOptions.rotation(currentRotation);
                        carOptions.anchor(0.5f, 0.5f);
                        carOptions.flat(true);
                        carMarker = map.addMarker(carOptions);
                        animationFinished = false;
                        initiateAnimation();
                    } else {
                        animationCounter++;
                        animationFinished = true;
                        if (animationCounter <= responseArray.size() - 1) {
                            decodeResult(responseArray.get(animationCounter));
                        }
                    }
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void drawInitialPath() {
        //Log.e("drawInitialPath", pathString + "here");
        new ParserTask().execute(pathString);
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }


                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(15);
                lineOptions.color(Color.DKGRAY);
            }
            map.addPolyline(lineOptions);


            //Log.e("DriverTrackingActivity", "strCarLat : " + strCarLat + "strCarLong : " + strCarLong);
            driverPoint = new LatLng(Double.parseDouble(strDriverLat), Double.parseDouble(strDriverLong));
            passengerPoint = new LatLng(Double.parseDouble(strPassengerLat), Double.parseDouble(strPassengerLong));
            FIRST_TIME = false;
            diverOptions = new MarkerOptions();
            diverOptions.position(driverPoint);
            diverOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pick_up_marker));
            passengerOtions = new MarkerOptions();
            passengerOtions.position(passengerPoint);
            passengerOtions.icon(BitmapDescriptorFactory.fromResource(R.drawable.drop_off_marker));
            driverMarker = map.addMarker(diverOptions);
            passengerMarker = map.addMarker(passengerOtions);
            carOptions = new MarkerOptions();
            carOptions.position(new LatLng(Double.parseDouble(strCarLat), Double.parseDouble(strCarLong)));
            carOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi));
            carOptions.anchor(0.5f, 0.5f);
            carOptions.flat(true);
            carMarker = map.addMarker(carOptions);
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            builder.include(carOptions.getPosition());
            LatLngBounds bounds = builder.build();
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));
            //map.animateCamera(CameraUpdateFactory.zoomTo(17));

            CameraPosition cameraPosition = new CameraPosition.Builder().
                    target(new LatLng(Double.parseDouble(strCarLat), Double.parseDouble(strCarLong))).
                    zoom(17).
                    bearing(0).
                    build();

            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            List<HashMap<String, String>> path1 = result.get(0);
            if (path1.size() > 2) {
                HashMap<String, String> currentPoint = path1.get(0);
                double lat = Double.parseDouble(currentPoint.get("lat"));
                double lng = Double.parseDouble(currentPoint.get("lng"));
                LatLng position = new LatLng(lat, lng);

                HashMap<String, String> nextPoint = path1.get(1);
                double nextLat = Double.parseDouble(nextPoint.get("lat"));
                double nextLng = Double.parseDouble(nextPoint.get("lng"));
                LatLng nextPos = new LatLng(nextLat, nextLng);


                double oldlat = position.latitude, oldlong = position.longitude;
                double newlat = nextPos.latitude, newlong = nextPos.longitude;
                Location prevLoc = new Location("service Provider");
                prevLoc.setLatitude(oldlat);
                prevLoc.setLongitude(oldlong);
                Location newLoc = new Location("service Provider");
                newLoc.setLatitude(newlat);
                newLoc.setLongitude(newlong);
                float bearing = prevLoc.bearingTo(newLoc);
                Log.e("DriverTrackingActivity", "onPostExecute: " + bearing);
                updateCamera(bearing);
                carMarker.setRotation(bearing);

            }

        }
    }


    private void initiateAnimation() {

        if (lastIndex < (locationArray.size() - 1)) {
            //animationTime = (Integer.parseInt(timeLapse)) / locationArray.size();
            //rotationTime = 200;

            rotateMarker();
        }
        // just in case via has only one value
        else {
            animationFinished = true;
            // Added as via length is one 23rd January, 12:07
            animationCounter++;
            //carMarker.setRotation(0);
        }
    }

    public void rotateMarker() {
        LatLng position = new LatLng(Double.parseDouble(locationArray.get(lastIndex + 1).getLat()), Double.parseDouble(locationArray.get(lastIndex + 1).getLongitude()));
        LatLng oldposition = new LatLng(Double.parseDouble(locationArray.get(lastIndex).getLat()), Double.parseDouble(locationArray.get(lastIndex).getLongitude()));
        double oldlat = oldposition.latitude, oldlong = oldposition.longitude;
        double newlat = position.latitude, newlong = position.longitude;
        Location prevLoc = new Location("service Provider");
        prevLoc.setLatitude(oldlat);
        prevLoc.setLongitude(oldlong);
        Location newLoc = new Location("service Provider");
        newLoc.setLatitude(newlat);
        newLoc.setLongitude(newlong);
        float bearing = prevLoc.bearingTo(newLoc);
        //updateCamera(bearing);
        //bearing = prevLoc.bearingTo(newLoc);
        if (bearing != 0.0) {
            currentRotation = bearing;
            Log.e("rotateMarker", "bearing : " + bearing);
            rotateMarker(carMarker, bearing, carMarker.getRotation());
            updateCamera(bearing);
        } else {
            //TODO: Calculate distance from two points


            LatLng newPoint = new LatLng(Double.parseDouble(locationArray.get(lastIndex + 1).getLat()), Double.parseDouble(locationArray.get(lastIndex + 1).getLongitude()));
            /*lastLat = Double.parseDouble(locationArray.get(lastIndex + 1).getLat());
            lastLong = Double.parseDouble(locationArray.get(lastIndex + 1).getLongitude());
*/
            // Calculating time for animation between two points
            updateCamera(bearing);
            if (totalDistance != 0) {
                float unitTime = Float.parseFloat(timeLapse) / totalDistance;
                float animationTimeNew = unitTime * Float.parseFloat(locationArray.get(lastIndex + 1).getDistance());
                animateMarker(newPoint, false, animationTimeNew);
            } else {
                carMarker.setPosition(newPoint);
                lastIndex++;
                if (lastIndex < (locationArray.size() - 1)) {
                    initiateAnimation();
                } else if (lastIndex == (locationArray.size() - 1)) {
                    animationCounter++;
                    animationFinished = true;
                    if (animationCounter <= responseArray.size() - 1) {
                        decodeResult(responseArray.get(animationCounter));
                    }
                }
            }

        }
    }


    public void rotateMarker(final Marker marker, final float toRotation, final float st) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = st;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / 200);
                float rot = t * toRotation + (1 - t) * startRotation;
                marker.setRotation(-rot > 180 ? rot / 2 : rot);

                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                } else {
                    LatLng newPoint = new LatLng(Double.parseDouble(locationArray.get(lastIndex + 1).getLat()), Double.parseDouble(locationArray.get(lastIndex + 1).getLongitude()));


                    if (totalDistance != 0) {
                        float unitTime = Float.parseFloat(timeLapse) / totalDistance;
                        float animationTimeNew = unitTime * Float.parseFloat(locationArray.get(lastIndex + 1).getDistance());
                        animateMarker(newPoint, false, animationTimeNew);
                    } else {
                        carMarker.setPosition(newPoint);
                        lastIndex++;
                        if (lastIndex < (locationArray.size() - 1)) {
                            initiateAnimation();
                        } else if (lastIndex == (locationArray.size() - 1)) {
                            animationCounter++;
                            animationFinished = true;

                            if (animationCounter <= responseArray.size() - 1) {
                                decodeResult(responseArray.get(animationCounter));
                            }
                        }
                    }

                }
            }
        });
    }

    public void animateMarker(final LatLng toPosition, final boolean hideMarke, final float animationTimeNew) {
        final Handler handler1 = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();
        Point startPoint = proj.toScreenLocation(carMarker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final Interpolator interpolator = new LinearInterpolator();
        lastLat = startLatLng.latitude;
        lastLong = startLatLng.longitude;
        oldBearing = carMarker.getRotation();
        handler1.post(new Runnable() {
            @Override
            public void run() {

                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / animationTimeNew);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                final Location currentLoc = new Location("");
                currentLoc.setLongitude(lng);
                currentLoc.setLatitude(lat);
                final Location lastLoc = new Location("");
                lastLoc.setLongitude(lastLong);
                lastLoc.setLatitude(lastLat);

                float distance = currentLoc.distanceTo(lastLoc);
                //Log.e("Current distance", distance + "");
                if (distance > 1.0) {
                    carMarker.setPosition(new LatLng(lat, lng));
                    lastLat = lat;
                    lastLong = lng;
                }

                /*Log.e("DriverTrackingActivity", "animateMarker : run : set pos t : "+ t + " " + animationFinished);
                carMarker.setPosition(new LatLng(lat, lng));*/

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler1.postDelayed(this, 16);
                    //[{"lat":22.28592090182224,"long":"70.79294214381845"},{"lat":22.28628070122583,"long":"70.79276536642698"}]size : 2
                } else {
                    if (hideMarke) {
                        carMarker.setVisible(false);
                    } else {
                        carMarker.setVisible(true);
                    }
                    //Log.e("last Index", lastIndex + "");
                    lastIndex++;

                    long elapsedTime = System.currentTimeMillis() - startTime;

                    if (lastIndex < (locationArray.size() - 1)) {
                        initiateAnimation();
                    } else if (lastIndex == (locationArray.size() - 1)) {
                        animationCounter++;
                        animationFinished = true;
                        Log.e("TimeCalculation", "Total elapsed http request/response time in milliseconds: " + elapsedTime);
                        //updateBounds();
                        //updateBounds();
                        if (animationCounter <= responseArray.size() - 1) {
                            decodeResult(responseArray.get(animationCounter));
                        }
                    }
                }
            }
        });
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
                if (!(new ConnectionCheck().isNetworkConnected(DriverTrackingActivity.this))) {
                    Log.e("RideDetailActivity", "disconnected");
                    if (!ApplicationController.isOnline) {
                        if (PrefsUtil.isInternetConnectedShowing) {
                            if (PrefsUtil.dialogInternetConnected != null) {
                                PrefsUtil.dialogInternetConnected.dismiss();
                                PrefsUtil.isInternetConnectedShowing = false;
                            }
                        }
                        final Dialog d = new Dialog(DriverTrackingActivity.this,
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
            if (event.getMessage().equalsIgnoreCase("connected")) {
                connectWebSocket();
            }
        }

    }

    boolean backPressed = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mWebSocketClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    boolean first_time_animate = true;
    float firstBearing;

    public void updateCamera(float bearing) {

        /*LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(carOptions.getPosition());
        builder.include(passengerMarker.getPosition());
        LatLngBounds bounds = builder.build();
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50),1000,null);*/
        if (first_time_animate) {
            bearing = (bearing + 180) % 360;
            firstBearing = bearing;

            Log.e("DriverTrackingActivity", "updateCamera: getMeasuredWidth : " + findViewById(R.id.maps_fragment).getMeasuredWidth() + " getMeasuredHeight : " + findViewById(R.id.maps_fragment).getMeasuredHeight());

            CameraPosition currentPlace = new CameraPosition.Builder()
                    .target(map.getCameraPosition().target)
                    .bearing(bearing)
                    .zoom(map.getCameraPosition().zoom).build();

            map.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace), 500, null);
            first_time_animate = false;
        } else {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            builder.include(carMarker.getPosition());
            builder.include(passengerMarker.getPosition());
            final LatLngBounds bounds = builder.build();
            LatLng ne = bounds.northeast;
            LatLng sw = bounds.southwest;
            LatLng center = new LatLng((ne.latitude + sw.latitude) / 2,
                    (ne.longitude + sw.longitude) / 2);
            CameraPosition currentPlace = new CameraPosition.Builder()
                    .target(center)
                    .bearing(firstBearing)
                    .zoom(getBoundsZoomLevel(ne, sw, findViewById(R.id.maps_fragment).getMeasuredWidth(), findViewById(R.id.maps_fragment).getMeasuredHeight())).build();

       /* CameraPosition currentPlace = new CameraPosition.Builder()
                .target(bounds.getCenter())
                .bearing(bearing).zoom(map.getCameraPosition().zoom).build();*/
            //map.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));

            map.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace), 500, null);

        }
    }

    public int getBoundsZoomLevel(LatLng northeast, LatLng southwest,
                                  int width, int height) {
        final float GLOBE_WIDTH = 256 * getResources().getDisplayMetrics().density; // a constant in Google's map projection
        final float ZOOM_MAX = 17;
        double latFraction = (latRad(northeast.latitude) - latRad(southwest.latitude)) / Math.PI;
        double lngDiff = northeast.longitude - southwest.longitude;
        double lngFraction = ((lngDiff < 0) ? (lngDiff + 360) : lngDiff) / 360;
        double latZoom = zoom(height, GLOBE_WIDTH, latFraction);
        double lngZoom = zoom(width, GLOBE_WIDTH, lngFraction);
        double zoom = Math.min(Math.min(latZoom, lngZoom), ZOOM_MAX);
        Log.e("DriverTrackingActivity", "getBoundsZoomLevel: zoom :  " + zoom);
        return (int) (zoom);
    }

    private double latRad(double lat) {
        double sin = Math.sin(lat * Math.PI / 180);
        double radX2 = Math.log((1 + sin) / (1 - sin)) / 2;
        return Math.max(Math.min(radX2, Math.PI), -Math.PI) / 2;
    }

    private double zoom(double mapPx, double worldPx, double fraction) {
        final double LN2 = .693147180559945309417;
        return (Math.log(mapPx / worldPx / fraction) / LN2);
    }

    /*private void moveToCurrentLocation(LatLng currentLocation)
    {
        *//*map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);*//*

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


    }*/

    /*private void updateCamera(float bearing) {
        CameraPosition oldPos = map.getCameraPosition();

        CameraPosition pos = CameraPosition.builder(oldPos).bearing(bearing).build();
        map.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
    }*/

    @Override
    public void onBackPressed() {
        backPressed = true;
        Intent i = new Intent(this, RideInformationActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.putExtra("rideId", rideId);
        startActivity(i);
        super.onBackPressed();
    }
}