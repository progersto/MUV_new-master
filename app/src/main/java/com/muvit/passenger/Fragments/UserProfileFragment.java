package com.muvit.passenger.Fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.muvit.passenger.Activities.HomeLocationActivity;
import com.muvit.passenger.Activities.Step2Activity;
import com.muvit.passenger.Activities.UserEditProfileActivity;
import com.muvit.passenger.Activities.WorkLocationActivity;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Models.CommonPOJO;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.Models.ProfileItem;
import com.muvit.passenger.Models.Response;
import com.muvit.passenger.Models.UserProfilePOJO;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.ImgUtils;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.Transform;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserProfileFragment extends Fragment {

    private TextView txtAddWorkLocation, txtAddHomeLocation, txtName, txtMobileNo, txtEmail, txtPaymentMethod, txtWorkLocation, txtHomeLocation;
    private Intent intent;
    private CircleImageView imgProfile;
    private ImageView imgEditInfo, imgEditPaymentMethod, imgPaymentMethod, imgEditWorkLocation, imgEditHomeLocation, imgDeleteHomeLocation, imgDeleteWorkLocation;
    private LinearLayout layoutHomeLocation, layoutWorkLocation;
    private final int PROFILE_UPDATE_CODE = 500;
    ImageView back_btn;




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);

        ((Step2Activity)getActivity()).hideToolbar();
        ((Step2Activity)getActivity()).hideToolbarTiltle();
        intiView(rootView);

        txtAddWorkLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), WorkLocationActivity.class);
                i.putExtra("dataAvailable", false);
                startActivityForResult(i, PROFILE_UPDATE_CODE);
            }
        });

        txtAddHomeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(getContext(), HomeLocationActivity.class);
                intent.putExtra("dataAvailable", false);
                startActivityForResult(intent, PROFILE_UPDATE_CODE);
            }
        });


        imgDeleteHomeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you want to delete this location?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteLocation("home");
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                // Create the AlertDialog object and return it
                builder.create();
                builder.show();
            }
        });

        imgDeleteWorkLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you want to delete this location?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteLocation("work");
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                // Create the AlertDialog object and return it
                builder.create();
                builder.show();
            }
        });
        if (new ConnectionCheck().isNetworkConnected(getActivity())) {
            getUserProfile();
        } else {
            getOfflineUserDetails();
        }


        return rootView;
    }

    private void deleteLocation(String type) {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.userdeletelocation;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        params.add("locationType");

        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(getActivity()).readInt("uId")));
        values.add(type);


        new ParseJSON(getActivity(), url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    CommonPOJO resultObj = (CommonPOJO) obj;
                    Toast.makeText(getActivity(), resultObj.getMessage(), Toast.LENGTH_SHORT).show();
                    getUserProfile();
                } else {
                    Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    private void intiView(View rootView) {
        back_btn = (ImageView) rootView.findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Step2Activity)getActivity()).showToolbar();
                FragmentManager fm = getActivity()
                        .getSupportFragmentManager();
                fm.popBackStack();
            }
        });
        txtAddWorkLocation = (TextView) rootView.findViewById(R.id.txtAddWorkLocation);
        txtAddHomeLocation = (TextView) rootView.findViewById(R.id.txtAddHomeLocation);
        txtWorkLocation = (TextView) rootView.findViewById(R.id.txtWorkLocation);
        txtHomeLocation = (TextView) rootView.findViewById(R.id.txtHomeLocation);
        txtName = (TextView) rootView.findViewById(R.id.txtName);
        txtMobileNo = (TextView) rootView.findViewById(R.id.txtMobileNo);
        txtEmail = (TextView) rootView.findViewById(R.id.txtEmail);
        txtPaymentMethod = (TextView) rootView.findViewById(R.id.txtPaymentMethod);
        imgEditInfo = (ImageView) rootView.findViewById(R.id.imgEditInfo);
        imgEditPaymentMethod = (ImageView) rootView.findViewById(R.id.imgEditPaymentMethod);
        imgPaymentMethod = (ImageView) rootView.findViewById(R.id.imgPaymentMethod);
        imgProfile = (CircleImageView) rootView.findViewById(R.id.imgProfile);
        imgEditWorkLocation = (ImageView) rootView.findViewById(R.id.imgEditWorkLocation);
        imgEditHomeLocation = (ImageView) rootView.findViewById(R.id.imgEditHomeLocation);
        imgDeleteHomeLocation = (ImageView) rootView.findViewById(R.id.imgDeleteHomeLocation);
        imgDeleteWorkLocation = (ImageView) rootView.findViewById(R.id.imgDeleteWorkLocation);
        layoutHomeLocation = (LinearLayout) rootView.findViewById(R.id.layoutHomeLocation);
        layoutWorkLocation = (LinearLayout) rootView.findViewById(R.id.layoutWorkLocation);
    }

    public void getUserProfile() {
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");

        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(getActivity()).readInt("uId")));
        new ParseJSON(getActivity(), WebServiceUrl.ServiceUrl + WebServiceUrl.getuserprofile, params, values, UserProfilePOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    UserProfilePOJO resultObj = (UserProfilePOJO) obj;
                    final ProfileItem profileItem = resultObj.getProfile().get(0);
                    txtName.setText(profileItem.getFirstName() + " " + profileItem.getLastName());
                    txtMobileNo.setText(profileItem.getMobileNo());
                    txtEmail.setText(profileItem.getEmail());
                    if (profileItem.getDefaultPaymentMethod().equalsIgnoreCase("c")) {
                        txtPaymentMethod.setText("Cash");
                        imgPaymentMethod.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.cash));
                    } else {
                        txtPaymentMethod.setText("Wallet");
                        imgPaymentMethod.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.wallet_profile));
                    }


                    if (checkIfNullOfEmpty(profileItem.getHomeLocation())) {
                        layoutHomeLocation.setVisibility(View.GONE);
                        txtAddHomeLocation.setVisibility(View.VISIBLE);
                    } else {
                        txtHomeLocation.setText(profileItem.getHomeLocation().toString());
                        txtAddHomeLocation.setVisibility(View.GONE);
                        layoutHomeLocation.setVisibility(View.VISIBLE);
                        imgEditHomeLocation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(getActivity(), HomeLocationActivity.class);
                                i.putExtra("dataAvailable", true);
                                i.putExtra("lat", String.valueOf(profileItem.getHomeLat()));
                                i.putExtra("long", String.valueOf(profileItem.getHomeLong()));
                                startActivityForResult(i, PROFILE_UPDATE_CODE);
                            }
                        });
                    }

                    if (checkIfNullOfEmpty(profileItem.getWorkLocation())) {
                        txtAddWorkLocation.setVisibility(View.VISIBLE);
                        layoutWorkLocation.setVisibility(View.GONE);
                    } else {
                        txtWorkLocation.setText(profileItem.getWorkLocation().toString());
                        txtAddWorkLocation.setVisibility(View.GONE);
                        layoutWorkLocation.setVisibility(View.VISIBLE);
                        imgEditWorkLocation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(getActivity(), WorkLocationActivity.class);
                                i.putExtra("dataAvailable", true);
                                i.putExtra("lat", String.valueOf(profileItem.getWorkLat()));
                                i.putExtra("long", String.valueOf(profileItem.getWorkLong()));
                                startActivityForResult(i, PROFILE_UPDATE_CODE);
                            }
                        });
                    }

                    imgEditInfo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getActivity(), UserEditProfileActivity.class);
                            i.putExtra("firstName", profileItem.getFirstName());
                            i.putExtra("lastName", profileItem.getLastName());
                            i.putExtra("email", profileItem.getEmail());
                            i.putExtra("number", profileItem.getMobileNo());
                            i.putExtra("defaultMethod", profileItem.getDefaultPaymentMethod());
                            i.putExtra("code", profileItem.getCountryCode());
                            i.putExtra("picUrl", WebServiceUrl.profileUrl + profileItem.getUId() + "/" + profileItem.getProfileImage());
                            i.putExtra("from", "userProfile");
                            startActivityForResult(i, PROFILE_UPDATE_CODE);
                        }
                    });

                    imgEditPaymentMethod.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            intent = new Intent(getActivity(), UserEditProfileActivity.class);
                            intent.putExtra("firstName", profileItem.getFirstName());
                            intent.putExtra("lastName", profileItem.getLastName());
                            intent.putExtra("email", profileItem.getEmail());
                            intent.putExtra("number", profileItem.getMobileNo());
                            intent.putExtra("defaultMethod", profileItem.getDefaultPaymentMethod());
                            intent.putExtra("code", profileItem.getCountryCode());
                            intent.putExtra("picUrl", WebServiceUrl.profileUrl + profileItem.getUId() + "/" + profileItem.getProfileImage());
                            intent.putExtra("from", "userProfile");
                            startActivityForResult(intent, PROFILE_UPDATE_CODE);
                        }
                    });

