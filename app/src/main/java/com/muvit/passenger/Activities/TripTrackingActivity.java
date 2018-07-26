package com.muvit.passenger.Activities;

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
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.Models.TripTrackPOJO;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.DirectionsJSONParser;
import com.muvit.passenger.Utils.GPSTracker;
import com.muvit.passenger.Utils.GPSTrackerForUpdate;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TripTrackingActivity extends AppCompatActivity {
    private GoogleMap map;
    String strDropOffLat = "", strDropOffLong = "", strPickUpLat = "", strPickUpLong = "", pathString = "";
    MarkerOptions dropOffOptions, pickUpOtions, currentOptions;
    Marker dropOffMarker, pickUpMarker, currentMarker;
    LatLng dropOffPoint, pickUpPoint, currentPoint;
    private String rideId;
    float currentRotation = 0;
    double lastLat, lastLong, oldBearing, oldLat, oldLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_driver);
        Intent i = getIntent();

        try {
            rideId = i.getExtras().getString("rideId");
        } catch (Exception e) {
            //rideId = "62";
            e.printStackTrace();
        }
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps_fragment);
        fm.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                if (ActivityCompat.checkSelfPermission(TripTrackingActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(TripTrackingActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                map = googleMap;
                map.setMyLocationEnabled(false);
                map.animateCamera(CameraUpdateFactory.zoomTo(12));
                //// TODO: 7/12/16 setup socket
                //setupSocketClient();
                //connectWebSocket();


                getPathString();
            }
        });


        //message = "{\"driverLat\":22.280431,\"driverLong\":70.80318,\"passengerLat\":22.280055,\"passengerLong\":70.798927,\"carLat\":\"a\",\"carLong\":\"a\",\"pathString\":\"{\\n   \\\"geocoded_waypoints\\\" : [\\n      {\\n         \\\"geocoder_status\\\" : \\\"OK\\\",\\n         \\\"place_id\\\" : \\\"ChIJcbgBM3PKWTkRYPk2qHyMPko\\\",\\n         \\\"types\\\" : [ \\\"street_address\\\" ]\\n      },\\n      {\\n         \\\"geocoder_status\\\" : \\\"OK\\\",\\n         \\\"place_id\\\" : \\\"ChIJlxvquRLKWTkRXMytX1G1b08\\\",\\n         \\\"types\\\" : [ \\\"street_address\\\" ]\\n      }\\n   ],\\n   \\\"routes\\\" : [\\n      {\\n         \\\"bounds\\\" : {\\n            \\\"northeast\\\" : {\\n               \\\"lat\\\" : 22.2806156,\\n               \\\"lng\\\" : 70.8043505\\n            },\\n            \\\"southwest\\\" : {\\n               \\\"lat\\\" : 22.2796667,\\n               \\\"lng\\\" : 70.79920729999999\\n            }\\n         },\\n         \\\"copyrights\\\" : \\\"Map data ©2016 Google\\\",\\n         \\\"legs\\\" : [\\n            {\\n               \\\"distance\\\" : {\\n                  \\\"text\\\" : \\\"0.7 km\\\",\\n                  \\\"value\\\" : 730\\n               },\\n               \\\"duration\\\" : {\\n                  \\\"text\\\" : \\\"4 mins\\\",\\n                  \\\"value\\\" : 222\\n               },\\n               \\\"end_address\\\" : \\\"28, Gondal Rd, Udhyog Nagar Colony, Bhakti Nagar, Rajkot, Gujarat 360002, India\\\",\\n               \\\"end_location\\\" : {\\n                  \\\"lat\\\" : 22.28002,\\n                  \\\"lng\\\" : 70.7992496\\n               },\\n               \\\"start_address\\\" : \\\"Sahajanand House, Street No. 9, 80 Feet Rd, Near Bhakti Nagar Circle, Bhaktinagar Society, Bhakti Nagar, Rajkot, Gujarat 360002, India\\\",\\n               \\\"start_location\\\" : {\\n                  \\\"lat\\\" : 22.280423,\\n                  \\\"lng\\\" : 70.80318149999999\\n               },\\n               \\\"steps\\\" : [\\n                  {\\n                     \\\"distance\\\" : {\\n                        \\\"text\\\" : \\\"0.1 km\\\",\\n                        \\\"value\\\" : 122\\n                     },\\n                     \\\"duration\\\" : {\\n                        \\\"text\\\" : \\\"1 min\\\",\\n                        \\\"value\\\" : 16\\n                     },\\n                     \\\"end_location\\\" : {\\n                        \\\"lat\\\" : 22.2806156,\\n                        \\\"lng\\\" : 70.8043505\\n                     },\\n                     \\\"html_instructions\\\" : \\\"Head \\\\u003cb\\\\u003eeast\\\\u003c/b\\\\u003e on \\\\u003cb\\\\u003e80 Feet Rd\\\\u003c/b\\\\u003e\\\",\\n                     \\\"polyline\\\" : {\\n                        \\\"points\\\" : \\\"ss~fC{vcoLg@iF\\\"\\n                     },\\n                     \\\"start_location\\\" : {\\n                        \\\"lat\\\" : 22.280423,\\n                        \\\"lng\\\" : 70.80318149999999\\n                     },\\n                     \\\"travel_mode\\\" : \\\"DRIVING\\\"\\n                  },\\n                  {\\n                     \\\"distance\\\" : {\\n                        \\\"text\\\" : \\\"0.2 km\\\",\\n                        \\\"value\\\" : 224\\n                     },\\n                     \\\"duration\\\" : {\\n                        \\\"text\\\" : \\\"2 mins\\\",\\n                        \\\"value\\\" : 107\\n                     },\\n                     \\\"end_location\\\" : {\\n                        \\\"lat\\\" : 22.2802231,\\n                        \\\"lng\\\" : 70.80227959999999\\n                     },\\n                     \\\"html_instructions\\\" : \\\"Make a \\\\u003cb\\\\u003eU-turn\\\\u003c/b\\\\u003e at BHAGVATI TRADERS\\\\u003cdiv style=\\\\\\\"font-size:0.9em\\\\\\\"\\\\u003ePass by Doshi Plastic (on the left)\\\\u003c/div\\\\u003e\\\",\\n                     \\\"maneuver\\\" : \\\"uturn-right\\\",\\n                     \\\"polyline\\\" : {\\n                        \\\"points\\\" : \\\"{t~fCe~coLPBj@dGBn@LbB\\\"\\n                     },\\n                     \\\"start_location\\\" : {\\n                        \\\"lat\\\" : 22.2806156,\\n                        \\\"lng\\\" : 70.8043505\\n                     },\\n                     \\\"travel_mode\\\" : \\\"DRIVING\\\"\\n                  },\\n                  {\\n                     \\\"distance\\\" : {\\n                        \\\"text\\\" : \\\"0.3 km\\\",\\n                        \\\"value\\\" : 344\\n                     },\\n                     \\\"duration\\\" : {\\n                        \\\"text\\\" : \\\"1 min\\\",\\n                        \\\"value\\\" : 84\\n                     },\\n                     \\\"end_location\\\" : {\\n                        \\\"lat\\\" : 22.2796667,\\n                        \\\"lng\\\" : 70.79920729999999\\n                     },\\n                     \\\"html_instructions\\\" : \\\"At \\\\u003cb\\\\u003eBhakti Nagar Cir\\\\u003c/b\\\\u003e, take the \\\\u003cb\\\\u003e2nd\\\\u003c/b\\\\u003e exit and stay on \\\\u003cb\\\\u003e80 Feet Rd\\\\u003c/b\\\\u003e\\\\u003cdiv style=\\\\\\\"font-size:0.9em\\\\\\\"\\\\u003ePass by Corporation Bank ATM (on the left)\\\\u003c/div\\\\u003e\\\",\\n                     \\\"maneuver\\\" : \\\"roundabout-left\\\",\\n                     \\\"polyline\\\" : {\\n                        \\\"points\\\" : \\\"kr~fCgqcoLD?D@B@B@@?DBBB?@@@@@?@@@?@?@@D@B?B?@?B?BAB?BABA@ABA@ABA@C@A@C?A@E@RjALbA^jD@TBTJhAN|A\\\"\\n                     },\\n                     \\\"start_location\\\" : {\\n                        \\\"lat\\\" : 22.2802231,\\n                        \\\"lng\\\" : 70.80227959999999\\n                     },\\n                     \\\"travel_mode\\\" : \\\"DRIVING\\\"\\n                  },\\n                  {\\n                     \\\"distance\\\" : {\\n                        \\\"text\\\" : \\\"40 m\\\",\\n                        \\\"value\\\" : 40\\n                     },\\n                     \\\"duration\\\" : {\\n                        \\\"text\\\" : \\\"1 min\\\",\\n                        \\\"value\\\" : 15\\n                     },\\n                     \\\"end_location\\\" : {\\n                        \\\"lat\\\" : 22.28002,\\n                        \\\"lng\\\" : 70.7992496\\n                     },\\n                     \\\"html_instructions\\\" : \\\"Turn \\\\u003cb\\\\u003eright\\\\u003c/b\\\\u003e at \\\\u003cb\\\\u003eMakkam Chowk\\\\u003c/b\\\\u003e onto \\\\u003cb\\\\u003eGondal Rd\\\\u003c/b\\\\u003e\\\\u003cdiv style=\\\\\\\"font-size:0.9em\\\\\\\"\\\\u003eDestination will be on the left\\\\u003c/div\\\\u003e\\\",\\n                     \\\"maneuver\\\" : \\\"turn-right\\\",\\n                     \\\"polyline\\\" : {\\n                        \\\"points\\\" : \\\"}n~fCa~boLM?w@G\\\"\\n                     },\\n                     \\\"start_location\\\" : {\\n                        \\\"lat\\\" : 22.2796667,\\n                        \\\"lng\\\" : 70.79920729999999\\n                     },\\n                     \\\"travel_mode\\\" : \\\"DRIVING\\\"\\n                  }\\n               ],\\n               \\\"traffic_speed_entry\\\" : [],\\n               \\\"via_waypoint\\\" : []\\n            }\\n         ],\\n         \\\"overview_polyline\\\" : {\\n            \\\"points\\\" : \\\"ss~fC{vcoLg@iFPBn@tHLbBD?HBNHBFDXCNGJKDGB`@nCp@`HN|AM?w@G\\\"\\n         },\\n         \\\"summary\\\" : \\\"80 Feet Rd\\\",\\n         \\\"warnings\\\" : [],\\n         \\\"waypoint_order\\\" : []\\n      }\\n   ],\\n   \\\"status\\\" : \\\"OK\\\"\\n}\\n\",\"timeLapse\":30000,\"via\":[{\"lat\":22.27975406991726,\"long\":\"70.79970458009169\"},{\"lat\":22.2797508,\"long\":\"70.7996847\"},{\"lat\":22.2797508,\"long\":\"70.7996847\"},{\"lat\":22.279696747321072,\"long\":\"70.79937786491136\"},{\"lat\":22.279875274171363,\"long\":\"70.80043591989431\"},{\"lat\":22.2798441,\"long\":\"70.80026529999999\"},{\"lat\":22.2798441,\"long\":\"70.80026529999999\"},{\"lat\":22.279827899999994,\"long\":\"70.800162\"},{\"lat\":22.279827899999994,\"long\":\"70.800162\"},{\"lat\":22.279811,\"long\":\"70.8000507\"},{\"lat\":22.279811,\"long\":\"70.8000507\"},{\"lat\":22.27975406991726,\"long\":\"70.79970458009169\"}]}";

        //decodeResult(message);

        /*if (source.equals("new")) {
            // Call new service
        } else if (source.equals("current")) {
            // Send message to server to get existing data
        }*/

    }

    private void initiateTracking() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!stopFlag) {
                    //GPSTracker gpsTracker = new GPSTracker(TripTrackingActivity.this);
                    GPSTrackerForUpdate gpsTracker =
                            new GPSTrackerForUpdate(TripTrackingActivity.this);
                    Location location = gpsTracker.getLocation();
                    if (location.getLatitude() == 9999999 || location.getLongitude() == 9999999) {
                        Toast.makeText(TripTrackingActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            if (currentMarker != null) {
                                oldLat = currentMarker.getPosition().latitude;
                                oldLong = currentMarker.getPosition().longitude;
                                //currentMarker.remove();
                                Log.e("TripTracking", "removing");
                                Location prevLoc = new Location("service Provider");
                                prevLoc.setLatitude(oldLat);
                                prevLoc.setLongitude(oldLong);
                                Location newLoc = new Location("service Provider");
                                newLoc.setLatitude(location.getLatitude());
                                newLoc.setLongitude(location.getLongitude());
                                if (newLoc.distanceTo(prevLoc) > 10) {
                                    rotateMarker(new LatLng(location.getLatitude(), location.getLongitude()), new LatLng(oldLat, oldLong));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    /*Geocoder geocoder = new Geocoder(getActivity());
                    List<Address> addresses  = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                    Log.e("UserProfileFragment", "onMapReady: "+addresses.get(0).getCountryName());*/



               /* LatLngBounds bounds = builder.build();
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));
                map.moveCamera(CameraUpdateFactory.zoomTo(18));*/
                    handler.postDelayed(this, TimeUnit.SECONDS.toMillis(3));
                }
            }
        }, 2000);
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
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

        // Executes in UI thread, after the parsing process
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
            markPoints();
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
                currentMarker.setRotation(bearing);

            }

        }
    }

    private void markPoints() {
        dropOffPoint = new LatLng(Double.parseDouble(strDropOffLat), Double.parseDouble(strDropOffLong));
        pickUpPoint = new LatLng(Double.parseDouble(strPickUpLat), Double.parseDouble(strPickUpLong));

        dropOffOptions = new MarkerOptions();
        dropOffOptions.position(dropOffPoint);
        dropOffOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.drop_off_marker));
        pickUpOtions = new MarkerOptions();
        pickUpOtions.position(pickUpPoint);
        pickUpOtions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pick_up_marker));
        dropOffMarker = map.addMarker(dropOffOptions);
        pickUpMarker = map.addMarker(pickUpOtions);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(dropOffOptions.getPosition());
        builder.include(pickUpOtions.getPosition());
        LatLngBounds bounds = builder.build();
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));

        GPSTracker gpsTracker = new GPSTracker(TripTrackingActivity.this);
        Location location = gpsTracker.getLocation();
        if (location.getLatitude() == 9999999 || location.getLongitude() == 9999999) {
            Toast.makeText(TripTrackingActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
        }
                    /*Geocoder geocoder = new Geocoder(getActivity());
                    List<Address> addresses  = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                    Log.e("UserProfileFragment", "onMapReady: "+addresses.get(0).getCountryName());*/

        currentOptions = new MarkerOptions();
        currentOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
        currentOptions.title("Your are here");
        currentOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi));
        currentMarker = map.addMarker(currentOptions);
        initiateTracking();

    }


    private void getPathString() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.getpathstring;
        ArrayList<String> params = new ArrayList<>();
        params.add("rideId");
        ArrayList<String> values = new ArrayList<>();
        values.add(rideId);
        new ParseJSON(this, url, params, values, TripTrackPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    TripTrackPOJO resultObj = (TripTrackPOJO) obj;
                    new TripTrackingActivity.ParserTask().execute(resultObj.getTripTrack().getRidePathString());
                    strDropOffLat = resultObj.getTripTrack().getDropOffLat();
                    strDropOffLong = resultObj.getTripTrack().getDropOffLong();
                    strPickUpLat = resultObj.getTripTrack().getPickUpLat();
                    strPickUpLong = resultObj.getTripTrack().getPickUpLong();
                } else {
                    Toast.makeText(TripTrackingActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    public void rotateMarker(LatLng position, LatLng oldposition) {
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
            rotateMarker(currentMarker, bearing, currentMarker.getRotation(), position);
            // updateCamera(bearing);
        } else {
            //TODO: Calculate distance from two points


            LatLng newPoint = position;
            /*lastLat = Double.parseDouble(locationArray.get(lastIndex + 1).getLat());
            lastLong = Double.parseDouble(locationArray.get(lastIndex + 1).getLongitude());
*/
            // Calculating time for animation between two points
            //updateCamera(bearing);
            float animationTimeNew = 2500;
            animateMarker(newPoint, false, animationTimeNew);
           /* if (totalDistance != 0) {
                float animationTimeNew = 1000;
                animateMarker(newPoint, false, animationTimeNew);
            } else {
                currentMarker.setPosition(newPoint);
                //lastIndex++;
                initiateAnimation();
                if (lastIndex < (locationArray.size() - 1)) {
                    initiateAnimation();
                } else if (lastIndex == (locationArray.size() - 1)) {
                    animationCounter++;
                    animationFinished = true;
                    if (animationCounter <= responseArray.size() - 1) {
                        decodeResult(responseArray.get(animationCounter));
                    }
                }
            }*/

        }
    }

    public void rotateMarker(final Marker marker, final float toRotation, final float st, final LatLng position) {
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
                    LatLng newPoint = position;
                    float animationTimeNew = 2500;
                    animateMarker(newPoint, false, animationTimeNew);

                    /*if (totalDistance != 0) {
                        float unitTime = Float.parseFloat(timeLapse) / totalDistance;
                        float animationTimeNew = unitTime * Float.parseFloat(locationArray.get(lastIndex + 1).getDistance());
                        animateMarker(newPoint, false, animationTimeNew);
                    } else {
                        currentMarker.setPosition(newPoint);
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
                    }*/

                }
            }
        });
    }

    public void animateMarker(final LatLng toPosition, final boolean hideMarke, final float animationTimeNew) {
        final Handler handler1 = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();
        Point startPoint = proj.toScreenLocation(currentMarker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final Interpolator interpolator = new LinearInterpolator();
        lastLat = startLatLng.latitude;
        lastLong = startLatLng.longitude;
        oldBearing = currentMarker.getRotation();
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
                    currentMarker.setPosition(new LatLng(lat, lng));
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
                        currentMarker.setVisible(false);
                    } else {
                        currentMarker.setVisible(true);
                    }
                    //Log.e("last Index", lastIndex + "");
                   /* lastIndex++;


                    if (lastIndex < (locationArray.size() - 1)) {
                        initiateAnimation();
                    } else if (lastIndex == (locationArray.size() - 1)) {
                        animationCounter++;
                        animationFinished = true;
                        //updateBounds();
                        //updateBounds();
                        if (animationCounter <= responseArray.size() - 1) {
                            decodeResult(responseArray.get(animationCounter));
                        }
                    }*/
                }
            }
        });
    }

    boolean stopFlag = false;

    @Override
    protected void onPause() {
        stopFlag = true;
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        /*Intent i = new Intent(this, StartedRideInformationActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.putExtra("rideId", rideId);
        startActivity(i);*/
        super.onBackPressed();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.getType().equalsIgnoreCase("cancelride")
                || event.getType().equalsIgnoreCase("fareEstimate")) {
            finish();
        }
        if (event.getType().equalsIgnoreCase("connection")) {
            if (event.getMessage().equalsIgnoreCase("disconnected")) {
                if (!(new ConnectionCheck().isNetworkConnected(TripTrackingActivity.this))) {
                    Log.e("RideDetailActivity", "disconnected");
                    if (!ApplicationController.isOnline) {
                        if (PrefsUtil.isInternetConnectedShowing) {
                            if (PrefsUtil.dialogInternetConnected != null) {
                                PrefsUtil.dialogInternetConnected.dismiss();
                                PrefsUtil.isInternetConnectedShowing = false;
                            }
                        }
                        final Dialog d = new Dialog(TripTrackingActivity.this,
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
