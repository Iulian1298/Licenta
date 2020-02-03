package com.licenta.ServiceFinder.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.licenta.ServiceFinder.AsyncRequest;
import com.licenta.ServiceFinder.adapters.CommentsAdapter;
import com.licenta.ServiceFinder.R;
import com.licenta.ServiceFinder.SharedPreferencesManager;
import com.licenta.ServiceFinder.models.Comment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class CommentsActivity extends AppCompatActivity {

    private static final String TAG = "CommentsActivity";

    private ArrayList<Comment> mCommentsList;
    private CommentsAdapter mCommentsAdapter;
    private RecyclerView mCommentsRecyclerView;
    private String mUrl;
    private String mServiceId;
    private ArrayList<LinearLayout> mRatingBarsForFilterAction;
    private TextView mSeeAllComments;
    private Intent mReturnIntent;
    private Context mCtx;
    private int mCommentsLimit;
    private int mCommentsOffset;
    private ProgressBar mGetNewCommentsFromDatabase;
    private Button mLoadMoreComments;
    private boolean mExistMoreComments;
    private SharedPreferencesManager mPreferencesManager;
    private boolean mDisplayNoComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate() -> Create Activity Main");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        mPreferencesManager = SharedPreferencesManager.getInstance(this);
        mCtx = getApplicationContext();

        final SwipeRefreshLayout refreshComments = findViewById(R.id.refreshComments);
        refreshComments.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                init();
                refreshComments.setRefreshing(false);
            }
        });

        init();
    }

    private void init() {
        Log.i(TAG, "init: ");
        mCommentsRecyclerView = findViewById(R.id.commentsList);
        mCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRatingBarsForFilterAction = new ArrayList<>();
        mRatingBarsForFilterAction.add((LinearLayout) findViewById(R.id.ratingOneStar));
        mRatingBarsForFilterAction.add((LinearLayout) findViewById(R.id.ratingTwoStar));
        mRatingBarsForFilterAction.add((LinearLayout) findViewById(R.id.ratingThreeStar));
        mRatingBarsForFilterAction.add((LinearLayout) findViewById(R.id.ratingFourStar));
        mRatingBarsForFilterAction.add((LinearLayout) findViewById(R.id.ratingFiveStar));
        mGetNewCommentsFromDatabase = findViewById(R.id.getNewCommentsFromDatabase);
        mLoadMoreComments = findViewById(R.id.loadMoreComments);
        mExistMoreComments = true;
        mCommentsOffset = 0;
        mCommentsLimit = 17;
        mSeeAllComments = findViewById(R.id.seeAllComments);
        mUrl = mPreferencesManager.getServerUrl();
        Intent intent = getIntent();
        mServiceId = intent.getStringExtra("serviceId");
        getSupportActionBar().setTitle("Păreri și comentarii");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mCommentsList = new ArrayList<>();
        mCommentsAdapter = new CommentsAdapter(this, mCommentsList);
        mCommentsRecyclerView.setAdapter(mCommentsAdapter);
        try {
            String url = mUrl + "/comments/forService/" + mServiceId + "/getIdsBetween/offset/" + mCommentsOffset + "/limit/" + mCommentsLimit;
            populateCommentList(url);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mDisplayNoComment) {
            TextView noCommentAdded = new TextView(mCtx);
            noCommentAdded.setText("Acest service nu are niciun comentariu!");
            noCommentAdded.setPadding(20, 20, 20, 20);
            noCommentAdded.setGravity(Gravity.CENTER);
            noCommentAdded.setTextSize(18);
            noCommentAdded.setTextColor(Color.DKGRAY);
            AlertDialog dialog = new AlertDialog.Builder(CommentsActivity.this)
                    .setView(noCommentAdded)
                    .setPositiveButton("Am inteles!", null)
                    .create();
            dialog.show();
        }
        mReturnIntent = new Intent();
        setResult(RESULT_CANCELED, mReturnIntent);

        mLoadMoreComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadMoreComments.setVisibility(View.GONE);
                mGetNewCommentsFromDatabase.setVisibility(View.VISIBLE);
                mCommentsOffset += 17;
                try {
                    String url = mUrl + "/comments/forService/" + mServiceId + "/getIdsBetween/offset/" + mCommentsOffset + "/limit/" + mCommentsLimit;
                    populateCommentList(url);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mGetNewCommentsFromDatabase.setVisibility(View.GONE);
                    }
                }, 2000);
            }
        });
        mCommentsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = recyclerView.getChildCount();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                if (dy < 0) {
                    mLoadMoreComments.setVisibility(View.GONE);
                }
                Log.i(TAG, String.format("onScrolled: dy: %d totalItemCount: %d firstVisibleItemCount: %d visibleItemCount: %d", dy, totalItemCount, firstVisibleItem, visibleItemCount));
                if ((totalItemCount - visibleItemCount) <= (firstVisibleItem + 2)) {
                    if (dy > 0 & mExistMoreComments) {
                        mLoadMoreComments.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        //set listeners
        mCommentsAdapter.setDeleteClickListener(new CommentsAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(View view, int pos) throws ExecutionException, InterruptedException {
                Log.i(TAG, "onDeleteClick: delete comment at pos: " + pos);
                String url = mUrl + "/comments/deleteById/" + mCommentsList.get(pos).getId() + "/serviceId/" + mCommentsList.get(pos).getServiceId();
                Response<JsonObject> response = Ion.with(mCtx)
                        .load("DELETE", url)
                        .setHeader("Authorization", mPreferencesManager.getToken())
                        .asJsonObject()
                        .withResponse()
                        .get();
                if (response.getHeaders().code() == 200) {
                    Log.i(TAG, "onDeleteClick: Comment with id: " + mCommentsList.get(pos).getId() + "deleted");
                    setResult(RESULT_OK, mReturnIntent);
                    mReturnIntent.putExtra("deletionNewRating", response.getResult().get("newRating").getAsFloat());
                    mCommentsList.remove(pos);
                    mCommentsAdapter.notifyDataSetChanged();
                    Toast.makeText(mCtx, "Comentariul a fost șters!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mCtx, "Err code: " + response.getHeaders().code(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        setListenersForFilterByRating();
    }


    private void setListenersForFilterByRating() {
        for (int i = 1; i <= 5; i++) {
            final int finalI = i;
            mRatingBarsForFilterAction.get(i - 1).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCommentsOffset = 0;
                    mCommentsList.clear();
                    mCommentsAdapter.notifyDataSetChanged();
                    String url = mUrl + "/comments/forService/" + mServiceId + "/withRatingStars/" + finalI + "/getIdsBetween/offset/" + mCommentsOffset + "/limit/" + mCommentsLimit;
                    try {
                        populateCommentList(url);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        mSeeAllComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommentsOffset = 0;
                mCommentsList.clear();
                mCommentsAdapter.notifyDataSetChanged();
                String url = mUrl + "/comments/forService/" + mServiceId + "/withRatingStars/" + 0 + "/getIdsBetween/offset/" + mCommentsOffset + "/limit/" + mCommentsLimit;
                try {
                    populateCommentList(url);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void populateCommentList(String url) throws ExecutionException, InterruptedException {
        Log.i(TAG, "populateCommentList: ");
        final Response<JsonObject> response = Ion.with(mCtx)
                .load("GET", url)
                .asJsonObject()
                .withResponse()
                .get();
        if (response.getHeaders().code() == 200) {
            Log.i(TAG, "populateCommentList: comments Ids received");
            if (response.getResult() != null) {
                final JsonArray commentsId = response.getResult().get("Ids").getAsJsonArray();
                mDisplayNoComment = commentsId.size() == 0;
                mExistMoreComments = commentsId.size() == 17;
                for (int i = 0; i < commentsId.size(); i++) {
                    final String commentId = commentsId.get(i).getAsString();
                    Log.i(TAG, "populateCommentList: add comment with Id: " + commentId);
                    final AsyncRequest httpGetService = new AsyncRequest(mPreferencesManager, new AsyncRequest.Listener() {
                        @Override
                        public void onResult(String result) {
                            if (!result.isEmpty()) {
                                try {
                                    Log.i(TAG, "onResult: Comment received");
                                    JSONObject comment = new JSONObject(result).getJSONObject("comment");
                                    mCommentsList.add(new Comment(
                                            comment.getString("id"),
                                            Uri.parse(comment.getString("profileImage")),
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
                    String urlComment = mUrl + "/comments/getById/" + commentId;
                    httpGetService.execute("GET", urlComment);
                }
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
