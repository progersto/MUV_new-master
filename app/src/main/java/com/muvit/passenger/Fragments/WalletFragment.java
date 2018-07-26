package com.muvit.passenger.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.muvit.passenger.Activities.AddCardActivity;
import com.muvit.passenger.Activities.DepositFundActivity;
import com.muvit.passenger.Activities.RedeemHistoryActivity;
import com.muvit.passenger.Activities.RedeemRequestActivity;
import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.Models.Response;
import com.muvit.passenger.Models.WalletDetailPOJO;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.muvit.passenger.database.AppDatabase;
import com.muvit.passenger.database.Card;
import com.muvit.passenger.database.CardDao;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by nct119 on 24/10/16.
 */

public class WalletFragment extends Fragment {

    private Button btnDepositFund, btnRedeemRequest;
    private Intent intent;
    private TextView txtCurrentBal, txtDepositFunds, txtRedeemRequest,txtRedeemHistory;
    private int WALLET_UPDATE_CODE = 555;
    ImageView back_btn;
    private Button btnAddMoney;
    private EditText rechargeEt;
    private Disposable disposable;
    private CardDao cardDao;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wallet, container, false);
        initViews(rootView);

        btnDepositFund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(getContext(), DepositFundActivity.class);
                startActivityForResult(intent, WALLET_UPDATE_CODE);
            }
        });

        btnRedeemRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(getContext(), RedeemRequestActivity.class);
                startActivity(intent);
            }
        });

        txtRedeemHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), RedeemHistoryActivity.class));
            }
        });

        if (new ConnectionCheck().isNetworkConnected(getActivity())) {
            getWalletDetails();
        } else {
            getOfflineWalletDetails();
        }

        return rootView;
    }


    public void getWalletDetails() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.getuserwalletdetails;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(getActivity()).readInt("uId")));
        new ParseJSON(getActivity(), url, params, values, WalletDetailPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    WalletDetailPOJO resultObj = (WalletDetailPOJO) obj;
                    if (!resultObj.getWallet().get(0).getCurrenctBalance().isEmpty()) {
                        String totalWallet = getString(R.string.currencySign)+resultObj.getWallet().get(0).getCurrenctBalance();
                        PrefsUtil.with(getContext()).write("total_wallet", totalWallet);
                        txtCurrentBal.setText(totalWallet);
                    }
                    if (!resultObj.getWallet().get(0).getDepositFund().isEmpty()) {
                        txtDepositFunds.setText(getString(R.string.currencySign)+resultObj.getWallet().get(0).getDepositFund());
                    }
                    if (!resultObj.getWallet().get(0).getRedeemRequest().isEmpty()) {
                        txtRedeemRequest.setText(getString(R.string.currencySign)+resultObj.getWallet().get(0).getRedeemRequest());
                    }

                } else {
                    Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getOfflineWalletDetails() {
        try {
            File f = new File(getActivity().getFilesDir().getPath() + "/" + "offline.json");
            //check whether file exists
            FileInputStream is = new FileInputStream(f);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String s = new String(buffer);
            Log.d("TAG", "" + s);
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("M/d/yy hh:mm a"); //Format of our JSON dates
            Gson gson = gsonBuilder.create();
            Response item = new Response();
            item = gson.fromJson(s, Response.class);
            WalletDetailPOJO resultObj = (WalletDetailPOJO) item.getOfflineDataModel().get(0).getGetuserwalletdetails().getDataAns();
            if (!resultObj.getWallet().get(0).getCurrenctBalance().isEmpty()) {
                txtCurrentBal.setText(getString(R.string.currencySign)+resultObj.getWallet().get(0).getCurrenctBalance());
            }
            if (!resultObj.getWallet().get(0).getDepositFund().isEmpty()) {
                txtDepositFunds.setText(getString(R.string.currencySign)+resultObj.getWallet().get(0).getDepositFund());
            }
            if (!resultObj.getWallet().get(0).getRedeemRequest().isEmpty()) {
                txtRedeemRequest.setText(getString(R.string.currencySign)+resultObj.getWallet().get(0).getRedeemRequest());
            }
        }catch(Exception e){
            Log.e("TAG", "Error in Reading: ",e);
        }
    }

    private void initViews(View rootView) {
        back_btn = (ImageView) rootView.findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.popBackStack();
            }
        });
        btnDepositFund = (Button) rootView.findViewById(R.id.btnDepositFund);
        btnRedeemRequest = (Button) rootView.findViewById(R.id.btnRedeemRequest);
        txtCurrentBal = (TextView) rootView.findViewById(R.id.txtCurrentBal);
        txtDepositFunds = (TextView) rootView.findViewById(R.id.txtDepositFunds);
        txtRedeemRequest = (TextView) rootView.findViewById(R.id.txtRedeemRequest);
        txtRedeemHistory = (TextView) rootView.findViewById(R.id.txtRedeemHistory);
        rechargeEt = rootView.findViewById(R.id.rechargeEt);
        btnAddMoney = rootView.findViewById(R.id.btnAddMoney);
        btnAddMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!rechargeEt.getText().toString().isEmpty()){
                    //запрашиваем карты
                    getListAllCards();
                }else {
                    Toast toast = Toast.makeText(getContext(), "Enter recharge amount!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });
    }

    public void getListAllCards() {
        AppDatabase db = ApplicationController.getInstance().getDatabase();
        cardDao = db.cardDao();
        disposable = cardDao.getListCards()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listAccounts -> {
                    disposable.dispose();
                    checkCard(listAccounts);
                });
    }//getListAllCards


    private void checkCard(List<Card> listCards) {
        if (listCards.size() > 0) {
            // идем в  активность пополнения счета
            Intent intent = new Intent(getContext(), DepositFundActivity.class);
            intent.putExtra("numCard", listCards.get(0).getNumberCard());
            intent.putExtra("mm", listCards.get(0).getMonth());
            intent.putExtra("yy", listCards.get(0).getYear());
            intent.putExtra("cvv", listCards.get(0).getCvv());
            intent.putExtra("amount", rechargeEt.getText().toString());
            startActivity(intent);
        } else {
            // открываем добавление карты в базу
            startActivity(new Intent(getContext(), AddCardActivity.class));
        }//if
    }//checkCard

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == WALLET_UPDATE_CODE && resultCode == Activity.RESULT_OK) {
            Log.e("WalletFragment", "onActivityResult: Got Called!");
            getWalletDetails();
        }
    }


    @Override
    public void onResume() {
        Log.e("onResume", "onResume");
        if (new ConnectionCheck().isNetworkConnected(getActivity())) {
            getWalletDetails();
        } else {
            getOfflineWalletDetails();
        }
        super.onResume();
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection")){
                if (event.getMessage().equalsIgnoreCase("connected")) {
                    getWalletDetails();
                }else if (event.getMessage().equalsIgnoreCase("disconnected")){
                    getOfflineWalletDetails();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
