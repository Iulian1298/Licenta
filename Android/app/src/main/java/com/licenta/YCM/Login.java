package com.licenta.YCM;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.licenta.YCM.activities.AuthenticationActivity;


import java.util.concurrent.ExecutionException;


public class Login implements Authentication {
    private static final String TAG = "Login";
    private Context mCtx;
    private SharedPreferencesManager mPreferencesManager;
    private String mLoginUrl;

    private EditText mEmail;
    private EditText mPassword;
    private View mLoginView;
    private TextView mLoginTitle;

    public Login(Context ctx) {
        Log.i(TAG, "Login: Create login worker");
        mCtx = ctx;
        mLoginUrl = "http://10.0.2.2:5000/login";
        mPreferencesManager = SharedPreferencesManager.getInstance(mCtx);

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
    }

    @Override
    public void performAuth() {
        Log.i(TAG, "auth: Login");
        final AlertDialog loginPopUp = new AlertDialog.Builder(mCtx)
                .setCustomTitle(mLoginTitle)
                .setView(mLoginView)
                .setPositiveButton("Confirma", null)
                .setNegativeButton("Anuleaza", null)
                .create();
        loginPopUp.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button confirm = loginPopUp.getButton(AlertDialog.BUTTON_POSITIVE);
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "onClick: alert dialog login");
                        if (verifyInputOnClientSide()) {
                            try {
                                if (verifyInputOnServerSide()) {
                                    loginPopUp.dismiss();
                                    ((AuthenticationActivity) mCtx).finish();
                                } else {
                                    mPassword.setText("");
                                    confirm.setError("Parola sau email gresit!");
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
        loginPopUp.show();
    }

    private boolean verifyInputOnServerSide() throws ExecutionException, InterruptedException {
        Log.i(TAG, "verifyInputOnServerSide: Login");
        boolean resultOk = true;
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("email", mEmail.getText().toString().trim());
        jsonBody.addProperty("password", mPassword.getText().toString().trim());
        Response<JsonObject> response = Ion.with(mCtx)
                .load("POST", mLoginUrl)
                .setJsonObjectBody(jsonBody)
                .asJsonObject()
                .withResponse()
                .get();
        if (response.getHeaders().code() == 200) {
            System.out.println(response.getResult());
            try {
                mPreferencesManager.setToken(response.getResult().get("token").getAsString());
                mPreferencesManager.setImage(response.getResult().get("user").getAsJsonObject().get("imageEncoded").getAsString());
                mPreferencesManager.setUsername(response.getResult().get("user").getAsJsonObject().get("fullName").getAsString());
                mPreferencesManager.setUserId(response.getResult().get("user").getAsJsonObject().get("id").getAsString());
                mPreferencesManager.setUserMail(response.getResult().get("user").getAsJsonObject().get("email").getAsString());
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
            mEmail.setError("Completeaza campul!");
            resultOk = false;
        } else {
            if (!Patterns.EMAIL_ADDRESS.matcher(mEmail.getText().toString().trim()).matches()) {
                mEmail.setError("Adresa nevalida!");
                resultOk = false;
            }
        }
        if (mPassword.getText().toString().trim().isEmpty()) {
            mPassword.setError("Completeaza campul!");
            resultOk = false;
        }
        return resultOk;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {

    }
/*
    private boolean verifyInputOnServerSide1() throws ExecutionException, InterruptedException {
        Log.i(TAG, "verifyInputOnServerSide: Login");
        boolean resultOk = true;
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("email", mEmail.getText().toString().trim());
        jsonBody.addProperty("password", mPassword.getText().toString().trim());

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create("application/json; charset=utf-8", MediaType.parse(jsonBody.toString()));
        Request request = new Request.Builder()
                .url(mLoginUrl)
                .post(body)
                .build();
        try {
            okhttp3.Response response = client.newCall(request).execute();
            System.out.println(response.body().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultOk;
    }*/
}
