package com.licenta.YCM.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.licenta.YCM.Authentication;
import com.licenta.YCM.Login;
import com.licenta.YCM.R;
import com.licenta.YCM.Register;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class AuthenticationActivity extends AppCompatActivity {

    private static final String TAG = "AuthenticationActivity";
    private Button mLoginButton;
    private Button mRegisterButton;
    private Authentication mAuthenticationWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: Create Activity Authentication");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        doInit();
        requestReadExternalStoragePermision();
    }

    private void requestReadExternalStoragePermision() {
        Log.i(TAG, "requestReadExternalStoragePermision: ");
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        if (result != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "requestLocationPermission: request permission");
            ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, 95);
        } else {
            Log.i(TAG, "requestLocationPermission: permission already granted");
        }
    }

    private void doInit() {
        Log.i(TAG, "doInit: ");
        mLoginButton = findViewById(R.id.loginButton);
        mRegisterButton = findViewById(R.id.registerButton);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: perform login");
                mAuthenticationWorker = new Login(AuthenticationActivity.this);
                mAuthenticationWorker.performAuth();
            }
        });
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: perform register");
                mAuthenticationWorker = new Register(AuthenticationActivity.this);
                mAuthenticationWorker.performAuth();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mAuthenticationWorker.onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        mAuthenticationWorker.onActivityResult(requestCode, resultCode, result);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy: Destroy Activity Authentication");
        super.onDestroy();
    }
}
