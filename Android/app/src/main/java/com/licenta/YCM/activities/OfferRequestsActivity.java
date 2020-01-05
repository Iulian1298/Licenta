package com.licenta.YCM.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.licenta.YCM.R;

import java.util.Objects;

public class OfferRequestsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_requests);

        //move on init
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Cereri de oferta");
    }
}
