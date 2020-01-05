package com.licenta.YCM.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextClock;
import android.widget.TextView;

import com.licenta.YCM.R;
import com.licenta.YCM.models.ScheduledHour;

import java.util.ArrayList;

public class ScheduledHourAdapter extends RecyclerView.Adapter<ScheduledHourAdapter.ViewHolder> {

    private ArrayList<ScheduledHour> mScheduledHourList;
    private Context mCtx;
    private OnPhoneClickListener mPhoneClickListener;

    public ScheduledHourAdapter(Context context, ArrayList<ScheduledHour> scheduledHourList) {
        this.mScheduledHourList = scheduledHourList;
        this.mCtx = context;
    }


    @NonNull
    @Override
    public ScheduledHourAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) mCtx.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.scheduled_hour_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduledHourAdapter.ViewHolder viewHolder, int i) {
        ScheduledHour scheduledHour = mScheduledHourList.get(i);
        viewHolder.mHour.setText(scheduledHour.getHour());
        viewHolder.mOwner.setText(scheduledHour.getOwnerUsername());
        viewHolder.mUserPhone.setText(scheduledHour.getUserPhone());
        viewHolder.mShortDescription.setText(scheduledHour.getShortDescription());
    }

    @Override
    public int getItemCount() {
        return mScheduledHourList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mHour;
        private TextView mOwner;
        private TextView mUserPhone;
        private TextView mShortDescription;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mHour = itemView.findViewById(R.id.scheduledHourHour);
            mOwner = itemView.findViewById(R.id.scheduledHourOwner);
            mUserPhone = itemView.findViewById(R.id.scheduledHourUserPhone);
            mShortDescription = itemView.findViewById(R.id.scheduledHourShordDescription);

            mUserPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPhoneClickListener != null) {
                        mPhoneClickListener.onPhoneClick(v, getAdapterPosition());
                    }
                }
            });

        }
    }

    public void setPhoneClickListener(OnPhoneClickListener clickListener) {
        this.mPhoneClickListener = clickListener;
    }

    public interface OnPhoneClickListener {
        void onPhoneClick(View view, int pos);
    }
}
