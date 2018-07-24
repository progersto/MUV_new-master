package com.muvit.passenger.Fragments;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VerifyFragment extends DialogFragment {

    public static final String CARDNO = "cardno";
    public static final String EXPIRY_MM = "expiry mm";
    public static final String EXPIRY_YY = "expiry yy";
    public static final String CVV = "cvv";
    public static final String PAYMENT_AMOUNT = "payment amount";

    private boolean isChargeMade = false;
    private String transactionReference;


    public VerifyFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.fragment_verify, null);
        AlertDialog.Builder adb = new AlertDialog.Builder(getContext());

        String baseUrl = "https://api.ravepay.co/";
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        RaveApiRequests service = retrofit.create(RaveApiRequests.class);

        String secretKey = getResources().getString(R.string.rave_payment_secret_key);
        String publicKey = getResources().getString(R.string.rave_payment_public_key);

        assert getArguments() != null;
        String numCard = getArguments().getString(CARDNO).replace(" ", "");
        String cvv = getArguments().getString(CVV);
        String mm = getArguments().getString(EXPIRY_MM);
        String yy = getArguments().getString(EXPIRY_YY);
        String paymentAmount = getArguments().getString(PAYMENT_AMOUNT);

        EditText otp = view.findViewById(R.id.otp_text);
        Button confirm = view.findViewById(R.id.send_otp);
        ProgressBar progressBar = view.findViewById(R.id.otp_progress_bar);

        confirm.setOnClickListener(v -> {
            confirm.setClickable(false);
            progressBar.setVisibility(View.VISIBLE);
            if (isChargeMade){
                if (otp.getText().length() > 0){
                    Call<JsonElement> call = service.verifyCharge(
                            getResources().getString(R.string.rave_payment_public_key),
                            transactionReference, otp.getText().toString());
                    call.enqueue(new Callback<JsonElement>() {
                        @Override
                        public void onResponse(Call<JsonElement> call, retrofit2.Response<JsonElement> response) {
                            Log.d("ENCRYPTING", "Successfully charged.\n" + response.body());
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
            }else {
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
                }else {
                    confirm.setClickable(true);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "PIN must not be empty", Toast.LENGTH_SHORT).show();
                }
            }

        });
        adb.setView(view);


        Dialog dialog = adb.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
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
