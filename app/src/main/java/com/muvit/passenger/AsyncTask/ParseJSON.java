package com.muvit.passenger.AsyncTask;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.util.ArrayList;

public class ParseJSON {

    public String url;
    public ArrayList<String> params;
    public ArrayList<String> values;
    public Object model;
    ConnectionCheck cd;
    boolean isInternetAvailable;
    boolean wantProgress = true;
    private GetAsyncTask getAsyncTask;
    private Uri.Builder builder;
    private ProgressDialog prd;
    private Context mContext;
    private OnResultListner onResultListner;

    public ParseJSON() {
    }


    public ParseJSON(Context mContext, String url, ArrayList<String> params, ArrayList<String> values, Object model, OnResultListner onResultListner) {
        this.url = url;
        this.params = params;
        this.values = values;
        this.model = model;
        this.mContext = mContext;
        this.onResultListner = onResultListner;
        this.wantProgress = true;
        cd = new ConnectionCheck();
        isInternetAvailable = cd.isNetworkConnected(mContext);
        if (isInternetAvailable) {
            getData();
        } else {
            try {
                new ConnectionCheck().showconnectiondialog(mContext).show();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public ParseJSON(Context mContext, String url, ArrayList<String> params,
                     ArrayList<String> values, Object model,
                     OnResultListner onResultListner, boolean wantProgress) {
        this.url = url;
        this.params = params;
        this.values = values;
        this.model = model;
        this.mContext = mContext;
        this.onResultListner = onResultListner;
        this.wantProgress = wantProgress;
        cd = new ConnectionCheck();
        isInternetAvailable = cd.isNetworkConnected(mContext);
        if (isInternetAvailable) {
            getData();
        } else {
            try {
                new ConnectionCheck().showconnectiondialog(mContext).show();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void getData() {
        builder = new Uri.Builder();

        //final Object[] resultObj = new Object[1];

        Log.e("URL is : ", url);

        for (int i = 0; i < params.size(); i++) {
            try {
                Log.e(params.get(i), values.get(i));
            } catch (NullPointerException npe) {
                Log.e(params.get(i), "");
            }
            builder.appendQueryParameter(params.get(i), values.get(i));
        }


        //builder.appendQueryParameter("")
        if (wantProgress) {
            try {
                prd = new ProgressDialog(mContext,R.style.DialogTheme);
                prd.setTitle("Loading...");
                prd.setMessage("Please Wait While Loading");
                prd.setCancelable(false);
                prd.show();
            } catch (Exception e) {

            }
        }


        // Async Result
        OnAsyncResult onAsyncResult = new OnAsyncResult() {
            @Override
            public void OnSuccess(String result) {
                try {
                    prd.dismiss();
                } catch (Exception e) {

                }

                try {

                    JSONObject object = new JSONObject(result);
                    if (object.getBoolean("status")) {
                        GsonBuilder gsonBuilder = new GsonBuilder();
                        gsonBuilder.setDateFormat("M/d/yy hh:mm a"); //Format of our JSON dates
                        Gson gson = gsonBuilder.create();
                        onResultListner.onResult(true, gson.fromJson(result, (Class) model));
                    } else {
                        onResultListner.onResult(false, object.getString("message"));
                    }


                    //resultObj[0] = gson.fromJson(result, (Class)model);

                   /* if (post.getStatus()) {
                        Log.e("GSON", "Size : " + post.getData().size());
                    }*/


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void OnFailure(String result) {
                try {
                    prd.dismiss();
                } catch (Exception e) {

                }

                getAsyncTask = new GetAsyncTask(url, this, builder);
                //Toast.makeText(SplashScreenActivity.this,result,Toast.LENGTH_LONG).show();
                /*AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Error");
                builder.setMessage(result);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            prd.show();
                        } catch (Exception e) {

                        }
                        getAsyncTask.execute();
                    }
                });
                onResultListner.onResult(false, "Error");
                builder.setNegativeButton("No", null);
                builder.show();*/

                final Dialog d = new Dialog(mContext, android.R.style.Theme_Light_NoTitleBar);
                d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                d.setContentView(R.layout.dialog_connection_error);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(d.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                d.getWindow().setAttributes(lp);
                TextView txtRetry = (TextView) d.findViewById(R.id.txtRetry);
                TextView txtExit = (TextView) d.findViewById(R.id.txtExit);
                txtRetry.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getAsyncTask.execute();
                        d.dismiss();
                    }
                });
                txtExit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        d.dismiss();
                        System.exit(0);
                    }
                });
                d.setCancelable(true);
                try {
                    d.show();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        };
        getAsyncTask = new GetAsyncTask(url, onAsyncResult, builder);
        getAsyncTask.execute();

    }

    public interface OnResultListner {
        void onResult(boolean status, Object obj);
    }


}
