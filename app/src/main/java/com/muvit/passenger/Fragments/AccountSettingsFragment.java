package com.muvit.passenger.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.muvit.passenger.Activities.Step2Activity;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Models.AccountSettingsItem;
import com.muvit.passenger.Models.AccountSettingsPOJO;
import com.muvit.passenger.Models.CommonPOJO;
import com.muvit.passenger.Models.CountryItem;
import com.muvit.passenger.Models.CountryPOJO;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.KeyboardUtils;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountSettingsFragment extends Fragment {

    EditText edtOldPassword, edtNewPassword, edtConfPassword/*, edtPayPalEmail*/,edtPanicContact;
    Button btnChangePassword/*, btnChangePaypalEmail*/, btnSaveSettings, btnChangeContactNo;
    AppCompatCheckBox chkBuyerNotif1, chkBuyerNotif2, chkBuyerNotif3, chkBuyerNotif4;
    LinearLayout accountSettingsLayout;

    private Spinner spinnerCountryCode;
    private ArrayAdapter adapterCode;
    ArrayList<CountryItem> arrCountries = new ArrayList<>();

    public AccountSettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_account_settings, container, false);
        initView(rootView);
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
        /*btnChangePaypalEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePaypalEmail();
            }
        });*/
        btnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {changeNotificationSettings();
            }
        });
        btnChangeContactNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePanicNumber();
            }
        });
        getAccountSettings();
        return rootView;
    }

    private void initView(View rootView) {
        edtOldPassword = (EditText) rootView.findViewById(R.id.edtOldPassword);
        edtNewPassword = (EditText) rootView.findViewById(R.id.edtNewPassword);
        edtConfPassword = (EditText) rootView.findViewById(R.id.edtConfPassword);
        //edtPayPalEmail = (EditText) rootView.findViewById(R.id.edtPayPalEmail);
        edtPanicContact = (EditText) rootView.findViewById(R.id.edtPanicContact);

        btnChangePassword = (Button) rootView.findViewById(R.id.btnChangePassword);
        //btnChangePaypalEmail = (Button) rootView.findViewById(R.id.btnChangePaypalEmail);
        btnSaveSettings = (Button) rootView.findViewById(R.id.btnSaveSettings);
        btnChangeContactNo = (Button) rootView.findViewById(R.id.btnChangeContactNo);

        chkBuyerNotif1 = (AppCompatCheckBox) rootView.findViewById(R.id.chkBuyerNotif1);
        chkBuyerNotif2 = (AppCompatCheckBox) rootView.findViewById(R.id.chkBuyerNotif2);
        chkBuyerNotif3 = (AppCompatCheckBox) rootView.findViewById(R.id.chkBuyerNotif3);
        chkBuyerNotif4 = (AppCompatCheckBox) rootView.findViewById(R.id.chkBuyerNotif4);

        spinnerCountryCode = (Spinner) rootView.findViewById(R.id.spinnerCountryCode);


        new KeyboardUtils().setupUI(rootView.findViewById(R.id.account_settings_layout),getActivity());

        adapterCode = new ArrayAdapter<>(getActivity(),
                R.layout.custom_yellow_spinner, arrCountries);
        adapterCode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountryCode.setAdapter(adapterCode);
        getCountryCode();
    }

    private boolean validateChangePassword() {
        boolean result = true;
        if (edtOldPassword.getText().toString().length() == 0) {
            edtOldPassword.setError("Please enter old password");
            edtOldPassword.requestFocus();
            result = false;
        }
        if (edtNewPassword.getText().toString().length() == 0) {
            edtNewPassword.setError("Please enter new password");
            edtNewPassword.requestFocus();
            result = false;
        }
        if (edtConfPassword.getText().toString().length() == 0) {
            edtConfPassword.setError("Please enter confirm password");
            edtConfPassword.requestFocus();
            result = false;
        }
        if (result) {
            if (!edtNewPassword.getText().toString().equalsIgnoreCase(edtConfPassword.getText().toString())) {
                edtConfPassword.setError("Password and Confirm Password must be equal");
                result = false;
            }
        }
        return result;
    }

    /*private boolean validateChangePaypal() {
        boolean result = true;
        if (edtPayPalEmail.getText().toString().length() == 0) {
            edtPayPalEmail.setError("Please enter paypal email");
            edtPayPalEmail.requestFocus();
            result = false;
        }

        if (result) {
            if (!Patterns.EMAIL_ADDRESS.matcher(edtPayPalEmail.getText().toString()).matches()) {
                edtPayPalEmail.setError("Enter Valid E-mail");
                edtPayPalEmail.requestFocus();
                result = false;
            }
        }
        return result;
    }*/

    private boolean validatePanicContact() {
        boolean result = true;
        if (edtPanicContact.getText().toString().length() == 0) {
            edtPanicContact.setError("Please Enter Panic Contact Number");
            edtPanicContact.requestFocus();
            result = false;
        }

        return result;
    }

    private void changePassword() {
        if (validateChangePassword()) {
            String url = WebServiceUrl.ServiceUrl + WebServiceUrl.changepassword;
            ArrayList<String> params = new ArrayList<>();
            params.add("userId");
            params.add("userType");
            params.add("oldPassword");
            params.add("newPassword");
            ArrayList<String> values = new ArrayList<>();
            values.add(String.valueOf(PrefsUtil.with(getActivity()).readInt("uId")));
            values.add("u");
            values.add(edtOldPassword.getText().toString());
            values.add(edtNewPassword.getText().toString());
            new ParseJSON(getActivity(), url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
                @Override
                public void onResult(boolean status, Object obj) {
                    if (status) {
                        CommonPOJO resultObj = (CommonPOJO) obj;
                        Toast.makeText(getActivity(), resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /*private void changePaypalEmail() {
        if (validateChangePaypal()) {
            String url = WebServiceUrl.ServiceUrl + WebServiceUrl.editpaypalemail;
            ArrayList<String> params = new ArrayList<>();
            params.add("userId");
            params.add("email");
            ArrayList<String> values = new ArrayList<>();
            values.add(String.valueOf(PrefsUtil.with(getActivity()).readInt("uId")));
            values.add(edtPayPalEmail.getText().toString());
            new ParseJSON(getActivity(), url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
                @Override
                public void onResult(boolean status, Object obj) {
                    if (status) {
                        CommonPOJO resultObj = (CommonPOJO) obj;
                        Toast.makeText(getActivity(), resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                        PrefsUtil.with(getActivity()).write("paypalEmail", edtPayPalEmail.getText().toString());
                    } else {
                        Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }*/

    private void changeNotificationSettings() {
        String ride_cancel,ride_complete,deposit_fund,redeem_request_accept_or_reject;
        if (chkBuyerNotif1.isChecked()){
            ride_cancel="y";
        }else {
            ride_cancel="n";
        }
        if (chkBuyerNotif2.isChecked()){
            ride_complete="y";
        }else {
            ride_complete="n";
        }
        if (chkBuyerNotif3.isChecked()){
            deposit_fund="y";
        }else {
            deposit_fund="n";
        }
        if (chkBuyerNotif4.isChecked()){
            redeem_request_accept_or_reject="y";
        }else {
            redeem_request_accept_or_reject="n";
        }
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.updateaccountsetting;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        params.add("ride_cancel");
        params.add("ride_complete");
        params.add("deposit_fund");
        params.add("redeem_request_accept_or_reject");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(getActivity()).readInt("uId")));
        values.add(ride_cancel);
        values.add(ride_complete);
        values.add(deposit_fund);
        values.add(redeem_request_accept_or_reject);
        new ParseJSON(getActivity(), url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    CommonPOJO resultObj = (CommonPOJO) obj;
                    Toast.makeText(getActivity(), resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void getAccountSettings() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.getaccountsetting;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(getActivity()).readInt("uId")));
        new ParseJSON(getActivity(), url, params, values, AccountSettingsPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    AccountSettingsPOJO resultObj = (AccountSettingsPOJO) obj;
                    try {
                        //edtPayPalEmail.setText(resultObj.getUserSettings().get(0).getPaypalEmail());
                        PrefsUtil.with(getActivity()).write("paypalEmail", resultObj.getUserSettings().get(0).getPaypalEmail());
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    try {
                        edtPanicContact.setText(resultObj.getUserSettings().get(0).getPanicNumber());
                    }catch (Exception e){
                        e.printStackTrace();
                    }


                    ArrayList<AccountSettingsItem> accountSettings = new ArrayList<>();
                    accountSettings.addAll(resultObj.getUserSettings().get(0).getAccountSettings());
                    if (accountSettings.get(0).getNotifyAns().equalsIgnoreCase("y")) {
                        chkBuyerNotif1.setChecked(true);
                    } else {
                        chkBuyerNotif1.setChecked(false);
                    }
                    if (accountSettings.get(1).getNotifyAns().equalsIgnoreCase("y")) {
                        chkBuyerNotif2.setChecked(true);
                    } else {
                        chkBuyerNotif2.setChecked(false);
                    }
                    if (accountSettings.get(2).getNotifyAns().equalsIgnoreCase("y")) {
                        chkBuyerNotif3.setChecked(true);
                    } else {
                        chkBuyerNotif3.setChecked(false);
                    }
                    if (accountSettings.get(3).getNotifyAns().equalsIgnoreCase("y")) {
                        chkBuyerNotif4.setChecked(true);
                    } else {
                        chkBuyerNotif4.setChecked(false);
                    }
                } else {
                    Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updatePanicNumber() {
        if (validatePanicContact()) {
            String url = WebServiceUrl.ServiceUrl + WebServiceUrl.editpanicnumber;
            ArrayList<String> params = new ArrayList<>();
            params.add("userId");
            params.add("panicCountryCode");
            params.add("panicNumber");
            ArrayList<String> values = new ArrayList<>();
            values.add(String.valueOf(PrefsUtil.with(getActivity()).readInt("uId")));
            values.add(String.valueOf(arrCountries.get(spinnerCountryCode.getSelectedItemPosition()).getId()));
            values.add(edtPanicContact.getText().toString());
            new ParseJSON(getActivity(), url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
                @Override
                public void onResult(boolean status, Object obj) {
                    if (status) {
                        CommonPOJO resultObj = (CommonPOJO) obj;
                        Toast.makeText(getActivity(), resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void getCountryCode(){
        ArrayList<String> params = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        new ParseJSON(getActivity(), WebServiceUrl.ServiceUrl+WebServiceUrl.getcountrycode, params, values, CountryPOJO.class, new ParseJSON.OnResultListner() {
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

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity() instanceof Step2Activity){
            ((Step2Activity)getActivity()).showToolbar();
            ((Step2Activity)getActivity()).showToolbarTiltle("Settings");
            ((Step2Activity)getActivity()).turnActionBarIconWhite();
        }
    }
}
