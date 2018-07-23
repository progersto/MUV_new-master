package com.muvit.passenger.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flutterwave.raveandroid.Meta;
import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Models.CurrentBalancePOJO;
import com.muvit.passenger.Models.DepositFundPOJO;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.KeyboardUtils;
import com.muvit.passenger.Utils.PayPalConfig;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DepositFundActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView txtTitle;
    private TextView txtAvailableBal;
    private EditText edtDepositAmount;
    private RelativeLayout btnPay;
    LinearLayout activity_deposit_fund;
    public static final int PAYPAL_REQUEST_CODE = 123;
    private static PayPalConfiguration config = new PayPalConfiguration()
            // Start with mock environment.  When ready, switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(PayPalConfig.PAYPAL_CLIENT_ID);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        setContentView(R.layout.activity_deposit_fund);
        initViews();
        getBalance();
        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateViews()) {
                    //через PayPal
                  //  getPayment();
                    //через ravepay
                    getPaymentNew();
                }
            }
        });
        String amount = getIntent().getStringExtra("amount");
        if (amount != null){
            edtDepositAmount.setText(amount);
        }
    }

    private void initViews() {
        txtAvailableBal = (TextView) findViewById(R.id.txtAvailableBal);
        edtDepositAmount = (EditText) findViewById(R.id.edtDepositAmount);
        btnPay = (RelativeLayout) findViewById(R.id.btnPay);
        activity_deposit_fund = (LinearLayout) findViewById(R.id.activity_deposit_fund);
        new KeyboardUtils().setupUI(activity_deposit_fund,DepositFundActivity.this);
        setupToolbar();
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.deposit_fund_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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


    public void getBalance() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.getuserbalance;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");

        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(this).readInt("uId")));

        new ParseJSON(this, url, params, values, CurrentBalancePOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    CurrentBalancePOJO resultObj = (CurrentBalancePOJO) obj;
                    txtAvailableBal.setText(resultObj.getBalance().get(0).getCurrenctBalance());
                } else {
                    Toast.makeText(DepositFundActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private boolean validateViews() {
        boolean result = true;
        if (edtDepositAmount.getText().toString().isEmpty()) {
            edtDepositAmount.setError("Please fill deposit amount");
            edtDepositAmount.requestFocus();
            result = false;
        }
        return result;
    }


    private void getPaymentNew(){
        //Getting the amount from editText
        String paymentAmount = edtDepositAmount.getText().toString();

        //Create unique reference id for payment
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmdd_hhmmss");
        String uniqueRef = simpleDateFormat.format(new Date());

        String numCard = getIntent().getStringExtra("numCard");
        String mm = getIntent().getStringExtra("mm");
        String yy = getIntent().getStringExtra("yy");
        String cvv = getIntent().getStringExtra("cvv");
        List<Meta> metaList = new ArrayList<>();
        metaList.add(new Meta("cardNum", numCard));
        metaList.add(new Meta("mm", mm));
        metaList.add(new Meta("yy", yy));
        metaList.add(new Meta("cvv", cvv));

        new RavePayManager(DepositFundActivity.this).setAmount(Double.parseDouble(paymentAmount))
                .setCountry(getString(R.string.rave_payment_country))
                .setCurrency(getString(R.string.rave_payment_currency))
                .setEmail(PrefsUtil.with(DepositFundActivity.this).readString("email"))
                .setPublicKey(getString(R.string.rave_payment_public_key))
                .setSecretKey(getString(R.string.rave_payment_secret_key))
                .setTxRef(uniqueRef)//уникальная ссылка
                .acceptAccountPayments(false)
                .acceptCardPayments(true)
                .onStagingEnv(true)
                .setMeta(metaList)
                // .withTheme(styleId)// измененная тема
                .initialize();
    }


    private void getPayment() {
        //Getting the amount from editText
        String paymentAmount = edtDepositAmount.getText().toString();

        //Creating a paypalpayment
        PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf(paymentAmount)), "USD", "Deposit Amount Fee",
                PayPalPayment.PAYMENT_INTENT_SALE);

        //Creating Paypal Payment activity intent
        Intent intent = new Intent(this, PaymentActivity.class);

        //putting the paypal configuration to the intent
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        //Puting paypal payment to the intent
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        //Starting the intent activity for result
        //the request code will be used on the method onActivityResult
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }


    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If the result is from paypal
        if (requestCode == PAYPAL_REQUEST_CODE) {

            //If the result is OK i.e. user has not canceled the payment
            if (resultCode == Activity.RESULT_OK) {
                //Getting the payment confirmation
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                //if confirmation is not null
                if (confirm != null) {
                    try {
                        //Getting the payment details
                        String paymentDetails = confirm.toJSONObject().toString(4);
                        Log.e("paymentExample", paymentDetails);
                        JSONObject obj = new JSONObject(confirm.toJSONObject().toString());
                        JSONObject responseObj = obj.getJSONObject("response");
                        if (responseObj.getString("state").equalsIgnoreCase("approved")) {
                            depositFund(responseObj.getString("id"));
                        }
                        //Starting a new activity for the payment details and also putting the payment details with intent
                       /* startActivity(new Intent(this, ConfirmationActivity.class)
                                .putExtra("PaymentDetails", paymentDetails)
                                .putExtra("PaymentAmount", paymentAmount));*/

                    } catch (JSONException e) {
                        Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("paymentExample", "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                //if confirmation is not null
                if (message != null) {
                    try {
                        //Getting the payment details
                        JSONObject obj = new JSONObject(message);
                        if (obj.getString("status").equalsIgnoreCase("Transaction successfully fetched")) {
                            JSONObject dataObj = obj.getJSONObject("data");
                            depositFund(dataObj.getString("id"));
                        }
                    } catch (JSONException e) {
                        Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                    Toast toast = Toast.makeText(DepositFundActivity.this, "Payment successful", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                Toast.makeText(this, "ERROR " + message, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Toast.makeText(this, "CANCELLED " + message, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }//onActivityResult

    private void depositFund(String transactionId) {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.depositfund;

        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        params.add("amount");
        params.add("paymentStaus");
        params.add("transactionId");

        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(this).readInt("uId")));
        values.add(edtDepositAmount.getText().toString());
        values.add("c");
        values.add(transactionId);

        new ParseJSON(this, url, params, values, DepositFundPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    DepositFundPOJO resultObj = (DepositFundPOJO) obj;
                    Toast.makeText(DepositFundActivity.this, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(DepositFundActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection")) {
                if (event.getMessage().equalsIgnoreCase("disconnected")) {
                    if (!(new ConnectionCheck().isNetworkConnected(DepositFundActivity.this))) {
                        Log.e("RideDetailActivity", "disconnected");
                        if (!ApplicationController.isOnline) {
                            if (PrefsUtil.isInternetConnectedShowing) {
                                if (PrefsUtil.dialogInternetConnected != null) {
                                    PrefsUtil.dialogInternetConnected.dismiss();
                                    PrefsUtil.isInternetConnectedShowing = false;
                                }
                            }
                            final Dialog d = new Dialog(DepositFundActivity.this,
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
        } catch (Exception e) {
            e.printStackTrace();
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
