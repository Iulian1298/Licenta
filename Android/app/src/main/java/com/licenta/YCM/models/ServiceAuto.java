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
    private String mOwnerId;
    private int mType;
    private String mAcceptedBrands;

    public ServiceAuto(String serviceId,
                       Bitmap image,
                       String name,
                       String description,
                       String address,
                       float rating,
                       String phoneNumber,
                       String email,
                       double latitude,
                       double longitude,
                       String ownerId,
                       int type,
                       String acceptedBrands) {
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
        this.mOwnerId = ownerId;
        this.mType = type;
        this.mAcceptedBrands = acceptedBrands;
    }

    public String getServiceId() {
        return mServiceId;
    }

    public void setServiceId(String newServiceId) {
        mServiceId = newServiceId;
    }

    public Bitmap getImage() {
        return mImage;
    }

    public void setImage(Bitmap newImage) {
        mImage = newImage;
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

    public void setName(String newName) {
        mName = newName;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String newDescription) {
        mDescription = newDescription;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String newAddress) {
        mAddress = newAddress;
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

    public void setContactPhoneNumber(String newContactPhoneNumber) {
        mContactPhoneNumber = newContactPhoneNumber;
    }

    public String getContactEmail() {
        return mContactEmail;
    }

    public void setContactEmail(String newContactEmail) {
        mContactEmail = newContactEmail;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double newLatitude) {
        mLatitude = newLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double newLongitude) {
        mLongitude = newLongitude;
    }

    public String getOwnerId() {
        return mOwnerId;
    }

    public void setOwnerId(String newOwnerId) {
        mOwnerId = newOwnerId;
    }

    public int getType() {
        return mType;
    }

    public void setType(int newType) {
        mType = newType;
    }

    public String getAcceptedBrands() {
        return mAcceptedBrands;
    }

    public void setAcceptedBrands(String newAcceptedBrands) {
        mAcceptedBrands = newAcceptedBrands;
    }

    public double calculateDistance(double lat2, double long2) {
        if ((mLatitude == lat2) && (mLongitude == long2)) {
            return 0;
        } else {
            double lat1 = Math.toRadians(mLatitude);
            double long1 = Math.toRadians(mLongitude);
            lat2 = Math.toRadians(lat2);
            long2 = Math.toRadians(long2);
            double earthRadius = 6371.01; //Kilometers
            double distance = earthRadius * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(long1 - long2));
            return distance;
        }
    }

}

