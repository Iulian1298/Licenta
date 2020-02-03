package com.licenta.ServiceFinder.models;

public class RequestOffer {
    private String mId;
    private String mServiceName;
    private String mUserName;
    private String mRequestText;
    private boolean mWithUserParts;
    private String mCarType;
    private String mCarModel;
    private String mCarYear;
    private String mCarVin;
    private String mServiceResponse;
    private String mServicePriceResponse;
    private String mFixStartDate;
    private String mFixEndDate;
    private int mServiceAcceptance;
    private int mUserAcceptance;
    private String mUserOrServicePhone;
    private int mSeen;

    public RequestOffer(String id,
                        String serviceName,
                        String userName,
                        String requestText,
                        boolean withUserParts,
                        String carType,
                        String carModel,
                        String carYear,
                        String carVin,
                        String serviceResponse,
                        String servicePriceResponse,
                        String fixStartDate,
                        String fixEndDate,
                        int serviceAcceptance,
                        int userAcceptance,
                        String userOrServicePhone,
                        int seen) {
        this.mId = id;
        this.mServiceName = serviceName;
        this.mUserName = userName;
        this.mRequestText = requestText;
        this.mWithUserParts = withUserParts;
        this.mCarType = carType;
        this.mCarModel = carModel;
        this.mCarYear = carYear;
        this.mCarVin = carVin;
        this.mServiceResponse = serviceResponse;
        this.mServicePriceResponse = servicePriceResponse;
        this.mFixStartDate = fixStartDate;
        this.mFixEndDate = fixEndDate;
        this.mServiceAcceptance = serviceAcceptance;
        this.mUserAcceptance = userAcceptance;
        this.mUserOrServicePhone = userOrServicePhone;
        this.mSeen = seen;
    }

    public String getRequestId() {
        return mId;
    }

    public String getServiceName() {
        return mServiceName;
    }

    public String getUserName() {
        return mUserName;
    }


    public String getRequestText() {
        return mRequestText;
    }

    public boolean isWithUserParts() {
        return mWithUserParts;
    }

    public String getCarType() {
        return mCarType;
    }

    public String getCarModel() {
        return mCarModel;
    }

    public String getCarYear() {
        return mCarYear;
    }

    public String getCarVin() {
        return mCarVin;
    }

    public String getServiceResponse() {
        return mServiceResponse;
    }

    public void setServiceResponse(String response) {
        mServiceResponse = response;
    }

    public String getServicePriceResponse() {
        return mServicePriceResponse;
    }

    public void setServicePriceResponse(String price) {
        mServicePriceResponse = price;
    }

    public String getFixStartDate() {
        return mFixStartDate;
    }

    public void setFixStartDate(String startDate) {
        mFixStartDate = startDate;
    }

    public String getFixEndDate() {
        return mFixEndDate;
    }

    public void setFixEndDate(String endDate) {
        mFixEndDate = endDate;
    }

    public int getServiceAcceptance() {
        return mServiceAcceptance;
    }

    public void setServiceAcceptance(int value) {
        mServiceAcceptance = value;
    }

    public int getUserAcceptance() {
        return mUserAcceptance;
    }

    public void setUserAcceptance(int value) {
        mUserAcceptance = value;
    }

    public String getUserOrServicePhone() {
        return mUserOrServicePhone;
    }

    public int getSeen() {
        return mSeen;
    }

    public void setSeen(int mSeen) {
        this.mSeen = mSeen;
    }
}
