package com.licenta.ServiceFinder.models;

import android.net.Uri;

public class Comment {
    private String mId;
    private Uri mProfileImage;
    private String mComment;
    private String mOwnerUsername;
    private Float mRating;
    private String mCreationTime;  //need to see if String it's OK
    private String mServiceId;
    private String mOwnerId;

    public Comment(String id,
                   Uri profileImage,
                   String comment,
                   String ownerUsername,
                   Float rating,
                   String creationTime,
                   String serviceId,
                   String ownerId) {
        this.mId = id;
        this.mProfileImage = profileImage;
        this.mComment = comment;
        this.mOwnerUsername = ownerUsername;
        this.mRating = rating;
        this.mCreationTime = creationTime;
        this.mServiceId = serviceId;
        this.mOwnerId = ownerId;
    }


    public String getId() {
        return mId;
    }

    public Uri getProfileImage() {
        return mProfileImage;
    }

    public String getComment() {
        return mComment;
    }

    public String getOwnerId() {
        return mOwnerId;
    }

    public String getServiceId() {
        return mServiceId;
    }

    public String getCreationTime() {
        return mCreationTime;
    }

    public String getOwnerUsername() {
        return mOwnerUsername;
    }

    public Float getRating() {
        return mRating;
    }
}
