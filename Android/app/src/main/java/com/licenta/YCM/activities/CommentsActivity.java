package com.licenta.YCM.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.licenta.YCM.R;
import com.licenta.YCM.models.Comment;

import java.util.ArrayList;

public class CommentsActivity extends AppCompatActivity {

    private static final String TAG = "CommentsActivity";

    private ArrayList<Comment> mCommentsList;
    private CommentsAdapter mCommentsAdapter;
    private RecyclerView mCommentsRecyclerView;
    private String mAllCommentsIDUrl;
    private String mCommentByIDUrl;
    private String mDeleteCommentByIdUrl;
    private String mDoctorId;
    private ArrayList<RatingBar> mRatingBarsForFilterAction;
    private TextView mSeeAllComments;
    private Intent mReturnIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
    }
}
