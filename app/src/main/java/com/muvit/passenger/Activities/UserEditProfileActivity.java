package com.muvit.passenger.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.muvit.passenger.Application.ApplicationController;
import com.muvit.passenger.AsyncTask.ParseJSON;
import com.muvit.passenger.Models.CommonPOJO;
import com.muvit.passenger.Models.CountryItem;
import com.muvit.passenger.Models.CountryPOJO;
import com.muvit.passenger.Models.MessageEvent;
import com.muvit.passenger.R;
import com.muvit.passenger.Utils.ConnectionCheck;
import com.muvit.passenger.Utils.ImgUtils;
import com.muvit.passenger.Utils.KeyboardUtils;
import com.muvit.passenger.Utils.PrefsUtil;
import com.muvit.passenger.WebServices.WebServiceUrl;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.Transform;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserEditProfileActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private TextView txtTitle, txtWallet, txtCash;
    private LinearLayout layoutWallet, layoutCash;
    private ImageView imgWallet, imgCash,back_btn;
    private CircleImageView imgProfile;
    private Context context;
    private EditText edtFirstName, edtLastName, edtEmail, edtMobileNo;
    private String strFirstName, strLastName, strEmail, strMobileNo;
    private Button btnUpdateProfile;
    private Spinner spinnerCountryCode;
    private ArrayAdapter adapterCode;
    ArrayList<CountryItem> arrCountries = new ArrayList<>();

    private Uri imageUri;
    private static final int PICK_FROM_CAMERA = 1;
    private static final int SELECT_PICTURE = 2;
    private String dirPath = Environment.getExternalStorageDirectory() + File.separator + "MUV";
    private String selectedImagePath;
    Boolean pic_changed = false;
    private String TAG = "UserEditProfileActivity";
    private String countryCode = "0";
    private String defaultMethod = "c";
    private String from = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit_profile);
        initViews();

        getCountryCode();

        try {

            from = getIntent().getStringExtra("from");

            if (from.equalsIgnoreCase("signup")) {
                edtFirstName.setText(PrefsUtil.with(context).readString("firstName"));
                edtLastName.setText(PrefsUtil.with(context).readString("lastName"));
                edtEmail.setText(PrefsUtil.with(context).readString("email"));
                txtTitle.setText(R.string.complete_profile);
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
                        .load("android.resource://" + getPackageName() + "/drawable/no_image");
            } else {
                edtFirstName.setText(getIntent().getStringExtra("firstName"));
                edtLastName.setText(getIntent().getStringExtra("lastName"));
                edtMobileNo.setText(getIntent().getStringExtra("number"));
                edtEmail.setText(getIntent().getStringExtra("email"));
                countryCode = getIntent().getStringExtra("code");
                defaultMethod = getIntent().getStringExtra("defaultMethod");
                txtTitle.setText(R.string.user_edit_profile_title);
//                Ion.with(imgProfile)
//                        .error(R.drawable.no_image)
//                        .transform(new Transform() {
//                            @Override
//                            public Bitmap transform(Bitmap b) {
//                                return ImgUtils.createCircleBitmap(b);
//                            }
//
//                            @Override
//                            public String key() {
//                                return null;
//                            }
//                        })
//                        .load(getIntent().getStringExtra("picUrl"));
                 Picasso.get().load(getIntent().getStringExtra("picUrl")).placeholder(R.drawable.profile_placeholder).error(R.drawable.profile_placeholder).into(imgProfile);

            }
            /*edtFirstName.setText(getIntent().getStringExtra("firstName"));
            edtLastName.setText(getIntent().getStringExtra("lastName"));
            edtMobileNo.setText(getIntent().getStringExtra("number"));
            edtEmail.setText(getIntent().getStringExtra("email"));
            countryCode = getIntent().getIntExtra("code",0);
            defaultMethod = getIntent().getStringExtra("defaultMethod");*/
            if (defaultMethod.equalsIgnoreCase("c")) {
                txtWallet.setTextColor(Color.parseColor("#777777"));
                imgWallet.setColorFilter(ContextCompat.getColor(context, R.color.grey2));
                txtCash.setTextColor(Color.parseColor("#000000"));
                imgCash.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            } else {
                txtWallet.setTextColor(Color.parseColor("#000000"));
                imgWallet.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                txtCash.setTextColor(Color.parseColor("#777777"));
                imgCash.setColorFilter(ContextCompat.getColor(context, R.color.grey2));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        layoutWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtWallet.setTextColor(Color.parseColor("#000000"));
                imgWallet.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                defaultMethod = "w";
                txtCash.setTextColor(Color.parseColor("#777777"));
                imgCash.setColorFilter(ContextCompat.getColor(context, R.color.grey2));
            }
        });

        layoutCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtWallet.setTextColor(Color.parseColor("#777777"));
                imgWallet.setColorFilter(ContextCompat.getColor(context, R.color.grey2));
                defaultMethod = "c";
                txtCash.setTextColor(Color.parseColor("#000000"));
                imgCash.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            }
        });

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strFirstName = edtFirstName.getText().toString().trim();
                strLastName = edtLastName.getText().toString().trim();
                strEmail = edtEmail.getText().toString().trim();
                strMobileNo = edtMobileNo.getText().toString().trim();

                Boolean validationResult = formValidation();
                if (validationResult) {
                    if (pic_changed) {
                        editProfileWithPic();
                    } else {
                        editProfile();
                    }
                }
            }
        });


        // Take image from camera and gallery.
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] items = {"Gallery", "Camera"};

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Pick From ");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0:
                                Intent intent = new Intent();
                                intent.setType("image/*");
                                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                                intent.setAction(Intent.ACTION_PICK);
                                startActivityForResult(Intent.createChooser(intent,
                                        "Select Picture"), SELECT_PICTURE);
                                break;
                            case 1:
                                try {
                                    ContentValues values = new ContentValues();
                                    values.put(MediaStore.Images.Media.TITLE, "New Picture");
                                    values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                                    imageUri = getContentResolver().insert(
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                    Intent cameraaIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    cameraaIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                    startActivityForResult(cameraaIntent, PICK_FROM_CAMERA);
                        /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, PICK_FROM_CAMERA);*/
                                } catch (ActivityNotFoundException anfe) {
                                    Toast toast = Toast.makeText(context, "This device doesn't support Camera!",
                                            Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                                break;
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void initViews() {
        setupToolbar();
        context = UserEditProfileActivity.this;
        back_btn = (ImageView)  findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        layoutWallet = (LinearLayout) findViewById(R.id.layoutWallet);

        layoutCash = (LinearLayout) findViewById(R.id.layoutCash);
        imgWallet = (ImageView) findViewById(R.id.imgWallet);
        imgCash = (ImageView) findViewById(R.id.imgCash);
        imgProfile = (CircleImageView) findViewById(R.id.imgProfile);

        txtWallet = (TextView) findViewById(R.id.txtWallet);
        txtCash = (TextView) findViewById(R.id.txtCash);

        edtFirstName = (EditText) findViewById(R.id.edtFirstName);
        edtLastName = (EditText) findViewById(R.id.edtLastName);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtMobileNo = (EditText) findViewById(R.id.edtMobileNo);

        btnUpdateProfile = (Button) findViewById(R.id.btnUpdateProfile);

        spinnerCountryCode = (Spinner) findViewById(R.id.spinnerCountryCode);

        adapterCode = new ArrayAdapter<>(context,
                R.layout.custom_yellow_spinner, arrCountries);
        adapterCode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountryCode.setAdapter(adapterCode);

        new KeyboardUtils().setupUI(findViewById(R.id.activity_user_edit_profile), UserEditProfileActivity.this);
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.user_edit_profile_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Boolean formValidation() {
        Boolean validationResult = true;
        if (strFirstName.length() == 0) {
            edtFirstName.setError("First Name cannot be empty.");
            edtFirstName.requestFocus();
            validationResult = false;
        } else if (strLastName.length() == 0) {
            edtLastName.setError("Last Name cannot be empty.");
            edtLastName.requestFocus();
            validationResult = false;
        } else if (strEmail.length() == 0) {
            edtEmail.setError("E-mail cannot be empty.");
            edtEmail.requestFocus();
            validationResult = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()) {
            edtEmail.setError("Enter Valid E-mail");
            edtEmail.requestFocus();
            validationResult = false;
        } else if (strMobileNo.length() == 0) {
            edtMobileNo.setError("Mobile No. cannot be empty.");
            edtMobileNo.requestFocus();
            validationResult = false;
        } /*else if (strMobileNo.length() < 10) {
            edtMobileNo.setError("Please enter valid mobile number");
            edtMobileNo.requestFocus();
            validationResult = false;
        }*/
        return validationResult;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FROM_CAMERA && resultCode == RESULT_OK) {
            Bitmap thumbnail;
            if (resultCode == Activity.RESULT_OK) {
                try {
                    thumbnail = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), imageUri);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;
                    //imgView.setImageBitmap(thumbnail);
                    int height = thumbnail.getHeight(), width = thumbnail.getWidth();

                    if (height > 1280 && width > 960) {
                        Bitmap imgbitmap = BitmapFactory.decodeFile(getRealPathFromURI(imageUri), options);
                        //mCropView.setImageBitmap(imgbitmap);
                        ((ApplicationController) getApplication()).img = imgbitmap;
                        Intent i = new Intent(context, CropActivity.class);

                        startActivityForResult(i, 555);
                        System.out.println("Need to resize");

                    } else {
                        ((ApplicationController) getApplication()).img = thumbnail;
                        Intent i = new Intent(context, CropActivity.class);
                        startActivityForResult(i, 555);
                        //mCropView.setImageBitmap(thumbnail);
                        System.out.println("WORKS");
                    }
                    //mCropView.setImageBitmap(thumbnail);
                    //imageurl = getRealPathFromURI(imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            //Bitmap thumbnail = (Bitmap) result.getExtras().get("data");
        } else if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
            //gallery
            Uri selectedImageUri = data.getData();
            Log.e(TAG, "Uri : " + selectedImageUri.toString());
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            if (Build.VERSION.SDK_INT < 19) {
                selectedImagePath = getRealPathFromURI(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                int height = bitmap.getHeight(), width = bitmap.getWidth();
                if (height > 1280 && width > 960) {
                    Bitmap imgbitmap = BitmapFactory.decodeFile(getRealPathFromURI(selectedImageUri), options);
                    // mCropView.setImageBitmap(imgbitmap);
                    ((ApplicationController) getApplication()).img = imgbitmap;
                    Intent i = new Intent(context, CropActivity.class);
                    startActivityForResult(i, 555);
                    System.out.println("Need to resize");

                } else {
                    ((ApplicationController) getApplication()).img = bitmap;
                    Intent i = new Intent(context, CropActivity.class);
                    startActivityForResult(i, 555);
                    //mCropView.setImageBitmap(bitmap);
                    System.out.println("WORKS");
                }

            } else {
                ParcelFileDescriptor parcelFileDescriptor;
                try {
                    parcelFileDescriptor = getContentResolver().openFileDescriptor(selectedImageUri, "r");
                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                    parcelFileDescriptor.close();
                    int height = image.getHeight(), width = image.getWidth();
                    if (height > 1280 && width > 960) {
                        Bitmap imgbitmap = BitmapFactory.decodeFile(getRealPathFromURI1(selectedImageUri), options);
                        ((ApplicationController) getApplication()).img = imgbitmap;
                        Intent i = new Intent(context, CropActivity.class);
                        startActivityForResult(i, 555);

                        System.out.println("Need to resize");

                    } else {
                        ((ApplicationController) getApplication()).img = image;
                        Intent i = new Intent(context, CropActivity.class);
                        startActivityForResult(i, 555);
                        System.out.println("WORKS");
                    }

                    //mCropView.setImageBitmap(image);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else if (requestCode == 555 & resultCode == RESULT_OK) {
            //after cropping result.
            Log.e(TAG, "Result ok code 555");
            pic_changed = true;
            Bitmap bmp = ((ApplicationController) getApplication()).cropped;
            savePicture(bmp);
            imgProfile.setImageBitmap(ImgUtils.createCircleBitmap(bmp));

        }
    }

    //get image path for api level above 19
    private String getRealPathFromURI1(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    //get image path for api level 19 and below
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void savePicture(Bitmap bitmap) {
        FileOutputStream out = null;
        try {

            if (createDir(dirPath)) {
                out = new FileOutputStream(dirPath + File.separator + "profile.jpg");
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                selectedImagePath = dirPath + File.separator + "profile.jpg";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Boolean createDir(String path) {
        File folder = new File(path);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            return true;
        } else {
            return false;
        }
    }

    private boolean hasImage(@NonNull ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable) drawable).getBitmap() != null;
        }

        return hasImage;
    }

    public void getCountryCode() {
        ArrayList<String> params = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        new ParseJSON(context, WebServiceUrl.ServiceUrl + WebServiceUrl.getcountrycode, params, values, CountryPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    CountryPOJO countryObj = (CountryPOJO) obj;
                    if (countryObj.isStatus()) {

                        arrCountries.addAll(countryObj.getCountry());
                        adapterCode.notifyDataSetChanged();

                    }
                }else {
                    Toast.makeText(UserEditProfileActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    public void editProfileWithPic() {
        final ProgressDialog pd = new ProgressDialog(context, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
        pd.setMessage("Please Wait...");
        pd.setCancelable(false);
        pd.show();
        Ion.with(UserEditProfileActivity.this)
                .load(WebServiceUrl.ServiceUrl + WebServiceUrl.usereditprofile)
                .setMultipartParameter("userId", String.valueOf(PrefsUtil.with(UserEditProfileActivity.this).readInt("uId")))
                .setMultipartParameter("firstName", edtFirstName.getText().toString())
                .setMultipartParameter("lastName", edtLastName.getText().toString())
                .setMultipartParameter("mobileNo", edtMobileNo.getText().toString())
                .setMultipartParameter("countryCode", String.valueOf(arrCountries.get(spinnerCountryCode.getSelectedItemPosition()).getCountryCode()))
                .setMultipartParameter("paymentMethod", "c")
                .setMultipartFile("userProfileImage", "image/jpeg", new File(selectedImagePath))
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        pd.dismiss();
                        if (e == null) {
                            String service_result = result.toString();
                            Log.e("EditProfileActivity", "result : " + service_result);
                            try {
                                JSONObject obj = new JSONObject(service_result);
                                String status = obj.getString("status");
                                if (status.equals("true")) {
                                    //EventBus.getDefault().post(new MessageEvent("EditBuyerProfileActivity"));
                                    if (from.equalsIgnoreCase("signup")) {
                                        registerDeviceFromSocial();
                                        /*Intent intent = new Intent(UserEditProfileActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish();*/
                                    } else {
                                        Intent resultIntent = new Intent();
                                        setResult(RESULT_OK, resultIntent);
                                        finish();
                                    }
                                    Toast.makeText(UserEditProfileActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                                } else if (status.equals("false")) {
                                    Toast.makeText(UserEditProfileActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        } else {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void editProfile() {
        final ProgressDialog pd = new ProgressDialog(context, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
        pd.setMessage("Please Wait...");
        pd.setCancelable(false);
        pd.show();
        Ion.with(UserEditProfileActivity.this)
                .load(WebServiceUrl.ServiceUrl + WebServiceUrl.usereditprofile)
                .setMultipartParameter("userId", String.valueOf(PrefsUtil.with(UserEditProfileActivity.this).readInt("uId")))
                .setMultipartParameter("firstName", edtFirstName.getText().toString())
                .setMultipartParameter("lastName", edtLastName.getText().toString())
                .setMultipartParameter("mobileNo", edtMobileNo.getText().toString())
                .setMultipartParameter("paymentMethod", "c")
                .setMultipartParameter("countryCode", String.valueOf(arrCountries.get(spinnerCountryCode.getSelectedItemPosition()).getCountryCode()))
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        pd.dismiss();
                        if (e == null) {
                            String service_result = result.toString();
                            Log.e("EditProfileActivity", "result : " + service_result);
                            try {
                                JSONObject obj = new JSONObject(service_result);
                                String status = obj.getString("status");
                                if (status.equals("true")) {
                                    //EventBus.getDefault().post(new MessageEvent("EditBuyerProfileActivity"));
                                    if (from.equalsIgnoreCase("signup")) {
                                        registerDeviceFromSocial();
                                        /*Intent intent = new Intent(UserEditProfileActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish();*/
                                    } else {
                                        Intent resultIntent = new Intent();
                                        setResult(RESULT_OK, resultIntent);
                                        finish();
                                    }
                                    Toast.makeText(UserEditProfileActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                                } else if (status.equals("false")) {
                                    Toast.makeText(UserEditProfileActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        } else {
                            e.printStackTrace();
                        }


                    }
                });
    }

    private void registerDeviceFromSocial() {
        String url = WebServiceUrl.ServiceUrl + WebServiceUrl.registerdevice;
        ArrayList<String> params = new ArrayList<>();
        params.add("userId");
        params.add("userType");
        params.add("deviceId");
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(PrefsUtil.with(UserEditProfileActivity.this).readInt("uId")));
        values.add("u");
        values.add(FirebaseInstanceId.getInstance().getToken());
        new ParseJSON(UserEditProfileActivity.this, url, params, values, CommonPOJO.class, new ParseJSON.OnResultListner() {
            @Override
            public void onResult(boolean status, Object obj) {
                if (status) {
                    Intent intent;
                    intent = new Intent(UserEditProfileActivity.this, HomeActivity.class);
                    PrefsUtil.with(UserEditProfileActivity.this).write("completeProfile", true);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(UserEditProfileActivity.this, (String) obj, Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        try {
            if (event.getType().equalsIgnoreCase("connection")) {
                if (event.getMessage().equalsIgnoreCase("disconnected")) {
                    if (!(new ConnectionCheck().isNetworkConnected(UserEditProfileActivity.this))) {
                        Log.e("RideDetailActivity", "disconnected");
                        if (!ApplicationController.isOnline) {
                            if (PrefsUtil.isInternetConnectedShowing) {
                                if (PrefsUtil.dialogInternetConnected != null) {
                                    PrefsUtil.dialogInternetConnected.dismiss();
                                    PrefsUtil.isInternetConnectedShowing = false;
                                }
                            }
                            final Dialog d = new Dialog(UserEditProfileActivity.this,
                                    android.R.style.Theme_Light_NoTitleBar);
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
}
