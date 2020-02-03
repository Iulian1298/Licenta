package com.licenta.ServiceFinder.adapters;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.licenta.ServiceFinder.R;
import com.licenta.ServiceFinder.activities.HomeActivity;
import com.licenta.ServiceFinder.models.RequestOffer;

import java.util.ArrayList;

public class RequestOfferAdapter extends RecyclerView.Adapter<RequestOfferAdapter.ViewHolder> {
    private ArrayList<RequestOffer> mRequestOfferList;
    private Context mContext;
    private boolean mIsUserOrService;
    private OnPhoneClickListener mPhoneCLickListener;
    private OnAcceptClickListener mAcceptClickListener;
    private OnDeclineClickListener mDeclineCLickListener;
    private OnDeleteClickListener mDeleteCLickListener;
    private OnOfferUpdateClickListener mOfferUpdatedClickListener;

    public RequestOfferAdapter(Context context, ArrayList<RequestOffer> requestOfferList, boolean isUserOrService) {
        this.mRequestOfferList = requestOfferList;
        this.mContext = context;
        this.mIsUserOrService = isUserOrService;
        this.mPhoneCLickListener = null;
        this.mAcceptClickListener = null;
        this.mDeclineCLickListener = null;
        this.mDeleteCLickListener = null;
        this.mOfferUpdatedClickListener = null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.request_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    private void bindForUser(final ViewHolder viewHolder, final RequestOffer requestOffer) {
        viewHolder.mUserOrService.setText(requestOffer.getServiceName());
        viewHolder.mCarAllDetails.setText(
                String.format("%s %s, an: %s, vin: %s, piese client: %s",
                        requestOffer.getCarType(),
                        requestOffer.getCarModel(),
                        requestOffer.getCarYear(),
                        requestOffer.getCarVin(),
                        requestOffer.isWithUserParts() ? "Da" : "Nu"));
        viewHolder.serviceResponseFull.setText(requestOffer.getServiceAcceptance() == 1 ?
                String.format("Perioadă reparație: %s - %s, preț estimativ: %s Lei",
                        requestOffer.getFixStartDate(),
                        requestOffer.getFixEndDate(),
                        requestOffer.getServicePriceResponse()) :
                requestOffer.getServiceAcceptance() == 2 ?
                        String.format("Cerere refuzată deoarece: %s ",
                                requestOffer.getServiceResponse()) : "");
        viewHolder.serviceResponseFull.setVisibility((requestOffer.getServiceAcceptance() == 1) || (requestOffer.getServiceAcceptance() == 2) ? View.VISIBLE : View.GONE);
        viewHolder.mUserOrServicePhone.setText(requestOffer.getUserOrServicePhone());
        if (requestOffer.getRequestText().length() > 100) {
            SpannableStringBuilder spannable = new SpannableStringBuilder(String.format("%s... Vezi tot!", requestOffer.getRequestText().substring(0, 89)));
            ForegroundColorSpan color = new ForegroundColorSpan(Color.parseColor("#ff0099cc"));
            final StyleSpan style = new StyleSpan(Typeface.ITALIC);
            spannable.setSpan(color, 93, 102, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            spannable.setSpan(style, 93, 102, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            viewHolder.mRequestText.setText(spannable);
            viewHolder.mRequestText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewHolder.mRequestText.setText(requestOffer.getRequestText());
                }
            });
        } else {
            viewHolder.mRequestText.setText(requestOffer.getRequestText());
        }
        viewHolder.mAccept.setVisibility(requestOffer.getServiceAcceptance() == 1
                && requestOffer.getUserAcceptance() == 0 ? View.VISIBLE : View.GONE);
        viewHolder.mDecline.setVisibility(requestOffer.getServiceAcceptance() == 1
                && requestOffer.getUserAcceptance() == 0 ? View.VISIBLE : View.GONE);
        viewHolder.mOfferUpdated.setVisibility(requestOffer.getSeen() == 2 ? View.VISIBLE : View.GONE);
        viewHolder.mUserAccepted.setVisibility(requestOffer.getUserAcceptance() == 1 ? View.VISIBLE : View.GONE);
        viewHolder.mUserRejected.setVisibility(requestOffer.getUserAcceptance() == 2 ? View.VISIBLE : View.GONE);
        viewHolder.mUserAcceptedOrRejected.setVisibility(requestOffer.getUserAcceptance() == 0 ? View.GONE : View.VISIBLE);
        viewHolder.mUserAcceptedOrRejected.setText(requestOffer.getUserAcceptance() == 1 ? "Acceptat" : "Refuzat");
        viewHolder.mUserAcceptedOrRejected.setTextColor(
                requestOffer.getUserAcceptance() == 1 ?
                        Color.parseColor("#03DA00") : Color.parseColor("#DA1800"));
    }

