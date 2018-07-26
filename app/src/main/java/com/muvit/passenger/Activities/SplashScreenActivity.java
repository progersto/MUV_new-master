package com.muvit.passenger.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.muvit.passenger.AsyncTask.GetAsyncTask;
import com.muvit.passenger.AsyncTask.OnAsyncResult;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Models.CommonPOJO;
import com.muvit.passenger.Models.DataAnsItem;
import com.muvit.passenger.Models.LoginPOJO;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class SplashScreenActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_ALL_PERMISSION = 200;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 201;
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 202;
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 203;
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_CAMERA = 204;
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;
    boolean cameraAccepted = false, locationAccepted = false, storageAccepted = false;

    private Uri.Builder builder;
    private GetAsyncTask getAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (checkOS()) {

            if (ContextCompat.checkSelfPermission(SplashScreenActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(SplashScreenActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(SplashScreenActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SplashScreenActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_ALL_PERMISSION);
            } else {
                if (ContextCompat.checkSelfPermission(SplashScreenActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(SplashScreenActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(SplashScreenActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(SplashScreenActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(SplashScreenActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    } else {
                        storageAccepted = true;
                    }
                    if (ContextCompat.checkSelfPermission(SplashScreenActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(SplashScreenActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    } else {
                        locationAccepted = true;
                    }

                    if (ContextCompat.checkSelfPermission(SplashScreenActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(SplashScreenActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_ACCESS_CAMERA);
                    } else {
                        cameraAccepted = true;
                    }
                } else {
                    getOfflineData();
                    manageRedirection();
                }
            }
        } else {
            getOfflineData();
            manageRedirection();
        }

    }


    private boolean checkOS() {

        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ALL_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                try {
                    Log.e("MainActivity", "Permissions :" + grantResults.length);
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean locationAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;


                    if (!storageAccepted || !cameraAccepted || !locationAccepted) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
                        //builder.setTitle("Error");
                        builder.setMessage("You have denied one or more permissions please allow all the permission and try again");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                        builder.show();
                    } else {
                        getOfflineData();
                        manageRedirection();
                    }
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    aiobe.printStackTrace();
                }
            }
            break;

            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                try {
                    Log.e("MainActivity", "Permissions :" + grantResults.length);
                    boolean storageAcceptedLocal = grantResults[0] == PackageManager.PERMISSION_GRANTED;


                    if (!storageAcceptedLocal) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
                        //builder.setTitle("Error");
                        builder.setMessage("You have denied one or more permissions please allow all the permission and try again");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                        //builder.setNegativeButton("Cancel", null);
                        builder.show();
                    } else {
                        storageAccepted = true;
                        checkIfAcceptedAll();

                    }
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    aiobe.printStackTrace();
                }
            }
            break;

            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                try {
                    Log.e("MainActivity", "Permissions :" + grantResults.length);
                    boolean cameraAcceptedLocal = grantResults[0] == PackageManager.PERMISSION_GRANTED;


                    if (!cameraAcceptedLocal) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
                        //builder.setTitle("Error");
                        builder.setMessage("You have denied one or more permissions please allow all the permission and try again");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                        //builder.setNegativeButton("Cancel", null);
                        builder.show();
                    } else {
                        locationAccepted = true;
                        checkIfAcceptedAll();
                    }
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    aiobe.printStackTrace();
                }
            }
            break;
            case MY_PERMISSIONS_REQUEST_ACCESS_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                try {
                    Log.e("MainActivity", "Permissions :" + grantResults.length);
                    boolean cameraAcceptedLocal = grantResults[0] == PackageManager.PERMISSION_GRANTED;


                    if (!cameraAcceptedLocal) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
                        //builder.setTitle("Error");
                        builder.setMessage("You have denied one or more permissions please allow all the permission and try again");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                        //builder.setNegativeButton("Cancel", null);
                        builder.show();
                    } else {
                        cameraAccepted = true;
                        checkIfAcceptedAll();
                    }
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    aiobe.printStackTrace();
                }
            }
            break;
        }
    }

    public void checkIfAcceptedAll() {
        if (cameraAccepted && storageAccepted && locationAccepted) {
            getOfflineData();
            Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }


    public void getOfflineData() {
        //prd.show();
        builder = new Uri.Builder()
                .appendQueryParameter("userId", String.valueOf(PrefsUtil.with(SplashScreenActivity.this).readInt("uId")))
                .appendQueryParameter("userType", "u");

        // Async Result
        OnAsyncResult onAsyncResult = new OnAsyncResult() {
            @Override
            public void OnSuccess(String result) {
                //prd.dismiss();

                try {
                    JSONObject obj = new JSONObject(result);
                    if (obj.getBoolean("status")) {
                        try {
                            FileWriter file = new FileWriter(getFilesDir().getPath() + "/" + "offline.json");
                            file.write(result);
                            file.flush();
                            file.close();
                            Log.e("TAG", "Success");
                        } catch (IOException e) {
                            Log.e("TAG", "Error in Writing: " + e.getLocalizedMessage());
                        }
                    } else {

                    }
                } catch (JSONException e) {
                    Log.e("TAG", "Error");
                    e.printStackTrace();
                }
            }

            @Override
            public void OnFailure(String result) {
                //prd.dismiss();

            }
        };
        getAsyncTask = new GetAsyncTask(WebServiceUrl.ServiceUrl
                + WebServiceUrl.offlineservice, onAsyncResult, builder);
        getAsyncTask.execute();
    }

    public void loginWithCreds(String strEmail, String strPassword) {
        ArrayList<String> params = new ArrayList<>();
        params.add("email");
        params.add("password");
        ArrayList<String> values = new ArrayList<>();
        values.add(strEmail);
        values.add(strPassword);
        new ParseJSON(SplashScreenActivity.this, WebServiceUrl.ServiceUrl
                + WebServiceUrl.userlogin, params, values, LoginPOJO.class,
                new ParseJSON.OnResultListner() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        if (status) {
                            LoginPOJO loginObj = (LoginPOJO) obj;
                            if (loginObj.isStatus()) {
                                    /*intent = new Intent(mContext, HomeActivity.class);
                                    startActivity(intent);*/
                                if (loginObj.getDataAns().get(0).getIsActive().equalsIgnoreCase("y")) {
                                    //Toast.makeText(SplashScreenActivity.this, loginObj.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("Service Success", loginObj.getMessage());
                                    registerDevice(loginObj.getDataAns().get(0));
                                } else {
                                    showActivationDialog(loginObj.getDataAns().get(0));
                                }

                                   /* DataAnsItem loginData = loginObj.getDataAns().get(0);
                                    PrefsUtil.with(LoginActivity.this).write("isLoggedIn",true);
                                    PrefsUtil.with(LoginActivity.this).write("uId",loginData.getUId());
                                    PrefsUtil.with(LoginActivity.this).write("firstName",loginData.getFirstName());
                                    PrefsUtil.with(LoginActivity.this).write("lastName",loginData.getLastName());
                                    PrefsUtil.with(LoginActivity.this).write("profileImage",loginData.getProfileImage());
                                    PrefsUtil.with(LoginActivity.this).write("mobileNo",loginData.getMobileNo());
                                    PrefsUtil.with(LoginActivity.this).write("email",loginData.getEmail());*/
                                //finish();
                            }
                        } else {
                            //Toast.makeText(SplashScreenActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                            Log.e("Service Fail", (String) obj);
                        }
                    }
                }, false);
    }

    private void registerDevice(final DataAnsItem loginData) {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.registerdevice;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        params.add("userType");
        params.add("deviceId");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(loginData.getUId()));
        values.add("u");
        values.add(FirebaseInstanceId.getInstance().getToken());
        new ParseJSON(SplashScreenActivity.this, url, params, values, CommonPOJO.class,
                new ParseJSON.OnResultListner() {
                    @Override
                    public void onResult(boolean status, Object obj) {
                        if (status) {
//                            Intent intent = new Intent(SplashScreenActivity.this, HomeActivity.class);
                            Intent intent = new Intent(SplashScreenActivity.this, Step2Activity.class);
                            startActivity(intent);
                            PrefsUtil.with(SplashScreenActivity.this).clearPrefs();
                            PrefsUtil.with(SplashScreenActivity.this).write("isLoggedIn", true);
                            PrefsUtil.with(SplashScreenActivity.this).write("completeProfile", true);
                            PrefsUtil.with(SplashScreenActivity.this).write("uId", loginData.getUId());
                            PrefsUtil.with(SplashScreenActivity.this).write("firstName", loginData.getFirstName());
                            PrefsUtil.with(SplashScreenActivity.this).write("lastName", loginData.getLastName());
                            PrefsUtil.with(SplashScreenActivity.this).write("profileImage", WebServiceUrl.profileUrl + loginData.getUId() + "/" + loginData.getProfileImage());
                            PrefsUtil.with(SplashScreenActivity.this).write("mobileNo", loginData.getMobileNo());
                            PrefsUtil.with(SplashScreenActivity.this).write("email", loginData.getEmail());
                            PrefsUtil.with(SplashScreenActivity.this).write("paypalEmail", loginData.getPaypalEmail());
                            PrefsUtil.with(SplashScreenActivity.this).write("password", loginData.getPassword());
                            finish();
                        } else {
                            //Toast.makeText(SplashScreenActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                            Log.e("Service Fail", (String) obj);
                        }


                    }
                }, false);
    }

    private void showActivationDialog(final DataAnsItem loginData) {
        final Dialog dialog = new Dialog(SplashScreenActivity.this);
        try {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog.setContentView(R.layout.dialog_resend_email);
        dialog.setCancelable(false);

        TextView txtClose = (TextView) dialog.findViewById(R.id.txtClose);
        TextView txtResend = (TextView) dialog.findViewById(R.id.txtResend);
        //final EditText edtEmail1 = (EditText) dialog.findViewById(R.id.edtEmail1);

        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        txtResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                resendMail(String.valueOf(loginData.getUId()), dialog);

            }
        });

        // now that the dialog is set up, it's time to show it
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void resendMail(String uId, final Dialog dialog) {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.resendAuthenticationMail;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        ArrayList<String> values = new ArrayList<>();
        values.add(uId);
        new ParseJSON(SplashScreenActivity.this, url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    CommonPOJO resultObj = (CommonPOJO) obj;
                    //Toast.makeText(SplashScreenActivity.this, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Service Success", resultObj.getMessage());
                    try {
                        dialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //Toast.makeText(SplashScreenActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                    Log.e("Service Fail", (String) obj);
                }
            }
        }, false);
    }

    private void manageRedirection() {
        if (PrefsUtil.with(SplashScreenActivity.this).readBoolean("isLoggedIn")) {
            if (new ConnectionCheck().isNetworkConnected(SplashScreenActivity.this)) {
                Log.e("HomeActivity", "connected");
                loginWithCreds(PrefsUtil.with(SplashScreenActivity.this).readString("email"), PrefsUtil.with(SplashScreenActivity.this).readString("password"));
            } else {
                if (PrefsUtil.with(SplashScreenActivity.this).readBoolean("completeProfile")) {
//                    Intent intent = new Intent(SplashScreenActivity.this, HomeActivity.class);
                    Intent intent = new Intent(SplashScreenActivity.this, Step2Activity.class);
                    startActivity(intent);
                    finish();
                }
            }
        } else {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 1800);
        }
    }
}
