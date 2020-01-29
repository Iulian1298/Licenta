package com.licenta.YCM.models;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class ServiceAuto {
    private String mServiceId;
    private Uri mImageUri;
    private String mName;
    private String mDescription;
    private String mAddress;
    private String mCity;
    private float mRating;
    private String mContactPhoneNumber;
    private String mContactEmail;
    private double mLatitude;
    private double mLongitude;
    private String mOwnerId;
    private int mType;
    private int mPriceService;
    private int mPriceChassis;
    private int mPriceTire;
    private int mPriceItp;
    private String mAcceptedBrands;

    public ServiceAuto(String serviceId,
                       Uri imageUri,
                       String name,
                       String description,
                       String address,
                       String city,
                       float rating,
                       String phoneNumber,
                       String email,
                       double latitude,
                       double longitude,
                       String ownerId,
                       int type,
                       String acceptedBrands,
                       int priceService,
                       int priceTire,
                       int priceChassis,
                       int priceItp) {
        this.mServiceId = serviceId;
        this.mImageUri = imageUri;
        this.mName = name;
        this.mDescription = description;
        this.mAddress = address;
        this.mCity = city;
        this.mRating = rating;
        this.mContactPhoneNumber = phoneNumber;
        this.mContactEmail = email;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mOwnerId = ownerId;
        this.mType = type;
        this.mPriceService = priceService;
        this.mPriceChassis = priceChassis;
        this.mPriceTire = priceTire;
        this.mPriceItp = priceItp;
        this.mAcceptedBrands = acceptedBrands;
    }

    public String getServiceId() {
        return mServiceId;
    }

    public void setServiceId(String newServiceId) {
        mServiceId = newServiceId;
    }

    public Uri getImage() {
        return mImageUri;
    }

    public void setImage(Uri newImageUri) {
        mImageUri = newImageUri;
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

    public String getCity() {
        return mCity;
    }

    public void setCity(String newCity) {
        mCity = newCity;
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
            double earthRadius = 6371.01;
            return earthRadius * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(long1 - long2));
        }
    }

    public int getPriceService() {
        return mPriceService;
    }

    public void setPriceService(int mPriceService) {
        this.mPriceService = mPriceService;
    }

    public int getPriceChassis() {
        return mPriceChassis;
    }

    public void setPriceChassis(int mPriceChassis) {
        this.mPriceChassis = mPriceChassis;
    }

    public int getPriceTire() {
        return mPriceTire;
    }

    public void setPriceTire(int mPriceTire) {
        this.mPriceTire = mPriceTire;
    }

    public int getPriceItp() {
        return mPriceItp;
    }

    public void setPriceItp(int mPriceItp) {
        this.mPriceItp = mPriceItp;
    }
}

