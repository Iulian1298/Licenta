package com.licenta.YCM.fragments;


import android.app.AlertDialog;
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
import com.licenta.YCM.activities.HomeActivity;
import com.licenta.YCM.adapters.RequestOfferAdapter;
import com.licenta.YCM.models.RequestOffer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MyRequestFragment extends Fragment {

    private static final String TAG = "MyRequestFragment";
    private SharedPreferencesManager mPreferencesManager;
    private ArrayList<RequestOffer> mRequestOfferList;
    private RequestOfferAdapter mRequestOfferAdapter;
    private RecyclerView mRequestOfferRecyclerView;
    private String mUrl;
    private Context mCtx;
    private int mRequestOffset;
    private int mRequestLimit;
    private boolean mExistMoreRequest;
    private ProgressBar mGetNewRequestFromDatabase;
    private Button mLoadMoreRequests;
    private boolean mDisplayNoRequest;
    private TextView mNoRequest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_my_request, container, false);
        mCtx = getContext();
        mPreferencesManager = SharedPreferencesManager.getInstance(mCtx);

        final SwipeRefreshLayout refreshServiceRequest = fragmentView.findViewById(R.id.refreshMyRequest);
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
        Log.i(TAG, "refreshPage: called refresh page");
        //crash some time on instant build (apply changes) -> now most probably no
        mRequestOffset = 0;
        if (mRequestOfferList != null && mRequestOfferAdapter != null && mNoRequest != null) {
            mRequestOfferList.clear();
            mRequestOfferAdapter.notifyDataSetChanged();
            try {
                String url = mUrl + "/requestedOffers/getMyRequestsIds/" + mPreferencesManager.getUserId() +
                        "/limit/" + mRequestLimit + "/offset/" + mRequestOffset;
                populateRequestList(url);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (mDisplayNoRequest) {
                /*TextView noRequestPerformed = new TextView(mCtx);
                noRequestPerformed.setText("Nu ai facut nicio cerere de ofertă. Mergi pe pagina service-ului și accesează butonul de cerere din meniu pentru a face o cerere!");
                noRequestPerformed.setPadding(20, 20, 20, 20);
                noRequestPerformed.setGravity(Gravity.CENTER);
                noRequestPerformed.setTextSize(18);
                noRequestPerformed.setTextColor(Color.DKGRAY);
                AlertDialog dialog = new AlertDialog.Builder((HomeActivity) mCtx)
                        .setView(noRequestPerformed)
                        .setPositiveButton("Am ințeles!", null)
                        .create();
                dialog.show();*/
                mNoRequest.setVisibility(View.VISIBLE);
            }
        }
    }

    private void init(View v) {
        Log.i(TAG, "init: ");
        mUrl = mPreferencesManager.getServerUrl();
        mGetNewRequestFromDatabase = v.findViewById(R.id.getNewMyRequestsFromDatabase);
        mLoadMoreRequests = v.findViewById(R.id.loadMoreMyRequests);
        mNoRequest = v.findViewById(R.id.noRequest);
        mExistMoreRequest = true;
        mRequestLimit = 11;
        mRequestOffset = 0;
        mRequestOfferRecyclerView = v.findViewById(R.id.myRequestList);
        mRequestOfferRecyclerView.setLayoutManager(new LinearLayoutManager(mCtx));
        mRequestOfferList = new ArrayList<>();
        mRequestOfferAdapter = new RequestOfferAdapter(mCtx, mRequestOfferList, true);
        mRequestOfferRecyclerView.setAdapter(mRequestOfferAdapter);
        try {
            String url = mUrl + "/requestedOffers/getMyRequestsIds/" + mPreferencesManager.getUserId() +
                    "/limit/" + mRequestLimit + "/offset/" + mRequestOffset;
            populateRequestList(url);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mDisplayNoRequest) {
            /*TextView noRequestPerformed = new TextView(mCtx);
            noRequestPerformed.setText("Nu ai facut nicio cerere de ofertă. Mergi pe pagina service-ului și accesează butonul de cerere din meniu pentru a face o cerere!");
            noRequestPerformed.setPadding(20, 20, 20, 20);
            noRequestPerformed.setGravity(Gravity.CENTER);
            noRequestPerformed.setTextSize(18);
            noRequestPerformed.setTextColor(Color.DKGRAY);
            AlertDialog dialog = new AlertDialog.Builder((HomeActivity) mCtx)
                    .setView(noRequestPerformed)
                    .setPositiveButton("Am ințeles!", null)
                    .create();
            dialog.show();*/
            mNoRequest.setVisibility(View.VISIBLE);
        }
        mLoadMoreRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadMoreRequests.setVisibility(View.GONE);
                mGetNewRequestFromDatabase.setVisibility(View.VISIBLE);
                mRequestOffset += 11;
                try {
                    String url = mUrl + "/requestedOffers/getMyRequestsIds/" + mPreferencesManager.getUserId() +
                            "/limit/" + mRequestLimit + "/offset/" + mRequestOffset;
                    populateRequestList(url);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mGetNewRequestFromDatabase.setVisibility(View.GONE);
                    }
                }, 2000);
            }
        });
        mRequestOfferRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = recyclerView.getChildCount();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                if (dy < 0) {
                    mLoadMoreRequests.setVisibility(View.GONE);
                }
                Log.i(TAG, String.format("onScrolled: dy: %d totalItemCount: %d firstVisibleItemCount: %d visibleItemCount: %d", dy, totalItemCount, firstVisibleItem, visibleItemCount));
                if ((totalItemCount - visibleItemCount) <= (firstVisibleItem + 2)) {
                    if (dy > 0 & mExistMoreRequest) {
                        mLoadMoreRequests.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        mRequestOfferAdapter.setPhoneClickListener(new RequestOfferAdapter.OnPhoneClickListener() {
            @Override
            public void onPhoneClick(View view, int pos) {
                Log.i(TAG, "onPhoneClick: ");
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel: +4" + mRequestOfferList.get(pos).getUserOrServicePhone()));
                startActivity(dialIntent);
            }
        });
        mRequestOfferAdapter.setAcceptClickListener(new RequestOfferAdapter.OnAcceptClickListener() {
            @Override
            public void onAcceptClick(View view, int pos) {
                Log.i(TAG, "onAcceptClick: ");
                acceptDeclineOfferRequestServiceResponse(pos, 1);
            }
        });
        mRequestOfferAdapter.setDeclineClickListener(new RequestOfferAdapter.OnDeclineClickListener() {
            @Override
            public void onDeclineClick(View view, int pos) {
                Log.i(TAG, "onDeclineClick: ");
                acceptDeclineOfferRequestServiceResponse(pos, 2);
            }
        });
        mRequestOfferAdapter.setDeleteClickListener(new RequestOfferAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(View view, int pos) {
                Log.i(TAG, "onDeleteClick: ");
                deleteOfferRequest(pos);
            }
        });
        mRequestOfferAdapter.setOfferUpdateClickListener(new RequestOfferAdapter.OnOfferUpdateClickListener() {
            @Override
            public void onOfferUpdateClick(View view, int pos) {
                Log.i(TAG, "onOfferUpdateClick: ");
                try {
                    Response<JsonObject> response = Ion.with(mCtx)
                            .load("PUT", mUrl + "/requestedOffers/seenBy/" + "1" + "/requestOfferId/" + mRequestOfferList.get(pos).getRequestId())
                            .setHeader("Authorization", mPreferencesManager.getToken())
                            .asJsonObject()
                            .withResponse()
                            .get();
                    if (response.getHeaders().code() == 200) {
                        Log.i(TAG, "onOfferUpdateClick: Request with id: " + mRequestOfferList.get(pos).getRequestId() + "seen");
                        RequestOffer requestOffer = mRequestOfferList.get(pos);
                        requestOffer.setSeen(requestOffer.getSeen() + 1);
                        mRequestOfferList.set(pos, requestOffer);
                        mRequestOfferAdapter.notifyDataSetChanged();
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void acceptDeclineOfferRequestServiceResponse(int pos, int userAcceptance) {
        try {
            String url = mUrl + "/requestedOffer/addClientResponse/" + userAcceptance +
                    "/forRequest/" + mRequestOfferList.get(pos).getRequestId();
            Response<JsonObject> response = Ion.with(mCtx)
                    .load("PUT", url)
                    .setHeader("Authorization", mPreferencesManager.getToken())
                    .asJsonObject()
                    .withResponse()
                    .get();
            if (response.getHeaders().code() == 200) {
                if (userAcceptance == 1) {
                    Log.i(TAG, "acceptDeclineOfferRequestServiceResponse: client confirm offer");
                    Toast.makeText(mCtx, "Ofertă acceptată!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "acceptDeclineOfferRequestServiceResponse: client decline offer");
                    Toast.makeText(mCtx, "Ofertă refuzată!", Toast.LENGTH_SHORT).show();

                }
                RequestOffer requestOffer = mRequestOfferList.get(pos);
                requestOffer.setUserAcceptance(userAcceptance);
                mRequestOfferList.set(pos, requestOffer);
                mRequestOfferAdapter.notifyDataSetChanged();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void deleteOfferRequest(int pos) {
        Log.i(TAG, "deleteOfferRequest: ");
        String url = mUrl + "/requestedOffers/deleteRequestForClientById/" + mRequestOfferList.get(pos).getRequestId();
        try {
            Response<JsonObject> response = Ion.with(mCtx)
                    .load("DELETE", url)
                    .setHeader("Authorization", mPreferencesManager.getToken())
                    .asJsonObject()
                    .withResponse()
                    .get();
            if (response.getHeaders().code() == 200) {
                Log.i(TAG, "deleteOfferRequest: Request with id: " + mRequestOfferList.get(pos).getRequestId() + "deleted");
                mRequestOfferList.remove(pos);
                mRequestOfferAdapter.notifyDataSetChanged();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void populateRequestList(String url) throws ExecutionException, InterruptedException {
        Log.i(TAG, "populateRequestList: ");
        Response<JsonObject> response = Ion.with(mCtx)
                .load("GET", url)
                .setHeader("Authorization", mPreferencesManager.getToken())
                .asJsonObject()
                .withResponse()
                .get();
        if (response.getHeaders().code() == 200) {
            Log.i(TAG, "populateRequestList: requests Ids received");
            if (response.getResult() != null) {
                final JsonArray requestsId = response.getResult().get("Ids").getAsJsonArray();
                mDisplayNoRequest = requestsId.size() == 0;
                mExistMoreRequest = requestsId.size() == 11;
                for (int i = 0; i < requestsId.size(); i++) {
                    final String requestId = requestsId.get(i).getAsString();
                    Log.i(TAG, "populateRequestList: add request with Id: " + requestId);
                    final AsyncRequest httpGetRequest = new AsyncRequest(mPreferencesManager, new AsyncRequest.Listener() {
                        @Override
                        public void onResult(String result) {
                            if (!result.isEmpty()) {
                                try {
                                    Log.i(TAG, "onResult: Request received");
                                    JSONObject request = new JSONObject(result).getJSONObject("request");
                                    mRequestOfferList.add(new RequestOffer(
                                            request.getString("id"),
                                            request.getString("serviceName"),
                                            request.getString("userName"),
                                            request.getString("request"),
                                            request.getBoolean("withUserParts"),
                                            request.getString("carType"),
                                            request.getString("carModel"),
                                            request.getString("carYear"),
                                            request.getString("carVin"),
                                            request.getString("serviceResponse"),
                                            request.getString("servicePriceResponse"),
                                            request.getString("fixStartDate"),
                                            request.getString("fixEndDate"),
                                            request.getInt("serviceAcceptance"),
                                            request.getInt("userAcceptance"),
                                            request.getString("servicePhoneNumber"),
                                            request.getInt("seen")));
                                    mRequestOfferAdapter.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "onResult: No Result");
                                }
                            } else {
                                Log.e(TAG, "onResult: Request with id: " + requestId + "not received!");
                            }
                        }
                    });
                    String urlRequest = mUrl + "/requestedOffers/getById/" + requestId + "/userOrService/" + 1;
                    httpGetRequest.execute("GET", urlRequest);
                }
            } else {
                Log.e(TAG, "populateRequestList: All Ids not received");
            }
        } else {
            Toast.makeText(mCtx, "Error code: " + response.getHeaders().code(), Toast.LENGTH_SHORT).show();
        }
    }

    public MyRequestFragment() {
    }
}
