package com.muvit.passenger.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.muvit.passenger.R;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Models.CommonPOJO;
import com.muvit.passenger.Models.DataAnsItem;
import com.muvit.passenger.Models.LoginPOJO;
import com.muvit.passenger.Models.SocialLoginItem;
import com.muvit.passenger.Models.SocialLoginPOJO;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.KeyboardUtils;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SignInActivity extends AppCompatActivity  {

//facebook
private static final int FB_SIGN_IN = 9000;
//google plus
private static final int RC_SIGN_IN = 9001;
        BroadcastReceiver mMessageReceiver;
        CallbackManager callbackmanager;
        String deviceToken = "";
private TextView txtForgotPassword;

private RelativeLayout txtSignUp;
private Intent intent;
private Button btnLogin;
private EditText edtEmail, edtPassword;
private RelativeLayout imgFacebook, imgGooglePlus;
private Context mContext;
private String TAG = "SignInActivity";
private PopupWindow popupWindow;
private LinearLayout activity_login;
private GoogleApiClient mGoogleApiClient;
private ImageView back_btn;

@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher("helloworld");
        boolean found = matcher.find();
        Log.e("MainActivity", "found " + found);
        mContext = SignInActivity.this;
        deviceToken = FirebaseInstanceId.getInstance().getToken();
        PrefsUtil.with(mContext).write("deviceToken", FirebaseInstanceId.getInstance().getToken());
        Log.e("MainActivity", "InstanceID token: " + deviceToken);

        initViews();
        generateKeyHash();


        if (PrefsUtil.with(mContext).readBoolean("isLoggedIn")) {
        if (new ConnectionCheck().isNetworkConnected(SignInActivity.this)) {
        Log.e("HomeActivity", "connected");
        loginWithCreds(PrefsUtil.with(mContext).readString("email"), PrefsUtil.with(mContext).readString("password"));
        } else {
        if (PrefsUtil.with(mContext).readBoolean("completeProfile")) {
//        intent = new Intent(mContext, HomeActivity.class);
        intent = new Intent(mContext, Step2Activity.class);
        startActivity(intent);
        finish();
        } else {
        Intent i = new Intent(SignInActivity.this, UserEditProfileActivity.class);
        i.putExtra("from", "signup");
        //startActivity(i);
        //finish();
        }
        }
        }
//


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefsUtil.with(mContext).clearPrefs();
                Boolean validationResult = formValidation();
                if (validationResult) {
                    loginWithCreds(edtEmail.getText().toString().trim(), edtPassword.getText().toString().trim());
                }
            }
        });






//
        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPasswordDialog();
            }
        });
        }

private void initViews() {
        mMessageReceiver = new BroadcastReceiver() {
@Override
public void onReceive(Context context, Intent intent) {
        if (!(new ConnectionCheck().isNetworkConnected(context))) {
        //new ConnectionCheck().showDialogWithMessage(context, getString(R.string.sync_data_message)).show();
        Log.e("HomeActivity", "disconnected");
        if (ApplicationController.isOnline) {
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
        };

//        txtSignUp = (RelativeLayout) findViewById(R.id.txtSignUp);
        txtForgotPassword = (TextView) findViewById(R.id.txtForgotPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        back_btn = (ImageView)findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        finish();
                }
        });
//        imgFacebook = (RelativeLayout) findViewById(R.id.imgFacebook);
//        imgGooglePlus = (RelativeLayout) findViewById(R.id.imgGooglePlus);
        activity_login = (LinearLayout) findViewById(R.id.activity_sign_up);
        KeyboardUtils ku = new KeyboardUtils();
        ku.setupUI(activity_login, SignInActivity.this);
        }

private Boolean formValidation() {
        Boolean validationResult = true;
        if (edtEmail.getText().length() == 0) {
        edtEmail.setError("E-mail cannot be empty.");
        edtEmail.requestFocus();
        validationResult = false;
        } else if (edtPassword.getText().length() == 0) {
        edtPassword.setError("Password cannot be empty.");
        edtPassword.requestFocus();
        validationResult = false;
        }
        return validationResult;
        }

private void generateKeyHash() {
        try {
        PackageInfo info = getPackageManager().getPackageInfo(
        "com.muvit.passenger",
        PackageManager.GET_SIGNATURES);
        for (Signature signature : info.signatures) {
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.update(signature.toByteArray());
        Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
        }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
        }

//Login with Facebook
private void loginWithFacebook() {
        Log.e("Log", "Login with facebook");
        callbackmanager = CallbackManager.Factory.create();

        // Set permissions
        LoginManager.getInstance().logInWithReadPermissions(SignInActivity.this, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(callbackmanager,
        new FacebookCallback<LoginResult>() {
@Override
public void onSuccess(LoginResult loginResult) {
        GraphRequest request1 = GraphRequest.newMeRequest(
        loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
@Override
public void onCompleted(JSONObject json, GraphResponse response) {
        if (response.getError() != null) {
        // handle error
        Log.e("Response ERROR : ", "JSON Result" + json.toString());
        Log.e("Response ERROR : ", "GraphResponse Result" + response.toString());
        } else {
        Log.e("Response  SUCCESS", "JSON Result" + json.toString());
        try {
        String jsonresult = String.valueOf(json);
        String str_email = json.getString("email");
        String str_id = json.getString("id");
        String str_firstname = json.getString("first_name");
        String str_lastname = json.getString("last_name");
        Log.e("str_firstname : ", str_firstname);
        Log.e("str_lastname : ", str_lastname);
        Log.e("str_id : ", str_id);
        Log.e("response", jsonresult);
        socialSignIn(str_email, str_firstname, str_lastname);
        } catch (JSONException e) {
        Toast.makeText(mContext, "Oops. We cannot get your information. Please change your privacy settings and try again", Toast.LENGTH_LONG).show();
        e.printStackTrace();
        }
        }
        }
        }
        );
        Bundle parameter = new Bundle();
        parameter.putString("fields", "id,name,email,first_name,last_name");
        request1.setParameters(parameter);
        request1.executeAsync();
        }

@Override
public void onCancel() {
        Log.d("Cancel", "On cancel");
        }

@Override
public void onError(FacebookException error) {
        Toast.makeText(mContext, "Error. Please sign up with email", Toast.LENGTH_LONG).show();
        Log.d("ERROR", error.toString());
        if (error instanceof FacebookAuthorizationException) {
        if (AccessToken.getCurrentAccessToken() != null) {
        LoginManager.getInstance().logOut();
        }
        }
        }
        }
        );
        }

//Login with Google Plus
private void loginWithGooglePlus() {
        intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, RC_SIGN_IN);
        }

@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
        if (requestCode == RC_SIGN_IN) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        handleSignInResult(result);
        } else {
        callbackmanager.onActivityResult(requestCode, resultCode, data);
        }
        } catch (Exception e){
        e.printStackTrace();
        }
        }

