package com.muvit.passenger.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.muvit.passenger.Adapters.InformationAdapter;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Models.InfoItem;
import com.muvit.passenger.Models.InfoPOJO;
import com.muvit.passenger.R;
import com.muvit.passenger.WebServices.WebServiceUrl;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment {

    private RecyclerView recyclerView;
    private InformationAdapter adapter;
    private ArrayList<InfoItem> arrayList;

    public InfoFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        initView(view);
        getInformationListing();
        return view;
    }

    private void initView(View rootView) {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        arrayList = new ArrayList<>();
        adapter = new InformationAdapter(getActivity(), arrayList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void getInformationListing(){
        String url= WebServiceUrl.ServiceUrl+WebServiceUrl.getcmslist;
        ArrayList<String> params = new ArrayList<>();
                params.add("userType");
                ArrayList<String> values = new ArrayList<>();
                values.add("u");
                new ParseJSON(getActivity(), url, params, values, InfoPOJO.class, new ParseJSON.OnResultListner() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        if (status) {
                            InfoPOJO resultObj = (InfoPOJO) obj;
                            arrayList.addAll(resultObj.getInfo());
                            adapter.notifyDataSetChanged();
                        }else {
                            Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                        }
        
        
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
