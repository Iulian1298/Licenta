package com.licenta.ServiceFinder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.licenta.ServiceFinder.activities.AuthenticationActivity;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Register implements Authentication {
    private static final String TAG = "Register";
    private Context mCtx;
    private String mUrl;

    private EditText mEmail;
    private EditText mPassword;
    private EditText mRePassword;
    private EditText mFullName;
    private ImageView mProfileImage;
    private View mRegisterView;
    private TextView mRegisterTitle;
    private TextView mEmailRegistered;
    private TextView mPhoneNumber;
    private AlertDialog mRegisterPopUp;
    private SharedPreferencesManager mPreferencesManager;
    private boolean mUseDefaultPhoto;
    private Uri mUserImageUri;
    private Button confirm;
    private Button cancel;

    public Register(Context ctx) {
        Log.i(TAG, "Register: Create register worker");
        mCtx = ctx;
        mPreferencesManager = SharedPreferencesManager.getInstance(mCtx);
        mUrl = mPreferencesManager.getServerUrl();


        LayoutInflater inflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRegisterView = inflater.inflate(R.layout.register_popup_layout, null);
        mEmail = mRegisterView.findViewById(R.id.registerPopUpEmail);
        mPhoneNumber = mRegisterView.findViewById(R.id.registerPopUpPhone);
        mPassword = mRegisterView.findViewById(R.id.registerPopUpPassword);
        mRePassword = mRegisterView.findViewById(R.id.registerPopUpRePassword);
        mFullName = mRegisterView.findViewById(R.id.registerPopUpFullName);
        mProfileImage = mRegisterView.findViewById(R.id.registerPopUpImage);
        mEmailRegistered = mRegisterView.findViewById(R.id.emailRegistered);
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(mCtx, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    ((AuthenticationActivity) mCtx).startActivityForResult(galleryIntent, 2);
                } else {
                    Toast.makeText((AuthenticationActivity) mCtx, "Mai întai permite aplicației să acceseze galeria!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mRegisterTitle = new TextView(mCtx);
        mRegisterTitle.setText("Înregistrare");
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
                .setPositiveButton("Confirmă", null)
                .setNegativeButton("Anulează", null)
                .create();
        mRegisterPopUp.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                confirm = mRegisterPopUp.getButton(AlertDialog.BUTTON_POSITIVE);
                cancel = mRegisterPopUp.getButton(AlertDialog.BUTTON_NEGATIVE);
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "onClick: alert dialog register");
                        if (verifyInputOnClientSide()) {
                            try {
                                if (verifyInputOnServerSide()) {
                                } else {
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
        setEnableFields(false);
        Log.i(TAG, "verifyInputOnServerSide: Register");
        final boolean[] resultOk = {true};
        final JsonObject jsonBody = new JsonObject();
        if (mUseDefaultPhoto) {
            mProfileImage.setImageURI(Uri.parse("android.resource://" + mCtx.getPackageName() + "/drawable/" + "default_profile_image"));
            mUserImageUri = Uri.parse("android.resource://" + mCtx.getPackageName() + "/drawable/" + "default_profile_image");
        }
        jsonBody.addProperty("email", mEmail.getText().toString().trim());
        jsonBody.addProperty("phoneNumber", mPhoneNumber.getText().toString().trim());
        jsonBody.addProperty("password", mPassword.getText().toString().trim());
        jsonBody.addProperty("fullName", mFullName.getText().toString().trim());

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("user_image");
        final StorageReference imageFilePath = storageReference.child(UUID.randomUUID() + "_" + mUserImageUri.getLastPathSegment());
        imageFilePath.putFile(mUserImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageDownloadLink = uri.toString();
                        jsonBody.addProperty("imageDownloadLink", imageDownloadLink);

                        Response<JsonObject> response = null;
                        try {
                            response = Ion.with(mCtx)
                                    .load("POST", mUrl + "/register")
                                    .setJsonObjectBody(jsonBody)
                                    .asJsonObject()
                                    .withResponse()
                                    .get();

                            if (response.getHeaders().code() == 201) {
                                System.out.println(response.getResult());
                                mPreferencesManager.setToken(response.getResult().get("token").getAsString());
                                mPreferencesManager.setImage(imageDownloadLink);
                                mPreferencesManager.setUsername(response.getResult().get("user").getAsJsonObject().get("fullName").getAsString());
                                mPreferencesManager.setUserId(response.getResult().get("user").getAsJsonObject().get("id").getAsString());
                                mPreferencesManager.setUserMail(response.getResult().get("user").getAsJsonObject().get("email").getAsString());
                                mPreferencesManager.setUserPhone(response.getResult().get("user").getAsJsonObject().get("phoneNumber").getAsString());
                                mRegisterPopUp.dismiss();
                                ((AuthenticationActivity) mCtx).finish();
                            } else {
                                if (response.getHeaders().code() == 409) {
                                    setEnableFields(true);
                                    mEmailRegistered.setVisibility(View.VISIBLE);
                                    resultOk[0] = false;
                                } else {
                                    setEnableFields(true);
                                    Toast.makeText(mCtx, "Ceva nu a mers! Verifică conexiunea la internet!", Toast.LENGTH_SHORT).show();
                                    resultOk[0] = false;
                                }
                                StorageReference imageStorageReference = FirebaseStorage.getInstance()
                                        .getReferenceFromUrl(imageDownloadLink);
                                imageStorageReference.delete();
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
                        setEnableFields(true);
                        Log.e(TAG, "onFailure: Something goes wrong to get image link");
                        Toast.makeText(mCtx, "Ceva nu a mers! Verifică conexiunea la internet!", Toast.LENGTH_SHORT).show();
                        resultOk[0] = false;
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                setEnableFields(true);
                Log.e(TAG, "onFailure: Something goes wrong to put image on firebase");
                Toast.makeText(mCtx, "Ceva nu a mers! Verifica conexiuneă la internet!", Toast.LENGTH_SHORT).show();
                resultOk[0] = false;
            }
        });


        return resultOk[0];
    }

    private void setEnableFields(boolean value) {
        mEmail.setEnabled(value);
        mPhoneNumber.setEnabled(value);
        mPassword.setEnabled(value);
        mRePassword.setEnabled(value);
        mFullName.setEnabled(value);
        mProfileImage.setEnabled(value);
        confirm.setEnabled(value);
        cancel.setEnabled(value);
    }

    private boolean verifyInputOnClientSide() {
        Log.i(TAG, "verifyInputOnClientSide: Register");
        boolean resultOk = true;
        if (mFullName.getText().toString().trim().isEmpty()) {
            mFullName.setError("Completează campul!");
            resultOk = false;
        }
        if (mEmail.getText().toString().trim().isEmpty()) {
            mEmail.setError("Completează campul!");
            resultOk = false;
        } else {
            if (!Patterns.EMAIL_ADDRESS.matcher(mEmail.getText().toString().trim()).matches()) {
                mEmail.setError("Adresă nevalidă!");
                resultOk = false;
            }
        }
        if (mPhoneNumber.getText().toString().trim().isEmpty()) {
            mPhoneNumber.setError("Completează campul!");
            resultOk = false;
        } else {
            if (!Patterns.PHONE.matcher(mPhoneNumber.getText().toString().trim()).matches()) {
                mPhoneNumber.setError("Numar invalid!");
                resultOk = false;
            }
        }
        if (mPassword.getText().toString().trim().isEmpty()) {
            mPassword.setError("Completează campul!");
            resultOk = false;
        } else if (mPassword.getText().toString().trim().length() < 6) {
            mPassword.setError("Introdu minim 6 caractere!");
            resultOk = false;
        }
        if (mRePassword.getText().toString().trim().isEmpty()) {
            mRePassword.setError("Completează campul!");
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
            mUserImageUri = result.getData();
            mProfileImage.setImageURI(mUserImageUri);
            mUseDefaultPhoto = false;
        }
    }
}
