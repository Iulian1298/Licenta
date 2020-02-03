package com.licenta.ServiceFinder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.licenta.ServiceFinder.activities.AuthenticationActivity;


import java.util.concurrent.ExecutionException;


public class Login implements Authentication {
    private static final String TAG = "Login";
    private Context mCtx;
    private SharedPreferencesManager mPreferencesManager;
    private String mUrl;

    private EditText mEmail;
    private EditText mPassword;
    private View mLoginView;
    private TextView mLoginTitle;
    private TextView mWrongCredidentials;
    private AlertDialog mLoginPopUp;

    public Login(Context ctx) {
        Log.i(TAG, "Login: Create login worker");
        mCtx = ctx;
        mPreferencesManager = SharedPreferencesManager.getInstance(mCtx);
        mUrl = mPreferencesManager.getServerUrl();


        LayoutInflater inflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLoginView = inflater.inflate(R.layout.login_popup_layout, null);
        mEmail = mLoginView.findViewById(R.id.loginPopUpEmail);
        mPassword = mLoginView.findViewById(R.id.loginPopUpPassword);
        mLoginTitle = new TextView(mCtx);
        mLoginTitle.setText("Login");
        mLoginTitle.setGravity(Gravity.CENTER);
        mLoginTitle.setPadding(10, 10, 10, 10);
        mLoginTitle.setTextSize(18);
        mLoginTitle.setTextColor(Color.DKGRAY);
        mWrongCredidentials = mLoginView.findViewById(R.id.wrongCredidentials);
        mWrongCredidentials.setVisibility(View.GONE);
    }

    @Override
    public void performAuth() {
        Log.i(TAG, "auth: Login");
        mLoginPopUp = new AlertDialog.Builder(mCtx)
                .setCustomTitle(mLoginTitle)
                .setView(mLoginView)
                .setPositiveButton("Confirmă", null)
                .setNegativeButton("Anulează", null)
                .create();
        mLoginPopUp.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button confirm = mLoginPopUp.getButton(AlertDialog.BUTTON_POSITIVE);
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "onClick: alert dialog login");
                        if (verifyInputOnClientSide()) {
                            try {
                                if (verifyInputOnServerSide()) {
                                    mLoginPopUp.dismiss();
                                    ((AuthenticationActivity) mCtx).finish();
                                } else {
                                    mPassword.setText("");
                                    mWrongCredidentials.setVisibility(View.VISIBLE);
                                }
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
        mLoginPopUp.show();
    }

    private boolean verifyInputOnServerSide() throws ExecutionException, InterruptedException {
        Log.i(TAG, "verifyInputOnServerSide: Login");
        boolean resultOk = true;
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("email", mEmail.getText().toString().trim());
        jsonBody.addProperty("password", mPassword.getText().toString().trim());
        Response<JsonObject> response = Ion.with(mCtx)
                .load("POST", mUrl + "/login")
                .setJsonObjectBody(jsonBody)
                .asJsonObject()
                .withResponse()
                .get();
        if (response.getHeaders().code() == 200) {
            System.out.println(response.getResult());
            try {
                mPreferencesManager.setToken(response.getResult().get("token").getAsString());
                mPreferencesManager.setImage(response.getResult().get("user").getAsJsonObject().get("imageUrl").getAsString());
                mPreferencesManager.setUsername(response.getResult().get("user").getAsJsonObject().get("fullName").getAsString());
                mPreferencesManager.setUserId(response.getResult().get("user").getAsJsonObject().get("id").getAsString());
                mPreferencesManager.setUserMail(response.getResult().get("user").getAsJsonObject().get("email").getAsString());
                mPreferencesManager.setUserPhone(response.getResult().get("user").getAsJsonObject().get("phoneNumber").getAsString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (response.getHeaders().code() == 401) {
                resultOk = false;
                Toast.makeText(mCtx, "Parola sau email gresit!", Toast.LENGTH_SHORT).show();
            } else {
                resultOk = false;
                Toast.makeText(mCtx, "Error code: " + response.getHeaders().code(), Toast.LENGTH_SHORT).show();
            }
        }
        return resultOk;
    }

    private boolean verifyInputOnClientSide() {
        Log.i(TAG, "verifyInputOnClientSide: Login");
        boolean resultOk = true;
        if (mEmail.getText().toString().trim().isEmpty()) {
            mEmail.setError("Completează campul!");
            resultOk = false;
        } else {
            if (!Patterns.EMAIL_ADDRESS.matcher(mEmail.getText().toString().trim()).matches()) {
                mEmail.setError("Adresă nevalidă!");
                resultOk = false;
            }
        }
        if (mPassword.getText().toString().trim().isEmpty()) {
            mPassword.setError("Completează campul!");
            resultOk = false;
        }
        return resultOk;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mLoginPopUp.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {

    }
}