private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
        // Signed in successfully, show authenticated UI.
        GoogleSignInAccount acct = result.getSignInAccount();
        Log.e("DisplayName : ", acct.getDisplayName());
        Log.e("Email : ", acct.getEmail());
        socialSignIn(acct.getEmail(), acct.getGivenName(), acct.getFamilyName());
        } else {
        Log.d(TAG, "handleSignInResult:" + result.getStatus().getStatusCode());

        // Signed out, show unauthenticated UI.
        }
        }



public void socialSignIn(String email, String firstName, String lastName) {
        PrefsUtil.with(mContext).clearPrefs();
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.sociallogin;
        ArrayList<String> params = new ArrayList<>();
        params.add("userType");
        params.add("email");
        params.add("firstName");
        params.add("lastName");
        ArrayList<String> values = new ArrayList<>();
        values.add("u");
        values.add(email);
        values.add(firstName);
        values.add(lastName);
        new ParseJSON(mContext, url, params, values, SocialLoginPOJO.class,
        new ParseJSON.OnResultListner() {
@Override
public void onResult(boolean status, Object obj) {
        if (status) {
        SocialLoginPOJO resultObj = (SocialLoginPOJO) obj;
        if (resultObj.isStatus()) {
        //Toast.makeText(SignInActivity.this, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
        SocialLoginItem loginData = resultObj.getSocialLogin().get(0);
        if (loginData.getRegister().equalsIgnoreCase("y")) {
        Intent i = new Intent(SignInActivity.this, UserEditProfileActivity.class);
        i.putExtra("from", "signup");
        PrefsUtil.with(SignInActivity.this).clearPrefs();
        PrefsUtil.with(SignInActivity.this).write("isLoggedIn", true);
        PrefsUtil.with(SignInActivity.this).write("completeProfile", false);
        PrefsUtil.with(SignInActivity.this).write("uId", loginData.getUId());
        PrefsUtil.with(SignInActivity.this).write("firstName", loginData.getFirstName());
        PrefsUtil.with(SignInActivity.this).write("lastName", loginData.getLastName());
        PrefsUtil.with(SignInActivity.this).write("email", loginData.getEmail());
        PrefsUtil.with(SignInActivity.this).write("paypalEmail", loginData.getPaypalEmail());
        PrefsUtil.with(SignInActivity.this).write("password", loginData.getPassword());
        startActivity(i);
        //finish();
        } else {
        if (TextUtils.isEmpty(loginData.getMobileNo().toString()) || TextUtils.isEmpty(loginData.getDefaultPaymentMethod().toString())) {
        Intent i = new Intent(SignInActivity.this, UserEditProfileActivity.class);
        i.putExtra("from", "signup");
        PrefsUtil.with(SignInActivity.this).clearPrefs();
        PrefsUtil.with(SignInActivity.this).write("isLoggedIn", true);
        PrefsUtil.with(SignInActivity.this).write("completeProfile", false);
        PrefsUtil.with(SignInActivity.this).write("uId", loginData.getUId());
        PrefsUtil.with(SignInActivity.this).write("firstName", loginData.getFirstName());
        PrefsUtil.with(SignInActivity.this).write("lastName", loginData.getLastName());
        PrefsUtil.with(SignInActivity.this).write("email", loginData.getEmail());
        PrefsUtil.with(SignInActivity.this).write("paypalEmail", loginData.getPaypalEmail());
        PrefsUtil.with(SignInActivity.this).write("password", loginData.getPassword());
        startActivity(i);
        } else {
        registerDeviceFromSocial(loginData);
        }

                                    /*PrefsUtil.with(SignInActivity.this).write("completeProfile",true);
                                    PrefsUtil.with(SignInActivity.this).write("isLoggedIn",true);
                                    PrefsUtil.with(SignInActivity.this).write("uId",loginData.getUId());
                                    PrefsUtil.with(SignInActivity.this).write("firstName",loginData.getFirstName());
                                    PrefsUtil.with(SignInActivity.this).write("lastName",loginData.getLastName());
                                    PrefsUtil.with(SignInActivity.this).write("profileImage",loginData.getProfileImage());
                                    PrefsUtil.with(SignInActivity.this).write("mobileNo",loginData.getMobileNo());
                                    PrefsUtil.with(SignInActivity.this).write("email",loginData.getEmail());
                                    intent = new Intent(mContext, HomeActivity.class);
                                    startActivity(intent);*/
        }

        //finish();
        }
        } else {
        Toast.makeText(mContext, (String) obj, Toast.LENGTH_SHORT).show();
        }


        }
        });
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
        new ParseJSON(mContext, url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
@Override
public void onResult(boolean status, Object obj) {
        if (status) {
//        intent = new Intent(mContext, HomeActivity.class);
        intent = new Intent(mContext, Step2Activity.class);
        startActivity(intent);
        PrefsUtil.with(SignInActivity.this).clearPrefs();
        PrefsUtil.with(SignInActivity.this).write("isLoggedIn", true);
        PrefsUtil.with(SignInActivity.this).write("completeProfile", true);
        PrefsUtil.with(SignInActivity.this).write("uId", loginData.getUId());
        PrefsUtil.with(SignInActivity.this).write("firstName", loginData.getFirstName());
        PrefsUtil.with(SignInActivity.this).write("lastName", loginData.getLastName());
        PrefsUtil.with(SignInActivity.this).write("profileImage", WebServiceUrl.profileUrl + loginData.getUId() + "/" + loginData.getProfileImage());
        PrefsUtil.with(SignInActivity.this).write("mobileNo", loginData.getMobileNo());
        PrefsUtil.with(SignInActivity.this).write("email", loginData.getEmail());
        PrefsUtil.with(SignInActivity.this).write("paypalEmail", loginData.getPaypalEmail());
        PrefsUtil.with(SignInActivity.this).write("password", loginData.getPassword());
        finish();
        } else {
        Toast.makeText(mContext, (String) obj, Toast.LENGTH_SHORT).show();
        }


        }
        });
        }

