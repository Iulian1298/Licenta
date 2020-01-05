package com.licenta.YCM.models;

public class ScheduledHour {
    private String mOwnerUsername;
    private String mDayId;
    private String mHour;
    private String mShortDescription;
    private String mUserPhone;

    public ScheduledHour(String ownerUsername,
                         String dayId,
                         String hour,
                         String shortDescription,
                         String userPhone) {
        this.mOwnerUsername = ownerUsername;
        this.mDayId = dayId;
        this.mHour = hour;
        this.mShortDescription = shortDescription;
        this.mUserPhone = userPhone;
    }

    public String getOwnerUsername() {
        return mOwnerUsername;
    }

    public String getDayId() {
        return mDayId;
    }

    public String getHour() {
        return mHour;
    }

    public String getShortDescription() {
        return mShortDescription;
    }

    public String getUserPhone() {
        return mUserPhone;
    }
}
