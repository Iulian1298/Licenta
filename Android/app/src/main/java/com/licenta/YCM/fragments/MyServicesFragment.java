package com.licenta.YCM.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.licenta.YCM.R;


public class MyServicesFragment extends Fragment {


    public MyServicesFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_my_services, container, false);
        return fragmentView;
    }

}
