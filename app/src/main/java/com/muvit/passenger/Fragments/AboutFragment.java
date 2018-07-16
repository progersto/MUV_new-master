package com.muvit.passenger.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.muvit.passenger.Activities.ContactUs;
import com.muvit.passenger.Activities.InformationActivity;
import com.muvit.passenger.Activities.Step2Activity;
import com.muvit.passenger.R;
import com.muvit.passenger.WebServices.WebServiceUrl;


public class AboutFragment extends Fragment {

//    private WebView infoWebView;

    LinearLayout info_tab,policy_tab,terms_tab,contact_us_tab;
    ImageView back_btn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        ((Step2Activity)getActivity()).hideToolbar();
        ((Step2Activity)getActivity()).hideToolbarTiltle();
        intiView(rootView);

//        try {
//            infoWebView.loadUrl(WebServiceUrl.helpUrl);
//        }catch (Exception e){
//            e.printStackTrace();
//        }


        return rootView;
    }

    private void intiView(View rootView) {
//        infoWebView = (WebView) rootView.findViewById(R.id.infoWebView);
        info_tab = rootView.findViewById(R.id.info_tab);
        policy_tab= rootView.findViewById(R.id.policy_tab);
        terms_tab=rootView.findViewById(R.id.terms_tab);
        contact_us_tab=rootView.findViewById(R.id.contact_us_tab);
        back_btn = rootView.findViewById(R.id.back_btn);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.popBackStack();
            }
        });

        info_tab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    startActivity(new Intent(getActivity(), InformationActivity.class));
            }
        });

        policy_tab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(getActivity(), InformationActivity.class));
            }
        });
        terms_tab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(getActivity(), InformationActivity.class));
            }
        });
        contact_us_tab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ContactUs.class));
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();
        ((Step2Activity)getActivity()).hideToolbar();
        ((Step2Activity)getActivity()).hideToolbarTiltle();
    }

}
