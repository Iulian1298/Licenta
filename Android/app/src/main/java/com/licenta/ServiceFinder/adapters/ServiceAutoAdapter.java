package com.licenta.ServiceFinder.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.licenta.ServiceFinder.R;
import com.licenta.ServiceFinder.SharedPreferencesManager;
import com.licenta.ServiceFinder.activities.HomeActivity;
import com.licenta.ServiceFinder.models.ServiceAuto;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class ServiceAutoAdapter extends RecyclerView.Adapter<ServiceAutoAdapter.ViewHolder> {

    private ArrayList<ServiceAuto> mServiceAutoList;
    private ArrayList<ServiceAuto> mServiceAutoListFiltered;
    private Context mContext;
    private OnItemServiceAutoClickListener mItemServiceAutoClickListener;
    private SharedPreferencesManager mPreferencesManager;
    private OnDeleteClickListener mDeleteClickListener;


    public ServiceAutoAdapter(Context context, ArrayList<ServiceAuto> serviceAutoList) {
        this.mContext = context;
        mPreferencesManager = SharedPreferencesManager.getInstance(mContext);
        this.mServiceAutoList = serviceAutoList;
        this.mServiceAutoListFiltered = serviceAutoList;
        this.mItemServiceAutoClickListener = null;
        this.mDeleteClickListener = null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.service_layout, viewGroup, false);
        return new ViewHolder(view);
    }


    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        System.out.println("onBindViewHolder serviceautoadapter");
        ServiceAuto serviceAuto = mServiceAutoListFiltered.get(i);

        /*Picasso.get()
                .load("http://10.0.2.2:5000/services/getImageById/3")
                //.load(http://i.imgur.com/DvpvklR.png)
                .into(viewHolder.mImage);
        */
        Glide.with(mContext)
                .asBitmap()
                //.skipMemoryCache(true)
                //.diskCacheStrategy(DiskCacheStrategy.NONE)
                .load(serviceAuto.getImage())
                .into(viewHolder.mImage);
        //viewHolder.mImage.setImageBitmap(serviceAuto.getImage());
        viewHolder.mName.setText(serviceAuto.getName());
        if (serviceAuto.getDescription().length() > 100) {
            viewHolder.mDescription.setText(String.format("%s...", serviceAuto.getDescription().substring(0, 97)));
        } else {
            viewHolder.mDescription.setText(serviceAuto.getDescription());
        }
        if (ContextCompat.checkSelfPermission(mContext, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && !mPreferencesManager.getOnlyMyServices()) {
            Log.i("ServiceAutoAdapter", "onBindViewHolder: location permission available");
            viewHolder.mDistance.setVisibility(View.VISIBLE);
            viewHolder.mDistanceIcon.setVisibility(View.VISIBLE);
            double distance = serviceAuto.calculateDistance(mPreferencesManager.getUserLatitude(), mPreferencesManager.getUserLongitude());
            if (distance < 1) {
                viewHolder.mDistance.setText(String.format("%d m", (int) (distance * 1000)));
            } else {
                viewHolder.mDistance.setText(String.format("%.2f km", distance));
            }
        } else {
            viewHolder.mDistance.setVisibility(View.GONE);
            viewHolder.mDistanceIcon.setVisibility(View.GONE);
            if (mPreferencesManager.getOnlyMyServices()) {
                if (serviceAuto.getSeenOffers() == 1) {
                    viewHolder.mNewRequestOfferIcon.setVisibility(View.VISIBLE);
                    viewHolder.mNewRequestOffertext.setVisibility(View.VISIBLE);
                }
                if (serviceAuto.getSeenOffers() != 1) {
                    viewHolder.mNewRequestOfferIcon.setVisibility(View.GONE);
                    viewHolder.mNewRequestOffertext.setVisibility(View.GONE);
                }
            }
        }
        if (mPreferencesManager.getOnlyMyServices()) {
            viewHolder.mDeleteService.setVisibility(View.VISIBLE);
        }
        viewHolder.mAddress.setText(serviceAuto.getCity() + ", " + serviceAuto.getAddress());
        viewHolder.mRatingBar.setRating(serviceAuto.getRating());
    }

    @Override
    public int getItemCount() {
        return mServiceAutoListFiltered.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImage;
        private TextView mName;
        private TextView mDescription;
        private TextView mDistance;
        private TextView mAddress;
        private RatingBar mRatingBar;
        private ImageView mDistanceIcon;
        private ImageView mDeleteService;
        private ImageView mNewRequestOfferIcon;
        private TextView mNewRequestOffertext;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.logoImage);
            mName = itemView.findViewById(R.id.serviceName);
            mDescription = itemView.findViewById(R.id.serviceDescription);
            mDistance = itemView.findViewById(R.id.distanceFromYou);
            mDistanceIcon = itemView.findViewById(R.id.distanceFromYouIcon);
            mAddress = itemView.findViewById(R.id.address);
            mRatingBar = itemView.findViewById(R.id.rating);
            mDeleteService = itemView.findViewById(R.id.deleteService);
            mNewRequestOfferIcon = itemView.findViewById(R.id.serviceHasRequestIcon);
            mNewRequestOffertext = itemView.findViewById(R.id.serviceHasRequestText);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemServiceAutoClickListener != null) {
                        mItemServiceAutoClickListener.onItemServiceAutoClick(view, getAdapterPosition());
                    }
                }
            });
            mDeleteService.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (mDeleteClickListener != null) {
                        TextView areYouSureTitle = new TextView(mContext);
                        areYouSureTitle.setText("Ești sigur că vrei să ștergi acest service?");
                        areYouSureTitle.setGravity(Gravity.CENTER);
                        areYouSureTitle.setPadding(10, 10, 10, 10);
                        areYouSureTitle.setTextSize(18);
                        areYouSureTitle.setTextColor(Color.DKGRAY);
                        AlertDialog areYouSure = new AlertDialog.Builder((HomeActivity) mContext)
                                .setCustomTitle(areYouSureTitle)
                                .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.i("HomeFragment->AdapterService", "onClick: are you sure: yes");
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

    public void setClickListener(OnItemServiceAutoClickListener clickListener) {
        this.mItemServiceAutoClickListener = clickListener;
    }

    public interface OnItemServiceAutoClickListener {
        void onItemServiceAutoClick(View view, int pos);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(View view, int pos);
    }

    public void setDeleteClickListener(OnDeleteClickListener clickListener) {
        this.mDeleteClickListener = clickListener;
    }

    private class ServicesHomeListFilter extends Filter {


        public ServicesHomeListFilter() {
        }


        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String searchText = constraint.toString();
            FilterResults filterResults = new FilterResults();

            if (searchText.isEmpty()) {
                filterResults.values = mServiceAutoList;
            } else {
                ArrayList<ServiceAuto> serviceAutoListFiltered = new ArrayList<>();
                for (ServiceAuto serviceAuto : mServiceAutoList) {
                    if (serviceAuto.getName().toLowerCase().contains(searchText.toLowerCase())
                            || serviceAuto.getAddress().toLowerCase().contains(searchText.toLowerCase())
                            || serviceAuto.getCity().toLowerCase().contains(searchText.toLowerCase())) {
                        serviceAutoListFiltered.add(serviceAuto);
                    }
                }
                filterResults.values = serviceAutoListFiltered;
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values == null) {
                mServiceAutoListFiltered = mServiceAutoList;
                notifyDataSetChanged();
            } else {
                mServiceAutoListFiltered = (ArrayList<ServiceAuto>) results.values;
                notifyDataSetChanged();
            }
        }

    }


    public Filter getFilter() {
        return new ServicesHomeListFilter();
    }

    public ServiceAuto getElemInFilteredListAtPos(int pos) {
        return this.mServiceAutoListFiltered.get(pos);
    }
}
