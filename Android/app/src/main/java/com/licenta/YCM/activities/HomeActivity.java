package com.licenta.YCM.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.licenta.YCM.R;
import com.licenta.YCM.SharedPreferencesManager;
import com.licenta.YCM.fragments.HomeFragment;
import com.licenta.YCM.fragments.MyRequestFragment;

import android.support.design.widget.NavigationView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mPreferencesManager = SharedPreferencesManager.getInstance(this);
        init();
        setNavigationView();
        setNavigationDrawer();
        //set startup fragment

        getSupportActionBar().setTitle("Acasa");
        mPreferencesManager.setOnlyMyServices(false);
        getSupportFragmentManager().beginTransaction().replace(R.id.homeContainer, new HomeFragment(),"Acasa").commit();

        requestLocationPermission();
    }

    private void init() {
        mPreferencesManager.setPermissionLocation(false);
        mCtx = getApplicationContext();
        mToolbar = findViewById(R.id.homeToolbar);
        mDrawerLayout = findViewById(R.id.drawerLayout);
        setSupportActionBar(mToolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView = findViewById(R.id.navigationView);
        mLocationManager = (LocationManager) mCtx.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i(TAG, "onLocationChanged: latitude: " + location.getLatitude() + " longitude: " + location.getLongitude());
                mPreferencesManager.setUserLatitude((float) location.getLatitude());
                mPreferencesManager.setUserLongitude((float) location.getLongitude());
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
            setHeaderHomeMenu();
            setHomeMenu();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setHomeMenu() throws ExecutionException, InterruptedException {
        Log.i(TAG, "setHomeMenu: ");
        Menu menu = mNavigationView.getMenu();
        MenuItem logoutButton = menu.findItem(R.id.menuLogout);
        MenuItem authButton = menu.findItem(R.id.menuAuth);
        MenuItem profileButton = menu.findItem(R.id.menuProfile);
        MenuItem myRequestOfferButton = menu.findItem(R.id.menuMyRequestOffer);
        MenuItem myServiceButton = menu.findItem(R.id.menuMyServices);
        if (!mPreferencesManager.isLoggedIn()) {
            logoutButton.setVisible(false);
            authButton.setVisible(true);
            profileButton.setVisible(false);
            myRequestOfferButton.setVisible(false);
            myServiceButton.setVisible(false);
        } else {
            logoutButton.setVisible(true);
            authButton.setVisible(false);
            profileButton.setVisible(true);
            myRequestOfferButton.setVisible(true);
            myServiceButton.setVisible(true);
        }
    }

    private void setHeaderHomeMenu() throws ExecutionException, InterruptedException {
        Log.i(TAG, "setHeaderHomeMenu: ");
        View headerView = mNavigationView.getHeaderView(0);
        TextView headerMenuUsername = headerView.findViewById(R.id.headerMenuUsername);
        TextView headerMenuUserMail = headerView.findViewById(R.id.headerMenuUserMail);
        ImageView headerMenuUserImage = headerView.findViewById(R.id.headerMenuUserImage);


        if (!mPreferencesManager.isLoggedIn()) {
            headerMenuUserImage.setImageURI(Uri.parse("android.resource://com.licenta.YCM/drawable/" + mPreferencesManager.getImage()));
            headerMenuUserMail.setVisibility(View.GONE);
            headerMenuUsername.setVisibility(View.GONE);
        } else {
            headerMenuUserImage.setImageBitmap(stringToBitmap(mPreferencesManager.getImage()));
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
                switch (menuItem.getItemId()) {
                    case R.id.menuHome:
                        getSupportActionBar().setTitle("Acasa");
                        mPreferencesManager.setOnlyMyServices(false);
                        getSupportFragmentManager().beginTransaction().replace(R.id.homeContainer, new HomeFragment(), "Acasa").commit();
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.menuProfile:
                        getSupportActionBar().setTitle("Profilul meu");
                        //getSupportFragmentManager().beginTransaction().replace(R.id.homeDoctorsContainer, new ProfileFragment()).commit();
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.menuMyServices:
                        getSupportActionBar().setTitle("Service-urile mele");
                        mPreferencesManager.setOnlyMyServices(true);
                        getSupportFragmentManager().beginTransaction().replace(R.id.homeContainer, new HomeFragment()).commit();
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.menuMyRequestOffer:
                        getSupportActionBar().setTitle("Cererile mele");
                        getSupportFragmentManager().beginTransaction().replace(R.id.homeContainer, new MyRequestFragment()).commit();
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.menuLogout:
                        logout();
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.menuAuth:
                        Intent intent = new Intent(mCtx, AuthenticationActivity.class);
                        startActivityForResult(intent, 3);
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                }
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult: setNavigationView");
        setNavigationView();
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
            HomeFragment fragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("Acasa");
            if (fragment != null) {
                fragment.refreshPage();
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, mLocationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        String message = "";
        if (requestCode == 11) {
            message = "Permite aplicatiei sa foloseasca locatia pentru a vede distanta fata de service-uri!";
        } else {
            message = "Permite aplicatiei sa foloseasca locatia pentru a seta locatia noului service!";
        }
        if (requestCode == 11 || requestCode == 12) {
            if (grantResults.length > 0) {
                boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (!locationAccepted) {
                    Log.i(TAG, "onRequestPermissionsResult: permision not accepted");
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
                    HomeFragment fragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("Acasa");
                    if (fragment != null) {
                        fragment.refreshPage();
                    }
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, mLocationListener);
                    }
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu: ");
        //getMenuInflater().inflate(R.menu.other_page_main_menu, menu);
        setNavigationView();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy() -> Destroy Activity Main");
        super.onDestroy();
    }
}
