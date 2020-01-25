package com.licenta.YCM.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.licenta.YCM.R;
import com.licenta.YCM.models.Appointment;

import java.util.ArrayList;

public class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsAdapter.ViewHolder> {

    private ArrayList<Appointment> mAppointmentList;
    private Context mCtx;
    private OnPhoneClickListener mPhoneClickListener;
    private OnDeleteClickListener mDeleteClickListener;
    private boolean mShowMyAppointments;

    public AppointmentsAdapter(Context context, ArrayList<Appointment> appointmentList, boolean showMyAppointments) {
        this.mAppointmentList = appointmentList;
        this.mCtx = context;
        this.mShowMyAppointments = showMyAppointments;
        this.mPhoneClickListener = null;
        this.mDeleteClickListener = null;
    }


    @NonNull
    @Override
    public AppointmentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) mCtx.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.appointment_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentsAdapter.ViewHolder viewHolder, int i) {
        Appointment appointment = mAppointmentList.get(i);
        viewHolder.mHour.setText(appointment.getHour());
        viewHolder.mOwner.setText(appointment.getOwnerOrServiceUsername());
        viewHolder.mUserPhone.setText(appointment.getUserPhone());
        viewHolder.mShortDescription.setText(appointment.getShortDescription());
        StringBuilder offeredServices = new StringBuilder();
        if ((appointment.getAppointmentType() & 1) == 1) {
            offeredServices.append("Service, ");
        }
        if ((appointment.getAppointmentType() & 2) == 2) {
            offeredServices.append("Vulcanizare, ");
        }
        if ((appointment.getAppointmentType() & 4) == 4) {
            offeredServices.append("Tinichigerie, ");
        }
        if ((appointment.getAppointmentType() & 8) == 8) {
            offeredServices.append("ITP, ");
        }
        SpannableStringBuilder spannable = new SpannableStringBuilder(String.format("Tipul Programarii: %s", offeredServices.subSequence(0, offeredServices.length() - 2)));
        ForegroundColorSpan color = new ForegroundColorSpan(Color.parseColor("#001952"));
        final StyleSpan style = new StyleSpan(Typeface.ITALIC);
        spannable.setSpan(color, 18, spannable.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannable.setSpan(style, 18, spannable.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        viewHolder.mAppointmentType.setText(spannable);
    }

    @Override
    public int getItemCount() {
        return mAppointmentList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mHour;
        private TextView mOwner;
        private TextView mUserPhone;
        private TextView mShortDescription;
        private TextView mAppointmentType;
        private ImageView mDeleteAppointment;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mHour = itemView.findViewById(R.id.appointmentHour);
            mOwner = itemView.findViewById(R.id.appointmentOwnerOrService);
            mUserPhone = itemView.findViewById(R.id.appointmentOwnerOrServicePhone);
            mShortDescription = itemView.findViewById(R.id.appointmentShortDescription);
            mAppointmentType = itemView.findViewById(R.id.appointmentType);
            mDeleteAppointment = itemView.findViewById(R.id.deleteAppointment);
            if (mShowMyAppointments) {
                mDeleteAppointment.setVisibility(View.VISIBLE);
            }
            mUserPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPhoneClickListener != null) {
                        mPhoneClickListener.onPhoneClick(v, getAdapterPosition());
                    }
                }
            });
            mDeleteAppointment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDeleteClickListener != null) {
                        mDeleteClickListener.onDeleteClick(v, getAdapterPosition());
                    }
                }
            });


        }
    }

    public void setPhoneClickListener(OnPhoneClickListener clickListener) {
        this.mPhoneClickListener = clickListener;
    }

    public void setDeleteClickListener(OnDeleteClickListener clickListener) {
        this.mDeleteClickListener = clickListener;
    }

    public interface OnPhoneClickListener {
        void onPhoneClick(View view, int pos);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(View view, int pos);
    }
}
