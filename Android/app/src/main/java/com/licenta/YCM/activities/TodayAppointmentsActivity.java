package com.licenta.YCM.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.licenta.YCM.R;
import com.licenta.YCM.SharedPreferencesManager;
import com.licenta.YCM.adapters.AppointmentsAdapter;
import com.licenta.YCM.models.Appointment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class TodayAppointmentsActivity extends AppCompatActivity {

    private static final String TAG = "TodayAppointmentsActivity";
    private ArrayList<Appointment> mAppointmentList;
    private AppointmentsAdapter mAppointmentsAdapter;
    private RecyclerView mAppointmentsRecyclerView;
    private String mUrl;
    private String mServiceId;
    private Context mCtx;
    private SharedPreferencesManager mPreferencesManager;
    private Toolbar mTodayAppointmentsToolbar;
    private boolean mDisplayNoAppointmentsToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_appointments);
        mCtx = getApplicationContext();
        mPreferencesManager = SharedPreferencesManager.getInstance(mCtx);

        final SwipeRefreshLayout refreshComments = findViewById(R.id.refreshAppointments);
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
        mAppointmentsRecyclerView = findViewById(R.id.appointmentsList);
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy");
        mTodayAppointmentsToolbar = findViewById(R.id.todayAppointmentsToolbar);
        setSupportActionBar(mTodayAppointmentsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView toolbarTitle = findViewById(R.id.todayAppointmentsToolbarTitle);
        toolbarTitle.setText("Programările pe azi - " + format.format(currentDate));
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        mAppointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(mCtx));
        mUrl = mPreferencesManager.getServerUrl();
        mAppointmentList = new ArrayList<>();
        mAppointmentsAdapter = new AppointmentsAdapter(mCtx, mAppointmentList,false);
        //mAppointmentsRecyclerView.addItemDecoration(new DividerItemDecoration(mCtx, DividerItemDecoration.VERTICAL));
        mAppointmentsRecyclerView.setAdapter(mAppointmentsAdapter);
        Intent intent = getIntent();
        mServiceId = intent.getStringExtra("serviceId");
        populateAppointmentList();
        if (mDisplayNoAppointmentsToday) {
            TextView noRequestPerformed = new TextView(mCtx);
            noRequestPerformed.setText("Nu ai nicio programare pe astăzi!");
            noRequestPerformed.setPadding(20, 20, 20, 0);
            noRequestPerformed.setGravity(Gravity.CENTER);
            noRequestPerformed.setTextSize(18);
            noRequestPerformed.setTextColor(Color.DKGRAY);
            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(TodayAppointmentsActivity.this)
                    .setView(noRequestPerformed)
                    .setPositiveButton("Am ințeles!", null)
                    .create();
            dialog.show();
        }
        mAppointmentsAdapter.setPhoneClickListener(new AppointmentsAdapter.OnPhoneClickListener() {
            @Override
            public void onPhoneClick(View view, int pos) {
                Log.i(TAG, "onPhoneClick: ");
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel: +4" + mAppointmentList.get(pos).getUserPhone()));
                startActivity(dialIntent);
            }
        });
    }

    private void populateAppointmentList() {
        Log.i(TAG, "populateAppointmentList: ");
        Response<JsonObject> result = null;
        try {
            result = Ion.with(mCtx)
                    .load("GET", mUrl + "/getLockedHoursForTodayForService/" + mServiceId)
                    .setHeader("Authorization", mPreferencesManager.getToken())
                    .asJsonObject()
                    .withResponse()
                    .get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (result != null) {
            if (result.getHeaders().code() == 200) {
                if (result.getResult() != null) {
                    JsonArray appointments = result.getResult().get("appointment").getAsJsonArray();
                    System.out.println(appointments.toString());
                    mDisplayNoAppointmentsToday = appointments.size() == 0;
                    for (JsonElement element : appointments) {
                        try {
                            System.out.println(element.toString());
                            JSONObject jsonObject = new JSONObject(element.toString());
                            mAppointmentList.add(new Appointment(
                                    jsonObject.getString("username"),
                                    jsonObject.getString("appointmentId"),
                                    jsonObject.getString("hour"),
                                    jsonObject.getString("shortDescription"),
                                    jsonObject.getString("phoneNumber"),
                                    jsonObject.getInt("appointmentType")
                            ));
                            mAppointmentsAdapter.notifyDataSetChanged();
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                } else {
                    Log.e(TAG, "onCompleted: result.getResult() is null");
                }
            } else {
                Toast.makeText(mCtx, "Error code: " + result.getHeaders().code(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "onCompleted: result is null");
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.v(TAG, "onSupportNavigateUp()");
        onBackPressed();
        return true;
    }
}
