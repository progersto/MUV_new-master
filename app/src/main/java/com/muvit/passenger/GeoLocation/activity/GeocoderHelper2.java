package com.muvit.passenger.GeoLocation.activity;

/**
 * Created by nct58 on 5/9/17.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;


public class GeocoderHelper2 {
    private Activity context;
    private double latitude;
    private double longitude;
    private onGetAddress listener;


    public GeocoderHelper2(Activity context, double latitude, double longitude, onGetAddress listener){
        this.context = context;
        this.latitude = latitude;
        this.longitude = longitude;
        this.listener = listener;

        fetchAddress();
    }

    @SuppressLint("StaticFieldLeak")
    public void fetchAddress() {

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                String Address = null;

                if (Geocoder.isPresent()) {
                    try {
                        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if (addresses.size() > 0) {
                            final Address address = addresses.get(0);
                            Log.e("Home", "run: address line 0: " + addresses.get(0).getAddressLine(0));
                            Log.e("Home", "run: address : " + addresses.get(0));


                            if (address != null) {
                                final StringBuilder sb = new StringBuilder();
                                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                                    sb.append(address.getAddressLine(i) + " ");
                                }

                                if (sb.toString().length() > 0) {
                                    Address = sb.toString();
                                } else {
                                    Log.e("FROM Third ELSE :", "ELSE");
                                }
                            } else {
                                Log.e("FROM second ELSE :", "ELSE");
                            }
                        } else {
                            Log.e("FROM FIRST ELSE :", "ELSE");
                        }
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                        // after a while, Geocoder start to trhow "Service not availalbe" exception. really weird since it was working before (same device, same Android version etc..
                    }
                }

                if (Address != null) // i.e., Geocoder succeed
                {
                    return Address;
                } else // i.e., Geocoder failed
                {
                    return fetchAddressUsingGoogleMap();
                }
            }

            // Geocoder failed :-(
            // Our B Plan : Google Map
            private String fetchAddressUsingGoogleMap() {
                String googleMapUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + ","
                        + longitude + "&sensor=false&language=en";

                try {
                    /*JSONObject googleMapResponse = new JSONObject(ANDROID_HTTP_CLIENT.execute(new HttpGet(googleMapUrl),
                            new BasicResponseHandler()));*/

                    URL url = new URL(googleMapUrl);
                    JSONObject googleMapResponse = null;

                    try {
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                        int responseCode = urlConnection.getResponseCode();

                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            String server_response = readStream(urlConnection.getInputStream());
                            if (server_response != null && server_response.length() > 0) {
                                googleMapResponse = new JSONObject(server_response);
                                urlConnection.disconnect();
                            }
                            Log.v("CatalogClient", server_response);
                        } else {
                            Log.e("MAP REQUEST : ", responseCode + "");
                            urlConnection.disconnect();
                        }

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // many nested loops.. not great -> use expression instead
                    // loop among all results
                    JSONArray results = (JSONArray) googleMapResponse.get("results");
                    if (results != null && results.length() > 0) {
                        JSONObject result = results.getJSONObject(0);

                        if (result.has("formatted_address")) {
                            final String address = result.getString("formatted_address");
                            if (address != null && address.length() > 0) {
                                return address;
                            }
                        }
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
                return "";
            }

            protected void onPostExecute(String Address) {
                if (Address != null) {
                    // Do something with cityName
                    Log.i("GeocoderHelper", Address);
                    listener.onSuccess(Address);
                } else {
                    listener.onFail();
                }
            }
        }.execute();
    }

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }

    public interface onGetAddress{
        public void onSuccess(String address);
        public void onFail();
    }
}
