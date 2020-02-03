package com.licenta.ServiceFinder.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.licenta.ServiceFinder.R;
import com.licenta.ServiceFinder.activities.HomeActivity;
import com.licenta.ServiceFinder.models.Appointment;

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
        viewHolder.mDay.setText(appointment.getDay());
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
        private TextView mDay;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mHour = itemView.findViewById(R.id.appointmentHour);
            mOwner = itemView.findViewById(R.id.appointmentOwnerOrService);
            mUserPhone = itemView.findViewById(R.id.appointmentOwnerOrServicePhone);
            mShortDescription = itemView.findViewById(R.id.appointmentShortDescription);
            mAppointmentType = itemView.findViewById(R.id.appointmentType);
            mDeleteAppointment = itemView.findViewById(R.id.deleteAppointment);
            mDay = itemView.findViewById(R.id.appointmentDay);
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
                public void onClick(final View v) {
                    if (mDeleteClickListener != null) {
                        TextView areYouSureTitle = new TextView(mCtx);
                        areYouSureTitle.setText("Ești sigur că vrei să ștergi această programare?");
                        areYouSureTitle.setGravity(Gravity.CENTER);
                        areYouSureTitle.setPadding(10, 10, 10, 10);
                        areYouSureTitle.setTextSize(18);
                        areYouSureTitle.setTextColor(Color.DKGRAY);
                        AlertDialog areYouSure = new AlertDialog.Builder((HomeActivity) mCtx)
                                .setCustomTitle(areYouSureTitle)
                                .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.i("HomeFragment->AdapterAppointment", "onClick: are you sure: yes");
                                        mDeleteClickListener.onDeleteClick(v, getAdapterPosition());
                                    }
                                })
                                .setNegativeButton("Nu", null)
                                .create();
                        areYouSure.show();
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
