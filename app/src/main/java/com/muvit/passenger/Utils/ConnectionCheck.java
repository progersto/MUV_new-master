package com.muvit.passenger.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;

import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.R;

public class ConnectionCheck {

    public boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }

    public AlertDialog.Builder showconnectiondialog(Context context) {
        /*if(ApplicationController.isOnline) {*/
            final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setIcon(R.drawable.ic_launcher)
                    .setTitle("Error!")
                    .setCancelable(false)
                    .setMessage("No Internet Connection.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

            return builder;
        /*} else {
            return null;
        }*/
    }
}