//                    Ion.with(imgProfile)
//                            .transform(new Transform() {
//                                @Override
//                                public Bitmap transform(Bitmap b) {
//                                    return ImgUtils.createCircleBitmap(b);
//                                }
//
//                                @Override
//                                public String key() {
//                                    return null;
//                                }
//                            })
//                            .load(WebServiceUrl.profileUrl + profileItem.getUId() + "/" + profileItem.getProfileImage());

                    String imgUrl = WebServiceUrl.profileUrl + profileItem.getUId() + "/" + profileItem.getProfileImage();
                    Picasso.get().load(imgUrl).placeholder(R.drawable.profile_placeholder).error(R.drawable.profile_placeholder).into(imgProfile);
                    PrefsUtil.with(getActivity()).write("firstName", profileItem.getFirstName());
                    PrefsUtil.with(getActivity()).write("lastName", profileItem.getLastName());
                    PrefsUtil.with(getActivity()).write("profileImage", WebServiceUrl.profileUrl + profileItem.getUId() + "/" + profileItem.getProfileImage());

                    try {
                        ((Step2Activity) getActivity()).updateHeader();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else {
                    Toast.makeText(getActivity(), (String) obj, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public boolean checkIfNullOfEmpty(Object str) {
        try {
            if (TextUtils.isEmpty(str.toString())) {
                return true;
            } else {
                return false;
            }
        } catch (NullPointerException e) {
            return true;
        }
    }

    public void getOfflineUserDetails() {
        try {
            File f = new File(getActivity().getFilesDir().getPath() + "/" + "offline.json");
            //check whether file exists
            FileInputStream is = new FileInputStream(f);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String s = new String(buffer);
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("M/d/yy hh:mm a"); //Format of our JSON dates
            Gson gson = gsonBuilder.create();
            Response item = new Response();
            item = gson.fromJson(s, Response.class);
            final ProfileItem profileItem = item.getOfflineDataModel().get(0).getGetuserprofile().getDataAns().get(0);
            txtName.setText(profileItem.getFirstName() + " " + profileItem.getLastName());
            txtMobileNo.setText(profileItem.getMobileNo());
            txtEmail.setText(profileItem.getEmail());
            if (profileItem.getDefaultPaymentMethod().equalsIgnoreCase("c")) {
                txtPaymentMethod.setText("Cash");
                imgPaymentMethod.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.cash));
            } else {
                txtPaymentMethod.setText("Wallet");
                imgPaymentMethod.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.wallet_profile));
            }


            layoutHomeLocation.setVisibility(View.GONE);
            txtAddHomeLocation.setVisibility(View.GONE);
            txtAddWorkLocation.setVisibility(View.GONE);
            layoutWorkLocation.setVisibility(View.GONE);
            /*if (checkIfNullOfEmpty(profileItem.getHomeLocation())) {
                layoutHomeLocation.setVisibility(View.GONE);
                txtAddHomeLocation.setVisibility(View.VISIBLE);
            } else {
                txtHomeLocation.setText(profileItem.getHomeLocation().toString());
                txtAddHomeLocation.setVisibility(View.GONE);
                layoutHomeLocation.setVisibility(View.VISIBLE);
                imgEditHomeLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), HomeLocationActivity.class);
                        i.putExtra("dataAvailable", true);
                        i.putExtra("lat", String.valueOf(profileItem.getHomeLat()));
                        i.putExtra("long", String.valueOf(profileItem.getHomeLong()));
                        startActivityForResult(i, PROFILE_UPDATE_CODE);
                    }
                });
            }

            if (checkIfNullOfEmpty(profileItem.getWorkLocation())) {
                txtAddWorkLocation.setVisibility(View.VISIBLE);
                layoutWorkLocation.setVisibility(View.GONE);
            } else {
                txtWorkLocation.setText(profileItem.getWorkLocation().toString());
                txtAddWorkLocation.setVisibility(View.GONE);
                layoutWorkLocation.setVisibility(View.VISIBLE);
                imgEditWorkLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), WorkLocationActivity.class);
                        i.putExtra("dataAvailable", true);
                        i.putExtra("lat", String.valueOf(profileItem.getWorkLat()));
                        i.putExtra("long", String.valueOf(profileItem.getWorkLong()));
                        startActivityForResult(i, PROFILE_UPDATE_CODE);
                    }
                });
            }*/



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
                    .load(WebServiceUrl.profileUrl + profileItem.getUId() + "/" + profileItem.getProfileImage());




    }catch(Exception e){
        Log.e("TAG", "Error in Reading: " + e.getLocalizedMessage());
    }

}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PROFILE_UPDATE_CODE && resultCode == Activity.RESULT_OK) {
            Log.e("UserProfileFragment", "onActivityResult: Got Called!");
            getUserProfile();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((Step2Activity)getActivity()).hideToolbar();
        ((Step2Activity)getActivity()).hideToolbarTiltle();
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
                    getUserProfile();
                }else if (event.getMessage().equalsIgnoreCase("disconnected")){
                    getOfflineUserDetails();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
