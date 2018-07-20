package com.muvit.passenger.Activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Fragments.AboutFragment;
import com.muvit.passenger.Fragments.AccountSettingsFragment;
import com.muvit.passenger.Fragments.HelpFragment;
import com.muvit.passenger.Fragments.HomeFragment;
import com.muvit.passenger.Fragments.InfoFragment;
import com.muvit.passenger.Fragments.MyTripsFragment;
import com.muvit.passenger.Fragments.NotificationsFragment;
import com.muvit.passenger.Fragments.UserProfileFragment;
import com.muvit.passenger.Fragments.WalletFragment;
import com.muvit.passenger.Models.CommonPOJO;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.ImgUtils;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;
import com.google.firebase.iid.FirebaseInstanceId;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.Transform;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView txtTitle;
    private Toolbar toolbar;
    public ActionBarDrawerToggle toggle;

    FragmentManager fragmentManager;
    BroadcastReceiver mMessageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initViews();
        fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(getListener());
        initNavigationDrawer();
    }

    private FragmentManager.OnBackStackChangedListener getListener()
    {
        FragmentManager.OnBackStackChangedListener result = new FragmentManager.OnBackStackChangedListener()
        {
            public void onBackStackChanged()
            {
                FragmentManager manager = getSupportFragmentManager();

                if (manager != null)
                {
                    Fragment currFrag = fragmentManager.findFragmentById(R.id.containerView);
                    if(currFrag instanceof HomeFragment || currFrag instanceof NotificationsFragment ||  currFrag instanceof MyTripsFragment ||
                            currFrag instanceof AccountSettingsFragment){
                        showToolbar();
                    }else{
                        hideToolbar();
                    }

                    if(currFrag instanceof NotificationsFragment  ||  currFrag instanceof MyTripsFragment ||
                            currFrag instanceof AccountSettingsFragment
                            ){
                        turnActionBarIconWhite();

                    }else{
                        turnActionBarIconBlack();
                    }
                    //showToolbar();
//                    MyFragment currFrag = (MyFragment) manager.findFragmentById(R.id.fragmentItem);
//
//                    currFrag.onFragmentResume();
                }
            }
        };

        return result;
    }

    private void initViews() {
        setupToolbar();
        drawerLayout = findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle
                (
                        this,
                        drawerLayout,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close
                )
        {
        };

        navigationView = findViewById(R.id.navigation_view);

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (new ConnectionCheck().isNetworkConnected(context)) {
                    Log.e("HomeActivity", "connected");
                    if(!ApplicationController.isOnline) {
                        if (PrefsUtil.isNoInternetShowing) {
                            if (PrefsUtil.dialogNoInternet != null) {
                                PrefsUtil.dialogNoInternet.dismiss();
                                PrefsUtil.isNoInternetShowing = false;
                            }
                        }
                        final Dialog d = new Dialog(context, android.R.style.Theme_Light_NoTitleBar);
                        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        d.setContentView(R.layout.dialog_connected);
                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                        lp.copyFrom(d.getWindow().getAttributes());
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                        d.getWindow().setAttributes(lp);
                        TextView txtRetry = (TextView) d.findViewById(R.id.txtRetry);
                        txtRetry.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PrefsUtil.isInternetConnectedShowing = false;
                                d.dismiss();
                            }
                        });
                        d.setCancelable(true);
                        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                PrefsUtil.isInternetConnectedShowing = false;
                            }
                        });
                        PrefsUtil.isInternetConnectedShowing = true;
                        PrefsUtil.dialogInternetConnected = d;
                        d.show();
                        ApplicationController.isOnline = true;
                    }
                    EventBus.getDefault().post(new MessageEvent("connection","connected"));
                } else {
                    //new ConnectionCheck().showDialogWithMessage(context, getString(R.string.sync_data_message)).show();
                    Log.e("HomeActivity", "disconnected");
                    if(ApplicationController.isOnline) {
                        if (PrefsUtil.isInternetConnectedShowing) {
                            if (PrefsUtil.dialogInternetConnected != null) {
                                PrefsUtil.dialogInternetConnected.dismiss();
                                PrefsUtil.isInternetConnectedShowing = false;
                            }
                        }
                        final Dialog d = new Dialog(context, android.R.style.Theme_Light_NoTitleBar);
                        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        d.setContentView(R.layout.dialog_no_internet);
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
                            }
                        });
                        d.setCancelable(true);
                        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                PrefsUtil.isNoInternetShowing = false;
                            }
                        });
                        PrefsUtil.isNoInternetShowing = true;
                        PrefsUtil.dialogNoInternet = d;
                        d.show();
                        ApplicationController.isOnline = false;
                    }
                    EventBus.getDefault().post(new MessageEvent("connection","disconnected"));
                }
            }
        };
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolBar);

        txtTitle = findViewById(R.id.txtTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void initNavigationDrawer() {
        txtTitle.setText(R.string.home);
        navigationView.setCheckedItem(R.id.home);
        updateDisplay(new HomeFragment(),R.string.home);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        int id = menuItem.getItemId();

                        switch (id) {
                            case R.id.home:
                                txtTitle.setText(R.string.home);
                                updateDisplay(new HomeFragment(),R.string.home);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.myProfile:
                                txtTitle.setText(R.string.my_profile);
                                updateDisplay(new UserProfileFragment(),R.string.my_profile);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.payments:
                                startActivity(new Intent(HomeActivity.this,PaymentsActivity.class));

                                break;
                            case R.id.wallet:
                                txtTitle.setText(R.string.nav_wallet);
                                updateDisplay(new WalletFragment(),R.string.nav_wallet);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.rides:
                                txtTitle.setText(R.string.rides);
                                updateDisplay(new MyTripsFragment(),R.string.rides);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.notifications:
                                txtTitle.setText(R.string.notifications);
                                updateDisplay(new NotificationsFragment(),R.string.notifications);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.account_settings:
                                txtTitle.setText(R.string.account_settings);
                                updateDisplay(new AccountSettingsFragment(),R.string.account_settings);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.help:
                                txtTitle.setText(R.string.help);
                                updateDisplay(new HelpFragment(),R.string.help);
                                drawerLayout.closeDrawers();
                                break;

                            case R.id.invite_friends:
                                startActivity(new Intent(HomeActivity.this,InviteFriendActivity.class));
                                break;
                            case R.id.about:
                                txtTitle.setText(R.string.about);
                                updateDisplay(new AboutFragment(),R.string.about);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.info:
                                txtTitle.setText(R.string.info);
                                updateDisplay(new InfoFragment(),R.string.info);
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.logout:

                                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                                builder.setMessage("Are you sure you want logout?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                        /*PrefsUtil.with(HomeActivity.this).clearPrefs();
                                        PrefsUtil.with(HomeActivity.this).write("isLoggedIn",false);
                                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();*/
                                                logout();
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {

                                            }
                                        });
                                // Create the AlertDialog object and return it
                                builder.create();
                                builder.show();

                                break;
                        }
                        return true;
                    }
                });
        View header = navigationView.getHeaderView(0);
        ImageView imgProfile = (ImageView) header.findViewById(R.id.imgProfile);
        //imgProfile.setImageResource(R.mipmap.ic_launcher);
        Ion.with(imgProfile)
                .error(R.drawable.no_image)
                .transform(new Transform() {
                    @Override
                    public Bitmap transform(Bitmap b) {
                        return ImgUtils.createCircleBitmap(b);
                    }

                    @Override
                    public String key() {
                        return null;
                    }
                })
                .load(PrefsUtil.with(this).readString("profileImage"));
        TextView txtUserName = (TextView) header.findViewById(R.id.txtUserName);
        txtUserName.setText(PrefsUtil.with(this).readString("firstName") + " " + PrefsUtil.with(this).readString("lastName"));
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View v) {
                InputMethodManager mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mImm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                v.requestFocus();
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                InputMethodManager mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mImm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                v.requestFocus();
                super.onDrawerOpened(v);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void updateDisplay(Fragment fragment,int res) {

        fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction().add(R.id.containerView, fragment).addToBackStack(getResources().getString(res)).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.logout;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        params.add("userType");
        params.add("deviceId");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(HomeActivity.this).readInt("uId")));
        values.add("u");
        values.add(FirebaseInstanceId.getInstance().getToken());
        new ParseJSON(HomeActivity.this, url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    CommonPOJO resultObj = (CommonPOJO) obj;
                    Toast.makeText(HomeActivity.this, resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                    PrefsUtil.with(HomeActivity.this).clearPrefs();
                    PrefsUtil.with(HomeActivity.this).write("isLoggedIn", false);
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(HomeActivity.this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                    PrefsUtil.with(HomeActivity.this).clearPrefs();
                    PrefsUtil.with(HomeActivity.this).write("isLoggedIn", false);
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }


            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        }else {

            if ((getSupportFragmentManager().getBackStackEntryCount() - 1) >= 0) {
                getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        try {
                            //Fragment fragment = getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getBackStackEntryCount() - 1);
                            FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1);
                            String tag = backEntry.getName();
                            txtTitle.setText(tag);
                           // fragment.onResume();
                        } catch (Exception e) {

                        }


                    }
                });
            }
            if (getSupportFragmentManager().getBackStackEntryCount() - 1 == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setMessage("Do you really want to Exit?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //if user pressed "yes", then he is allowed to exit from application
                        finish();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //if user select "No", just cancel this dialog and continue with app
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();


            } else {
                getSupportFragmentManager().popBackStack();

            }


        }

        //super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(PrefsUtil.dialogStartGPS != null){
            PrefsUtil.dialogStartGPS.dismiss();
            PrefsUtil.dialogStartGPS = null;
            PrefsUtil.isStartGPSShowing = false;
        }
        try {
            updateHeader();
            InputMethodManager mImm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            mImm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            registerReceiver(mMessageReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateHeader() {
        try {
            View header = navigationView.getHeaderView(0);
        /*CircleImageView imgProfile = (CircleImageView) header.findViewById(R.id.imgProfile);
        imgProfile.setImageResource(R.mipmap.ic_launcher);*/
            ImageView imgProfile = (ImageView) header.findViewById(R.id.imgProfile);
            //imgProfile.setImageResource(R.mipmap.ic_launcher);
            Ion.with(imgProfile)
                    .transform(new Transform() {
                        @Override
                        public Bitmap transform(Bitmap b) {
                            return ImgUtils.createCircleBitmap(b);
                        }

                        @Override
                        public String key() {
                            return null;
                        }
                    })
                    .load(PrefsUtil.with(HomeActivity.this).readString("profileImage"));
            TextView txtUserName = (TextView) header.findViewById(R.id.txtUserName);
            txtUserName.setText(PrefsUtil.with(this).readString("firstName") + " " + PrefsUtil.with(this).readString("lastName"));
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    @Override
    public void onDestroy() {
        unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    public void showToolbarTiltle(String title){
        txtTitle.setText(title);
        txtTitle.setVisibility(View.VISIBLE);
    }

    public void hideToolbarTiltle(){
        //txtTitle.setText(title);
        txtTitle.setVisibility(View.GONE);
    }

    public void turnActionBarIconWhite(){
        toggle = new ActionBarDrawerToggle
                (
                        this,
                        drawerLayout,
                        toolbar,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close
                )
        {
        };
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        toggle.setHomeAsUpIndicator(R.drawable.ic_hamburger_white);



        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        changeBackgroundToolBar(R.color.dull_golden);
    }

    public void turnActionBarIconBlack(){
        toggle = new ActionBarDrawerToggle
                (
                        this,
                        drawerLayout,
                        toolbar,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close
                )
        {
        };
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        toggle.setHomeAsUpIndicator(R.drawable.ic_hamburger_black);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.light_black));
        changeBackgroundToolBar(R.color.transparent);
    }



    public void hideToolbar(){
        toolbar.setVisibility(View.GONE);
    }

    public void showToolbar(){
        toolbar.setVisibility(View.VISIBLE);

    }

    public void changeBackgroundToolBar(int id){
        toolbar.setBackgroundColor(ContextCompat.getColor(this,id));
    }

}
