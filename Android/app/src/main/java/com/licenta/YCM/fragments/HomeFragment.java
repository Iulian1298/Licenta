package com.licenta.YCM.fragments;

import android.app.Activity;
import android.app.AlertDialog;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.licenta.YCM.AsyncHttpRequest;
import com.licenta.YCM.R;
import com.licenta.YCM.ServiceAutoAdapter;
import com.licenta.YCM.SharedPreferencesManager;
import com.licenta.YCM.activities.ServiceAutoActivity;
import com.licenta.YCM.models.ServiceAuto;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private SharedPreferencesManager mPreferencesManager;
    private ArrayList<ServiceAuto> mServiceAutoList;
    private ServiceAutoAdapter mServiceAutoAdapter;
    private RecyclerView mServiceAutoRecyclerView;
    private SearchView mSearchView;
    private String mServiceByIdURL;
    private String mAllServicesIdsURL;
    private Context mCtx;
    private boolean mIsLoggedIn;
    private String mServiceIdFilterBy;

    private int mLastElementClickedPosition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_home, container, false);
        mCtx = getContext();
        mPreferencesManager = SharedPreferencesManager.getInstance(mCtx);
        try {
            mIsLoggedIn = mPreferencesManager.isLoggedIn();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        setHasOptionsMenu(true);

        init(fragmentView);


        return fragmentView;
    }

    private void init(View v) {
        Log.i(TAG, "init: ");
        mServiceAutoRecyclerView = v.findViewById(R.id.servicesList);
        mServiceAutoRecyclerView.setLayoutManager(new LinearLayoutManager(mCtx));
        mServiceByIdURL = "http://10.0.2.2:5000/services/getById/";
        mAllServicesIdsURL = "http://10.0.2.2:5000/services/getAllIds";
        mServiceIdFilterBy = "http://10.0.2.2:5000/services/getIdsFilterBy/";
        mServiceAutoList = new ArrayList<>();
        mServiceAutoAdapter = new ServiceAutoAdapter(mCtx, mServiceAutoList);
        mServiceAutoRecyclerView.addItemDecoration(new DividerItemDecoration(mCtx, DividerItemDecoration.VERTICAL));
        mServiceAutoRecyclerView.setAdapter(mServiceAutoAdapter);
        mLastElementClickedPosition = 0;
        populateServiceAutoList();

        mServiceAutoAdapter.setClickListener(new ServiceAutoAdapter.OnItemServiceAutoClickListener() {
            @Override
            public void onItemServiceAutoClick(View view, int pos) {
                Log.i(TAG, "onItemServiceAutoClick: ");

                //Toast.makeText(mCtx, "You clicked item at position: " + pos, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mCtx, ServiceAutoActivity.class);
                ServiceAuto serviceAuto = mServiceAutoAdapter.getElemInFilteredListAtPos(pos);
                intent.putExtra("serviceId", serviceAuto.getServiceId());
                intent.putExtra("logoImage", createLocalImageFromBitmap(serviceAuto.getImage()));
                intent.putExtra("serviceName", serviceAuto.getName());
                intent.putExtra("description", serviceAuto.getDescription());
                intent.putExtra("latitude", serviceAuto.getLatitude());
                intent.putExtra("longitude", serviceAuto.getLongitude());
                intent.putExtra("address", serviceAuto.getAddress());
                intent.putExtra("rating", serviceAuto.getRating());
                intent.putExtra("contactPhoneNumber", serviceAuto.getContactPhoneNumber());
                intent.putExtra("contactEmail", serviceAuto.getContactEmail());
                mLastElementClickedPosition = pos;
                startActivityForResult(intent, 1);
            }
        });
    }

    private String createLocalImageFromBitmap(Bitmap bitmap) {
        Log.i(TAG, "createLocalImageFromBitmap: ");
        String fileName = "myImage";
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = mCtx.openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }

    private Bitmap stringToBitmap(String imageEncoded) {
        Log.i(TAG, "stringToBitmap: ");
        try {
            String cleanImage = imageEncoded.
                    replace("dataimage/pngbase64", "").
                    replace("dataimage/jpegbase64", "");
            //Log.i(TAG, "stringToBitmap: " + cleanImage);
            byte[] encodeByte = Base64.decode(cleanImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    private void populateServiceAutoList() {
        Log.i(TAG, "populateServiceAutoList: ");
        Ion.with(mCtx)
                .load("GET", mAllServicesIdsURL)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> response) {
                        if (response != null) {
                            if (response.getHeaders().code() == 200) {
                                Log.i(TAG, "populateServiceAutoList: services Ids received");
                                final JsonArray servicesId = response.getResult().get("ids").getAsJsonArray();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int i = 0; i < servicesId.size(); i++) {
                                            try {
                                                Thread.sleep(250);
                                            } catch (InterruptedException e1) {
                                                e1.printStackTrace();
                                            }
                                            final String finalI = servicesId.get(i).getAsString();
                                            Log.i(TAG, "populateServiceAutoList: add services with ID: " + finalI);
                                            final AsyncHttpRequest httpGetService = new AsyncHttpRequest(new AsyncHttpRequest.Listener() {
                                                @Override
                                                public void onResult(String result) {
                                                    if (!result.isEmpty()) {
                                                        try {
                                                            Log.i(TAG, "onResult: Received service with id: " + finalI);
                                                            JSONObject service = new JSONObject(result).getJSONObject("service");
                                                            mServiceAutoList.add(new ServiceAuto(
                                                                    service.getString("id"),
                                                                    stringToBitmap(service.getString("imageEncoded")),
                                                                    service.getString("name"),
                                                                    service.getString("description"),
                                                                    service.getString("address"),
                                                                    Float.valueOf(service.getString("rating")),
                                                                    service.getString("phoneNumber"),
                                                                    service.getString("email"),
                                                                    Double.valueOf(service.getString("latitude")),
                                                                    Double.valueOf(service.getString("longitude"))));
                                                            mServiceAutoAdapter.notifyDataSetChanged();
                                                        } catch (JSONException e1) {
                                                            e1.printStackTrace();
                                                            Log.e(TAG, "onResult: NoResult");
                                                        }
                                                    } else {
                                                        Log.e(TAG, "onResult: Service with id: " + finalI + "not received!");
                                                    }
                                                }
                                            });
                                            httpGetService.execute("GET", mServiceByIdURL + finalI);
                                        }
                                    }
                                }).start();
                            } else {
                                Toast.makeText(mCtx, "Error code: " + response.getHeaders().code(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "onCompleted: null response");
                        }
                    }
                });
    }

    private void populateServiceAutoList2() {
        Log.i(TAG, "populateServiceAutoList: ");
        Ion.with(mCtx)
                .load("GET", mAllServicesIdsURL)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> response) {
                        if (response != null) {
                            if (response.getHeaders().code() == 200) {
                                Log.i(TAG, "populateServiceAutoList: services Ids received");
                                final JsonArray servicesId = response.getResult().get("ids").getAsJsonArray();
                                for (int i = 0; i < servicesId.size(); i++) {
                                    final String finalI = servicesId.get(i).getAsString();
                                    Log.i(TAG, "populateServiceAutoList: add services with ID: " + finalI);

                                    OkHttpClient client = new OkHttpClient();
                                    Request request = new Request.Builder()
                                            .url(mServiceByIdURL + finalI)
                                            .get()
                                            .build();
                                    try {
                                        okhttp3.Response res = client.newCall(request).execute();
                                        System.out.println(res.body().toString());
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            } else {
                                Toast.makeText(mCtx, "Error code: " + response.getHeaders().code(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "onCompleted: null response");
                        }
                    }
                });
    }

    private void populateServiceAutoList1() {
        Log.i(TAG, "populateServiceAutoList: ");
        Ion.with(mCtx)
                .load("GET", mAllServicesIdsURL)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> response) {
                        if (response != null) {
                            if (response.getHeaders().code() == 200) {
                                Log.i(TAG, "populateServiceAutoList: services Ids received");
                                final JsonArray servicesId = response.getResult().get("ids").getAsJsonArray();
                                for (int i = 0; i < servicesId.size(); i++) {
                                    final String finalI = servicesId.get(i).getAsString();
                                    Log.i(TAG, "populateServiceAutoList: add services with ID: " + finalI);
                                    AsyncHttpRequest httpGetService = new AsyncHttpRequest(new AsyncHttpRequest.Listener() {
                                        @Override
                                        public void onResult(String result) {
                                            if (!result.isEmpty()) {
                                                try {
                                                    Log.i(TAG, "onResult: Received service with id: " + finalI);
                                                    JSONObject service = new JSONObject(result).getJSONObject("service");
                                                    mServiceAutoList.add(new ServiceAuto(
                                                            service.getString("id"),
                                                            null,
                                                            service.getString("name"),
                                                            service.getString("description"),
                                                            service.getString("address"),
                                                            Float.valueOf(service.getString("rating")),
                                                            service.getString("phoneNumber"),
                                                            service.getString("email"),
                                                            Double.valueOf(service.getString("latitude")),
                                                            Double.valueOf(service.getString("longitude"))));
                                                    mServiceAutoAdapter.notifyDataSetChanged();
                                                } catch (JSONException e1) {
                                                    e1.printStackTrace();
                                                    Log.e(TAG, "onResult: NoResult");
                                                }
                                            } else {
                                                Log.e(TAG, "onResult: Service with id: " + finalI + "not received!");
                                            }
                                        }
                                    });
                                    httpGetService.execute("GET", "http://10.0.2.2:5000/services/getInfoById/" + finalI);
                                }
                            } else {
                                Toast.makeText(mCtx, "Error code: " + response.getHeaders().code(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "onCompleted: null response");
                        }
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult: ");
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "onActivityResult: A new comment was added or deleted!");
                ServiceAuto serviceAuto = mServiceAutoList.get(mLastElementClickedPosition);
                serviceAuto.setRating(data.getFloatExtra("creationDeletionNewRating", 0));
                mServiceAutoList.set(mLastElementClickedPosition, serviceAuto);
                mServiceAutoAdapter.notifyDataSetChanged();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAG, "onActivityResult: No comment was added or deleted!");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(TAG, "onCreateOptionsMenu: ");
        inflater.inflate(R.menu.home_main_menu, menu);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.searchButton).getActionView();
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        mSearchView.setMaxWidth(Integer.MAX_VALUE);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG, "onQueryTextSubmit: ");
                mServiceAutoAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(TAG, "onQueryTextChange: ");
                mServiceAutoAdapter.getFilter().filter(newText);
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        Log.i(TAG, "onOptionsItemSelected: ");
        switch (menuItem.getItemId()) {
            case R.id.filterButton:
                Log.i(TAG, "Filter button pressed");
                final View advancedFilterView = getLayoutInflater().inflate(R.layout.filter_popup_layout, null);
                final RatingBar ratingMinBar = advancedFilterView.findViewById(R.id.ratingBarMinFilter);
                final RatingBar ratingMaxBar = advancedFilterView.findViewById(R.id.ratingBarMaxFilter);
                final EditText givenNameFilter = advancedFilterView.findViewById(R.id.givenNameFilter);
                final EditText rangeInput = advancedFilterView.findViewById(R.id.rangeInputFilter);
                final EditText cityInput = advancedFilterView.findViewById(R.id.cityInputFilter);
                final CheckBox resetFilterCheck = advancedFilterView.findViewById(R.id.resetFilterCheck);
                if (!mPreferencesManager.getPermissionLocation()) {
                    TextView rangeInputLabel = advancedFilterView.findViewById(R.id.range);
                    rangeInputLabel.setVisibility(View.GONE);
                    rangeInput.setVisibility(View.GONE);
                }
                TextView filterTitle = new TextView(getContext());
                filterTitle.setText("Filtrare avansata!");
                filterTitle.setGravity(Gravity.CENTER);
                filterTitle.setPadding(10, 10, 10, 10);
                filterTitle.setTextSize(18);
                filterTitle.setTextColor(Color.DKGRAY);
                AlertDialog advancedFilterPopUp = new AlertDialog.Builder(getActivity())
                        .setCustomTitle(filterTitle)
                        .setView(advancedFilterView)
                        .setPositiveButton("Filtreaza", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (ratingMinBar.getRating() > ratingMaxBar.getRating()) {
                                    Toast.makeText(getActivity(), "Rating-ul minim trebuie sa fie mai mic ca rating-ul maxim!", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (resetFilterCheck.isChecked()) {
                                        mServiceAutoAdapter.getFilter(null).filter("");
                                    } else {
                                        if (!(ratingMinBar.getRating() == 0
                                                && ratingMaxBar.getRating() == 0
                                                && givenNameFilter.getText().toString().isEmpty()
                                                && rangeInput.getText().toString().isEmpty()
                                                && cityInput.getText().toString().isEmpty())) {
                                            JsonObject advFilterParams = new JsonObject();
                                            advFilterParams.addProperty("minRating", Float.toString(ratingMinBar.getRating()));
                                            advFilterParams.addProperty("maxRating", Float.toString(ratingMaxBar.getRating()));
                                            advFilterParams.addProperty("givenNameFilter", givenNameFilter.getText().toString());
                                            advFilterParams.addProperty("distanceInput", rangeInput.getText().toString());
                                            advFilterParams.addProperty("cityInput", cityInput.getText().toString());
                                            advFilterParams.addProperty("longitute", mPreferencesManager.getUserLongitude());
                                            advFilterParams.addProperty("latitude", mPreferencesManager.getUserLatitude());
                                            try {
                                                Response<JsonObject> response = Ion.with(mCtx)
                                                        .load("GET", mServiceIdFilterBy)
                                                        .setJsonObjectBody(advFilterParams)
                                                        .asJsonObject()
                                                        .withResponse()
                                                        .get();
                                            } catch (ExecutionException e) {
                                                e.printStackTrace();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            mServiceAutoAdapter.getFilter(advFilterParams).filter("");
                                        }
                                    }
                                }
                            }
                        })
                        .setNegativeButton("Anuleaza", null)
                        .create();
                advancedFilterPopUp.show();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public HomeFragment() {

    }
}
