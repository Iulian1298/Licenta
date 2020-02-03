package com.licenta.ServiceFinder.fragments;


import android.os.Bundle;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.licenta.ServiceFinder.R;
import com.licenta.ServiceFinder.adapters.MyRequestsAndMyAppointmentsAdapter;


public class MyRequestsAndMyAppointmentsFragment extends Fragment {

    private static final String TAG = "MyRequestsAndMyAppointmentsFragment";
    private TabItem mTabMyRequests;
    private TabItem mTabMyAppointments;
    private TabLayout mRequestsAndAppointments;
    private ViewPager mTabView;
    private MyRequestsAndMyAppointmentsAdapter myRequestsAndMyAppointmentsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_my_requests_and_my_appointment, container, false);

        init(fragmentView);
        return fragmentView;
    }

    private void init(View v) {
        Log.i(TAG, "init: ");
        mRequestsAndAppointments = v.findViewById(R.id.requestsAndAppointments);
        mTabMyRequests = v.findViewById(R.id.tabMyRequest);
        mTabMyAppointments = v.findViewById(R.id.tabMyAppointment);
        mTabView = v.findViewById(R.id.tabView);
        myRequestsAndMyAppointmentsAdapter = new MyRequestsAndMyAppointmentsAdapter(getChildFragmentManager());
        mTabView.setAdapter(myRequestsAndMyAppointmentsAdapter);
        mRequestsAndAppointments.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mTabView.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mTabView.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mRequestsAndAppointments));
    }

    public MyRequestsAndMyAppointmentsFragment() {
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView: ");
        super.onDestroyView();
    }
}
