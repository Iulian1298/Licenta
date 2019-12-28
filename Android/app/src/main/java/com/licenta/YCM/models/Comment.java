package com.licenta.YCM.models;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class Comment {
    private String mId;
    private Bitmap mProfileImage;
    private String mComment;
    private String mOwnerUsername;
    private Float mRating;
    private String mCreationTime;  //need to see if String it's OK
    private String mServiceId;
    private String mOwnerId;

    public Comment(String id,
                   Bitmap profileImage,
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

    public String getImageAsString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mProfileImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public String getId() {
        return mId;
    }

    public Bitmap getProfileImage() {
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