    private void bindForService(final ViewHolder viewHolder, final RequestOffer requestOffer) {
        viewHolder.mUserOrService.setText(requestOffer.getUserName());
        viewHolder.mCarAllDetails.setText(
                String.format("%s %s, an: %s, vin: %s, piese client: %s",
                        requestOffer.getCarType(),
                        requestOffer.getCarModel(),
                        requestOffer.getCarYear(),
                        requestOffer.getCarVin(),
                        requestOffer.isWithUserParts() ? "Da" : "Nu"));
        viewHolder.serviceResponseFull.setText(requestOffer.getServiceAcceptance() == 1 ?
                String.format("Perioadă reparație: %s - %s, preț estimativ: %s Lei",
                        requestOffer.getFixStartDate(),
                        requestOffer.getFixEndDate(),
                        requestOffer.getServicePriceResponse()) :
                requestOffer.getServiceAcceptance() == 2 ?
                        String.format("Cerere refuzată deoarece: %s ",
                                requestOffer.getServiceResponse()) : "");
        viewHolder.serviceResponseFull.setVisibility((requestOffer.getServiceAcceptance() == 1) ||
                (requestOffer.getServiceAcceptance() == 2) ? View.VISIBLE : View.GONE);
        viewHolder.mUserOrServicePhone.setText(requestOffer.getUserOrServicePhone());
        if (requestOffer.getRequestText().length() > 100) {
            SpannableStringBuilder spannable = new SpannableStringBuilder(String.format("%s... Vezi tot!", requestOffer.getRequestText().substring(0, 89)));
            ForegroundColorSpan color = new ForegroundColorSpan(Color.parseColor("#ff0099cc"));
            final StyleSpan style = new StyleSpan(Typeface.ITALIC);
            spannable.setSpan(color, 93, 102, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            spannable.setSpan(style, 93, 102, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            viewHolder.mRequestText.setText(spannable);
            viewHolder.mRequestText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewHolder.mRequestText.setText(requestOffer.getRequestText());
                }
            });
        } else {
            viewHolder.mRequestText.setText(requestOffer.getRequestText());
        }
        viewHolder.mAccept.setVisibility(requestOffer.getServiceAcceptance() == 0
                && requestOffer.getUserAcceptance() == 0 ? View.VISIBLE : View.GONE);
        viewHolder.mDecline.setVisibility(requestOffer.getServiceAcceptance() == 0
                && requestOffer.getUserAcceptance() == 0 ? View.VISIBLE : View.GONE);
        viewHolder.mDelete.setEnabled(requestOffer.getServiceAcceptance() != 0);
        viewHolder.mOfferUpdated.setVisibility(requestOffer.getSeen() == 1 ? View.VISIBLE : View.GONE);
        viewHolder.mUserAccepted.setVisibility(requestOffer.getUserAcceptance() == 1 ? View.VISIBLE : View.GONE);
        viewHolder.mUserRejected.setVisibility(requestOffer.getUserAcceptance() == 2 ? View.VISIBLE : View.GONE);
        viewHolder.mUserAcceptedOrRejected.setVisibility(requestOffer.getUserAcceptance() == 0 ? View.GONE : View.VISIBLE);
        viewHolder.mUserAcceptedOrRejected.setText(requestOffer.getUserAcceptance() == 1 ? "Acceptat" : "Refuzat");
        viewHolder.mUserAcceptedOrRejected.setTextColor(
                requestOffer.getUserAcceptance() == 1 ?
                        Color.parseColor("#03DA00") : Color.parseColor("#DA1800"));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final RequestOffer requestOffer = mRequestOfferList.get(i);
        if (mIsUserOrService) {
            bindForUser(viewHolder, requestOffer);
        } else {
            bindForService(viewHolder, requestOffer);
        }
    }

    @Override
    public int getItemCount() {
        return mRequestOfferList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mUserOrService;
        private TextView mCarAllDetails;
        private TextView mRequestText;
        private TextView mUserOrServicePhone;
        private TextView serviceResponseFull;
        private ImageView mOfferUpdated;
        private ImageView mUserAccepted;
        private ImageView mUserRejected;
        private TextView mUserAcceptedOrRejected;
        private Button mAccept;
        private Button mDecline;
        private Button mDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mUserOrService = itemView.findViewById(R.id.userOrService);
            mCarAllDetails = itemView.findViewById(R.id.carFull);
            serviceResponseFull = itemView.findViewById(R.id.serviceResponseFull);
            mRequestText = itemView.findViewById(R.id.requestTextElement);
            mUserOrServicePhone = itemView.findViewById(R.id.userOrServicePhone);
            mOfferUpdated = itemView.findViewById(R.id.offerUpdated);
            mUserAccepted = itemView.findViewById(R.id.userAccepted);
            mUserRejected = itemView.findViewById(R.id.userRejected);
            mUserAcceptedOrRejected = itemView.findViewById(R.id.userAcceptedOrRejected);
            mAccept = itemView.findViewById(R.id.acceptRequestOffer);
            mDecline = itemView.findViewById(R.id.declineRequestOffer);
            mDelete = itemView.findViewById(R.id.deleteRequestOffer);
            mUserOrServicePhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPhoneCLickListener != null) {
                        mPhoneCLickListener.onPhoneClick(v, getAdapterPosition());
                    }
                }
            });
            mAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAcceptClickListener != null) {
                        mAcceptClickListener.onAcceptClick(v, getAdapterPosition());
                    }
                }
            });
            mDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDeclineCLickListener != null) {
                        mDeclineCLickListener.onDeclineClick(v, getAdapterPosition());
                    }
                }
            });
            mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (mDeleteCLickListener != null) {
                        TextView areYouSureTitle = new TextView(mContext);
                        areYouSureTitle.setText("Ești sigur că vrei să ștergi această cerere?");
                        areYouSureTitle.setGravity(Gravity.CENTER);
                        areYouSureTitle.setPadding(10, 10, 10, 10);
                        areYouSureTitle.setTextSize(18);
                        areYouSureTitle.setTextColor(Color.DKGRAY);
                        AlertDialog areYouSure = new AlertDialog.Builder((HomeActivity) mContext)
                                .setCustomTitle(areYouSureTitle)
                                .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.i("HomeFragment->AdapterRequestOffer", "onClick: are you sure: yes");
                                        mDeleteCLickListener.onDeleteClick(v, getAdapterPosition());
                                    }
                                })
                                .setNegativeButton("Nu", null)
                                .create();
                        areYouSure.show();
                    }
                }
            });
            mOfferUpdated.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOfferUpdatedClickListener != null) {
                        mOfferUpdatedClickListener.onOfferUpdateClick(v, getAdapterPosition());
                    }
                }
            });
        }
    }

    public void setPhoneClickListener(OnPhoneClickListener clickListener) {
        this.mPhoneCLickListener = clickListener;
    }

    public void setAcceptClickListener(OnAcceptClickListener clickListener) {
        this.mAcceptClickListener = clickListener;
    }

    public void setDeclineClickListener(OnDeclineClickListener clickListener) {
        this.mDeclineCLickListener = clickListener;
    }

    public void setDeleteClickListener(OnDeleteClickListener clickListener) {
        this.mDeleteCLickListener = clickListener;
    }

    public void setOfferUpdateClickListener(OnOfferUpdateClickListener clickListener) {
        this.mOfferUpdatedClickListener = clickListener;
    }

    public interface OnPhoneClickListener {
        void onPhoneClick(View view, int pos);
    }

    public interface OnAcceptClickListener {
        void onAcceptClick(View view, int pos);
    }

    public interface OnDeclineClickListener {
        void onDeclineClick(View view, int pos);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(View view, int pos);
    }

    public interface OnOfferUpdateClickListener {
        void onOfferUpdateClick(View view, int pos);
    }
}
