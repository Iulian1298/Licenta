package com.licenta.YCM.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.licenta.YCM.adapters.RequestOfferAdapter;
import com.licenta.YCM.models.RequestOffer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class OfferRequestsActivity extends AppCompatActivity {
    private static final String TAG = "OfferRequestsActivity";
    private Context mCtx;
    private SharedPreferencesManager mPreferencesManager;
    private String mUrl;
    private int mRequestOffset;
    private int mRequestLimit;
    private boolean mExistMoreRequest;
    private ArrayList<RequestOffer> mRequestOfferList;
    private RequestOfferAdapter mRequestOfferAdapter;
    private RecyclerView mRequestOfferRecyclerView;
    private String mServiceId;
    private ProgressBar mGetNewRequestFromDatabase;
    private Button mLoadMoreRequests;
    private boolean mDisplayNoRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_requests);
        mPreferencesManager = SharedPreferencesManager.getInstance(this);
        mCtx = getApplicationContext();

        final SwipeRefreshLayout refreshServiceRequest = findViewById(R.id.refreshServiceRequest);
        refreshServiceRequest.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                init();
                refreshServiceRequest.setRefreshing(false);
            }
        });
        init();
    }

    private void init() {
        Log.i(TAG, "init: ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Cereri de ofertă");
        mUrl = mPreferencesManager.getServerUrl();
        mGetNewRequestFromDatabase = findViewById(R.id.getNewRequestsFromDatabase);
        mLoadMoreRequests = findViewById(R.id.loadMoreRequests);
        mExistMoreRequest = true;
        mRequestLimit = 11;
        mRequestOffset = 0;
        Intent intent = getIntent();
        mServiceId = intent.getStringExtra("serviceId");
        mRequestOfferRecyclerView = findViewById(R.id.serviceRequestList);
        mRequestOfferRecyclerView.setLayoutManager(new LinearLayoutManager(mCtx));
        mRequestOfferList = new ArrayList<>();
        mRequestOfferAdapter = new RequestOfferAdapter(mCtx, mRequestOfferList, false);
        mRequestOfferRecyclerView.setAdapter(mRequestOfferAdapter);
        try {
            String url = mUrl + "/requestedOffers/getServiceRequestsIds/" + mServiceId +
                    "/limit/" + mRequestLimit + "/offset/" + mRequestOffset;
            populateRequestList(url);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mDisplayNoRequest) {
            TextView noRequestPerformed = new TextView(mCtx);
            noRequestPerformed.setText("Nu ai primit nicio cerere de ofertă!");
            noRequestPerformed.setPadding(20, 20, 20, 0);
            noRequestPerformed.setGravity(Gravity.CENTER);
            noRequestPerformed.setTextSize(18);
            noRequestPerformed.setTextColor(Color.DKGRAY);
            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(OfferRequestsActivity.this)
                    .setView(noRequestPerformed)
                    .setPositiveButton("Am ințeles!", null)
                    .create();
            dialog.show();
        }
        mLoadMoreRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadMoreRequests.setVisibility(View.GONE);
                mGetNewRequestFromDatabase.setVisibility(View.VISIBLE);
                mRequestOffset += 11;
                try {
                    String url = mUrl + "/requestedOffers/getServiceRequestsIds/" + mServiceId +
                            "/limit/" + mRequestLimit + "/offset/" + mRequestOffset;
                    populateRequestList(url);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
                acceptOfferRequest(pos);
            }
        });
        mRequestOfferAdapter.setDeclineClickListener(new RequestOfferAdapter.OnDeclineClickListener() {
            @Override
            public void onDeclineClick(View view, int pos) {
                Log.i(TAG, "onDeclineClick: ");
                declineOfferRequest(pos);
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
                            .load("PUT", mUrl + "/requestedOffers/seenBy/" + "2" + "/requestOfferId/" + mRequestOfferList.get(pos).getRequestId())
                            .setHeader("Authorization", mPreferencesManager.getToken())
                            .asJsonObject()
                            .withResponse()
                            .get();
                    if (response.getHeaders().code() == 200) {
                        Log.i(TAG, "onOfferUpdateClick: Request with id: " + mRequestOfferList.get(pos).getRequestId() + "seen");
                        RequestOffer requestOffer = mRequestOfferList.get(pos);
                        requestOffer.setSeen(requestOffer.getSeen() + 2);
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

    private void acceptOfferRequest(final int pos) {
        Log.i(TAG, "acceptOfferRequest: ");
        TextView acceptOfferRequestTitle = new TextView(mCtx);
        acceptOfferRequestTitle.setText("Raspunde cererii!");
        acceptOfferRequestTitle.setGravity(Gravity.CENTER);
        acceptOfferRequestTitle.setPadding(10, 10, 10, 10);
        acceptOfferRequestTitle.setTextSize(18);
        acceptOfferRequestTitle.setTextColor(Color.DKGRAY);
        View acceptOfferRequestContent = getLayoutInflater().inflate(R.layout.accept_offer_request_layout, null);
        final EditText startDate = acceptOfferRequestContent.findViewById(R.id.serviceResponseStartDate);
        final EditText endDate = acceptOfferRequestContent.findViewById(R.id.serviceResponseEndDate);
        final EditText price = acceptOfferRequestContent.findViewById(R.id.serviceResponsePrice);
        final AlertDialog acceptOfferRequest = new AlertDialog.Builder(OfferRequestsActivity.this)
                .setCustomTitle(acceptOfferRequestTitle)
                .setView(acceptOfferRequestContent)
                .setPositiveButton("Confirmă", null)
                .setNegativeButton("Anulează", null)
                .create();
        acceptOfferRequest.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button confirm = acceptOfferRequest.getButton(DialogInterface.BUTTON_POSITIVE);
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "onClick: startDate: " + startDate.getText().toString()
                                + " endDate: " + endDate.getText().toString() + " price: " + price.getText());
                        boolean resultOk = true;
                        if (endDate.getText().toString().trim().isEmpty()) {
                            endDate.setError("Completează campul");
                            resultOk = false;
                        }
                        if (startDate.getText().toString().trim().isEmpty()) {
                            startDate.setError("Completează campul");
                            resultOk = false;
                        }
                        if (price.getText().toString().trim().isEmpty()) {
                            price.setError("Completează campul");
                            resultOk = false;
                        }
                        if (resultOk) {
                            JsonObject jsonBody = new JsonObject();
                            jsonBody.addProperty("requestId", mRequestOfferList.get(pos).getRequestId());
                            jsonBody.addProperty("startDate", startDate.getText().toString().trim());
                            jsonBody.addProperty("endDate", endDate.getText().toString().trim());
                            jsonBody.addProperty("price", price.getText().toString().trim());
                            jsonBody.addProperty("serviceAcceptance", 1);
                            try {
                                Response<JsonObject> response = Ion.with(mCtx)
                                        .load("POST", mUrl + "/requestedOffers/addServiceResponse")
                                        .setHeader("Authorization", mPreferencesManager.getToken())
                                        .setJsonObjectBody(jsonBody)
                                        .asJsonObject()
                                        .withResponse()
                                        .get();
                                if (response.getHeaders().code() == 200) {
                                    Log.i(TAG, "onClick: service added offer response");
                                    Toast.makeText(mCtx, "Ofertă adaugată cu succes!", Toast.LENGTH_SHORT).show();
                                    RequestOffer requestOffer = mRequestOfferList.get(pos);
                                    requestOffer.setServiceAcceptance(1);
                                    requestOffer.setSeen(2);
                                    requestOffer.setFixStartDate(startDate.getText().toString().trim());
                                    requestOffer.setFixEndDate(endDate.getText().toString().trim());
                                    requestOffer.setServicePriceResponse(price.getText().toString().trim());
                                    mRequestOfferList.set(pos, requestOffer);
                                    mRequestOfferAdapter.notifyDataSetChanged();
                                    acceptOfferRequest.dismiss();
                                } else {
                                    if (response.getHeaders().code() == 404) {
                                        Log.i(TAG, "onClick: offer deleted by user since server last refresh");
                                        Toast.makeText(mCtx, "Oferta a fost ștearsa de user intre timp!", Toast.LENGTH_SHORT).show();
                                        mRequestOfferList.remove(pos);
                                        mRequestOfferAdapter.notifyDataSetChanged();
                                        acceptOfferRequest.dismiss();
                                    }
                                }
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            confirm.setError("Eroare");
                        }
                    }
                });
            }
        });
        acceptOfferRequest.show();
    }

    private void declineOfferRequest(final int pos) {
        Log.i(TAG, "declineOfferRequest: ");
        TextView declineOfferRequestTitle = new TextView(mCtx);
        declineOfferRequestTitle.setText("Raspunde cererii!");
        declineOfferRequestTitle.setGravity(Gravity.CENTER);
        declineOfferRequestTitle.setPadding(10, 10, 10, 10);
        declineOfferRequestTitle.setTextSize(18);
        declineOfferRequestTitle.setTextColor(Color.DKGRAY);
        final EditText declineResponse = new EditText(mCtx);
        final AlertDialog declineOfferRequest = new AlertDialog.Builder(OfferRequestsActivity.this)
                .setCustomTitle(declineOfferRequestTitle)
                .setView(declineResponse)
                .setPositiveButton("Confirmă", null)
                .setNegativeButton("Anulează", null)
                .create();
        declineOfferRequest.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button confirm = declineOfferRequest.getButton(DialogInterface.BUTTON_POSITIVE);
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "onClick: ");
                        boolean resultOk = true;
                        if (declineResponse.getText().toString().trim().isEmpty()) {
                            declineResponse.setError("Completează campul");
                            resultOk = false;
                        }
                        if (resultOk) {
                            JsonObject jsonBody = new JsonObject();
                            jsonBody.addProperty("requestId", mRequestOfferList.get(pos).getRequestId());
                            jsonBody.addProperty("serviceResponse", declineResponse.getText().toString().trim());
                            jsonBody.addProperty("serviceAcceptance", 2);
                            try {
                                Response<JsonObject> response = Ion.with(mCtx)
                                        .load("POST", mUrl + "/requestedOffers/addServiceResponse")
                                        .setHeader("Authorization", mPreferencesManager.getToken())
                                        .setJsonObjectBody(jsonBody)
                                        .asJsonObject()
                                        .withResponse()
                                        .get();
                                if (response.getHeaders().code() == 200) {
                                    Log.i(TAG, "onClick: service added offer response");
                                    Toast.makeText(mCtx, "Raspuns adaugat cu succes!", Toast.LENGTH_SHORT).show();
                                    RequestOffer requestOffer = mRequestOfferList.get(pos);
                                    requestOffer.setServiceAcceptance(2);
                                    requestOffer.setSeen(2);
                                    requestOffer.setServiceResponse(declineResponse.getText().toString().trim());
                                    mRequestOfferList.set(pos, requestOffer);
                                    mRequestOfferAdapter.notifyDataSetChanged();
                                    declineOfferRequest.dismiss();
                                } else {
                                    if (response.getHeaders().code() == 404) {
                                        Log.i(TAG, "onClick: offer deleted by user since server last refresh");
                                        Toast.makeText(mCtx, "Oferta a fost ștearsa de user intre timp!", Toast.LENGTH_SHORT).show();
                                        mRequestOfferList.remove(pos);
                                        mRequestOfferAdapter.notifyDataSetChanged();
                                        declineOfferRequest.dismiss();
                                    }
                                }
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
        declineOfferRequest.show();
    }

    private void deleteOfferRequest(int pos) {
        Log.i(TAG, "deleteOfferRequest: ");
        String url = mUrl + "/requestedOffers/deleteRequestForServiceById/" + mRequestOfferList.get(pos).getRequestId();
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
                                            request.getString("userPhoneNumber"),
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
                    String urlRequest = mUrl + "/requestedOffers/getById/" + requestId + "/userOrService/" + 0;
                    httpGetRequest.execute("GET", urlRequest);
                }
            } else {
                Log.e(TAG, "populateRequestList: All Ids not received");
            }
        } else {
            Toast.makeText(mCtx, "Error code: " + response.getHeaders().code(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.v(TAG, "onSupportNavigateUp()");
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy()");
        super.onDestroy();
    }
}