private void forgotPassword(String email) {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.forgatpassword;
        ArrayList<String> params = new ArrayList<>();
        params.add("email");
        params.add("userType");
        ArrayList<String> values = new ArrayList<>();
        values.add(email);
        values.add("u");
        new ParseJSON(mContext, url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
@Override
public void onResult(boolean status, Object obj) {
        if (status) {
        CommonPOJO resultObj = (CommonPOJO) obj;
        Toast.makeText(mContext, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
        } else {
        Toast.makeText(mContext, (String) obj, Toast.LENGTH_SHORT).show();
        }
        }
        });
        }


private void resendMail(String uId, final Dialog dialog) {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.resendAuthenticationMail;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        ArrayList<String> values = new ArrayList<>();
        values.add(uId);
        new ParseJSON(mContext, url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
@Override
public void onResult(boolean status, Object obj) {
        if (status) {
        CommonPOJO resultObj = (CommonPOJO) obj;
        Toast.makeText(mContext, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
        try {
        dialog.dismiss();
        } catch (Exception e) {
        e.printStackTrace();
        }
        } else {
        Toast.makeText(mContext, (String) obj, Toast.LENGTH_SHORT).show();
        }
        }
        });
        }

private void showForgotPasswordDialog() {
final Dialog dialog = new Dialog(mContext);
        try {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        } catch (Exception e) {
        e.printStackTrace();
        }
        dialog.setContentView(R.layout.dialog_forgot_password);
        dialog.setCancelable(false);

        TextView txtClose = (TextView) dialog.findViewById(R.id.txtClose);
        TextView txtDone = (TextView) dialog.findViewById(R.id.txtDone);
final EditText edtEmail1 = (EditText) dialog.findViewById(R.id.edtEmail1);

        txtClose.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
        dialog.dismiss();
        }
        });

        txtDone.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
        Boolean validationResult = true;
        if (edtEmail1.getText().toString().length() == 0) {
        edtEmail1.setError("E-mail cannot be empty.");
        edtEmail1.requestFocus();
        validationResult = false;
        }
        if (validationResult) {
        if (!Patterns.EMAIL_ADDRESS.matcher(edtEmail1.getText().toString()).matches()) {
        edtEmail1.setError("Enter Valid E-mail");
        edtEmail1.requestFocus();
        validationResult = false;
        }
        }

        if (validationResult) {
        forgotPassword(edtEmail1.getText().toString());
        }
        }
        });

        // now that the dialog is set up, it's time to show it
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

