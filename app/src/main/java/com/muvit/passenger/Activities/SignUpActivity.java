package com.muvit.passenger.Activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Models.CountryItem;
import com.muvit.passenger.Models.CountryPOJO;
import com.muvit.passenger.Models.SignUpPOJO;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.KeyboardUtils;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;

import java.util.ArrayList;

public class SignUpActivity extends AppCompatActivity {
    private ImageView back_btn;
    ArrayList<CountryItem> arrCountries = new ArrayList<>();
    BroadcastReceiver mMessageReceiver;
    private Toolbar toolbar;
    private TextView txtTitle;
    private EditText edtFirstName, edtLastName, edtEmail, edtMobileNo, edtPassword;
    private String strFirstName, strLastName, strEmail, strMobileNo, strPassword;
    private Button btnSubmit;
    private Intent intent;
    private Context context;
    private AppCompatSpinner spinnerCountryCode;
    private ArrayAdapter adapterCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initViews();

        getCountryCode();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strFirstName = edtFirstName.getText().toString().trim();
                strLastName = edtLastName.getText().toString().trim();
                strEmail = edtEmail.getText().toString().trim();
                strMobileNo = edtMobileNo.getText().toString().trim();
                strPassword = edtPassword.getText().toString().trim();

                Boolean validationResult = formValidation();
                if (validationResult) {
                    signUp();
                }
            }
        });
    }

    private Boolean formValidation() {
        Boolean validationResult = true;
        if (strFirstName.length() == 0) {
            edtFirstName.setError("First Name cannot be empty.");
            edtFirstName.requestFocus();
            validationResult = false;
        } else if (strLastName.length() == 0) {
            edtLastName.setError("Last Name cannot be empty.");
            edtLastName.requestFocus();
            validationResult = false;
        } else if (strEmail.length() == 0) {
            edtEmail.setError("E-mail cannot be empty.");
            edtEmail.requestFocus();
            validationResult = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()) {
            edtEmail.setError("Enter Valid E-mail");
            edtEmail.requestFocus();
            validationResult = false;
        } else if (strMobileNo.length() == 0) {
            edtMobileNo.setError("Mobile No. cannot be empty.");
            edtMobileNo.requestFocus();
            validationResult = false;
        } else if (strPassword.length() == 0) {
            edtPassword.setError("Password cannot be empty.");
            edtPassword.requestFocus();
            validationResult = false;
        }

        if (validationResult) {
            if (edtPassword.getText().length() < 6) {
                edtPassword.setError("Password must be at least 6 characters long");
                validationResult = false;
            }

           /* if (edtMobileNo.getText().length()<10) {
                edtMobileNo.setError("Please enter valid mobile number");
                validationResult = false;
            }*/
        }
        return validationResult;
    }

    private void initViews() {
        //setupToolbar();

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!(new ConnectionCheck().isNetworkConnected(context))) {
                    //new ConnectionCheck().showDialogWithMessage(context, getString(R.string.sync_data_message)).show();
                    Log.e("HomeActivity", "disconnected");
                    if (PrefsUtil.isInternetConnectedShowing) {
                        if (PrefsUtil.dialogInternetConnected != null) {
                            PrefsUtil.dialogInternetConnected.dismiss();
                            PrefsUtil.isInternetConnectedShowing = false;
                        }
                    }
                    final Dialog d = new Dialog(context, android.R.style.Theme_Light_NoTitleBar);
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
                            /*if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {*/
                            ActivityCompat.finishAffinity(SignUpActivity.this);
                            /*} else {
                                finishAffinity();
                            }*/
                            /*finish();
                            System.exit(0);*/
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
        };

        context = SignUpActivity.this;
        edtFirstName = (EditText) findViewById(R.id.edtFirstName);
        edtLastName = (EditText) findViewById(R.id.edtLastName);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtMobileNo = (EditText) findViewById(R.id.edtMobileNo);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        back_btn = (ImageView) findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        spinnerCountryCode = (AppCompatSpinner) findViewById(R.id.spinnerCountryCode);

        adapterCode = new ArrayAdapter<>(context,
                R.layout.custom_yellow_spinner, arrCountries);
        adapterCode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCountryCode.setAdapter(adapterCode);
//        spinnerCountryCode.getBackground().setColorFilter(getResources().getColor(R.color.yellow_light), PorterDuff.Mode.SRC_ATOP);
        new KeyboardUtils().setupUI(findViewById(R.id.activity_sign_up), SignUpActivity.this);
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.signup_title);
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

    public void getCountryCode() {
        ArrayList<String> params = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        new ParseJSON(context, WebServiceUrl.ServiceUrl + WebServiceUrl.getcountrycode, params, values, CountryPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    CountryPOJO countryObj = (CountryPOJO) obj;
                    if (countryObj.isStatus()) {

                        arrCountries.addAll(countryObj.getCountry());
                        adapterCode.notifyDataSetChanged();

                    }
                }
            }
        });
    }

    public void signUp() {
        ArrayList<String> params = new ArrayList<>();
        params.add("firstName");
        params.add("lastName");
        params.add("email");
        params.add("mobileNo");
        params.add("countryCode");
        params.add("password");
        ArrayList<String> values = new ArrayList<>();
        values.add(edtFirstName.getText().toString());
        values.add(edtLastName.getText().toString());
        values.add(edtEmail.getText().toString());
        values.add(edtMobileNo.getText().toString());
        values.add(String.valueOf(arrCountries.get(spinnerCountryCode.getSelectedItemPosition()).getId()));
        values.add(edtPassword.getText().toString());
        new ParseJSON(context, WebServiceUrl.ServiceUrl + WebServiceUrl.userregister, params, values, SignUpPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    SignUpPOJO resultObj = (SignUpPOJO) obj;
                    Log.e("SignUpActivity", "onResult: " + resultObj.getMessage());
                    //Toast.makeText(context, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                    if (resultObj.isStatus()) {
                        finish();
                    }
                } else {
                    Toast.makeText(context, (String) obj, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mMessageReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }


}
