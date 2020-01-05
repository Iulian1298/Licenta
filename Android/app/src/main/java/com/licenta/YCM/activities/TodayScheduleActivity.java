package com.licenta.YCM.activities;

import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.licenta.YCM.R;
import com.licenta.YCM.SharedPreferencesManager;
import com.licenta.YCM.adapters.ScheduledHourAdapter;
import com.licenta.YCM.models.ScheduledHour;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;

public class TodayScheduleActivity extends AppCompatActivity {

    private static final String TAG = "TodayScheduleActivity";
    private ArrayList<ScheduledHour> mScheduledHourList;
    private ScheduledHourAdapter mScheduledHourAdapter;
    private RecyclerView mScheduledHourRecyclerView;
    private String mUrl;
    private String mServiceId;
    private Context mCtx;
    private SharedPreferencesManager mPreferencesManager;
    private TextView mCurrentDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_schedule);
        mCtx = getApplicationContext();
        mPreferencesManager = SharedPreferencesManager.getInstance(mCtx);

        final SwipeRefreshLayout refreshComments = findViewById(R.id.refreshScheduledHours);
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Programarile pe azi");
        mScheduledHourRecyclerView = findViewById(R.id.scheduledHoursList);
        mCurrentDay = findViewById(R.id.currentDay);
        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy");
        mCurrentDay.setText(format.format(currentDate));
        mScheduledHourRecyclerView.setLayoutManager(new LinearLayoutManager(mCtx));
        mUrl = "http://10.0.2.2:5000";
        mScheduledHourList = new ArrayList<>();
        mScheduledHourAdapter = new ScheduledHourAdapter(mCtx, mScheduledHourList);
        mScheduledHourRecyclerView.addItemDecoration(new DividerItemDecoration(mCtx, DividerItemDecoration.VERTICAL));
        mScheduledHourRecyclerView.setAdapter(mScheduledHourAdapter);
        Intent intent = getIntent();
        mServiceId = intent.getStringExtra("serviceId");
        populateScheduledHourList();
        mScheduledHourAdapter.setPhoneClickListener(new ScheduledHourAdapter.OnPhoneClickListener() {
            @Override
            public void onPhoneClick(View view, int pos) {
                Log.i(TAG, "onPhoneClick: ");
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel: +4" + mScheduledHourList.get(pos).getUserPhone()));
                startActivity(dialIntent);
            }
        });
    }

    private void populateScheduledHourList() {
        Log.i(TAG, "populateScheduledHourList: ");
        Ion.with(mCtx)
                .load("GET", mUrl + "/getLockedHoursForTodayForService/" + mServiceId)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> result) {
                        if (result != null) {
                            if (result.getHeaders().code() == 200) {
                                if (result.getResult() != null) {
                                    JsonArray scheduledHours = result.getResult().get("lockedHours").getAsJsonArray();
                                    System.out.println(scheduledHours.toString());
                                    for (JsonElement element : scheduledHours) {
                                        try {
                                            System.out.println(element.toString());
                                            JSONObject jsonObject = new JSONObject(element.toString());
                                            mScheduledHourList.add(new ScheduledHour(
                                                    jsonObject.getString("username"),
                                                    jsonObject.getString("dayId"),
                                                    jsonObject.getString("hour"),
                                                    jsonObject.getString("shortDescription"),
                                                    jsonObject.getString("phoneNumber")
                                            ));
                                            mScheduledHourAdapter.notifyDataSetChanged();
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
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.v(TAG, "onSupportNavigateUp()");
        onBackPressed();
        return true;
    }
}
