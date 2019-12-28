package com.licenta.YCM;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.licenta.YCM.models.Comment;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
    private ArrayList<Comment> mCommentsList;
    private Context mContext;
    private OnDeleteClickListener mDeleteClickListener;

    public CommentsAdapter(Context context, ArrayList<Comment> commentsList) {
        this.mCommentsList = commentsList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.comment_layout, viewGroup, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int pos) {
        Comment comment = mCommentsList.get(pos);

        viewHolder.mProfileImage.setImageBitmap(comment.getProfileImage());
        if (comment.getComment().length() > 100) {
            viewHolder.mComment.setText(String.format("%s... See all!", comment.getComment().substring(0, 89)));
        } else {
            viewHolder.mComment.setText(comment.getComment());
        }
        viewHolder.mOwnerUsername.setText(comment.getOwnerUsername());
        viewHolder.mCreationTime.setText(comment.getCreationTime());
        viewHolder.mRatingBar.setRating(comment.getRating());
        if (!comment.getOwnerId().equals(SharedPreferencesManager.getInstance(mContext).getUserId())) {
            viewHolder.mDeleteComm.setVisibility(View.GONE);
        } else {
            viewHolder.mDeleteComm.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mCommentsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mProfileImage;
        private TextView mComment;
        private TextView mOwnerUsername;
        private TextView mCreationTime;
        private RatingBar mRatingBar;
        private TextView mDeleteComm;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mProfileImage = itemView.findViewById(R.id.userProfileImage);
            mComment = itemView.findViewById(R.id.comment);
            mOwnerUsername = itemView.findViewById(R.id.ownerUsername);
            mCreationTime = itemView.findViewById(R.id.commentDate);
            mRatingBar = itemView.findViewById(R.id.commentRating);
            mDeleteComm = itemView.findViewById(R.id.deleteMyComment);

            mDeleteComm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDeleteClickListener != null) {
                        try {
                            mDeleteClickListener.onDeleteClick(v, getAdapterPosition());
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });


        }
    }

    public void setDeleteClick(OnDeleteClickListener clickListener) {
        this.mDeleteClickListener = clickListener;
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(View view, int pos) throws ExecutionException, InterruptedException;
    }
}
