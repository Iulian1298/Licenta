package com.licenta.YCM.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.licenta.YCM.AsyncHttpRequest;
import com.licenta.YCM.CommentsAdapter;
import com.licenta.YCM.R;
import com.licenta.YCM.SharedPreferencesManager;
import com.licenta.YCM.models.Comment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class CommentsActivity extends AppCompatActivity {

    private static final String TAG = "CommentsActivity";

    private ArrayList<Comment> mCommentsList;
    private CommentsAdapter mCommentsAdapter;
    private RecyclerView mCommentsRecyclerView;
    private String mAllCommentsIdUrl;
    private String mCommentByIdUrl;
    private String mDeleteCommentByIdUrl;
    private String mServiceId;
    private ArrayList<RatingBar> mRatingBarsForFilterAction;
    private TextView mSeeAllComments;
    private Intent mReturnIntent;
    private Context mCtx;

    private SharedPreferencesManager mPreferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate() -> Create Activity Main");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        mPreferencesManager = SharedPreferencesManager.getInstance(this);
        mCtx = getApplicationContext();

        init();

    }

    private void init() {
        Log.i(TAG, "init: ");
        mCommentsRecyclerView = findViewById(R.id.commentsList);
        mCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRatingBarsForFilterAction = new ArrayList<>();
        mRatingBarsForFilterAction.add((RatingBar) findViewById(R.id.ratingOneStar));
        mRatingBarsForFilterAction.add((RatingBar) findViewById(R.id.ratingTwoStar));
        mRatingBarsForFilterAction.add((RatingBar) findViewById(R.id.ratingThreeStar));
        mRatingBarsForFilterAction.add((RatingBar) findViewById(R.id.ratingFourStar));
        mRatingBarsForFilterAction.add((RatingBar) findViewById(R.id.ratingFiveStar));
        mSeeAllComments = findViewById(R.id.seeAllComments);
        mAllCommentsIdUrl = "http://10.0.2.2:5000/comments/getAllIds/";
        mCommentByIdUrl = "http://10.0.2.2:5000/comments/getById/";
        mDeleteCommentByIdUrl = "http://10.0.2.2:5000/comments/deleteById/";
        Intent intent = getIntent();
        mServiceId = intent.getStringExtra("serviceId");
        getSupportActionBar().setTitle("Pareri si comentarii!");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mCommentsList = new ArrayList<>();
        mCommentsAdapter = new CommentsAdapter(this, mCommentsList);
        mCommentsRecyclerView.setAdapter(mCommentsAdapter);
        try {
            populateCommentList();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mReturnIntent = new Intent();
        setResult(RESULT_CANCELED, mReturnIntent);

        //set listeners
        mCommentsAdapter.setDeleteClick(new CommentsAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(View view, int pos) throws ExecutionException, InterruptedException {
                Log.i(TAG, "onDeleteClick: delete comment at pos: " + pos);
                Response<JsonObject> response = Ion.with(mCtx)
                        .load("DELETE", mDeleteCommentByIdUrl + mCommentsList.get(pos).getId() + "/serviceId/" + mCommentsList.get(pos).getServiceId())
                        .setHeader("Authorization", mPreferencesManager.getToken())
                        .asJsonObject()
                        .withResponse()
                        .get();
                if (response.getHeaders().code() == 200) {
                    setResult(RESULT_OK, mReturnIntent);
                    mReturnIntent.putExtra("deletionNewRating", response.getResult().get("newRating").getAsFloat());
                    mCommentsList.remove(pos);
                    mCommentsAdapter.notifyDataSetChanged();
                    Log.i(TAG, "onDeleteClick: Comment wit id: " + mCommentsList.get(pos).getId() + "deleted");
                }
            }
        });
    }

    private Bitmap stringToBitmap(String imageEncoded) {
        Log.i(TAG, "stringToBitmap: ");
        try {
            String cleanImage = imageEncoded.
                    replace("dataimage/pngbase64", "").
                    replace("dataimage/jpegbase64", "");
            //Log.i(TAG, "stringToBitmap: " + cleanImage);
            byte[] encodeByte = Base64.decode(cleanImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    private void populateCommentList() throws ExecutionException, InterruptedException {
        Log.i(TAG, "populateCommentList: ");
        final Response<JsonObject> response = Ion.with(mCtx)
                .load("GET", mAllCommentsIdUrl + mServiceId)
                .asJsonObject()
                .withResponse()
                .get();
        if (response.getHeaders().code() == 200) {
            Log.i(TAG, "populateCommentList: comments Ids received");
            if (response.getResult() != null) {
                final JsonArray commentsId = response.getResult().get("Ids").getAsJsonArray();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < commentsId.size(); i++) {
                            try {
                                Thread.sleep(250);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                            final String commentId = commentsId.get(i).getAsString();
                            Log.i(TAG, "populateCommentList: add comment with Id: " + commentId);
                            final AsyncHttpRequest httpGetService = new AsyncHttpRequest(new AsyncHttpRequest.Listener() {
                                @Override
                                public void onResult(String result) {
                                    if (!result.isEmpty()) {
                                        try {
                                            Log.i(TAG, "Comment received");
                                            JSONObject comment = new JSONObject(result).getJSONObject("comment");
                                            mCommentsList.add(new Comment(
                                                    comment.getString("id"),
                                                    stringToBitmap(comment.getString("imageEncoded")),
                                                    comment.getString("comment"),
                                                    comment.getString("ownerName"),
                                                    Float.valueOf(comment.getString("rating")),
                                                    comment.getString("creationTime"),
                                                    comment.getString("serviceId"),
                                                    comment.getString("ownerId")));
                                            mCommentsAdapter.notifyDataSetChanged();
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                            Log.e(TAG, "onResult: NoResult");
                                        }
                                    } else {
                                        Log.e(TAG, "onResult: Service with id: " + commentId + "not received!");
                                    }
                                }
                            });
                            httpGetService.execute("GET", mCommentByIdUrl + commentId);
                        }
                    }
                }).start();
            } else {
                Log.e(TAG, "populateCommentList: All Ids not received");
            }
        } else {
            Toast.makeText(getApplicationContext(), "Error code: " + response.getHeaders().code(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.v(TAG, "onSupportNavigateUp()");
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy()");
        super.onDestroy();
    }

}
