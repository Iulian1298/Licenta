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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;

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
    private EditText mAddServicePhone;
    private EditText mAddServiceEmail;
    private EditText mAddServiceDescription;
    private EditText mAddServiceAcceptedBrand;
    private CheckBox mRepairServiceCheck;
    private CheckBox mServiceTireCheck;
    private CheckBox mServiceChassisCheck;
    private ImageView mAddServiceImage;
    private boolean mUseDefaultServiceImage;
    private int mLastElementClickedPosition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_home, container, false);
        mCtx = getContext();
        mPreferencesManager = SharedPreferencesManager.getInstance(mCtx);
        mShowOnlyMyServices = mPreferencesManager.getOnlyMyServices();
        Log.i(TAG, "onCreateView: show only my services: " + mShowOnlyMyServices);
        try {
            mIsLoggedIn = mPreferencesManager.isLoggedIn();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final SwipeRefreshLayout refreshComments = fragmentView.findViewById(R.id.refreshServices);
        refreshComments.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                init(fragmentView);
                refreshComments.setRefreshing(false);
            }
        });


        setHasOptionsMenu(true);

        init(fragmentView);


        return fragmentView;
    }

    private void init(View v) {
        Log.i(TAG, "init: ");
        mServiceAutoRecyclerView = v.findViewById(R.id.servicesList);
        mAddServiceFloatingButton = v.findViewById(R.id.addServiceFloatingButton);
        mServiceAutoRecyclerView.setLayoutManager(new LinearLayoutManager(mCtx));
        mUrl = "http://10.0.2.2:5000";
        mServiceAutoList = new ArrayList<>();
        mServiceAutoAdapter = new ServiceAutoAdapter(mCtx, mServiceAutoList);
        mServiceAutoRecyclerView.addItemDecoration(new DividerItemDecoration(mCtx, DividerItemDecoration.VERTICAL));
        mServiceAutoRecyclerView.setAdapter(mServiceAutoAdapter);
        mLastElementClickedPosition = 0;
        if (mShowOnlyMyServices) {
            populateServiceAutoListWithMy();
        } else {
            populateServiceAutoListWithAll();
        }
        initAddServicePopUp();
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
        mUseDefaultServiceImage = true;
        mAddService = new Dialog(mCtx);
        mAddService.setContentView(R.layout.add_service_popup_layout);
        mAddService.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mAddService.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        mAddService.getWindow().getAttributes().gravity = Gravity.TOP;
        mAddServiceName = mAddService.findViewById(R.id.addServiceName);
        mAddServiceAddress = mAddService.findViewById(R.id.addServiceAddress);
        mAddServicePhone = mAddService.findViewById(R.id.addServicePhone);
        mAddServiceEmail = mAddService.findViewById(R.id.addServiceEmail);
        mAddServiceDescription = mAddService.findViewById(R.id.addServiceDescription);
        mAddServiceImage = mAddService.findViewById(R.id.addServiceImage);
        mAddServiceAcceptedBrand = mAddService.findViewById(R.id.addServiceAcceptedBrand);
        mServiceChassisCheck = mAddService.findViewById(R.id.serviceChassisCheck);
        mServiceTireCheck = mAddService.findViewById(R.id.serviceTireCheck);
        mRepairServiceCheck = mAddService.findViewById(R.id.repairServiceCheck);
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
        FloatingActionButton addServiceButton = mAddService.findViewById(R.id.addService);
        addServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: add button pressed");
                if (verifyInputOnClientSide()) {
                    try {
                        if (verifyInputOnServerSide()) {
                            mAddService.dismiss();
                            if (mShowOnlyMyServices) {
                                populateServiceAutoListWithMy();
                            }
                        } else {
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

    private boolean verifyInputOnServerSide() throws ExecutionException, InterruptedException {
        Log.i(TAG, "verifyInputOnServerSide: Add service");
        boolean resultOk = true;
        JsonObject jsonBody = new JsonObject();
        if (mUseDefaultServiceImage) {
            mAddServiceImage.setImageURI(Uri.parse("android.resource://" + mCtx.getPackageName() + "/drawable/" + "default_service_image"));
        }
        BitmapDrawable drawable = (BitmapDrawable) mAddServiceImage.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String image = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
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
        jsonBody.addProperty("serviceName", mAddServiceName.getText().toString().trim());
        jsonBody.addProperty("serviceAddress", mAddServiceAddress.getText().toString().trim());
        jsonBody.addProperty("servicePhone", mAddServicePhone.getText().toString().trim());
        jsonBody.addProperty("serviceEmail", mAddServiceEmail.getText().toString().trim());
        jsonBody.addProperty("serviceAcceptedBrand", mAddServiceAcceptedBrand.getText().toString().trim());
        jsonBody.addProperty("serviceDescription", mAddServiceDescription.getText().toString().trim());
        jsonBody.addProperty("serviceType", type);
        jsonBody.addProperty("longitude", mPreferencesManager.getUserLongitude());
        jsonBody.addProperty("latitude", mPreferencesManager.getUserLatitude());
        jsonBody.addProperty("imageEncoded", image);
        jsonBody.addProperty("serviceOwner", mPreferencesManager.getUserId());

        Response<JsonObject> response = Ion.with(mCtx)
                .load("POST", mUrl + "/services/addService")
                .setJsonObjectBody(jsonBody)
                .asJsonObject()
                .withResponse()
                .get();
        if (response.getHeaders().code() == 201) {
            Log.i(TAG, "verifyInputOnServerSide: Service added to database!");
            Toast.makeText(mCtx, "Service adaugat cu succes!", Toast.LENGTH_SHORT).show();
        } else {
            Log.i(TAG, "verifyInputOnServerSide: Service not added to database! err code: " + response.getHeaders().code());
            Toast.makeText(mCtx, "Error code: " + response.getHeaders().code(), Toast.LENGTH_SHORT).show();
        }
        //Log.i(TAG, "verifyInputOnServerSide: " + jsonBody);

        return resultOk;
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
        if (!mRepairServiceCheck.isChecked() && !mServiceTireCheck.isChecked() && !mServiceChassisCheck.isChecked()) {
            mRepairServiceCheck.setChecked(true);
        }
        return resultOk;
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

    private void populateServiceAutoListWithMy() {
        Log.i(TAG, "populateServiceAutoListWithMy: ");
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
                                /*new Thread(new Runnable() {
                                    @Override
                                    public void run() {*/
                                for (int i = 0; i < servicesId.size(); i++) {
                                            /*try {
                                                Thread.sleep(250);
                                            } catch (InterruptedException e1) {
                                                e1.printStackTrace();
                                            }*/
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

    private void populateServiceAutoListWithAll() {
        Log.i(TAG, "populateServiceAutoListWithAll: ");
        Ion.with(mCtx)
                .load("GET", mUrl + "/services/getAllIds")
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> response) {
                        if (response != null) {
                            if (response.getHeaders().code() == 200) {
                                Log.i(TAG, "populateServiceAutoList: services Ids received");
                                final JsonArray servicesId = response.getResult().get("ids").getAsJsonArray();
                                /*new Thread(new Runnable() {
                                    @Override
                                    public void run() {*/
                                for (int i = 0; i < servicesId.size(); i++) {
                                            /*try {
                                                Thread.sleep(250);
                                            } catch (InterruptedException e1) {
                                                e1.printStackTrace();
                                            }*/
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

    private void populateServiceAutoList2() {
        Log.i(TAG, "populateServiceAutoList: ");
        Ion.with(mCtx)
                .load("GET", mUrl + "/services/getAllIds")
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
                                            .url(mUrl + "/services/getById/" + finalI)
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
                .load("GET", mUrl + "/services/getAllIds")
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
        }
        if (requestCode == 2) {
            if (data != null) {
                Uri chosenImageByUser = data.getData();
                mAddServiceImage.setImageURI(chosenImageByUser);
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
                AlertDialog advancedFilterPopUp = new AlertDialog.Builder(mCtx)
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
                                                        .load("GET", mUrl + "/services/getIdsFilterBy/")
                                                        .setJsonObjectBody(advFilterParams)
                                                        .asJsonObject()
                                                        .withResponse()
                                                        .get();
                                            } catch (ExecutionException e) {
                                                e.printStackTrace();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            //ToDo: move filter on server
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
