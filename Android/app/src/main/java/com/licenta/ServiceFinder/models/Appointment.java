package com.licenta.ServiceFinder.models;

public class Appointment {
    private String mOwnerOrServiceName;
    private String mAppointmentId;
    private String mHour;
    private String mShortDescription;
    private String mUserPhone;
    private int mAppointmentType;
    private String mDay;

    public Appointment(String ownerOrServiceName,
                       String dayId,
                       String hour,
                       String shortDescription,
                       String userPhone,
                       int type,
                       String day) {
        this.mOwnerOrServiceName = ownerOrServiceName;
        this.mAppointmentId = dayId;
        this.mHour = hour;
        this.mShortDescription = shortDescription;
        this.mUserPhone = userPhone;
        this.mAppointmentType = type;
        this.mDay = day;
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

    public String getDay() {
        return mDay;
    }
}
