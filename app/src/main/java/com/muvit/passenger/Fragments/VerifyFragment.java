package com.muvit.passenger.Fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.muvit.passenger.AsyncTask.RaveApiRequests;
import com.muvit.passenger.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VerifyFragment extends DialogFragment {

    public static final String CARDNO = "cardno";
    public static final String EXPIRY_MM = "expiry mm";
    public static final String EXPIRY_YY = "expiry yy";
    public static final String CVV = "cvv";
    public static final String PAYMENT_AMOUNT = "payment amount";

    private boolean isChargeMade = false;
    private boolean isConfirmationNeeded = false;
    private String confirmationType = "";
    private String transactionReference;
    private LatLng myLocation = null;
    private LocationManager lm;
    private String baseUrl = "https://api.ravepay.co/";
    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = builder.create();
    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();
    private RaveApiRequests service = retrofit.create(RaveApiRequests.class);
    private TextView message;
    private EditText otp;
    private Button confirm;
    private ProgressBar progressBar;
    private String secretKey;
    private String publicKey;
    private String numCard;
    private String cvv;
    private String mm;
    private String yy;
    private String paymentAmount;


    public VerifyFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.fragment_verify, null);
        AlertDialog.Builder adb = new AlertDialog.Builder(getContext());

        secretKey = getResources().getString(R.string.rave_payment_secret_key);
        publicKey = getResources().getString(R.string.rave_payment_public_key);

        assert getArguments() != null;
        numCard = getArguments().getString(CARDNO).replace(" ", "");
        cvv = getArguments().getString(CVV);
        mm = getArguments().getString(EXPIRY_MM);
        yy = getArguments().getString(EXPIRY_YY);
        paymentAmount = getArguments().getString(PAYMENT_AMOUNT);

        message = view.findViewById(R.id.message);
        otp = view.findViewById(R.id.otp_text);
        otp.setVisibility(View.GONE);
        confirm = view.findViewById(R.id.send_otp);
        progressBar = view.findViewById(R.id.otp_progress_bar);

        confirm.setOnClickListener(v -> {
            confirm.setClickable(false);
            progressBar.setVisibility(View.VISIBLE);
            if (!isChargeMade) {
                if (!isConfirmationNeeded) {
                    Log.d("ENCRYPTING", "Execute firs request");
                    String data = "{\n" +
                            "  \"PBFPubKey\": \"" + publicKey + "\",\n" +
                            "  \"cardno\": \"" + numCard + "\",\n" +
                            "  \"cvv\": \"" + cvv + "\",\n" +
                            "  \"expirymonth\": \"" + mm + "\",\n" +
                            "  \"expiryyear\": \"" + yy + "\",\n" +
                            "  \"currency\": \"NGN\",\n" +
                            "  \"country\": \"NG\",\n" +
                            "  \"amount\": \"" + paymentAmount + "\",\n" +
                            "  \"email\": \"user@gmail.com\",\n" +
                            "  \"phonenumber\": \"0903621878\",\n" +
                            "  \"firstname\": \"Test\",\n" +
                            "  \"lastname\": \"Test2\",\n" +
                            "  \"redirect_url\": \"https://rave-webhook.herokuapp.com/receivepayment\",\n" +
                            "  \"IP\": \"355426087298442\",\n" +
                            "  \"txRef\": \"MC-" + System.currentTimeMillis() + "\"\n" +
                            "}";
                    Log.d("ENCRYPTING", "data needs to be encrypted:\n" + data);
                    String client = encryptData(data, getKey(secretKey));
                    Log.d("ENCRYPTING", "Encrypted string: " + client);
                    Call<JsonElement> call = service.makeCharge(publicKey, client, "3DES-24");
                    call.enqueue(new Callback<JsonElement>() {
                        @Override
                        public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                            Log.d("ENCRYPTING", "first response: " + response.body() + " : " + response.code());
                            if (response.errorBody() != null){
                                try {
                                    Log.d("ENCRYPTING", "Response error: " + response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (response.body() != null && response.code() == 200) {
                                JsonObject jsonObject = response.body().getAsJsonObject();
                                if (jsonObject.get("message").getAsString().equals("AUTH_SUGGESTION")) {
                                    isConfirmationNeeded = true;
                                    confirmationType = jsonObject.getAsJsonObject("data").get("suggested_auth").getAsString();
                                    if (confirmationType.equals("PIN")) {
                                        otp.setVisibility(View.VISIBLE);
                                    } else {
                                        message.setText("Confirm your location to authenticate your card");
                                    }
                                    confirm.setClickable(true);
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "Error. Please check your network connection or card details.", Toast.LENGTH_SHORT).show();
                                } else if (jsonObject.get("message").getAsString().equals("V-COMP")) {
                                    jsonObject = response.body().getAsJsonObject();
                                    transactionReference = jsonObject.getAsJsonObject("data")
                                            .get("flwRef").getAsString();
                                    progressBar.setVisibility(View.GONE);
                                    otp.setText("");
                                    otp.setHint("Enter your OTP");
                                    isChargeMade = true;
                                    Toast.makeText(getContext(), "Card successfully authenticate", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                confirm.setClickable(true);
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "Error. Please check your network connection or card details.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonElement> call, Throwable t) {
                            confirm.setClickable(true);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Error. Please check your network connection or card details.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Log.d("ENCRYPTING", "Execute second request");
                if (otp.getText().length() > 0){
                    Call<JsonElement> call = service.verifyCharge(
                            getResources().getString(R.string.rave_payment_public_key),
                            transactionReference, otp.getText().toString());
                    call.enqueue(new Callback<JsonElement>() {
                        @Override
                        public void onResponse(Call<JsonElement> call, retrofit2.Response<JsonElement> response) {
                            Log.d("ENCRYPTING", "Successfully charged.\n" + response.body());
                            if (response.errorBody() != null){
                                try {
                                    Log.d("ENCRYPTING", "Response error: " + response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            confirm.setClickable(true);
                            if (response.body() != null) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "Successfully charged", Toast.LENGTH_SHORT).show();
                                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (imm != null) {
                                    imm.hideSoftInputFromWindow(getDialog().getCurrentFocus().getWindowToken(), 0);
                                }
                                dismiss();
                            }else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "Error. Please check your network connection or card details.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonElement> call, Throwable t) {
                            confirm.setClickable(true);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Error. Please check your network connection or card details.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    confirm.setClickable(true);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "OTP must not be empty", Toast.LENGTH_SHORT).show();
                }
            }

            if (isConfirmationNeeded){
                switch (confirmationType) {
                    case "PIN":
                        //Authentication with pin
                        Log.d("ENCRYPTING", "Execute third request");
                        if (otp.getText().length() > 0) {
                            String data = "{\n" +
                                    "  \"PBFPubKey\": \"" + publicKey + "\",\n" +
                                    "  \"cardno\": \"" + numCard + "\",\n" +
                                    "  \"cvv\": \"" + cvv + "\",\n" +
                                    "  \"expirymonth\": \"" + mm + "\",\n" +
                                    "  \"expiryyear\": \"" + yy + "\",\n" +
                                    "  \"currency\": \"NGN\",\n" +
                                    "  \"pin\": \"" + otp.getText().toString() + "\",\n" +
                                    "  \"suggested_auth\": \"PIN\",\n" +
                                    "  \"country\": \"NG\",\n" +
                                    "  \"amount\": \"" + paymentAmount + "\",\n" +
                                    "  \"email\": \"user@gmail.com\",\n" +
                                    "  \"phonenumber\": \"0903621878\",\n" +
                                    "  \"firstname\": \"Test\",\n" +
                                    "  \"lastname\": \"Test2\",\n" +
                                    "  \"redirect_url\": \"https://rave-webhook.herokuapp.com/receivepayment\",\n" +
                                    "  \"IP\": \"355426087298442\",\n" +
                                    "  \"txRef\": \"MC-" + System.currentTimeMillis() + "\"\n" +
                                    "}";
                            Log.d("ENCRYPTING", "data needs to be encrypted:\n" + data);
                            String client = encryptData(data, getKey(secretKey));
                            Log.d("ENCRYPTING", "Encrypted string: " + client);
                            Call<JsonElement> call = service.makeCharge(publicKey, client, "3DES-24");
                            call.enqueue(new Callback<JsonElement>() {
                                @Override
                                public void onResponse(Call<JsonElement> call, retrofit2.Response<JsonElement> response) {
                                    Log.d("ENCRYPTING", "Request url: " + call.request().toString() + "Response: " + response.body() + ", response code: " + response.code());
                                    if (response.errorBody() != null){
                                        try {
                                            Log.d("ENCRYPTING", "Response error: " + response.errorBody().string());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    confirm.setClickable(true);
                                    if (response.body() != null) {
                                        JsonObject jsonObject = response.body().getAsJsonObject();
                                        transactionReference = jsonObject.getAsJsonObject("data")
                                                .get("flwRef").getAsString();
                                        progressBar.setVisibility(View.GONE);
                                        otp.setText("");
                                        otp.setHint("Enter your OTP");
                                        isChargeMade = true;
                                        Toast.makeText(getContext(), "Card successfully authenticate", Toast.LENGTH_SHORT).show();
                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(getContext(), "Error. Please check your network connection or card details.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<JsonElement> call, Throwable t) {
                                    confirm.setClickable(true);
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "Error. Please check your network connection or card details.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else {
                            confirm.setClickable(true);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "PIN must not be empty", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "NOAUTH_INTERNATIONAL":
                        Log.d("ENCRYPTING", "Execute fourth request");
                        lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
                            return;
                        }else {
                            requestLocation();
//                            makeChargeWithZip(publicKey, numCard, cvv, mm, yy, paymentAmount, secretKey);
                        }
                        otp.setVisibility(View.VISIBLE);

                        break;
                }
            }
        });
        adb.setView(view);

        Dialog dialog = adb.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length ==2
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED){
            requestLocation();
//            makeChargeWithZip(publicKey, numCard, cvv, mm, yy, paymentAmount, secretKey);
        }else {
            Toast.makeText(getContext(), "Cannot get current location", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestLocation(){
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        makeChargeWithZip(publicKey, numCard, cvv, mm, yy, paymentAmount, secretKey);
                        Log.d("ENCRYPTING", "Current location is: " + myLocation.latitude + " : " + myLocation.longitude);
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
                });
    }

    private void makeChargeWithZip(String publicKey, String numCard, String cvv, String mm, String yy, String paymentAmount, String secretKey){
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            Address address = geocoder.getFromLocation(myLocation.latitude, myLocation.longitude, 1).get(0);
            String data = "{\n" +
                    "  \"PBFPubKey\": \"" + publicKey + "\",\n" +
                    "  \"cardno\": \"" + numCard + "\",\n" +
                    "  \"cvv\": \"" + cvv + "\",\n" +
                    "  \"expirymonth\": \"" + mm + "\",\n" +
                    "  \"expiryyear\": \"" + yy + "\",\n" +
                    "  \"currency\": \"NGN\",\n" +
                    "  \"suggested_auth\": \"NOAUTH_INTERNATIONAL\",\n" +
                    "  \"billingzip\": \"" + address.getPostalCode() + "\",\n" +
                    "  \"billingcity\": \"" + address.getLocality() + "\",\n" +
                    "  \"billingaddress\": \"" + address.getAddressLine(0) + "\",\n" +
                    "  \"billingstate\": \"" + address.getAdminArea() + "\",\n" +
                    "  \"billingcountry\": \"" + address.getCountryCode() + "\",\n" +
                    "  \"country\": \"NG\",\n" +
                    "  \"amount\": \"" + paymentAmount + "\",\n" +
                    "  \"email\": \"user@gmail.com\",\n" +
                    "  \"phonenumber\": \"0903621878\",\n" +
                    "  \"firstname\": \"Test\",\n" +
                    "  \"lastname\": \"Test2\",\n" +
                    "  \"redirect_url\": \"https://rave-webhook.herokuapp.com/receivepayment\",\n" +
                    "  \"IP\": \"355426087298442\",\n" +
                    "  \"txRef\": \"MC-" + System.currentTimeMillis() + "\"\n" +
                    "}";
            Log.d("ENCRYPTING", "data needs to be encrypted:\n" + data);
            String client = encryptData(data, getKey(secretKey));
            Log.d("ENCRYPTING", "Encrypted string: " + client);
            Call<JsonElement> call = service.makeCharge(publicKey, client, "3DES-24");
            call.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, retrofit2.Response<JsonElement> response) {
                    Log.d("ENCRYPTING", "Request url: " + call.request().toString() + "Response: " + response.body() + ", response code: " + response.code());
                    if (response.errorBody() != null){
                        try {
                            Log.d("ENCRYPTING", "Response error: " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    confirm.setClickable(true);
                    if (response.body() != null) {
                        JsonObject jsonObject = response.body().getAsJsonObject();
                        transactionReference = jsonObject.getAsJsonObject("data")
                                .get("flwRef").getAsString();
                        progressBar.setVisibility(View.GONE);
                        otp.setText("");
                        otp.setHint("Enter your OTP");
                        isChargeMade = true;
                        Toast.makeText(getContext(), "Card successfully authenticate", Toast.LENGTH_SHORT).show();
                    }else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error. Please check your network connection or card details.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    confirm.setClickable(true);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error. Please check your network connection or card details.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            confirm.setClickable(true);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error. Please check your network connection or card details.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    public static String toHexStr(byte[] bytes) {

        StringBuilder builder = new StringBuilder();

        for (byte aByte : bytes) {
            builder.append(String.format("%02x", aByte));
        }

        return builder.toString();
    }

    // this is the getKey function that generates an encryption Key for you by passing your Secret Key as a parameter.

    public static String getKey(String seedKey) {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] hashedString = md.digest(seedKey.getBytes("utf-8"));
            byte[] subHashString = toHexStr(Arrays.copyOfRange(hashedString, hashedString.length - 12, hashedString.length)).getBytes("utf-8");
            String subSeedKey = seedKey.replace("FLWSECK-", "");
            subSeedKey = subSeedKey.substring(0, 12);
            byte[] combineArray = new byte[24];
            System.arraycopy(subSeedKey.getBytes(), 0, combineArray, 0, 12);
            System.arraycopy(subHashString, subHashString.length - 12, combineArray, 12, 12);
            return new String(combineArray);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // This is the encryption function that encrypts your payload by passing the stringified format and your encryption Key.

    public static String encryptData(String message, String _encryptionKey) {
        try {
            final byte[] digestOfPassword = _encryptionKey.getBytes("utf-8");
            final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);

            final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
            @SuppressLint("GetInstance")
            final Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            final byte[] plainTextBytes = message.getBytes("utf-8");
            final byte[] cipherText = cipher.doFinal(plainTextBytes);
            return Base64.encodeToString(cipherText, Base64.DEFAULT);

        } catch (Exception e) {

            e.printStackTrace();
            return "";
        }
    }
}
