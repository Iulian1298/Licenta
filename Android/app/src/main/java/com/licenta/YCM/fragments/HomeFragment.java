package com.licenta.YCM.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.licenta.YCM.AsyncHttpRequest;
import com.licenta.YCM.R;
import com.licenta.YCM.activities.AuthenticationActivity;
import com.licenta.YCM.activities.HomeActivity;
import com.licenta.YCM.adapters.ServiceAutoAdapter;
import com.licenta.YCM.SharedPreferencesManager;
import com.licenta.YCM.activities.ServiceAutoActivity;
import com.licenta.YCM.models.ServiceAuto;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private SharedPreferencesManager mPreferencesManager;
    private ArrayList<ServiceAuto> mServiceAutoList;
    private ServiceAutoAdapter mServiceAutoAdapter;
    private RecyclerView mServiceAutoRecyclerView;
    private SearchView mSearchView;
    private String mUrl;
    private Context mCtx;
    private boolean mIsLoggedIn;
    private boolean mShowOnlyMyServices;
    private FloatingActionButton mAddServiceFloatingButton;
    private Dialog mAddService;
    private EditText mAddServiceName;
    private EditText mAddServiceAddress;
    private EditText mAddServiceCity;
    private EditText mAddServicePhone;
    private EditText mAddServiceEmail;
    private EditText mAddServiceDescription;
    private EditText mAddServiceAcceptedBrand;
    private CheckBox mRepairServiceCheck;
    private CheckBox mServiceTireCheck;
    private CheckBox mServiceChassisCheck;
    private CheckBox mServiceItpCheck;
    private ImageView mAddServiceImage;
    private boolean mUseDefaultServiceImage;
    private int mLastElementClickedPosition;
    private View mFragmentView;
    private Uri mAddServiceImageUri;
    private ProgressBar mAddServiceProgressBar;
    private FloatingActionButton mAddServiceButton;
    private int mServiceAutoOffset;
    private int mServiceAutoLimit;
    private ProgressBar mGetNewServicesFromDatabase;
    private Button mLoadMoreServices;
    private boolean mExistMoreServices;
    private String mCompleteUrl;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_home, container, false);
        mFragmentView = fragmentView;
        mCtx = getContext();
        mPreferencesManager = SharedPreferencesManager.getInstance(mCtx);
        mShowOnlyMyServices = mPreferencesManager.getOnlyMyServices();


        Log.i(TAG, "onCreateView: show only my services: " + mShowOnlyMyServices);
        /*try {
            mIsLoggedIn = mPreferencesManager.isLoggedIn();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        final SwipeRefreshLayout refreshServices = mFragmentView.findViewById(R.id.refreshServices);
        refreshServices.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshPage();
                refreshServices.setRefreshing(false);
            }
        });


        setHasOptionsMenu(true);

        init(fragmentView);


        return fragmentView;
    }

    public void refreshLocation() {
        mServiceAutoAdapter.notifyDataSetChanged();
    }

    private void refreshPage() {
        Log.i(TAG, "refreshPage: called refresh page");
        //crash some time on instant build (apply changes) -> now most probably no
        mServiceAutoOffset = 0;
        if (mServiceAutoList != null && mServiceAutoAdapter != null) {
            mServiceAutoList.clear();
            mServiceAutoAdapter.notifyDataSetChanged();
            populateServiceAutoList();
        }
    }

    private void init(View v) {
        Log.i(TAG, "init: ");
        mServiceAutoRecyclerView = v.findViewById(R.id.servicesList);
        mAddServiceFloatingButton = v.findViewById(R.id.addServiceFloatingButton);
        mGetNewServicesFromDatabase = v.findViewById(R.id.getNewServicesFromDatabase);
        mServiceAutoRecyclerView.setLayoutManager(new LinearLayoutManager(mCtx));
        //mUrl = "https://agile-harbor-57300.herokuapp.com";
        mUrl = "http://10.0.2.2:5000";
        mServiceAutoList = new ArrayList<>();
        mServiceAutoAdapter = new ServiceAutoAdapter(mCtx, mServiceAutoList);
        //mServiceAutoRecyclerView.addItemDecoration(new DividerItemDecoration(mCtx, DividerItemDecoration.VERTICAL));
        mServiceAutoRecyclerView.setAdapter(mServiceAutoAdapter);
        mLoadMoreServices = v.findViewById(R.id.loadMoreServices);
        mExistMoreServices = true;
        mLastElementClickedPosition = 0;
        mServiceAutoLimit = 11;
        mServiceAutoOffset = 0;
        if (mShowOnlyMyServices) {
            mCompleteUrl = mUrl + "/services/getIdsBetweenForMyServices/" + mPreferencesManager.getUserId() + "/offset/" + mServiceAutoOffset + "/limit/"
                    + mServiceAutoLimit;
        } else {
            mCompleteUrl = mUrl + "/service/getIdsBetween/offset/" + mServiceAutoOffset + "/limit/"
                    + mServiceAutoLimit;
        }
        populateServiceAutoList();
        mLoadMoreServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadMoreServices.setVisibility(View.GONE);
                mGetNewServicesFromDatabase.setVisibility(View.VISIBLE);
                mServiceAutoOffset += 11;
                if (mShowOnlyMyServices) {
                    mCompleteUrl = mUrl + "/services/getIdsBetweenForMyServices/" + mPreferencesManager.getUserId() + "/offset/" + mServiceAutoOffset + "/limit/"
                            + mServiceAutoLimit;
                } else {
                    mCompleteUrl = mUrl + "/service/getIdsBetween/offset/" + mServiceAutoOffset + "/limit/"
                            + mServiceAutoLimit;

                }
                populateServiceAutoList();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mGetNewServicesFromDatabase.setVisibility(View.GONE);
                    }
                }, 2000);
            }
        });
        mServiceAutoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = recyclerView.getChildCount();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                if (dy < -10) {
                    mAddServiceFloatingButton.show();
                }
                if (dy > 10) {
                    mAddServiceFloatingButton.hide();
                }
                if (dy < 0) {
                    mLoadMoreServices.setVisibility(View.GONE);
                }
                Log.i(TAG, String.format("onScrolled: dy: %d mServiceAutoLimit: %d firstVisibleItemCount: %d visibleItemCount: %d", dy, mServiceAutoLimit, firstVisibleItem, visibleItemCount));
                if ((totalItemCount - visibleItemCount) <= (firstVisibleItem + 2)) {
                    if (dy > 0 & mExistMoreServices) {
                        mLoadMoreServices.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        initAddServicePopUp();
        mServiceAutoAdapter.setClickListener(new ServiceAutoAdapter.OnItemServiceAutoClickListener() {
            @Override
            public void onItemServiceAutoClick(View view, int pos) {
                Log.i(TAG, "onItemServiceAutoClick: ");

                //Toast.makeText(mCtx, "You clicked item at position: " + pos, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mCtx, ServiceAutoActivity.class);
                ServiceAuto serviceAuto = mServiceAutoAdapter.getElemInFilteredListAtPos(pos);
                intent.putExtra("serviceId", serviceAuto.getServiceId());
                intent.putExtra("logoImage", serviceAuto.getImage().toString());
                intent.putExtra("serviceName", serviceAuto.getName());
                intent.putExtra("description", serviceAuto.getDescription());
                intent.putExtra("latitude", serviceAuto.getLatitude());
                intent.putExtra("longitude", serviceAuto.getLongitude());
                intent.putExtra("address", serviceAuto.getAddress());
                intent.putExtra("city", serviceAuto.getCity());
                intent.putExtra("rating", serviceAuto.getRating());
                intent.putExtra("contactPhoneNumber", serviceAuto.getContactPhoneNumber());
                intent.putExtra("contactEmail", serviceAuto.getContactEmail());
                intent.putExtra("ownerId", serviceAuto.getOwnerId());
                intent.putExtra("serviceType", serviceAuto.getType());
                intent.putExtra("acceptedBrands", serviceAuto.getAcceptedBrands());
                mLastElementClickedPosition = pos;
                startActivityForResult(intent, 1);
            }
        });
        mAddServiceFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: show add service popup clicked");
                boolean isLoggedIn = false;
                try {
                    isLoggedIn = mPreferencesManager.isLoggedIn();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!isLoggedIn) {
                    showPopUpNotLogged();
                } else {
                    if (ContextCompat.checkSelfPermission(mCtx, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mAddService.show();
                    } else {
                        ActivityCompat.requestPermissions((HomeActivity) mCtx, new String[]{ACCESS_FINE_LOCATION}, 12);
                    }
                }
            }
        });
    }

    private void initAddServicePopUp() {
        Log.i(TAG, "initAddServicePopUp: ");
        mUseDefaultServiceImage = true;
        mAddService = new Dialog(mCtx);
        mAddService.setContentView(R.layout.add_service_popup_layout);
        mAddService.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mAddService.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        mAddService.getWindow().getAttributes().gravity = Gravity.TOP;
        mAddServiceName = mAddService.findViewById(R.id.addServiceName);
        mAddServiceAddress = mAddService.findViewById(R.id.addServiceAddress);
        mAddServiceCity = mAddService.findViewById(R.id.addServiceCity);
        mAddServicePhone = mAddService.findViewById(R.id.addServicePhone);
        mAddServiceEmail = mAddService.findViewById(R.id.addServiceEmail);
        mAddServiceDescription = mAddService.findViewById(R.id.addServiceDescription);
        mAddServiceImage = mAddService.findViewById(R.id.addServiceImage);
        mAddServiceAcceptedBrand = mAddService.findViewById(R.id.addServiceAcceptedBrand);
        mServiceChassisCheck = mAddService.findViewById(R.id.serviceChassisCheck);
        mServiceItpCheck = mAddService.findViewById(R.id.serviceItpCheck);
        mServiceTireCheck = mAddService.findViewById(R.id.serviceTireCheck);
        mRepairServiceCheck = mAddService.findViewById(R.id.repairServiceCheck);
        mAddServiceProgressBar = mAddService.findViewById(R.id.addServiceProgressBar);
        mAddServiceImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(mCtx, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, 2);
                } else {
                    Toast.makeText(mCtx, "Mai intai permite aplicatiei de a accesa galeria!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mAddServiceButton = mAddService.findViewById(R.id.addService);
        mAddServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddServiceProgressBar.setVisibility(View.VISIBLE);
                mAddServiceButton.hide();
                Log.i(TAG, "onClick: add button pressed");
                if (verifyInputOnClientSide()) {
                    if (verifyInputOnServerSide()) {
                    } else {
                    }
                } else {
                    mAddServiceProgressBar.setVisibility(View.GONE);
                    mAddServiceButton.show();
                }
            }
        });
    }

    private boolean verifyInputOnServerSide() {
        Log.i(TAG, "verifyInputOnServerSide: Add service");
        setEnableFields(false);
        final boolean[] resultOk = {true};
        final JsonObject jsonBody = new JsonObject();
        if (mUseDefaultServiceImage) {
            mAddServiceImage.setImageURI(Uri.parse("android.resource://" + mCtx.getPackageName() + "/drawable/" + "default_service_image"));
            mAddServiceImageUri = Uri.parse("android.resource://" + mCtx.getPackageName() + "/drawable/" + "default_service_image");
        }
        int type = 0;
        if (mRepairServiceCheck.isChecked()) {
            type |= 1;
        }
        if (mServiceTireCheck.isChecked()) {
            type |= 2;
        }
        if (mServiceChassisCheck.isChecked()) {
            type |= 4;
        }
        if (mServiceItpCheck.isChecked()) {
            type |= 8;
        }
        jsonBody.addProperty("serviceName", mAddServiceName.getText().toString().trim());
        jsonBody.addProperty("serviceAddress", mAddServiceAddress.getText().toString().trim());
        jsonBody.addProperty("serviceCity", mAddServiceCity.getText().toString().trim());
        jsonBody.addProperty("servicePhone", mAddServicePhone.getText().toString().trim());
        jsonBody.addProperty("serviceEmail", mAddServiceEmail.getText().toString().trim());
        jsonBody.addProperty("serviceAcceptedBrand", mAddServiceAcceptedBrand.getText().toString().trim());
        jsonBody.addProperty("serviceDescription", mAddServiceDescription.getText().toString().trim());
        jsonBody.addProperty("serviceType", type);
        jsonBody.addProperty("longitude", mPreferencesManager.getUserLongitude());
        jsonBody.addProperty("latitude", mPreferencesManager.getUserLatitude());
        jsonBody.addProperty("serviceOwner", mPreferencesManager.getUserId());
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("service_image");
        final StorageReference imageFilePath = storageReference.child(UUID.randomUUID() + "_" + mAddServiceImageUri.getLastPathSegment());
        imageFilePath.putFile(mAddServiceImageUri).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageDownloadLink = uri.toString();
                                jsonBody.addProperty("imageDownloadLink", imageDownloadLink);

                                Response<JsonObject> response = null;
                                try {
                                    response = Ion.with(mCtx)
                                            .load("POST", mUrl + "/services/addService")
                                            .setHeader("Authorization", mPreferencesManager.getToken())
                                            .setJsonObjectBody(jsonBody)
                                            .asJsonObject()
                                            .withResponse()
                                            .get();
                                    if (response.getHeaders().code() == 201) {
                                        Log.i(TAG, "verifyInputOnServerSide: Service added to database!");
                                        Toast.makeText(mCtx, "Service adaugat cu succes!", Toast.LENGTH_SHORT).show();
                                        mServiceAutoList.clear();
                                        mServiceAutoOffset = 0;
                                        mServiceAutoAdapter.notifyDataSetChanged();
                                        if (mShowOnlyMyServices) {
                                            mCompleteUrl = mUrl + "/services/getIdsBetweenForMyServices/" + mPreferencesManager.getUserId() + "/offset/" + mServiceAutoOffset + "/limit/"
                                                    + mServiceAutoLimit;
                                        } else {
                                            mCompleteUrl = mUrl + "/service/getIdsBetween/offset/" + mServiceAutoOffset + "/limit/"
                                                    + mServiceAutoLimit;
                                        }
                                        populateServiceAutoList();

                                        mAddServiceProgressBar.setVisibility(View.INVISIBLE);
                                        mAddService.dismiss();
                                    } else {
                                        Log.i(TAG, "verifyInputOnServerSide: Service not added to database! err code: " + response.getHeaders().code());
                                        Toast.makeText(mCtx, "Ceva nu a mers! Verifica conexiunea la internet", Toast.LENGTH_SHORT).show();
                                        setEnableFields(true);
                                        mAddServiceProgressBar.setVisibility(View.INVISIBLE);
                                        mAddServiceButton.show();
                                        resultOk[0] = false;
                                    }
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // something goes wrong uploading picture
                                Log.e(TAG, "onFailure: Something goes wrong to get dowload link");
                                Toast.makeText(mCtx, "Ceva nu a mers! Verifica conexiunea la internet", Toast.LENGTH_SHORT).show();
                                setEnableFields(true);
                                mAddServiceProgressBar.setVisibility(View.INVISIBLE);
                                mAddServiceButton.show();
                                resultOk[0] = false;
                            }
                        });
                    }
                }
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: Something goes wrong to get dowload link");
                Toast.makeText(mCtx, "Ceva nu a mers! Verifica conexiunea la internet", Toast.LENGTH_SHORT).show();
                setEnableFields(true);
                mAddServiceProgressBar.setVisibility(View.INVISIBLE);
                mAddServiceButton.show();
                resultOk[0] = false;
            }
        });


        return resultOk[0];
    }

    private void setEnableFields(boolean value) {
        mAddServiceName.setEnabled(value);
        mAddServiceAddress.setEnabled(value);
        mAddServiceCity.setEnabled(value);
        mAddServicePhone.setEnabled(value);
        mAddServiceEmail.setEnabled(value);
        mAddServiceDescription.setEnabled(value);
        mAddServiceImage.setEnabled(value);
        mAddServiceAcceptedBrand.setEnabled(value);
        mServiceChassisCheck.setEnabled(value);
        mServiceItpCheck.setEnabled(value);
        mServiceTireCheck.setEnabled(value);
        mRepairServiceCheck.setEnabled(value);
    }

    private boolean verifyInputOnClientSide() {
        Log.i(TAG, "verifyInputOnClientSide: Add service");
        boolean resultOk = true;
        if (mAddServiceName.getText().toString().trim().isEmpty()) {
            mAddServiceName.setError("Completeaza campul!");
            resultOk = false;
        }
        if (mAddServiceAddress.getText().toString().trim().isEmpty()) {
            mAddServiceAddress.setError("Completeaza campul!");
            resultOk = false;
        }
        if (mAddServiceCity.getText().toString().trim().isEmpty()) {
            mAddServiceCity.setError("Completeaza campul!");
            resultOk = false;
        }
        if (mAddServicePhone.getText().toString().trim().isEmpty()) {
            mAddServicePhone.setError("Completeaza campul!");
            resultOk = false;
        } else {
            if (!Patterns.PHONE.matcher(mAddServicePhone.getText().toString().trim()).matches()) {
                mAddServicePhone.setError("Numar invalid!");
                resultOk = false;
            }
        }
        if (mAddServiceEmail.getText().toString().trim().isEmpty()) {
            mAddServiceEmail.setError("Completeaza campul!");
            resultOk = false;
        } else {
            if (!Patterns.EMAIL_ADDRESS.matcher(mAddServiceEmail.getText().toString().trim()).matches()) {
                mAddServiceEmail.setError("Adresa nevalida!");
                resultOk = false;
            }
        }
        if (mAddServiceDescription.getText().toString().trim().isEmpty()) {
            mAddServiceDescription.setError("Completeaza campul!");
            resultOk = false;
        }
        if (mAddServiceAcceptedBrand.getText().toString().trim().isEmpty()) {
            mAddServiceAcceptedBrand.setError("Completeaza campul!");
            resultOk = false;
        }
        if (!mRepairServiceCheck.isChecked() && !mServiceTireCheck.isChecked() && !mServiceChassisCheck.isChecked() && !mServiceItpCheck.isChecked()) {
            mRepairServiceCheck.setChecked(true);
        }
        return resultOk;
    }

    private void showPopUpNotLogged() {
        Log.i(TAG, "showPopUpNotLogged: ");
        TextView notLoggedContent = new TextView(mCtx);
        notLoggedContent.setText("Pentru a executa aceasta actiune trebuie sa fii logat. Vrei sa te autentifici?");
        notLoggedContent.setGravity(Gravity.CENTER);
        notLoggedContent.setPadding(10, 10, 10, 10);
        TextView notLoggedTitle = new TextView(mCtx);
        notLoggedTitle.setText("Actiune interzisa!");
        notLoggedTitle.setGravity(Gravity.CENTER);
        notLoggedTitle.setPadding(10, 10, 10, 10);
        notLoggedTitle.setTextSize(18);
        notLoggedTitle.setTextColor(Color.DKGRAY);
        android.support.v7.app.AlertDialog notLoggedPopUp = new android.support.v7.app.AlertDialog.Builder((HomeActivity) mCtx)
                .setCustomTitle(notLoggedTitle)
                .setView(notLoggedContent)
                .setPositiveButton("Autentifica-te", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "onClick: go to auth page");
                        Intent intent = new Intent(mCtx, AuthenticationActivity.class);
                        startActivityForResult(intent, 3);
                    }
                })
                .setNegativeButton("Anuleaza", null)
                .create();
        notLoggedPopUp.show();
    }


    private void populateServiceAutoListWithMy() {
        Log.i(TAG, "populateServiceAutoListWithMy: ");
        /*

        Log.i(TAG, "populateServiceAutoListWithMy: ");
        final RequestQueue queue = Volley.newRequestQueue(mCtx);
        JsonObjectRequest idList = new JsonObjectRequest(com.android.volley.Request.Method.GET,
                mUrl + "/services/getAllIdsForMyServices/" + mPreferencesManager.getUserId(), null,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "populateServiceAutoList: services Ids received");
                        try {
                            final JSONArray servicesId = response.getJSONArray("ids");
                            for (int i = 0; i < servicesId.length(); i++) {

                                final String finalI = servicesId.get(i).toString().substring(2,servicesId.get(i).toString().length()-2);
                                Log.i(TAG, "populateServiceAutoList: add services with ID: " + finalI);
                                JsonObjectRequest service = new JsonObjectRequest(com.android.volley.Request.Method.GET,
                                        mUrl + "/services/getById/" + finalI, null,
                                        new com.android.volley.Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                if (response != null) {
                                                    try {
                                                        Log.i(TAG, "onResult: Received service with id: " + finalI);
                                                        JSONObject service = response.getJSONObject("service");
                                                        mServiceAutoList.add(new ServiceAuto(
                                                                service.getString("id"),
                                                                Uri.parse(service.getString("logoPath")),
                                                                service.getString("name"),
                                                                service.getString("description"),
                                                                service.getString("address"),
                                                                Float.valueOf(service.getString("rating")),
                                                                service.getString("phoneNumber"),
                                                                service.getString("email"),
                                                                Double.valueOf(service.getString("latitude")),
                                                                Double.valueOf(service.getString("longitude")),
                                                                service.getString("owner"),
                                                                service.getInt("serviceType"),
                                                                service.getString("acceptedBrand")));
                                                        mServiceAutoAdapter.notifyDataSetChanged();
                                                    } catch (JSONException e1) {
                                                        e1.printStackTrace();
                                                        Log.e(TAG, "onResult: NoResult");
                                                    }
                                                } else {
                                                    Log.e(TAG, "onResult: Service with id: " + finalI + "not received!");
                                                }

                                            }
                                        }, new com.android.volley.Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        System.out.println("error is = " + error);
                                    }
                                });
                                queue.add(service);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("error is = " + error);
            }
        });
        queue.add(idList);
        */
        Ion.with(mCtx)
                .load("GET", mUrl + "/services/getAllIdsForMyServices/" + mPreferencesManager.getUserId())
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> response) {
                        if (response != null) {
                            if (response.getHeaders().code() == 200) {
                                Log.i(TAG, "populateServiceAutoList: services Ids received");
                                final JsonArray servicesId = response.getResult().get("ids").getAsJsonArray();
                                if (servicesId.size() != 11) {
                                    mExistMoreServices = false;
                                } else {
                                    mExistMoreServices = true;
                                }
                                for (int i = 0; i < servicesId.size(); i++) {
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
                                                            Uri.parse(service.getString("logoPath")),
                                                            service.getString("name"),
                                                            service.getString("description"),
                                                            service.getString("address"),
                                                            service.getString("city"),
                                                            Float.valueOf(service.getString("rating")),
                                                            service.getString("phoneNumber"),
                                                            service.getString("email"),
                                                            Double.valueOf(service.getString("latitude")),
                                                            Double.valueOf(service.getString("longitude")),
                                                            service.getString("owner"),
                                                            service.getInt("serviceType"),
                                                            service.getString("acceptedBrand")));
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
                                    httpGetService.execute("GET", mUrl + "/services/getById/" + finalI);
                                }
                                // }
                                //}).start();
                            } else {
                                Toast.makeText(mCtx, "Error code: " + response.getHeaders().code(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "onCompleted: null response");
                        }
                    }
                });
    }

    private void populateServiceAutoList() {
        Log.i(TAG, "populateServiceAutoListWithAll: ");
        Ion.with(mCtx)
                .load("GET", mCompleteUrl)
                .setHeader("Authorization", mPreferencesManager.getToken())
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> response) {
                        if (response != null) {
                            if (response.getHeaders().code() == 200) {
                                Log.i(TAG, "populateServiceAutoList: services Ids received");
                                final JsonArray servicesId = response.getResult().get("ids").getAsJsonArray();
                                if (servicesId.size() != 11) {
                                    mExistMoreServices = false;
                                } else {
                                    mExistMoreServices = true;
                                }
                                for (int i = 0; i < servicesId.size(); i++) {
                                    final String id = servicesId.get(i).getAsString();
                                    Log.i(TAG, "populateServiceAutoList: add services with ID: " + id);
                                    final int finalI = i;
                                    final AsyncHttpRequest httpGetService = new AsyncHttpRequest(new AsyncHttpRequest.Listener() {
                                        @Override
                                        public void onResult(String result) {
                                            if (!result.isEmpty()) {
                                                try {
                                                    Log.i(TAG, "onResult: Received service with id: " + id);
                                                    JSONObject service = new JSONObject(result).getJSONObject("service");
                                                    mServiceAutoList.add(new ServiceAuto(
                                                            service.getString("id"),
                                                            Uri.parse(service.getString("logoPath")),
                                                            service.getString("name"),
                                                            service.getString("description"),
                                                            service.getString("address"),
                                                            service.getString("city"),
                                                            Float.valueOf(service.getString("rating")),
                                                            service.getString("phoneNumber"),
                                                            service.getString("email"),
                                                            Double.valueOf(service.getString("latitude")),
                                                            Double.valueOf(service.getString("longitude")),
                                                            service.getString("owner"),
                                                            service.getInt("serviceType"),
                                                            service.getString("acceptedBrand")));
                                                    mServiceAutoAdapter.notifyDataSetChanged();
                                                } catch (JSONException e1) {
                                                    e1.printStackTrace();
                                                    Log.e(TAG, "onResult: NoResult");
                                                }
                                            } else {
                                                Log.e(TAG, "onResult: Service with id: " + id + "not received!");
                                            }
                                        }
                                    });
                                    httpGetService.execute("GET", mUrl + "/services/getById/" + id);
                                }
                                // }
                                //}).start();
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
        Log.i(TAG, "onActivityResult: requestCode: " + requestCode);
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
            if (resultCode == 3) {
                Log.i(TAG, "onActivityResult: Apply edit changes");
                ServiceAuto serviceAuto = mServiceAutoList.get(mLastElementClickedPosition);
                serviceAuto.setName(data.getStringExtra("newServiceName"));
                serviceAuto.setAddress(data.getStringExtra("newServiceAddress"));
                serviceAuto.setContactEmail(data.getStringExtra("newServiceEmail"));
                serviceAuto.setDescription(data.getStringExtra("newServiceDescription"));
                serviceAuto.setContactPhoneNumber(data.getStringExtra("newServicePhone"));
                serviceAuto.setAcceptedBrands(data.getStringExtra("newServiceAcceptedBrand"));
                serviceAuto.setType(data.getIntExtra("newServiceType", 1));
                serviceAuto.setLatitude(data.getDoubleExtra("newLatitude", 0));
                serviceAuto.setLongitude(data.getDoubleExtra("newLongitude", 0));
                serviceAuto.setImage(Uri.parse(data.getStringExtra("newLogoImage")));
                mServiceAutoList.set(mLastElementClickedPosition, serviceAuto);
                mServiceAutoAdapter.notifyDataSetChanged();
            }
        }
        if (requestCode == 2) {
            if (data != null) {
                mAddServiceImageUri = data.getData();
                mAddServiceImage.setImageURI(mAddServiceImageUri);
                mUseDefaultServiceImage = false;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(TAG, "onCreateOptionsMenu: ");
        if (mShowOnlyMyServices) {
        } else {
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
        }
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
                final EditText addressInput = advancedFilterView.findViewById(R.id.addressInputFilter);
                final EditText cityInput = advancedFilterView.findViewById(R.id.cityInputFilter);
                final CheckBox repairServiceCheckFilter = advancedFilterView.findViewById(R.id.repairServiceCheckFilter);
                final CheckBox serviceChassisCheckFilter = advancedFilterView.findViewById(R.id.serviceChassisCheckFilter);
                final CheckBox serviceTireCheckFilter = advancedFilterView.findViewById(R.id.serviceTireCheckFilter);
                final CheckBox serviceItpCheckFilter = advancedFilterView.findViewById(R.id.serviceItpCheckFilter);
                final CheckBox resetFilterCheck = advancedFilterView.findViewById(R.id.resetFilterCheck);
                TextView filterTitle = new TextView(getContext());
                filterTitle.setText("Filtrare avansata!");
                filterTitle.setGravity(Gravity.CENTER);
                filterTitle.setPadding(10, 10, 10, 10);
                filterTitle.setTextSize(18);
                filterTitle.setTextColor(Color.DKGRAY);
                final AlertDialog advancedFilterPopUp = new AlertDialog.Builder(mCtx)
                        .setCustomTitle(filterTitle)
                        .setView(advancedFilterView)
                        .setPositiveButton("Filtreaza", null)
                        .setNegativeButton("Anuleaza", null)
                        .create();
                advancedFilterPopUp.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button filter = advancedFilterPopUp.getButton(AlertDialog.BUTTON_POSITIVE);
                        filter.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (resetFilterCheck.isChecked()) {
                                    mServiceAutoOffset = 0;
                                    mCompleteUrl = mUrl + "/service/getIdsBetween/offset/" + mServiceAutoOffset + "/limit/"
                                            + mServiceAutoLimit;
                                    mServiceAutoOffset = 0;
                                    mServiceAutoList.clear();
                                    mServiceAutoAdapter.notifyDataSetChanged();
                                    populateServiceAutoList();
                                    advancedFilterPopUp.dismiss();
                                } else {
                                    if (ratingMinBar.getRating() > ratingMaxBar.getRating()) {
                                        Toast.makeText(getActivity(), "Rating-ul minim trebuie sa fie mai mic ca rating-ul maxim!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (!(ratingMinBar.getRating() == 0
                                                && ratingMaxBar.getRating() == 0)
                                                && givenNameFilter.getText().toString().isEmpty()
                                                && addressInput.getText().toString().isEmpty()
                                                && cityInput.getText().toString().isEmpty()) {
                                            int type = 0;
                                            if (repairServiceCheckFilter.isChecked()) {
                                                type |= 1;
                                            }
                                            if (serviceTireCheckFilter.isChecked()) {
                                                type |= 2;
                                            }
                                            if (serviceChassisCheckFilter.isChecked()) {
                                                type |= 4;
                                            }
                                            if (serviceItpCheckFilter.isChecked()) {
                                                type |= 8;
                                            }
                                            if (type == 0) {
                                                type = 1;
                                            }
                                            mServiceAutoOffset = 0;
                                            String name;
                                            String address;
                                            String city;

                                            if (givenNameFilter.getText().toString().isEmpty()) {
                                                name = "empty";
                                            } else {
                                                name = givenNameFilter.getText().toString();
                                            }
                                            if (addressInput.getText().toString().isEmpty()) {
                                                address = "empty";
                                            } else {
                                                address = addressInput.getText().toString();
                                            }
                                            if (cityInput.getText().toString().isEmpty()) {
                                                city = "empty";
                                            } else {
                                                city = cityInput.getText().toString();
                                            }
                                            mCompleteUrl = mUrl
                                                    + "/services/getIdsBetweenWithFilter/offset/" + mServiceAutoOffset
                                                    + "/limit/" + mServiceAutoLimit
                                                    + "/minRating/" + (int) ratingMinBar.getRating()
                                                    + "/maxRating/" + (int) ratingMaxBar.getRating()
                                                    + "/name/" + name
                                                    + "/address/" + address
                                                    + "/city/" + city
                                                    + "/type/" + type;
                                            mServiceAutoOffset = 0;
                                            mServiceAutoList.clear();
                                            mServiceAutoAdapter.notifyDataSetChanged();
                                            populateServiceAutoList();
                                            advancedFilterPopUp.dismiss();
                                        } else {
                                            Toast.makeText(getActivity(), "Nu ai completat niciun camp", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }
                        });
                    }
                });
                advancedFilterPopUp.show();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public HomeFragment() {

    }
}