private void showActivationDialog(final DataAnsItem loginData) {
final Dialog dialog = new Dialog(mContext);
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

private void registerDeviceFromSocial(final SocialLoginItem loginData) {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.registerdevice;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        params.add("userType");
        params.add("deviceId");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(loginData.getUId()));
        values.add("u");
        values.add(FirebaseInstanceId.getInstance().getToken());
        new ParseJSON(mContext, url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
@Override
public void onResult(boolean status, Object obj) {
        if (status) {
//        intent = new Intent(mContext, HomeActivity.class);
        intent = new Intent(mContext, Step2Activity.class);
        startActivity(intent);
        PrefsUtil.with(SignInActivity.this).clearPrefs();
        PrefsUtil.with(SignInActivity.this).write("isLoggedIn", true);
        PrefsUtil.with(SignInActivity.this).write("completeProfile", true);
        PrefsUtil.with(SignInActivity.this).write("uId", loginData.getUId());
        PrefsUtil.with(SignInActivity.this).write("firstName", loginData.getFirstName());
        PrefsUtil.with(SignInActivity.this).write("lastName", loginData.getLastName());
        PrefsUtil.with(SignInActivity.this).write("profileImage", WebServiceUrl.profileUrl + loginData.getUId() + "/" + loginData.getProfileImage());
        PrefsUtil.with(SignInActivity.this).write("mobileNo", loginData.getMobileNo());
        PrefsUtil.with(SignInActivity.this).write("email", loginData.getEmail());
        PrefsUtil.with(SignInActivity.this).write("paypalEmail", loginData.getPaypalEmail());
        PrefsUtil.with(SignInActivity.this).write("password", loginData.getPassword());
        finish();
        } else {
        Toast.makeText(mContext, (String) obj, Toast.LENGTH_SHORT).show();
        }


        }
        });
        }

public void loginWithCreds(String strEmail, String strPassword) {
        ArrayList<String> params = new ArrayList<>();
        params.add("email");
        params.add("password");
        ArrayList<String> values = new ArrayList<>();
        values.add(strEmail);
        values.add(strPassword);
        new ParseJSON(SignInActivity.this, WebServiceUrl.ServiceUrl + WebServiceUrl.userlogin, params, values, LoginPOJO.class, new ParseJSON.OnResultListner() {
@Override
public void onResult(boolean status, Object obj) {
        if (status) {
        LoginPOJO loginObj = (LoginPOJO) obj;
        if (loginObj.isStatus()) {
                                    /*intent = new Intent(mContext, HomeActivity.class);
                                    startActivity(intent);*/
        if (loginObj.getDataAns().get(0).getIsActive().equalsIgnoreCase("y")) {
        //Toast.makeText(SignInActivity.this, loginObj.getMessage(), Toast.LENGTH_SHORT).show();
        registerDevice(loginObj.getDataAns().get(0));
        } else {
        showActivationDialog(loginObj.getDataAns().get(0));
        }

                                   /* DataAnsItem loginData = loginObj.getDataAns().get(0);
                                    PrefsUtil.with(SignInActivity.this).write("isLoggedIn",true);
                                    PrefsUtil.with(SignInActivity.this).write("uId",loginData.getUId());
                                    PrefsUtil.with(SignInActivity.this).write("firstName",loginData.getFirstName());
                                    PrefsUtil.with(SignInActivity.this).write("lastName",loginData.getLastName());
                                    PrefsUtil.with(SignInActivity.this).write("profileImage",loginData.getProfileImage());
                                    PrefsUtil.with(SignInActivity.this).write("mobileNo",loginData.getMobileNo());
                                    PrefsUtil.with(SignInActivity.this).write("email",loginData.getEmail());*/
        //finish();
        }
        } else {
        Toast.makeText(SignInActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
        }
        }
        });
        }

   /* public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(SignInActivity.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }*/

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
