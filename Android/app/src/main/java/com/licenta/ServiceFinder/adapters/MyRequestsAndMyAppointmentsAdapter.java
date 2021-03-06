package com.licenta.ServiceFinder.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.licenta.ServiceFinder.fragments.MyAppointmentFragment;
import com.licenta.ServiceFinder.fragments.MyRequestFragment;

public class MyRequestsAndMyAppointmentsAdapter extends FragmentPagerAdapter {
    public MyRequestsAndMyAppointmentsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new MyRequestFragment();
            case 1:
                return new MyAppointmentFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
