package com.licenta.YCM.fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.licenta.YCM.AsyncRequest;
import com.licenta.YCM.R;
import com.licenta.YCM.SharedPreferencesManager;
import com.licenta.YCM.activities.TodayAppointmentsActivity;
import com.licenta.YCM.adapters.AppointmentsAdapter;
import com.licenta.YCM.models.Appointment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MyAppointmentFragment extends Fragment {

    private static final String TAG = "MyAppointmentFragment";
    private SharedPreferencesManager mPreferencesManager;
    private Context mCtx;
    private ArrayList<Appointment> mMyAppointmentList;
    private AppointmentsAdapter mMyAppointmentsAdapter;
    private RecyclerView mMyAppointmentsRecyclerView;
    private String mUrl;
    private boolean mDisplayNoMyAppointments;
    private boolean mExistMoreAppointments;
    private int mAppointmentsOffset;
    private int mAppointmentsLimit;
    private Button mLoadMoreAppointments;
    private ProgressBar mGetNewMyAppointmentFromDatabase;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_my_appointment, container, false);
        mCtx = getContext();
        mPreferencesManager = SharedPreferencesManager.getInstance(mCtx);

        final SwipeRefreshLayout refreshServiceRequest = fragmentView.findViewById(R.id.refreshMyAppointments);
        refreshServiceRequest.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshPage();
                refreshServiceRequest.setRefreshing(false);
            }
        });

        init(fragmentView);

        return fragmentView;
    }

    private void refreshPage() {
        Log.i(TAG, "refreshPage: ");
        mAppointmentsOffset = 0;
        if (mMyAppointmentList != null && mMyAppointmentsAdapter != null) {
            mMyAppointmentList.clear();
            mMyAppointmentsAdapter.notifyDataSetChanged();
            try {
                populateAppointmentList();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (mDisplayNoMyAppointments) {
                TextView noRequestPerformed = new TextView(mCtx);
                noRequestPerformed.setText("Nu ai făcut nicio programare incă!");
                noRequestPerformed.setPadding(20, 20, 20, 0);
                noRequestPerformed.setGravity(Gravity.CENTER);
                noRequestPerformed.setTextSize(18);
                noRequestPerformed.setTextColor(Color.DKGRAY);
                android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(mCtx)
                        .setView(noRequestPerformed)
                        .setPositiveButton("Am ințeles!", null)
                        .create();
                dialog.show();
            }
        }
    }

    private void init(View v) {
        Log.i(TAG, "init: ");
        mMyAppointmentsRecyclerView = v.findViewById(R.id.myAppointmentList);
        mMyAppointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(mCtx));
        mUrl = mPreferencesManager.getServerUrl();
        mAppointmentsLimit = 11;
        mAppointmentsOffset = 0;
        mExistMoreAppointments = true;
        mMyAppointmentList = new ArrayList<>();
        mMyAppointmentsAdapter = new AppointmentsAdapter(mCtx, mMyAppointmentList, true);
        mMyAppointmentsRecyclerView.setAdapter(mMyAppointmentsAdapter);
        mLoadMoreAppointments = v.findViewById(R.id.loadMoreMyAppointments);
        mGetNewMyAppointmentFromDatabase = v.findViewById(R.id.getNewMyAppointmentFromDatabase);
        try {
            populateAppointmentList();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mDisplayNoMyAppointments) {
            TextView noRequestPerformed = new TextView(mCtx);
            noRequestPerformed.setText("Nu ai făcut nicio programare incă!");
            noRequestPerformed.setPadding(20, 20, 20, 0);
            noRequestPerformed.setGravity(Gravity.CENTER);
            noRequestPerformed.setTextSize(18);
            noRequestPerformed.setTextColor(Color.DKGRAY);
            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(mCtx)
                    .setView(noRequestPerformed)
                    .setPositiveButton("Am ințeles!", null)
                    .create();
            dialog.show();
        }
        mMyAppointmentsAdapter.setPhoneClickListener(new AppointmentsAdapter.OnPhoneClickListener() {
            @Override
            public void onPhoneClick(View view, int pos) {
                Log.i(TAG, "onPhoneClick: ");
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel: +4" + mMyAppointmentList.get(pos).getUserPhone()));
                startActivity(dialIntent);
            }
        });
        mLoadMoreAppointments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadMoreAppointments.setVisibility(View.GONE);
                mGetNewMyAppointmentFromDatabase.setVisibility(View.VISIBLE);
                mAppointmentsOffset += 11;
                try {
                    populateAppointmentList();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mGetNewMyAppointmentFromDatabase.setVisibility(View.GONE);
                    }
                }, 2000);
            }
        });
        mMyAppointmentsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = recyclerView.getChildCount();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                if (dy < 0) {
                    mLoadMoreAppointments.setVisibility(View.GONE);
                }
                Log.i(TAG, String.format("onScrolled: dy: %d totalItemCount: %d firstVisibleItemCount: %d visibleItemCount: %d", dy, totalItemCount, firstVisibleItem, visibleItemCount));
                if ((totalItemCount - visibleItemCount) <= (firstVisibleItem + 2)) {
                    if (dy > 0 & mExistMoreAppointments) {
                        mLoadMoreAppointments.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        mMyAppointmentsAdapter.setDeleteClickListener(new AppointmentsAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(View view, int pos) {
                Log.i(TAG, "onDeleteClick: ");
                String url = mUrl + "/lockedPeriod/deleteById/" + mMyAppointmentList.get(pos).getAppointmentId();
                try {
                    Response<JsonObject> response = Ion.with(mCtx)
                            .load("DELETE", url)
                            .setHeader("Authorization", mPreferencesManager.getToken())
                            .asJsonObject()
                            .withResponse()
                            .get();
                    if (response.getHeaders().code() == 200) {
                        Log.i(TAG, "deleteOfferRequest: Appointment with id: " + mMyAppointmentList.get(pos).getAppointmentId() + "deleted");
                        mMyAppointmentList.remove(pos);
                        mMyAppointmentsAdapter.notifyDataSetChanged();
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void populateAppointmentList() throws ExecutionException, InterruptedException {
        Log.i(TAG, "populateAppointmentList: ");
        String url = mUrl + "/lockedPeriod/getMyAppointmentsIds/" + mPreferencesManager.getUserId() +
                "/limit/" + mAppointmentsLimit + "/offset/" + mAppointmentsOffset;
        Response<JsonObject> response = Ion.with(mCtx)
                .load("GET", url)
                .setHeader("Authorization", mPreferencesManager.getToken())
                .asJsonObject()
                .withResponse()
                .get();
        if (response.getHeaders().code() == 200) {
            Log.i(TAG, "populateAppointmentList: my appointments ids received");
            if (response.getResult() != null) {
                final JsonArray appointmentsIds = response.getResult().get("Ids").getAsJsonArray();
                mDisplayNoMyAppointments = appointmentsIds.size() == 0;
                mExistMoreAppointments = appointmentsIds.size() == 11;
                for (int i = 0; i < appointmentsIds.size(); i++) {
                    final String appointmentId = appointmentsIds.get(i).getAsString();
                    Log.i(TAG, "populateAppointmentList: add appointment with id: " + appointmentId);
                    final AsyncRequest httpGetRequest = new AsyncRequest(mPreferencesManager, new AsyncRequest.Listener() {
                        @Override
                        public void onResult(String result) {
                            if (!result.isEmpty()) {
                                Log.i(TAG, "onResult: Appointment received");
                                try {
                                    JSONObject appointment = new JSONObject(result).getJSONObject("appointment");
                                    mMyAppointmentList.add(new Appointment(
                                            appointment.getString("serviceName"),
                                            appointmentId,
                                            appointment.getString("hour"),
                                            appointment.getString("shortDescription"),
                                            appointment.getString("phoneNumber"),
                                            appointment.getInt("appointmentType")
                                    ));
                                    mMyAppointmentsAdapter.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.e(TAG, "onResult: Appointment with id: " + appointmentId + "not received!");
                            }
                        }
                    });
                    String urlAppointment = mUrl + "/lockedPeriod/getById/" + appointmentId;
                    httpGetRequest.execute("GET", urlAppointment);
                }

            } else {
                Log.e(TAG, "populateAppointmentList: ids not received");
            }

        } else {
            Toast.makeText(mCtx, "Error code: " + response.getHeaders().code(), Toast.LENGTH_SHORT).show();

        }
    }

    public MyAppointmentFragment() {
    }

}
