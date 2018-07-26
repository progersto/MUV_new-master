package com.muvit.passenger.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Models.ContactUsPOJO;
import com.muvit.passenger.Models.CountryItem;
import com.muvit.passenger.Models.CountryPOJO;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.KeyboardUtils;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;

import java.util.ArrayList;

public class HelpFragment extends Fragment {

    EditText edtFirstName,edtLastName,edtEmail,edtMobileNo,edtQuery;
    Spinner spinnerCountryCode;
    ArrayList<CountryItem> arrCountries = new ArrayList<>();
    private ArrayAdapter adapterCode;
    private Button btnSend;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_help, container, false);
        intiView(rootView);
        getCountryCode();

        try {
            edtFirstName.setText(PrefsUtil.with(getActivity()).readString("firstName"));
            edtLastName.setText(PrefsUtil.with(getActivity()).readString("lastName"));
            edtEmail.setText(PrefsUtil.with(getActivity()).readString("email"));
            edtMobileNo.setText(PrefsUtil.with(getActivity()).readString("mobileNo"));
        }catch (Exception e){
            e.printStackTrace();
        }

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactUs();
            }
        });
        return rootView;
    }

    private void intiView(View rootView) {
        edtFirstName = (EditText) rootView.findViewById(R.id.edtFirstName);
        edtLastName = (EditText) rootView.findViewById(R.id.edtLastName);
        edtEmail = (EditText) rootView.findViewById(R.id.edtEmail);
        edtMobileNo = (EditText) rootView.findViewById(R.id.edtMobileNo);
        edtQuery = (EditText) rootView.findViewById(R.id.edtQuery);
        btnSend = (Button) rootView.findViewById(R.id.btnSend);
        spinnerCountryCode = (Spinner) rootView.findViewById(R.id.spinnerCountryCode);
        adapterCode = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, arrCountries);
        adapterCode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountryCode.setAdapter(adapterCode);
        new KeyboardUtils().setupUI(rootView.findViewById(R.id.content),getActivity());
    }

    public boolean validateViews(){
        boolean result = true;
        if (edtFirstName.getText().toString().isEmpty()) {
            edtFirstName.requestFocus();
            edtFirstName.setError("Please enter first name");
            result = false;
        }
        if (edtLastName.getText().toString().isEmpty()) {
            edtLastName.requestFocus();
            edtLastName.setError("Please enter last name");
            result = false;
        }
        if (edtEmail.getText().toString().isEmpty()) {
            edtEmail.requestFocus();
            edtEmail.setError("Please enter email");
            result = false;
        }
        if (edtMobileNo.getText().toString().isEmpty()) {
            edtMobileNo.requestFocus();
            edtMobileNo.setError("Please enter mobile number");
            result = false;
        }
        if (edtQuery.getText().toString().isEmpty()) {
            edtQuery.requestFocus();
            edtQuery.setError("Please enter your query");
            result = false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(edtEmail.getText().toString()).matches() && result) {
            edtEmail.setError("Enter Valid E-mail");
            edtEmail.requestFocus();
            result = false;
        }
        return result;
    }

    public void contactUs(){
        String id = "0";
        try {
            id = String.valueOf(arrCountries.get(spinnerCountryCode.getSelectedItemPosition()).getId());
            if (validateViews()) {
                String url= WebServiceUrl.ServiceUrl+WebServiceUrl.contactus;
                ArrayList<String> params = new ArrayList<>();
                params.add("firstName");
                params.add("lastName");
                params.add("emailAddress");
                params.add("countryCode");
                params.add("mobileNo");
                params.add("message");
                ArrayList<String> values = new ArrayList<>();
                values.add(edtFirstName.getText().toString());
                values.add(edtLastName.getText().toString());
                values.add(edtEmail.getText().toString());
                values.add(id);
                values.add(edtMobileNo.getText().toString());
                values.add(edtQuery.getText().toString());
                new ParseJSON(getActivity(), url, params, values, ContactUsPOJO.class, new ParseJSON.OnResultListner() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        if (status) {
                            ContactUsPOJO resultObj = (ContactUsPOJO) obj;
                            Toast.makeText(getActivity(), resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (Exception e){
            new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_launcher)
                    .setTitle("Error!")
                    .setCancelable(false)
                    .setMessage("There is something wrong while fetching your data. Please try again")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
            e.printStackTrace();
        }

    }

    public void getCountryCode(){
        ArrayList<String> params = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        new ParseJSON(getActivity(), WebServiceUrl.ServiceUrl+WebServiceUrl.getcountrycode, params, values, CountryPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                CountryPOJO countryObj = (CountryPOJO) obj;
                if (countryObj.isStatus()) {
                    arrCountries.addAll(countryObj.getCountry());
                    adapterCode.notifyDataSetChanged();
                }

                }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
