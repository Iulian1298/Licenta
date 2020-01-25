package com.licenta.YCM.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.licenta.YCM.R;
import com.licenta.YCM.SharedPreferencesManager;
import com.licenta.YCM.fragments.HomeFragment;
import com.licenta.YCM.fragments.MyRequestFragment;
import com.licenta.YCM.fragments.MyRequestsAndMyAppointmentsFragment;

import android.support.design.widget.NavigationView;
import android.util.Patterns;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private SharedPreferencesManager mPreferencesManager;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private LocationManager mLocationManager;
    private Context mCtx;
    private LocationListener mLocationListener;
    private AlertDialog mEditMyProfilePopUp;
    private ImageView mEditImage;
    private EditText mEditEmail;
    private EditText mEditPhone;
    private EditText mEditFullName;
    private EditText mEditOldPassword;
    private EditText mEditNewPassword;
    private EditText mEditNewRePassword;
    private Button mConfirm;
    private Button mCancel;
    private ProgressBar mEditMyProfileProgressBar;
    private LinearLayout mEditMyProfileLinearLayout;
    private String mUrl;
    private boolean mMenuItemSelected;
    private int mSelectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: Start-up");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mCtx = getApplicationContext();
        mPreferencesManager = SharedPreferencesManager.getInstance(mCtx);
        init();
        //set startup fragment

        getSupportActionBar().setTitle("Acasă");
        mPreferencesManager.setOnlyMyServices(false);
        getSupportFragmentManager().beginTransaction().replace(R.id.homeContainer, new HomeFragment(), "Acasă").commit();

        requestLocationPermission();
    }

    private void init() {
        Log.i(TAG, "init: ");
        mSelectedItem = 0;
        mMenuItemSelected = false;
        mUrl = mPreferencesManager.getServerUrl();
        mPreferencesManager.setPermissionLocation(false);
        mPreferencesManager.setUserLatitude(-1);
        mPreferencesManager.setUserLongitude(-1);
        mCtx = getApplicationContext();
        mToolbar = findViewById(R.id.homeToolbar);
        mDrawerLayout = findViewById(R.id.drawerLayout);
        setSupportActionBar(mToolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {
            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
                if (mMenuItemSelected) {
                    mMenuItemSelected = false;
                    switch (mSelectedItem) {
                        case 0:
                            getSupportActionBar().setTitle("Acasă");
                            mPreferencesManager.setOnlyMyServices(false);
                            getSupportFragmentManager().beginTransaction().replace(R.id.homeContainer, new HomeFragment(), "Acasa").commit();
                            break;
                        case 1:
                            getSupportActionBar().setTitle("Service-urile mele");
                            mPreferencesManager.setOnlyMyServices(true);
                            getSupportFragmentManager().beginTransaction().replace(R.id.homeContainer, new HomeFragment()).commit();
                            break;
                        case 2:
                            getSupportActionBar().setTitle("Cererile și programările mele");
                            getSupportFragmentManager().beginTransaction().replace(R.id.homeContainer, new MyRequestsAndMyAppointmentsFragment()).commit();
                            break;
                        case 3:
                            logout();
                            break;
                        case 4:
                            Intent intent = new Intent(mCtx, AuthenticationActivity.class);
                            startActivityForResult(intent, 3);
                            break;
                        default:
                            break;
                    }
                }
            }

            @Override
            public void onDrawerStateChanged(int i) {
            }
        });
        mNavigationView = findViewById(R.id.navigationView);
        mLocationManager = (LocationManager) mCtx.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i(TAG, "onLocationChanged: latitude: " + location.getLatitude() + " longitude: " + location.getLongitude());
                mPreferencesManager.setUserLatitude((float) location.getLatitude());
                mPreferencesManager.setUserLongitude((float) location.getLongitude());
                //TextView test = mNavigationView.getHeaderView(0).findViewById(R.id.test);
                //test.setText("Location: lat: " + location.getLatitude() + " long: " + location.getLongitude());
                HomeFragment fragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("Acasă");
                if (fragment != null) {
                    fragment.refreshLocation();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    private void setNavigationView() {
        Log.i(TAG, "setNavigationView: ");
        try {
            boolean loginStatus = mPreferencesManager.isLoggedIn();
            setHeaderHomeMenu(loginStatus);
            setHomeMenu(loginStatus);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setHomeMenu(boolean loginStatus) {
        Log.i(TAG, "setHomeMenu: ");
        Menu menu = mNavigationView.getMenu();
        MenuItem logoutButton = menu.findItem(R.id.menuLogout);
        MenuItem authButton = menu.findItem(R.id.menuAuth);
        MenuItem myRequestOfferButton = menu.findItem(R.id.menuMyRequestOffer);
        MenuItem myServiceButton = menu.findItem(R.id.menuMyServices);
        if (!loginStatus) {
            logoutButton.setVisible(false);
            authButton.setVisible(true);
            myRequestOfferButton.setVisible(false);
            myServiceButton.setVisible(false);
        } else {
            logoutButton.setVisible(true);
            authButton.setVisible(false);
            myRequestOfferButton.setVisible(true);
            myServiceButton.setVisible(true);
        }
    }

    private void setHeaderHomeMenu(final boolean loginStatus) {
        Log.i(TAG, "setHeaderHomeMenu: ");
        View headerView = mNavigationView.getHeaderView(0);
        TextView headerMenuUsername = headerView.findViewById(R.id.headerMenuUsername);
        TextView headerMenuUserMail = headerView.findViewById(R.id.headerMenuUserMail);
        ImageView headerMenuUserImage = headerView.findViewById(R.id.headerMenuUserImage);
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: edit profile");

                if (loginStatus) {
                    editProfile();
                }

            }
        });

        if (!loginStatus) {
            headerMenuUserImage.setImageURI(Uri.parse("android.resource://com.licenta.YCM/drawable/" + mPreferencesManager.getImage()));
            headerMenuUserMail.setVisibility(View.GONE);
            headerMenuUsername.setVisibility(View.GONE);
        } else {
            Glide.with(mCtx)
                    .asBitmap()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .load(Uri.parse(mPreferencesManager.getImage()))
                    .into(headerMenuUserImage);
            headerMenuUserMail.setVisibility(View.VISIBLE);
            headerMenuUsername.setVisibility(View.VISIBLE);
            headerMenuUsername.setText(mPreferencesManager.getUsername());
            headerMenuUserMail.setText(mPreferencesManager.getUserMail());
        }
    }

    private void setNavigationDrawer() {
        Log.i(TAG, "setNavigationDrawer: ");
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                mMenuItemSelected = true;
                switch (menuItem.getItemId()) {
                    case R.id.menuHome:
                        mSelectedItem = 0;
                        break;
                    case R.id.menuMyServices:
                        mSelectedItem = 1;
                        break;
                    case R.id.menuMyRequestOffer:
                        mSelectedItem = 2;
                        break;
                    case R.id.menuLogout:
                        mSelectedItem = 3;
                        break;
                    case R.id.menuAuth:
                        mSelectedItem = 4;
                        break;
                    default:
                        break;
                }
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult: setNavigationView");

        if (requestCode == 2) {
            if (data != null) {
                Uri chosenImageByUser = data.getData();
                mEditImage.setImageURI(chosenImageByUser);
            }
        } else {
            setNavigationView();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Bitmap stringToBitmap(String imageEncoded) {
        Log.i(TAG, "stringToBitmap: ");
        try {
            String cleanImage = imageEncoded.
                    replace("dataimage/pngbase64", "").
                    replace("dataimage/jpegbase64", "");
            //Log.i(TAG, "stringToBitmap: " + cleanImage);
            byte[] encodeByte = Base64.decode(cleanImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    private void logout() {
        Log.i(TAG, "logout: ");
        mPreferencesManager.logout();
        setNavigationView();
        getSupportActionBar().setTitle("Acasă");
        mPreferencesManager.setOnlyMyServices(false);
        getSupportFragmentManager().beginTransaction().replace(R.id.homeContainer, new HomeFragment(), "Acasă").commit();
        //ToDo: same as onActivityResult() or not(need investigation)
    }

    private void requestLocationPermission() {
        Log.i(TAG, "requestLocationPermission: ");
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        if (result != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "requestLocationPermission: request permission");
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 11);
        } else {
            Log.i(TAG, "requestLocationPermission: permission already granted");
            mPreferencesManager.setPermissionLocation(true);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 50, mLocationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        String message = "";
        if (requestCode == 11) {
            message = "Permite aplicației să folosească locația pentru a vedea distanța față de service-uri!";
        }
        if (requestCode == 11) {
            if (grantResults.length > 0) {
                boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (!locationAccepted) {
                    Log.i(TAG, "onRequestPermissionsResult: permission not accepted");
                    if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                        new AlertDialog.Builder(HomeActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.i(TAG, "onClick: positive");
                                        requestPermissions(new String[]{ACCESS_FINE_LOCATION}, 11);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .create()
                                .show();
                        mPreferencesManager.setPermissionLocation(false);
                    }
                } else {
                    Log.i(TAG, "onRequestPermissionsResult: apply changes");
                    mPreferencesManager.setPermissionLocation(true);
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, mLocationListener);
                    }
                }
            }
        }
    }

    private void setOnClearTextListeners(View editMyProfileView) {
        ImageView clearEditEmail = editMyProfileView.findViewById(R.id.clearEditEmail);
        ImageView clearEditPhone = editMyProfileView.findViewById(R.id.clearEditPhone);
        ImageView clearEditFullName = editMyProfileView.findViewById(R.id.clearEditFullName);
        ImageView clearEditOldPassword = editMyProfileView.findViewById(R.id.clearEditOldPassword);
        ImageView clearEditPassword = editMyProfileView.findViewById(R.id.clearEditPassword);
        ImageView clearEditPasswordRe = editMyProfileView.findViewById(R.id.clearEditPasswordRe);
        clearEditEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditEmail.setText("");
            }
        });
        clearEditPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditPhone.setText("");
            }
        });
        clearEditFullName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditFullName.setText("");
            }
        });
        clearEditOldPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditOldPassword.setText("");
            }
        });
        clearEditPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditNewPassword.setText("");
            }
        });
        clearEditPasswordRe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditNewRePassword.setText("");
            }
        });
    }

    private void editProfile() {
        Log.i(TAG, "editProfile: ");
        TextView editMyProfileTitle = new TextView(mCtx);
        editMyProfileTitle.setText("Editează profilul!");
        editMyProfileTitle.setGravity(Gravity.CENTER);
        editMyProfileTitle.setPadding(10, 10, 10, 10);
        editMyProfileTitle.setTextSize(18);
        editMyProfileTitle.setTextColor(Color.DKGRAY);
        View editMyProfileView = getLayoutInflater().inflate(R.layout.edit_myprofile_popup_layout, null);
        mEditMyProfileProgressBar = editMyProfileView.findViewById(R.id.editMyProfileProgressBar);
        mEditMyProfileLinearLayout = editMyProfileView.findViewById(R.id.editMyProfileLinearLayout);
        mEditImage = editMyProfileView.findViewById(R.id.editMyProfileImage);
        mEditEmail = editMyProfileView.findViewById(R.id.editMyProfileEmail);
        mEditPhone = editMyProfileView.findViewById(R.id.editMyProfilePhone);
        mEditFullName = editMyProfileView.findViewById(R.id.editMyProfileFullName);
        mEditOldPassword = editMyProfileView.findViewById(R.id.editOldPassword);
        mEditNewPassword = editMyProfileView.findViewById(R.id.editPassword);
        mEditNewRePassword = editMyProfileView.findViewById(R.id.editPasswordRe);
        mEditEmail.setText(mPreferencesManager.getUserMail());
        mEditPhone.setText(mPreferencesManager.getUserPhone());
        mEditFullName.setText(mPreferencesManager.getUsername());
        setOnClearTextListeners(editMyProfileView);
        Glide.with(mCtx)
                .asBitmap()
                //.diskCacheStrategy(DiskCacheStrategy.NONE)
                .load(mPreferencesManager.getImage())
                .into(mEditImage);
        mEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(mCtx, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, 2);
                } else {
                    Toast.makeText(mCtx, "Mai intai permite aplicației să acceseze galeria!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mEditMyProfilePopUp = new AlertDialog.Builder(HomeActivity.this)
                .setCustomTitle(editMyProfileTitle)
                .setView(editMyProfileView)
                .setPositiveButton("Confirmă", null)
                .setNegativeButton("Anulează", null)
                .create();
        mEditMyProfilePopUp.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mConfirm = mEditMyProfilePopUp.getButton(DialogInterface.BUTTON_POSITIVE);
                mCancel = mEditMyProfilePopUp.getButton(DialogInterface.BUTTON_NEGATIVE);
                mConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "onClick: Confirm edit");
                        if (verifyInputOnClientSide()) {
                            mEditMyProfileProgressBar.setVisibility(View.VISIBLE);
                            mEditMyProfileLinearLayout.setBackground(new ColorDrawable(Color.parseColor("#75676767")));
                            if (verifyInputOnServerSide()) {
                            } else {
                                mConfirm.setError("Eroare!");
                            }
                        }
                    }
                });
            }
        });
        mEditMyProfilePopUp.show();
    }

    private boolean verifyInputOnClientSide() {
        Log.i(TAG, "verifyInputOnClientSide: ");
        boolean resultOk = true;
        if (mEditFullName.getText().toString().trim().isEmpty()) {
            mEditFullName.setError("Completează campul!");
            resultOk = false;
        }
        if (mEditEmail.getText().toString().trim().isEmpty()) {
            mEditEmail.setError("Completează campul!");
            resultOk = false;
        } else {
            if (!Patterns.EMAIL_ADDRESS.matcher(mEditEmail.getText().toString().trim()).matches()) {
                mEditEmail.setError("Adresă nevalidă!");
                resultOk = false;
            }
        }
        if (mEditPhone.getText().toString().trim().isEmpty()) {
            mEditPhone.setError("Completează campul!");
            resultOk = false;
        } else {
            if (!Patterns.PHONE.matcher(mEditPhone.getText().toString().trim()).matches()) {
                mEditPhone.setError("Numar invalid!");
                resultOk = false;
            }
        }
        if (mEditOldPassword.getText().toString().trim().isEmpty()) {
            mEditOldPassword.setError("Completează campul!");
            resultOk = false;
        } else if (mEditOldPassword.getText().toString().trim().length() < 6) {
            mEditOldPassword.setError("Introdu minim 6 caractere!");
            resultOk = false;
        }
        if (mEditNewPassword.getText().toString().trim().isEmpty()) {
        } else {
            if (mEditNewPassword.getText().toString().trim().length() < 6) {
                mEditNewPassword.setError("Introdu minim 6 caractere!");
                resultOk = false;
            }
            if (!mEditNewRePassword.getText().toString().trim().equals(mEditNewRePassword.getText().toString().trim())) {
                mEditNewRePassword.setError("Parolele nu se potrivesc!");
                resultOk = false;
            }
        }
        return resultOk;
    }

    private boolean verifyInputOnServerSide() {
        Log.i(TAG, "verifyInputOnServerSide: verify edit profile");
        setEnableFields(false);
        boolean resultOk = true;
        final JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("userId", mPreferencesManager.getUserId());
        jsonBody.addProperty("newUserFullname", mEditFullName.getText().toString().trim());
        jsonBody.addProperty("newUserEmail", mEditEmail.getText().toString().trim());
        jsonBody.addProperty("newUserPhone", mEditPhone.getText().toString().trim());
        jsonBody.addProperty("userOldPassword", mEditOldPassword.getText().toString().trim());
        jsonBody.addProperty("userNewPassword", mEditNewPassword.getText().toString().trim());

        Bitmap bitmap = ((BitmapDrawable) mEditImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        Log.i(TAG, "verifyInputOnServerSide: getLastPathSegment: " + Uri.parse(mPreferencesManager.getImage()).getLastPathSegment());
        final StorageReference newImagePath = FirebaseStorage.getInstance().getReference().child(Uri.parse(mPreferencesManager.getImage()).getLastPathSegment());
        newImagePath.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                newImagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        final String newImageDownloadLink = uri.toString();
                        jsonBody.addProperty("newImagePath", newImageDownloadLink);
                        Response<JsonObject> response = null;
                        try {
                            response = Ion.with(mCtx)
                                    .load("PUT", mUrl + "/auth/changeProfile")
                                    .setHeader("Authorization", mPreferencesManager.getToken())
                                    .setJsonObjectBody(jsonBody)
                                    .asJsonObject()
                                    .withResponse()
                                    .get();
                            if (response.getHeaders().code() == 200) {

                                Log.i(TAG, "verifyInputOnServerSide: Profile edited");
                                Toast.makeText(mCtx, "Profil editat cu succes!", Toast.LENGTH_SHORT).show();
                                //updateUi check if need to update image on navView or will be auto
                                mPreferencesManager.setImage(newImageDownloadLink);
                                mPreferencesManager.setUserMail(mEditEmail.getText().toString().trim());
                                mPreferencesManager.setUserPhone(mEditPhone.getText().toString().trim());
                                mPreferencesManager.setUsername(mEditFullName.getText().toString().trim());
                                setNavigationView();
                                mEditMyProfileProgressBar.setVisibility(View.GONE);
                                mEditMyProfilePopUp.dismiss();
                            } else {
                                setEnableFields(true);
                                Log.i(TAG, "verifyInputOnServerSide: Profile not edited! err code: " + response.getHeaders().code());
                                Toast.makeText(mCtx, "Ceva nu a mers! Verifică conexiunea la internet!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "onFailure: fail to get download link");
                        Toast.makeText(mCtx, "Ceva nu a mers! Verifică conexiunea la internet!", Toast.LENGTH_SHORT).show();
                        setEnableFields(true);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: fail to get download link");
                Toast.makeText(mCtx, "Ceva nu a mers! Verifică conexiunea la internet!", Toast.LENGTH_SHORT).show();
                setEnableFields(true);
            }
        });


        return resultOk;
    }

    private void setEnableFields(boolean value) {
        mEditImage.setEnabled(value);
        mEditEmail.setEnabled(value);
        mEditPhone.setEnabled(value);
        mEditFullName.setEnabled(value);
        mEditOldPassword.setEnabled(value);
        mEditNewPassword.setEnabled(value);
        mEditNewRePassword.setEnabled(value);
        mEditEmail.setEnabled(value);
        mEditPhone.setEnabled(value);
        mEditFullName.setEnabled(value);
        mConfirm.setEnabled(value);
        mCancel.setEnabled(value);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "Resuming");
        //requestLocationPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu: ");
        //getMenuInflater().inflate(R.menu.other_page_main_menu, menu);
        setNavigationView();
        setNavigationDrawer();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy() -> Destroy Activity Main");
        super.onDestroy();
    }
}
