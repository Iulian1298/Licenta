package com.licenta.YCM;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.licenta.YCM.activities.AuthenticationActivity;
import com.licenta.YCM.activities.HomeActivity;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;

public class Register implements Authentication {
    private static final String TAG = "Register";
    private Context mCtx;
    private String mRegisterUrl;

    private EditText mEmail;
    private EditText mPassword;
    private EditText mRePassword;
    private EditText mFullName;
    private ImageView mProfileImage;
    private View mRegisterView;
    private TextView mRegisterTitle;
    private TextView mPhoneNumber;
    private AlertDialog mRegisterPopUp;
    private SharedPreferencesManager mPreferencesManager;
    private boolean mUseDefaultPhoto;

    public Register(Context ctx) {
        Log.i(TAG, "Register: Create register worker");
        mCtx = ctx;
        mRegisterUrl = "http://10.0.2.2:5000/register";
        mPreferencesManager = SharedPreferencesManager.getInstance(mCtx);

        LayoutInflater inflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRegisterView = inflater.inflate(R.layout.register_popup_layout, null);
        mEmail = mRegisterView.findViewById(R.id.registerPopUpEmail);
        mPhoneNumber = mRegisterView.findViewById(R.id.registerPopUpPhone);
        mPassword = mRegisterView.findViewById(R.id.registerPopUpPassword);
        mRePassword = mRegisterView.findViewById(R.id.registerPopUpRePassword);
        mFullName = mRegisterView.findViewById(R.id.registerPopUpFullName);
        mProfileImage = mRegisterView.findViewById(R.id.registerPopUpImage);
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(mCtx, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    ((AuthenticationActivity) mCtx).startActivityForResult(galleryIntent, 2);
                } else {
                    Toast.makeText((AuthenticationActivity) mCtx, "Mai intai permite aplicatiei de a accesa galeria!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mRegisterTitle = new TextView(mCtx);
        mRegisterTitle.setText("Register");
        mRegisterTitle.setGravity(Gravity.CENTER);
        mRegisterTitle.setPadding(10, 10, 10, 10);
        mRegisterTitle.setTextSize(18);
        mRegisterTitle.setTextColor(Color.DKGRAY);
        mUseDefaultPhoto = true;
    }

    @Override
    public void performAuth() {
        Log.i(TAG, "auth: Register");

        mRegisterPopUp = new AlertDialog.Builder(mCtx)
                .setCustomTitle(mRegisterTitle)
                .setView(mRegisterView)
                .setPositiveButton("Confirma", null)
                .setNegativeButton("Anuleaza", null)
                .create();
        mRegisterPopUp.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button confirm = mRegisterPopUp.getButton(AlertDialog.BUTTON_POSITIVE);
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "onClick: alert dialog register");
                        if (verifyInputOnClientSide()) {
                            try {
                                if (verifyInputOnServerSide()) {
                                    mRegisterPopUp.dismiss();
                                    ((AuthenticationActivity) mCtx).finish();
                                } else {
                                    confirm.setError("Eroare!");
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
        mRegisterPopUp.show();
    }

    private boolean verifyInputOnServerSide() throws ExecutionException, InterruptedException {
        Log.i(TAG, "verifyInputOnServerSide: Register");
        boolean resultOk = true;
        JsonObject jsonBody = new JsonObject();
        if (mUseDefaultPhoto) {
            mProfileImage.setImageURI(Uri.parse("android.resource://" + mCtx.getPackageName() + "/drawable/" + "default_profile_image"));
        }
        BitmapDrawable drawable = (BitmapDrawable) mProfileImage.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String image = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
        jsonBody.addProperty("email", mEmail.getText().toString().trim());
        jsonBody.addProperty("phoneNumber", mPhoneNumber.getText().toString().trim());
        jsonBody.addProperty("password", mPassword.getText().toString().trim());
        jsonBody.addProperty("fullName", mFullName.getText().toString().trim());
        jsonBody.addProperty("imageEncoded", image);

        Response<JsonObject> response = Ion.with(mCtx)
                .load("POST", mRegisterUrl)
                .setJsonObjectBody(jsonBody)
                .asJsonObject()
                .withResponse()
                .get();
        if (response.getHeaders().code() == 201) {
            System.out.println(response.getResult());
            mPreferencesManager.setToken(response.getResult().get("token").getAsString());
            mPreferencesManager.setImage(image);
            mPreferencesManager.setUsername(response.getResult().get("user").getAsJsonObject().get("fullName").getAsString());
            mPreferencesManager.setUserId(response.getResult().get("user").getAsJsonObject().get("id").getAsString());
            mPreferencesManager.setUserMail(response.getResult().get("user").getAsJsonObject().get("email").getAsString());
        } else {
            if (response.getHeaders().code() == 409) {
                resultOk = false;
                Toast.makeText(mCtx, "Acest email a fost deja inregistrat!", Toast.LENGTH_SHORT).show();
            } else {
                resultOk = false;
                Toast.makeText(mCtx, "Error code: " + response.getHeaders().code(), Toast.LENGTH_SHORT).show();
            }
        }
        return resultOk;
    }

    private boolean verifyInputOnClientSide() {
        Log.i(TAG, "verifyInputOnClientSide: Register");
        boolean resultOk = true;
        if (mFullName.getText().toString().trim().isEmpty()) {
            mFullName.setError("Completeaza campul!");
            resultOk = false;
        }
        if (mEmail.getText().toString().trim().isEmpty()) {
            mEmail.setError("Completeaza campul!");
            resultOk = false;
        } else {
            if (!Patterns.EMAIL_ADDRESS.matcher(mEmail.getText().toString().trim()).matches()) {
                mEmail.setError("Adresa nevalida!");
                resultOk = false;
            }
        }
        if (mPhoneNumber.getText().toString().trim().isEmpty()) {
            mPhoneNumber.setError("Completeaza campul!");
            resultOk = false;
        } else {
            if (!Patterns.PHONE.matcher(mPhoneNumber.getText().toString().trim()).matches()) {
                mPhoneNumber.setError("Numar invalid!");
                resultOk = false;
            }
        }
        if (mPassword.getText().toString().trim().isEmpty()) {
            mPassword.setError("Completeaza campul!");
            resultOk = false;
        } else if (mPassword.getText().toString().trim().length() < 6) {
            mPassword.setError("Introdu minim 6 caractere!");
            resultOk = false;
        }
        if (mRePassword.getText().toString().trim().isEmpty()) {
            mRePassword.setError("Completeaza campul!");
            resultOk = false;
        }
        if (!mPassword.getText().toString().trim().equals(mRePassword.getText().toString().trim())) {
            mRePassword.setError("Parolele nu se potrivesc!");
            resultOk = false;
        }
        return resultOk;

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mRegisterPopUp.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        Log.i(TAG, "onActivityResult: ");
        if (result != null) {
            Uri chosenImageByUser = result.getData();
            mProfileImage.setImageURI(chosenImageByUser);
            mUseDefaultPhoto = false;
        }
    }
}
