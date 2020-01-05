package com.licenta.YCM.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.licenta.YCM.R;
import com.licenta.YCM.SharedPreferencesManager;
import com.licenta.YCM.models.ServiceAuto;

import java.util.ArrayList;

public class ServiceAutoAdapter extends RecyclerView.Adapter<ServiceAutoAdapter.ViewHolder> {

    private ArrayList<ServiceAuto> mServiceAutoList;
    private ArrayList<ServiceAuto> mServiceAutoListFiltered;
    private Context mContext;
    private OnItemServiceAutoClickListener mItemServiceAutoClickListener;
    private SharedPreferencesManager mPreferencesManager;


    public ServiceAutoAdapter(Context context, ArrayList<ServiceAuto> serviceAutoList) {
        this.mContext = context;
        mPreferencesManager = SharedPreferencesManager.getInstance(mContext);
        this.mServiceAutoList = serviceAutoList;
        this.mServiceAutoListFiltered = serviceAutoList;
        this.mItemServiceAutoClickListener = null;
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
        Glide.with(mContext)
                .asBitmap()
                .load("http://10.0.2.2:5000/services/getImageById/34b64501-9f9a-4bcf-8a60-810d889d84f6")
                .into(viewHolder.mImage);*/
        viewHolder.mImage.setImageBitmap(serviceAuto.getImage());
        viewHolder.mName.setText(serviceAuto.getName());
        if (serviceAuto.getDescription().length() > 100) {
            viewHolder.mDescription.setText(String.format("%s...", serviceAuto.getDescription().substring(0, 97)));
        } else {
            viewHolder.mDescription.setText(serviceAuto.getDescription());
        }
        if (mPreferencesManager.getPermissionLocation() && !mPreferencesManager.getOnlyMyServices()) {
            viewHolder.mDistance.setVisibility(View.VISIBLE);
            double distance = serviceAuto.calculateDistance(mPreferencesManager.getUserLatitude(), mPreferencesManager.getUserLongitude());

            if (distance < 1) {
                viewHolder.mDistance.setText(String.format("%d m", (int) (distance * 1000)));
            } else {
                viewHolder.mDistance.setText(String.format("%.2f km", distance));
            }
        } else {
            viewHolder.mDistance.setVisibility(View.GONE);
        }
        viewHolder.mAddress.setText(serviceAuto.getAddress());
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.logoImage);
            mName = itemView.findViewById(R.id.serviceName);
            mDescription = itemView.findViewById(R.id.serviceDescription);
            mDistance = itemView.findViewById(R.id.distanceFromYou);
            mAddress = itemView.findViewById(R.id.address);
            mRatingBar = itemView.findViewById(R.id.rating);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemServiceAutoClickListener != null) {
                        mItemServiceAutoClickListener.onItemServiceAutoClick(view, getAdapterPosition());
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

    private class ServicesHomeListFilter extends Filter {
        private JsonObject otherFilterParams;


        public ServicesHomeListFilter(JsonObject params) {
            otherFilterParams = params;
        }


        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String searchText = constraint.toString();
            FilterResults filterResults = new FilterResults();

            if (searchText.isEmpty() && otherFilterParams == null) {
                filterResults.values = mServiceAutoList;
            } else {
                ArrayList<ServiceAuto> serviceAutoListFiltered = new ArrayList<>();
                if (otherFilterParams == null) {
                    for (ServiceAuto serviceAuto : mServiceAutoList) {
                        if (serviceAuto.getName().toLowerCase().contains(searchText.toLowerCase())
                                || serviceAuto.getAddress().toLowerCase().contains(searchText.toLowerCase())) {
                            serviceAutoListFiltered.add(serviceAuto);
                        }
                    }
                } else {
                    for (ServiceAuto serviceAuto : mServiceAutoList) {
                        if (
                                (otherFilterParams.get("minRating").getAsFloat() < serviceAuto.getRating()
                                        && serviceAuto.getRating() <= otherFilterParams.get("maxRating").getAsFloat())
                                        || (otherFilterParams.get("minRating").getAsFloat() == 0
                                        && otherFilterParams.get("maxRating").getAsFloat() == 0)
                        ) {
                            if (!otherFilterParams.get("givenNameFilter").getAsString().isEmpty()) {
                                if (serviceAuto.getName().toLowerCase().contains(otherFilterParams.get("givenNameFilter").getAsString().toLowerCase())) {
                                    if (!otherFilterParams.get("distanceInput").getAsString().isEmpty()) {
                                        if (serviceAuto.calculateDistance(mPreferencesManager.getUserLatitude(), mPreferencesManager.getUserLongitude()) < otherFilterParams.get("distanceInput").getAsDouble()) {
                                            if (!otherFilterParams.get("cityInput").getAsString().isEmpty()) {
                                                if (serviceAuto.getAddress().toLowerCase().contains(otherFilterParams.get("cityInput").getAsString().toLowerCase())) {
                                                    serviceAutoListFiltered.add(serviceAuto);
                                                }
                                            } else {
                                                serviceAutoListFiltered.add(serviceAuto);
                                            }
                                        }
                                    } else {
                                        if (!otherFilterParams.get("cityInput").getAsString().isEmpty()) {
                                            if (serviceAuto.getAddress().toLowerCase().contains(otherFilterParams.get("cityInput").getAsString().toLowerCase())) {
                                                serviceAutoListFiltered.add(serviceAuto);
                                            }
                                        } else {
                                            serviceAutoListFiltered.add(serviceAuto);
                                        }
                                    }
                                }
                            } else {
                                if (!otherFilterParams.get("distanceInput").getAsString().isEmpty()) {
                                    if (serviceAuto.calculateDistance(mPreferencesManager.getUserLatitude(), mPreferencesManager.getUserLongitude()) < otherFilterParams.get("distanceInput").getAsDouble()) {
                                        if (!otherFilterParams.get("cityInput").getAsString().isEmpty()) {
                                            if ((serviceAuto.getAddress().toLowerCase()).contains(otherFilterParams.get("cityInput").getAsString().toLowerCase())) {
                                                serviceAutoListFiltered.add(serviceAuto);
                                            }
                                        } else {
                                            serviceAutoListFiltered.add(serviceAuto);
                                        }
                                    }
                                } else {
                                    if (!otherFilterParams.get("cityInput").getAsString().isEmpty()) {
                                        if ((serviceAuto.getAddress().toLowerCase()).contains(otherFilterParams.get("cityInput").getAsString().toLowerCase())) {
                                            serviceAutoListFiltered.add(serviceAuto);
                                        }
                                    } else {
                                        serviceAutoListFiltered.add(serviceAuto);
                                    }
                                }
                            }
                        }
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


    public Filter getFilter(JsonObject advancedFilterParams) {
        return new ServicesHomeListFilter(advancedFilterParams);
    }

    public Filter getFilter() {
        return new ServicesHomeListFilter(null);
    }

    public ServiceAuto getElemInFilteredListAtPos(int pos) {
        return this.mServiceAutoListFiltered.get(pos);
    }
}
