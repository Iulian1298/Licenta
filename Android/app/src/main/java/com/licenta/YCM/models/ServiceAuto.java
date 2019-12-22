package com.licenta.YCM.models;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class ServiceAuto {
    private String mServiceId;
    private Bitmap mImage;
    private String mName;
    private String mDescription;
    private String mAddress;
    private float mRating;
    private String mContactPhoneNumber;
    private String mContactEmail;
    private double mLatitude;
    private double mLongitude;

    public ServiceAuto(String serviceId,
                       Bitmap image,
                       String name,
                       String description,
                       String address,
                       float rating,
                       String phoneNumber,
                       String email,
                       double latitude,
                       double longitude) {
        this.mServiceId = serviceId;
        this.mImage = image;
        this.mName = name;
        this.mDescription = description;
        this.mAddress = address;
        this.mRating = rating;
        this.mContactPhoneNumber = phoneNumber;
        this.mContactEmail = email;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
    }

    public String getServiceId() {
        return mServiceId;
    }

    public Bitmap getImage() {
        return mImage;
    }

    public String getImageAsString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getAddress() {
        return mAddress;
    }

    public float getRating() {
        return mRating;
    }

    public void setRating(float newRating) {
        mRating = newRating;
    }

    public String getContactPhoneNumber() {
        return mContactPhoneNumber;
    }

    public String getContactEmail() {
        return mContactEmail;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }
    public double calculateDistance(double lat2, double long2) {
        if ((mLatitude == lat2) && (mLongitude== long2)) {
            return 0;
        } else {
            double theta = mLongitude - long2;
            double dist = Math.sin(Math.toRadians(mLatitude)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(mLatitude)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 1.609344;
            return (dist);
        }
    }
}

