package com.licenta.YCM.models;

public class Appointment {
    private String mOwnerOrServiceName;
    private String mAppointmentId;
    private String mHour;
    private String mShortDescription;
    private String mUserPhone;
    private int mAppointmentType;

    public Appointment(String ownerOrServiceName,
                         String dayId,
                         String hour,
                         String shortDescription,
                         String userPhone,
                         int type) {
        this.mOwnerOrServiceName = ownerOrServiceName;
        this.mAppointmentId = dayId;
        this.mHour = hour;
        this.mShortDescription = shortDescription;
        this.mUserPhone = userPhone;
        this.mAppointmentType = type;
    }

    public String getOwnerOrServiceUsername() {
        return mOwnerOrServiceName;
    }

    public String getAppointmentId() {
        return mAppointmentId;
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

    public int getAppointmentType() {
        return mAppointmentType;
    }
}
