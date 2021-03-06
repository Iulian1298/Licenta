package com.licenta.ServiceFinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;


import java.util.concurrent.ExecutionException;


public class SharedPreferencesManager {

    private static final String TAG = "SharedPreferencesManager";
    private static final String mUrl = "http://10.0.2.2:5000";
    //private static final String mUrl = "https://agile-harbor-57300.herokuapp.com";


    private static final String SHARED_PREF_NAME = "ServiceFinder";
    private static final String KEY_IS_LOGGED = "isLogged";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_MAIL = "userMail";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_USER_ID = "userID";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_PERMISSION_LOCATION = "location";
    private static final String KEY_ONLY_MY_SERVICES = "onlyMyServices";


    private static SharedPreferencesManager mInstance;
    private static Context mContext;

    private SharedPreferencesManager(Context ctx) {
        mContext = ctx;
    }


    public static synchronized SharedPreferencesManager getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new SharedPreferencesManager(ctx);
        }
        return mInstance;
    }

    public String getServerUrl() {
        return mUrl;
    }

    public boolean getOnlyMyServices() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_ONLY_MY_SERVICES, false);
    }

    public void setOnlyMyServices(boolean status) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_ONLY_MY_SERVICES, status);
        editor.apply();
    }


    public void setPermissionLocation(boolean permission) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_PERMISSION_LOCATION, permission);
        editor.apply();
    }
    public boolean getPermissionLocation() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_PERMISSION_LOCATION, false);
    }

    public float getUserLongitude() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getFloat(KEY_LONGITUDE, -1);
    }

    public void setUserLongitude(float longitude) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(KEY_LONGITUDE, longitude);
        editor.apply();
    }

    public float getUserLatitude() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getFloat(KEY_LATITUDE, -1);
    }

    public void setUserLatitude(float latitude) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(KEY_LATITUDE, latitude);
        editor.apply();
    }

    public void setImage(String image) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_IMAGE, image);
        editor.apply();
    }


    public String getImage() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_IMAGE, "logo");
    }

    public void setUserId(String id) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, id);
        editor.apply();
    }

    public String getUserId() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public void setUsername(String username) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public String getUsername() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    public void setUserPhone(String userPhone) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_PHONE, userPhone);
        editor.apply();
    }

    public String getUserPhone() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_PHONE, null);
    }

    public void setUserMail(String userMail) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_MAIL, userMail);
        editor.apply();
    }


    public String getUserMail() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_MAIL, null);
    }


    public String getToken() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TOKEN, null);
    }


    public void setToken(String token) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_IS_LOGGED, "logged");
        editor.apply();
    }


    public boolean isLoggedIn() throws ExecutionException, InterruptedException {
        Log.i(TAG, "isLoggedIn()");
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences.getString(KEY_IS_LOGGED, null) != null) {
            Response<JsonObject> response = Ion.with(mContext)
                    .load("GET", mUrl + "/auth/checkLogged")
                    .setHeader("Authorization", getToken())
                    .asJsonObject()
                    .withResponse()
                    .get();

            if (response.getHeaders().code() == 202 || response.getHeaders().code() == 200) {
                Log.i(TAG, "isLoggedIn()->token still valid");
                //System.out.println(response.getResult());
                if (response.getResult() != null) {
                    Log.i(TAG, response.getResult().toString());
                } else {
                    Log.e(TAG, "isLoggedIn: result is null");
                }
            } else {
                if (response.getHeaders().code() == 403) {
                    Log.i(TAG, "isLoggedIn()->token not valid");
                    Log.i(TAG, response.getResult().toString());
                    logout();
                } else {
                    Toast.makeText(mContext, "Error code: " + response.getHeaders().code(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        return sharedPreferences.getString(KEY_IS_LOGGED, null) != null;
    }

    public void logout() {
        Log.i(TAG, "SharedPreferenceManager::logout()");
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //do not clear location(lat, long), need data in order to display distance
        float userLatitude = mInstance.getUserLatitude();
        float userLongitude = mInstance.getUserLongitude();
        editor.clear();
        editor.apply();
        mInstance.setUserLatitude(userLatitude);
        mInstance.setUserLongitude(userLongitude);
    }


}
