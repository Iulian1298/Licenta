package com.licenta.YCM.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.licenta.YCM.Authentication;
import com.licenta.YCM.Login;
import com.licenta.YCM.R;
import com.licenta.YCM.Register;

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
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode,resultCode,result);
        mAuthenticationWorker.onActivityResult(requestCode,resultCode,result);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy: Destroy Activity Authentication");
        super.onDestroy();
    }
}
