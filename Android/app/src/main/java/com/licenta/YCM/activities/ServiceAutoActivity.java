package com.licenta.YCM.activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.MapView;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.MarkerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.licenta.YCM.R;
import com.licenta.YCM.SharedPreferencesManager;
import com.licenta.YCM.models.ServiceAuto;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class ServiceAutoActivity extends AppCompatActivity {
    private static final String TAG = "ServiceAutoActivity";

    private SharedPreferencesManager mPreferencesManager;
    private ServiceAuto mServiceAuto;
    private ImageView mLogoImage;
    private RatingBar mRatingBar;
    private TextView mDescription;
    private TextView mContactPhoneNumber;
    private TextView mContactEmail;
    private TextView mDistanceFromYou;
    private ImageView mDistanceFromYouIcon;
    private boolean isMyService;
    private Context mCtx;
    private Intent mReturnIntent;
    private CaldroidFragment mDialogCaldroidFragment;
    private FloatingActionButton mMenuServiceFloatingButton;
    private boolean mFabMenuExpanded;
    private FloatingActionButton mViewCommentsFab;
    private FloatingActionButton mEditServiceFab;
    private FloatingActionButton mOfferRequestsFab;
    private FloatingActionButton mTodayScheduleFab;
    private FloatingActionButton mLeaveCommentFab;
    private FloatingActionButton mRequestOfferFab;
    private FloatingActionButton maddAppointmentFab;
    private FloatingActionButton mCallServiceFab;
    private FloatingActionButton mSendMailToServiceFab;
    private FloatingActionButton mCreateRouteToService;
    private CoordinatorLayout mServiceAutoFloatingButtons;
    private Toolbar mServiceToolbar;
    private TextView mServiceType;
    private TextView mServiceAcceptedBrands;
    private TextView mServicePrices;
    private AlertDialog mEditServicePopUp;
    private EditText mEditServiceName;
    private EditText mEditServiceAddress;
    private EditText mEditServiceCity;
    private EditText mEditServicePhone;
    private EditText mEditServiceEmail;
    private EditText mEditServiceDescription;
    private EditText mEditServiceAcceptedBrand;
    private CheckBox mEditRepairServiceCheck;
    private CheckBox mEditServiceTireCheck;
    private CheckBox mEditServiceChassisCheck;
    private CheckBox mEditServiceItpCheck;
    private EditText mEditPriceService;
    private EditText mEditPriceTire;
    private EditText mEditPriceChassis;
    private EditText mEditPriceITP;
    private ImageView mEditServiceImage;
    private String mUrl;
    private ProgressBar mEditServiceProgressBar;
    private LinearLayout mEditLinearLayout;
    private Button mConfirm;
    private Button mCancel;
    private MapView mMapEditService;
    private Bundle mSavedInstanceState;
    private LatLng mSelectedServiceLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_service_auto);
        mCtx = getApplicationContext();
        mPreferencesManager = SharedPreferencesManager.getInstance(mCtx);

        init();
    }

    private void init() {
        Log.i(TAG, "init: ");
        mUrl = mPreferencesManager.getServerUrl();
        mFabMenuExpanded = false;
        mServiceAutoFloatingButtons = findViewById(R.id.serviceAutoFloatingButtons);
        mLogoImage = findViewById(R.id.logoImageFull);
        mRatingBar = findViewById(R.id.ratingFull);
        mDescription = findViewById(R.id.serviceDescriptionFull);
        mContactPhoneNumber = findViewById(R.id.contactPhoneNumbeFullInfo);
        mContactEmail = findViewById(R.id.contactEmailFullInfo);
        mDistanceFromYou = findViewById(R.id.distanceFromYouFull);
        mDistanceFromYouIcon = findViewById(R.id.distanceFromYouFullIcon);
        mServiceType = findViewById(R.id.offeredServices);
        mServiceAcceptedBrands = findViewById(R.id.acceptedBrands);
        mServicePrices = findViewById(R.id.prices);
        mServiceToolbar = findViewById(R.id.serviceToolbar);
        setSupportActionBar(mServiceToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (ContextCompat.checkSelfPermission(mCtx, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mDistanceFromYou.setVisibility(View.GONE);
            mDistanceFromYouIcon.setVisibility(View.GONE);
        }

        Intent intent = getIntent();
        mServiceAuto = new ServiceAuto(
                intent.getStringExtra("serviceId"),
                Uri.parse(intent.getStringExtra("logoImage")),
                intent.getStringExtra("serviceName"),
                intent.getStringExtra("description"),
                intent.getStringExtra("address"),
                intent.getStringExtra("city"),
                intent.getFloatExtra("rating", 0),
                intent.getStringExtra("contactPhoneNumber"),
                intent.getStringExtra("contactEmail"),
                intent.getDoubleExtra("latitude", 0),
                intent.getDoubleExtra("longitude", 0),
                intent.getStringExtra("ownerId"),
                intent.getIntExtra("serviceType", 0),
                intent.getStringExtra("acceptedBrands"),
                intent.getIntExtra("priceService", -1),
                intent.getIntExtra("priceTire", -1),
                intent.getIntExtra("priceChassis", -1),
                intent.getIntExtra("priceItp", -1)
        );

        //Objects.requireNonNull(getSupportActionBar()).setTitle(mServiceAuto.getName() + " - " + mServiceAuto.getAddress());
        if (mServiceAuto.getOwnerId().equals(mPreferencesManager.getUserId())) {
            isMyService = true;
        }
        populateActivity();
        mLogoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: expand logo image");
                Dialog dialog = new Dialog(ServiceAutoActivity.this);
                dialog.setContentView(R.layout.show_full_image_popup_layout);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0xD9272727));
                dialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
                dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
                ImageView fullImage = dialog.findViewById(R.id.serviceImageFullScreen);
                Glide.with(mCtx)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .load(mServiceAuto.getImage())
                        .into(fullImage);
                dialog.show();

            }
        });
        mReturnIntent = new Intent();
        setResult(RESULT_CANCELED, mReturnIntent);

        setFloatingButtonsMenu();
    }

    private void setFloatingButtonsMenu() {
        if (!isMyService) {
            CoordinatorLayout floatingButtons;
            floatingButtons = (CoordinatorLayout) View.inflate(ServiceAutoActivity.this, R.layout.floating_button_service_auto_layout, null);
            mServiceAutoFloatingButtons.addView(floatingButtons);
            mCallServiceFab = floatingButtons.findViewById(R.id.callServiceFab);
            mSendMailToServiceFab = floatingButtons.findViewById(R.id.sendMailToServiceFab);
            mCreateRouteToService = floatingButtons.findViewById(R.id.createRouteToService);
            mCallServiceFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callService();
                    closeFloatingMenu();
                }
            });
            mSendMailToServiceFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMailToService();
                    closeFloatingMenu();
                }
            });
            mCreateRouteToService.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createRouteToService();
                    closeFloatingMenu();
                }
            });
            mMenuServiceFloatingButton = floatingButtons.findViewById(R.id.menuServiceFloatingButton);
            closeFloatingMenu();
            mMenuServiceFloatingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mFabMenuExpanded) {
                        closeFloatingMenu();
                    } else {
                        expandFloatingMenu();
                    }
                }
            });
        }
    }

    private void createRouteToService() {
        String uri = "http://maps.google.com/maps?saddr="
                + mPreferencesManager.getUserLatitude() + "," + mPreferencesManager.getUserLongitude()
                + "&daddr=" + mServiceAuto.getLatitude() + "," + mServiceAuto.getLongitude();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    private void callService() {
        Log.i(TAG, "callService: dial number: " + mServiceAuto.getContactPhoneNumber());
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel: +4" + mServiceAuto.getContactPhoneNumber()));
        startActivity(dialIntent);
    }

    private void sendMailToService() {
        Log.i(TAG, "sendMailToService: send mail to: " + mServiceAuto.getContactEmail());
        Intent sendMessageIntent = new Intent(Intent.ACTION_SENDTO);
        sendMessageIntent.setData(Uri.parse("mailto: " + mServiceAuto.getContactEmail()));
        startActivity(Intent.createChooser(sendMessageIntent, "Trimite mail-ul cu: "));
    }

    private void requestOffer() {
        Log.i(TAG, "requestOffer: show request offer popup");
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
            if (!((mServiceAuto.getType() & 2) == 2) && !((mServiceAuto.getType() & 4) == 4)) {
                showRequestOffer(true);
            } else {
                if ((mServiceAuto.getType() & 1) == 1) {
                    TextView requestTypeTitle = new TextView(mCtx);
                    requestTypeTitle.setText("Tipul de problemă");
                    requestTypeTitle.setGravity(Gravity.CENTER);
                    requestTypeTitle.setPadding(10, 10, 10, 10);
                    requestTypeTitle.setTextSize(18);
                    requestTypeTitle.setTextColor(Color.DKGRAY);
                    TextView requestTypeContent = new TextView(mCtx);
                    requestTypeContent.setText("Apasă \"DA\" daca vrei o ofertă pentru rezolvarea unei probleme mecanice sau \"NU\" altfel!");
                    requestTypeContent.setGravity(Gravity.CENTER);
                    requestTypeContent.setPadding(10, 10, 10, 10);
                    AlertDialog requestType = new AlertDialog.Builder(ServiceAutoActivity.this)
                            .setCustomTitle(requestTypeTitle)
                            .setView(requestTypeContent)
                            .setPositiveButton("DA", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i(TAG, "onClick: DA");
                                    showRequestOffer(true);
                                }
                            })
                            .setNegativeButton("NU", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i(TAG, "onClick: NU");
                                    showRequestOffer(false);
                                }
                            })
                            .create();
                    requestType.show();
                } else {
                    showRequestOffer(false);
                }
            }
        }
    }

    private void addAppointment() {
        Log.i(TAG, "onClick: schedule clicked");
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
            TextView scheduleDescriptionTitle = new TextView(mCtx);
            scheduleDescriptionTitle.setText("Descrie problema pentru care faci rezervare");
            scheduleDescriptionTitle.setGravity(Gravity.CENTER);
            scheduleDescriptionTitle.setPadding(10, 10, 10, 10);
            scheduleDescriptionTitle.setTextSize(18);
            scheduleDescriptionTitle.setTextColor(Color.DKGRAY);
            final View scheduleDescriptionContent = getLayoutInflater().inflate(R.layout.schedule_description_popup_layout, null);
            final EditText scheduleDescriptionText = scheduleDescriptionContent.findViewById(R.id.scheduleDescriptionText);
            final CheckBox scheduleRepairServiceCheck = scheduleDescriptionContent.findViewById(R.id.scheduleRepairServiceCheck);
            final CheckBox scheduleServiceTireCheck = scheduleDescriptionContent.findViewById(R.id.scheduleServiceTireCheck);
            final CheckBox scheduleServiceChassisCheck = scheduleDescriptionContent.findViewById(R.id.scheduleServiceChassisCheck);
            final CheckBox scheduleServiceItpCheck = scheduleDescriptionContent.findViewById(R.id.scheduleServiceItpCheck);
            AlertDialog schedulePopUp = new AlertDialog.Builder(ServiceAutoActivity.this)
                    .setCustomTitle(scheduleDescriptionTitle)
                    .setView(scheduleDescriptionContent)
                    .setPositiveButton("Continua", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (scheduleDescriptionText.getText().toString().trim().isEmpty() ||
                                    (!scheduleRepairServiceCheck.isChecked() &&
                                            !scheduleServiceTireCheck.isChecked() &&
                                            !scheduleServiceChassisCheck.isChecked() &&
                                            !scheduleServiceItpCheck.isChecked())) {
                                Toast.makeText(mCtx, "Trebuie completat câmpul din pop-up și cel puțin o casută bifată!", Toast.LENGTH_SHORT).show();
                            } else {
                                showDatePicker(scheduleDescriptionText.getText().toString().trim(),
                                        scheduleRepairServiceCheck.isChecked(),
                                        scheduleServiceTireCheck.isChecked(),
                                        scheduleServiceChassisCheck.isChecked(),
                                        scheduleServiceItpCheck.isChecked()
                                );
                            }
                        }
                    })
                    .setNegativeButton("Anulează", null)
                    .create();
            schedulePopUp.show();
        }
    }

    private void leaveComment() {
        Log.i(TAG, "leaveComment: show leave comment pop-up ");
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
            final View leaveCommentView = getLayoutInflater().inflate(R.layout.leave_comment_popup_layout, null);
            final EditText givenComment = leaveCommentView.findViewById(R.id.givenComment);
            final RatingBar givenRating = leaveCommentView.findViewById(R.id.givenRating);
            TextView addCommentTitle = new TextView(getApplicationContext());
            addCommentTitle.setText("Lasă un comentariu");
            addCommentTitle.setGravity(Gravity.CENTER);
            addCommentTitle.setPadding(10, 10, 10, 10);
            addCommentTitle.setTextSize(18);
            addCommentTitle.setTextColor(Color.DKGRAY);

            AlertDialog addCommentPopUp = new AlertDialog.Builder(ServiceAutoActivity.this)
                    .setCustomTitle(addCommentTitle)
                    .setView(leaveCommentView)
                    .setPositiveButton("Adaugă", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "onCick() -> onClick() -> Comentariu: " + givenComment.getText());
                            Log.i(TAG, "onCick() -> onClick() -> Rating: " + givenRating.getRating());
                            if (givenComment.getText().toString().trim().isEmpty() || givenRating.getRating() == 0) {
                                Toast.makeText(getApplicationContext(), "Completează toate campurile", Toast.LENGTH_SHORT).show();
                            } else {
                                JsonObject jsonBody = new JsonObject();
                                jsonBody.addProperty("userId", mPreferencesManager.getUserId());
                                jsonBody.addProperty("serviceId", mServiceAuto.getServiceId());
                                jsonBody.addProperty("comment", givenComment.getText().toString().trim());
                                jsonBody.addProperty("creationTime", new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
                                jsonBody.addProperty("userFullName", mPreferencesManager.getUsername());
                                jsonBody.addProperty("rating", givenRating.getRating());
                                try {
                                    Response<JsonObject> response = Ion.with(getApplicationContext())
                                            .load("POST", mUrl + "/comments/addComment")
                                            .setHeader("Authorization", mPreferencesManager.getToken())
                                            .setJsonObjectBody(jsonBody)
                                            .asJsonObject()
                                            .withResponse()
                                            .get();
                                    if (response.getHeaders().code() == 201) {
                                        Toast.makeText(getApplicationContext(), "Comentariu adaugat cu succes!", Toast.LENGTH_SHORT).show();
                                        mRatingBar.setRating(response.getResult().get("newRating").getAsFloat());
                                        setResult(RESULT_OK, mReturnIntent);
                                        mReturnIntent.putExtra("creationDeletionNewRating", mRatingBar.getRating());
                                        seeComments();
                                    } else {
                                        if (response.getHeaders().code() == 409) {
                                            Toast.makeText(getApplicationContext(), "Conflict in baza de date!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Error code: " + response.getHeaders().code(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    })
                    .setNegativeButton("Anulează", null)
                    .create();
            addCommentPopUp.show();
        }
    }

    private void seeComments() {
        Log.i(TAG, "seeComments: open comments page");
        Intent intentViewComments = new Intent(getApplicationContext(), CommentsActivity.class);
        intentViewComments.putExtra("serviceId", mServiceAuto.getServiceId());
        startActivityForResult(intentViewComments, 1);
    }

    private void setOnClearTextListeners(View editServiceView) {
        ImageView clearEditServiceName = editServiceView.findViewById(R.id.clearEditServiceName);
        ImageView clearEditServiceAddress = editServiceView.findViewById(R.id.clearEditServiceAddress);
        ImageView clearEditServiceCity = editServiceView.findViewById(R.id.clearEditServiceCity);
        ImageView clearEditServicePhone = editServiceView.findViewById(R.id.clearEditServicePhone);
        ImageView clearEditServiceEmail = editServiceView.findViewById(R.id.clearEditServiceEmail);
        ImageView clearEditServiceDescription = editServiceView.findViewById(R.id.clearEditServiceDescription);
        ImageView clearEditServiceAcceptedBrands = editServiceView.findViewById(R.id.clearEditServiceAcceptedBrands);
        clearEditServiceName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditServiceName.setText("");
            }
        });
        clearEditServiceAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditServiceAddress.setText("");
            }
        });
        clearEditServiceCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditServiceCity.setText("");
            }
        });
        clearEditServicePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditServicePhone.setText("");
            }
        });
        clearEditServiceEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditServiceEmail.setText("");
            }
        });
        clearEditServiceDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditServiceDescription.setText("");
            }
        });
        clearEditServiceAcceptedBrands.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditServiceAcceptedBrand.setText("");
            }
        });
    }

    private void editService() {
        Log.i(TAG, "editService: ");
        TextView editServiceTitle = new TextView(mCtx);
        editServiceTitle.setText("Editează service-ul");
        editServiceTitle.setGravity(Gravity.CENTER);
        editServiceTitle.setPadding(10, 10, 10, 10);
        editServiceTitle.setTextSize(18);
        editServiceTitle.setTextColor(Color.DKGRAY);
        View editServiceView = getLayoutInflater().inflate(R.layout.edit_service_popup_layout, null);
        mEditServiceName = editServiceView.findViewById(R.id.editServiceName);
        mEditServiceAddress = editServiceView.findViewById(R.id.editServiceAddress);
        mEditServiceCity = editServiceView.findViewById(R.id.editServiceCity);
        mEditServicePhone = editServiceView.findViewById(R.id.editServicePhone);
        mEditServiceEmail = editServiceView.findViewById(R.id.editServiceEmail);
        mEditServiceDescription = editServiceView.findViewById(R.id.editServiceDescription);
        mEditServiceAcceptedBrand = editServiceView.findViewById(R.id.editServiceAcceptedBrand);
        mEditRepairServiceCheck = editServiceView.findViewById(R.id.editRepairServiceCheck);
        mEditServiceTireCheck = editServiceView.findViewById(R.id.editServiceTireCheck);
        mEditServiceChassisCheck = editServiceView.findViewById(R.id.editServiceChassisCheck);
        mEditServiceItpCheck = editServiceView.findViewById(R.id.editServiceItpCheck);
        mEditPriceService = editServiceView.findViewById(R.id.editPriceRepairService);
        mEditPriceTire = editServiceView.findViewById(R.id.editPriceTire);
        mEditPriceChassis = editServiceView.findViewById(R.id.editPriceChassis);
        mEditPriceITP = editServiceView.findViewById(R.id.editPriceItp);
        mEditServiceImage = editServiceView.findViewById(R.id.editServiceImage);
        mEditServiceProgressBar = editServiceView.findViewById(R.id.editServiceProgressBar);
        mEditLinearLayout = editServiceView.findViewById(R.id.editLinearLayout);
        mMapEditService = editServiceView.findViewById(R.id.mapEditService);
        mMapEditService.onCreate(mSavedInstanceState);
        mMapEditService.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                LatLng curentServicePosition = new LatLng(mServiceAuto.getLatitude(), mServiceAuto.getLongitude());
                mSelectedServiceLocation = curentServicePosition;
                Log.i(TAG, "onMapReady: Set service current location: " + curentServicePosition.toString());
                googleMap.addMarker(new MarkerOptions().position(curentServicePosition).title("Locația service-ului"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curentServicePosition, 12.0f));
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        googleMap.clear();
                        mSelectedServiceLocation = latLng;
                        Log.i(TAG, "onMapClick: User selected: " + latLng.toString());
                        googleMap.addMarker(new MarkerOptions().position(latLng).title("Noua locația"));
                    }
                });
            }
        });
        mEditRepairServiceCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mEditPriceService.setEnabled(true);
                } else {
                    mEditPriceService.setEnabled(false);
                    mEditPriceService.setText("");
                }
            }
        });
        mEditServiceTireCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mEditPriceTire.setEnabled(true);
                } else {
                    mEditPriceTire.setEnabled(false);
                    mEditPriceTire.setText("");
                }
            }
        });
        mEditServiceChassisCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mEditPriceChassis.setEnabled(true);
                } else {
                    mEditPriceChassis.setEnabled(false);
                    mEditPriceChassis.setText("");
                }
            }
        });
        mEditServiceItpCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mEditPriceITP.setEnabled(true);
                } else {
                    mEditPriceITP.setEnabled(false);
                    mEditPriceITP.setText("");
                }
            }
        });
        mEditServiceName.setText(mServiceAuto.getName());
        mEditServiceAddress.setText(mServiceAuto.getAddress());
        mEditServiceCity.setText(mServiceAuto.getCity());
        mEditServicePhone.setText(mServiceAuto.getContactPhoneNumber());
        mEditServiceEmail.setText(mServiceAuto.getContactEmail());
        mEditServiceDescription.setText(mServiceAuto.getDescription());
        mEditServiceAcceptedBrand.setText(mServiceAuto.getAcceptedBrands());
        if ((mServiceAuto.getType() & 1) == 1) {
            mEditRepairServiceCheck.setChecked(true);
            mEditPriceService.setText(Integer.toString(mServiceAuto.getPriceService()));
        }
        if ((mServiceAuto.getType() & 2) == 2) {
            mEditServiceTireCheck.setChecked(true);
            mEditPriceTire.setText(Integer.toString(mServiceAuto.getPriceTire()));
        }
        if ((mServiceAuto.getType() & 4) == 4) {
            mEditServiceChassisCheck.setChecked(true);
            mEditPriceChassis.setText(Integer.toString(mServiceAuto.getPriceChassis()));
        }
        if ((mServiceAuto.getType() & 8) == 8) {
            mEditServiceItpCheck.setChecked(true);
            mEditPriceITP.setText(Integer.toString(mServiceAuto.getPriceItp()));
        }
        setOnClearTextListeners(editServiceView);
        Glide.with(mCtx)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .load(mServiceAuto.getImage())
                .into(mEditServiceImage);
        mEditServiceImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(mCtx, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, 2);
                } else {
                    Toast.makeText(mCtx, "Mai întai permite aplicației sa acceseze galeria!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mEditServicePopUp = new AlertDialog.Builder(ServiceAutoActivity.this)
                .setCustomTitle(editServiceTitle)
                .setView(editServiceView)
                .setPositiveButton("Confirmă", null)
                .setNegativeButton("Anulează", null)
                .create();
        mEditServicePopUp.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mConfirm = mEditServicePopUp.getButton(DialogInterface.BUTTON_POSITIVE);
                mCancel = mEditServicePopUp.getButton(DialogInterface.BUTTON_NEGATIVE);
                mConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "onClick: Confirma edit");
                        if (verifyInputOnClientSide()) {
                            mEditServiceProgressBar.setVisibility(View.VISIBLE);
                            mEditLinearLayout.setBackground(new ColorDrawable(Color.parseColor("#75676767")));
                            if (verifyInputOnServerSide()) {
                            } else {
                                mConfirm.setError("Eroare");
                            }
                        }
                    }
                });
            }
        });
        mEditServicePopUp.show();
    }

    private void setEnableFields(boolean value) {
        mEditServiceName.setEnabled(value);
        mEditServiceAddress.setEnabled(value);
        mEditServiceCity.setEnabled(value);
        mEditServicePhone.setEnabled(value);
        mEditServiceEmail.setEnabled(value);
        mEditServiceDescription.setEnabled(value);
        mEditServiceAcceptedBrand.setEnabled(value);
        mConfirm.setEnabled(value);
        mCancel.setEnabled(value);
        mEditRepairServiceCheck.setEnabled(value);
        mEditServiceTireCheck.setEnabled(value);
        mEditServiceChassisCheck.setEnabled(value);
        mEditServiceItpCheck.setEnabled(value);
        mEditServiceImage.setEnabled(value);
        mMapEditService.setEnabled(value);
        mEditPriceService.setEnabled(value);
        mEditPriceTire.setEnabled(value);
        mEditPriceChassis.setEnabled(value);
        mEditPriceITP.setEnabled(value);
    }

    private void updateUi() {
        int type = 0;
        if (mEditRepairServiceCheck.isChecked()) {
            type |= 1;
        }
        if (mEditServiceTireCheck.isChecked()) {
            type |= 2;
        }
        if (mEditServiceChassisCheck.isChecked()) {
            type |= 4;
        }
        if (mEditServiceItpCheck.isChecked()) {
            type |= 8;
        }
        mServiceAuto.setName(mEditServiceName.getText().toString().trim());
        mServiceAuto.setAddress(mEditServiceAddress.getText().toString().trim());
        mServiceAuto.setCity(mEditServiceCity.getText().toString().trim());
        mServiceAuto.setContactPhoneNumber(mEditServicePhone.getText().toString().trim());
        mServiceAuto.setContactEmail(mEditServiceEmail.getText().toString().trim());
        mServiceAuto.setDescription(mEditServiceDescription.getText().toString().trim());
        mServiceAuto.setAcceptedBrands(mEditServiceAcceptedBrand.getText().toString().trim());
        mServiceAuto.setType(type);

        mServiceAuto.setPriceService(mEditPriceService.getText().toString().trim().isEmpty() ? -1 : Integer.valueOf(mEditPriceService.getText().toString().trim()));
        mServiceAuto.setPriceTire(mEditPriceTire.getText().toString().trim().isEmpty() ? -1 : Integer.valueOf(mEditPriceTire.getText().toString().trim()));
        mServiceAuto.setPriceChassis(mEditPriceChassis.getText().toString().trim().isEmpty() ? -1 : Integer.valueOf(mEditPriceChassis.getText().toString().trim()));
        mServiceAuto.setPriceItp(mEditPriceITP.getText().toString().trim().isEmpty() ? -1 : Integer.valueOf(mEditPriceITP.getText().toString().trim()));
        mServiceAuto.setLongitude(mSelectedServiceLocation.longitude);
        mServiceAuto.setLatitude(mSelectedServiceLocation.latitude);

        setResult(3, mReturnIntent);
        mReturnIntent.putExtra("newServiceName", mEditServiceName.getText().toString().trim());
        mReturnIntent.putExtra("newServicePhone", mEditServicePhone.getText().toString().trim());
        mReturnIntent.putExtra("newServiceAddress", mEditServiceAddress.getText().toString().trim());
        mReturnIntent.putExtra("newServiceCity", mEditServiceCity.getText().toString().trim());
        mReturnIntent.putExtra("newServiceEmail", mEditServiceEmail.getText().toString().trim());
        mReturnIntent.putExtra("newServiceDescription", mEditServiceDescription.getText().toString().trim());
        mReturnIntent.putExtra("newServiceAcceptedBrand", mEditServiceAcceptedBrand.getText().toString().trim());
        mReturnIntent.putExtra("newServiceType", type);

        mReturnIntent.putExtra("priceService", mEditPriceService.getText().toString().trim().isEmpty() ? -1 : Integer.valueOf(mEditPriceService.getText().toString().trim()));
        mReturnIntent.putExtra("priceTire", mEditPriceTire.getText().toString().trim().isEmpty() ? -1 : Integer.valueOf(mEditPriceTire.getText().toString().trim()));
        mReturnIntent.putExtra("priceChassis", mEditPriceChassis.getText().toString().trim().isEmpty() ? -1 : Integer.valueOf(mEditPriceChassis.getText().toString().trim()));
        mReturnIntent.putExtra("priceItp", mEditPriceITP.getText().toString().trim().isEmpty() ? -1 : Integer.valueOf(mEditPriceITP.getText().toString().trim()));

        mReturnIntent.putExtra("newLongitude", mSelectedServiceLocation.longitude);
        mReturnIntent.putExtra("newLatitude", mSelectedServiceLocation.latitude);
    }

    private boolean verifyInputOnClientSide() {
        Log.i(TAG, "verifyInputOnClientSide:  verify edit service");
        boolean resultOk = true;
        if (mEditServiceName.getText().toString().trim().isEmpty()) {
            mEditServiceName.setError("Completează campul!");
            resultOk = false;
        }
        if (mEditServiceAddress.getText().toString().trim().isEmpty()) {
            mEditServiceAddress.setError("Completează campul!");
            resultOk = false;
        }
        if (mEditServiceCity.getText().toString().trim().isEmpty()) {
            mEditServiceCity.setError("Completează campul!");
            resultOk = false;
        }
        if (mEditServicePhone.getText().toString().trim().isEmpty()) {
            mEditServicePhone.setError("Completează campul!");
            resultOk = false;
        } else {
            if (!Patterns.PHONE.matcher(mEditServicePhone.getText().toString().trim()).matches()) {
                mEditServicePhone.setError("Numar invalid!");
                resultOk = false;
            }
        }
        if (mEditServiceEmail.getText().toString().trim().isEmpty()) {
            mEditServiceEmail.setError("Completează campul!");
            resultOk = false;
        } else {
            if (!Patterns.EMAIL_ADDRESS.matcher(mEditServiceEmail.getText().toString().trim()).matches()) {
                mEditServiceEmail.setError("Adresa nevalidă!");
                resultOk = false;
            }
        }
        if (mEditServiceDescription.getText().toString().trim().isEmpty()) {
            mEditServiceDescription.setError("Completează campul!");
            resultOk = false;
        }
        if (mEditServiceAcceptedBrand.getText().toString().trim().isEmpty()) {
            mEditServiceAcceptedBrand.setError("Completează campul!");
            resultOk = false;
        }
        if (!mEditRepairServiceCheck.isChecked() && !mEditServiceTireCheck.isChecked() && !mEditServiceChassisCheck.isChecked() && !mEditServiceItpCheck.isChecked()) {
            mEditPriceService.setError("Bifează o opțiune și adaugă prețul!");
            mEditPriceTire.setError("Bifează o opțiune și adaugă prețul!");
            mEditPriceChassis.setError("Bifează o opțiune și adaugă prețul!");
            mEditPriceITP.setError("Bifează o opțiune și adaugă prețul!");
            resultOk = false;
        }
        if (mEditPriceService.getText().toString().trim().isEmpty() && mEditRepairServiceCheck.isChecked()) {
            mEditPriceService.setError("Bifează o opțiune și adaugă prețul!");
            resultOk = false;
        }
        if (mEditPriceTire.getText().toString().trim().isEmpty() && mEditServiceTireCheck.isChecked()) {
            mEditPriceTire.setError("Bifează o opțiune și adaugă prețul!");
            resultOk = false;
        }
        if (mEditPriceChassis.getText().toString().trim().isEmpty() && mEditServiceChassisCheck.isChecked()) {
            mEditPriceChassis.setError("Bifează o opțiune și adaugă prețul!");
            resultOk = false;
        }
        if (mEditPriceITP.getText().toString().trim().isEmpty() && mEditServiceItpCheck.isChecked()) {
            mEditPriceITP.setError("Bifează o opțiune și adaugă prețul!");
            resultOk = false;
        }
        return resultOk;
    }

    private boolean verifyInputOnServerSide() {
        Log.i(TAG, "verifyInputOnServerSide: verify edit service");
        setEnableFields(false);
        boolean resultOk = true;
        final JsonObject jsonBody = new JsonObject();
        int type = 0;
        if (mEditRepairServiceCheck.isChecked()) {
            type |= 1;
        }
        if (mEditServiceTireCheck.isChecked()) {
            type |= 2;
        }
        if (mEditServiceChassisCheck.isChecked()) {
            type |= 4;
        }
        if (mEditServiceItpCheck.isChecked()) {
            type |= 8;
        }
        jsonBody.addProperty("serviceName", mEditServiceName.getText().toString().trim());
        jsonBody.addProperty("serviceAddress", mEditServiceAddress.getText().toString().trim());
        jsonBody.addProperty("serviceCity", mEditServiceAddress.getText().toString().trim());
        jsonBody.addProperty("servicePhone", mEditServicePhone.getText().toString().trim());
        jsonBody.addProperty("serviceEmail", mEditServiceEmail.getText().toString().trim());
        jsonBody.addProperty("serviceAcceptedBrand", mEditServiceAcceptedBrand.getText().toString().trim());
        jsonBody.addProperty("serviceDescription", mEditServiceDescription.getText().toString().trim());
        jsonBody.addProperty("serviceType", type);
        jsonBody.addProperty("longitude", mSelectedServiceLocation.longitude);
        jsonBody.addProperty("latitude", mSelectedServiceLocation.latitude);
        jsonBody.addProperty("serviceOwner", mServiceAuto.getOwnerId());
        jsonBody.addProperty("serviceId", mServiceAuto.getServiceId());
        jsonBody.addProperty("priceService", mEditPriceService.getText().toString());
        jsonBody.addProperty("priceTire", mEditPriceTire.getText().toString());
        jsonBody.addProperty("priceChassis", mEditPriceChassis.getText().toString());
        jsonBody.addProperty("priceItp", mEditPriceITP.getText().toString());

        Bitmap bitmap = ((BitmapDrawable) mEditServiceImage.getDrawable()).getBitmap();
        //need something more efficient, possibility of crash!! same on edit profile
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        Log.i(TAG, "verifyInputOnServerSide: getLastPathSegment: " + mServiceAuto.getImage().getLastPathSegment());
        final StorageReference newImagePath = FirebaseStorage.getInstance().getReference().child(mServiceAuto.getImage().getLastPathSegment());
        newImagePath.putBytes(data).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        newImagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String newImageDownloadLink = uri.toString();
                                jsonBody.addProperty("imagePath", newImageDownloadLink);
                                Response<JsonObject> response = null;
                                try {
                                    response = Ion.with(mCtx)
                                            .load("PUT", mUrl + "/service/editService")
                                            .setHeader("Authorization", mPreferencesManager.getToken())
                                            .setJsonObjectBody(jsonBody)
                                            .asJsonObject()
                                            .withResponse()
                                            .get();

                                    if (response.getHeaders().code() == 200) {
                                        Log.i(TAG, "verifyInputOnServerSide: Service edited!");
                                        Toast.makeText(mCtx, "Service editat cu succes!", Toast.LENGTH_SHORT).show();
                                        updateUi();
                                        mServiceAuto.setImage(Uri.parse(newImageDownloadLink));
                                        mReturnIntent.putExtra("newLogoImage", newImageDownloadLink);
                                        populateActivity();
                                        mEditServiceProgressBar.setVisibility(View.GONE);
                                        mEditServicePopUp.dismiss();
                                    } else {
                                        setEnableFields(true);
                                        mEditServiceProgressBar.setVisibility(View.GONE);
                                        Log.i(TAG, "verifyInputOnServerSide: Service not edited! err code: " + response.getHeaders().code());
                                        Toast.makeText(mCtx, "Ceva nu a mers! Verifica conexiunea la internet!", Toast.LENGTH_SHORT).show();
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
                                Log.i(TAG, "onFailure: fail to get download link");
                                setEnableFields(true);
                                Toast.makeText(mCtx, "Ceva nu a mers! Verifica conexiunea la internet!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: Fail to add image to firebase");
                Toast.makeText(mCtx, "Ceva nu a mers! Verifica conexiunea la internet!", Toast.LENGTH_SHORT).show();
                setEnableFields(true);
            }
        });

        return resultOk;
    }

    private void seeOfferRequests() {
        Log.i(TAG, "seeOfferRequests: ");

        Intent startOfferRequests = new Intent(mCtx, OfferRequestsActivity.class);
        startOfferRequests.putExtra("serviceId", mServiceAuto.getServiceId());
        startActivity(startOfferRequests);
    }

    private void seeTodaySchedule() {
        Log.i(TAG, "seeTodaySchedule: ");
        Intent startTodaySchedule = new Intent(mCtx, TodayAppointmentsActivity.class);
        startTodaySchedule.putExtra("serviceId", mServiceAuto.getServiceId());
        startActivity(startTodaySchedule);
    }

    private void expandFloatingMenu() {
        mCallServiceFab.show();
        mSendMailToServiceFab.show();
        if (ContextCompat.checkSelfPermission(mCtx, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mCreateRouteToService.show();
        }
        mFabMenuExpanded = true;
        mMenuServiceFloatingButton.setImageResource(R.drawable.ic_close_black_24dp);
    }

    private void closeFloatingMenu() {
        mCallServiceFab.hide();
        mSendMailToServiceFab.hide();
        mCreateRouteToService.hide();
        mFabMenuExpanded = false;
        mMenuServiceFloatingButton.setImageResource(R.drawable.ic_menu_black_24dp);
    }

    private void showRequestOffer(boolean withMyPartsAvailable) {
        View requestOffer = getLayoutInflater().inflate(R.layout.request_offer_popup_layout, null);
        final EditText requestText = requestOffer.findViewById(R.id.requestText);
        final EditText carType = requestOffer.findViewById(R.id.carType);
        final EditText carModel = requestOffer.findViewById(R.id.carModel);
        final EditText carYear = requestOffer.findViewById(R.id.carYear);
        final EditText carVin = requestOffer.findViewById(R.id.carVin);
        TextView withMyPartsLabel = requestOffer.findViewById(R.id.withMyPartsLabel);
        final CheckBox withMyPartsCheck = requestOffer.findViewById(R.id.withMyPartsCheck);
        if (!withMyPartsAvailable) {
            withMyPartsLabel.setVisibility(View.GONE);
            withMyPartsCheck.setVisibility(View.GONE);
        }
        TextView requestOfferTitle = new TextView(mCtx);
        requestOfferTitle.setText("Cere ofertă");
        requestOfferTitle.setGravity(Gravity.CENTER);
        requestOfferTitle.setPadding(10, 10, 10, 10);
        requestOfferTitle.setTextSize(18);
        requestOfferTitle.setTextColor(Color.DKGRAY);
        AlertDialog requestOfferPopUp = new AlertDialog.Builder(ServiceAutoActivity.this)
                .setCustomTitle(requestOfferTitle)
                .setView(requestOffer)
                .setPositiveButton("Confirmă", new DialogInterface.OnClickListener() {
                    //ToDo: ca in login sa nu se inchida dialogul daca ceva nu e valid
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (requestText.getText().toString().trim().isEmpty() ||
                                carType.getText().toString().trim().isEmpty() ||
                                carModel.getText().toString().trim().isEmpty() ||
                                carYear.getText().toString().trim().isEmpty() ||
                                carVin.getText().toString().trim().isEmpty()) {
                            Toast.makeText(mCtx, "Trebuie completate toate câmpurile cererii!", Toast.LENGTH_SHORT).show();
                        } else {
                            if (carVin.getText().toString().trim().length() != 17) {
                                Toast.makeText(mCtx, "VIN-ul completat nu este valid!", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.i(TAG, "onClick: Request: " + requestText.getText().toString() +
                                        " With My Parts: " + withMyPartsCheck.isChecked() +
                                        " carType: " + carType.getText().toString() +
                                        " carModel: " + carModel.getText().toString() +
                                        " carYear: " + carYear.getText().toString() +
                                        " carVin: " + carVin.getText().toString());
                                JsonObject requestDetails = new JsonObject();
                                requestDetails.addProperty("serviceId", mServiceAuto.getServiceId());
                                requestDetails.addProperty("userId", mPreferencesManager.getUserId());
                                requestDetails.addProperty("request", requestText.getText().toString());
                                requestDetails.addProperty("withUserParts", withMyPartsCheck.isChecked());
                                requestDetails.addProperty("carType", carType.getText().toString());
                                requestDetails.addProperty("carModel", carModel.getText().toString());
                                requestDetails.addProperty("carYear", carYear.getText().toString());
                                requestDetails.addProperty("carVin", carVin.getText().toString());
                                try {
                                    Response<JsonObject> response = Ion.with(mCtx)
                                            .load("POST", mUrl + "/requestedOffers/addRequestedOffer")
                                            .setHeader("Authorization", mPreferencesManager.getToken())
                                            .setJsonObjectBody(requestDetails)
                                            .asJsonObject()
                                            .withResponse()
                                            .get();
                                    if (response.getHeaders().code() == 201) {
                                        Log.i(TAG, "onClick: requestAdded");
                                        Toast.makeText(getApplicationContext(), "Cerere realizată cu succes!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (response.getHeaders().code() == 409) {
                                            Toast.makeText(getApplicationContext(), "Conflict in baza de date!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Error code: " + response.getHeaders().code(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                })
                .setNegativeButton("Anulează", null)
                .create();
        requestOfferPopUp.show();
    }

    private void showDatePicker(final String shortDescription,
                                final boolean checkRepair,
                                final boolean checkTire,
                                final boolean checkChassis,
                                final boolean checkItp) {
        mDialogCaldroidFragment = CaldroidFragment.newInstance("Alege data", Calendar.getInstance().get(Calendar.MONTH) + 1, Calendar.getInstance().get(Calendar.YEAR));
        ArrayList<String> lockedDays = new ArrayList<>();
        mDialogCaldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(Color.parseColor("#3385ff")), Calendar.getInstance().getTime());
        mDialogCaldroidFragment.setTextColorForDate(R.color.colorWhite, Calendar.getInstance().getTime());
        //set default locked days (SUNDAY, SATURDAY and days after current date + one year)
        setDefaultLockedDays(mDialogCaldroidFragment, lockedDays);
        //get locked days from server
        try {
            Response<JsonObject> response = Ion.with(mCtx)
                    .load("GET", mUrl + "/getLockedDays/" + mServiceAuto.getServiceId())
                    .setHeader("Authorization", mPreferencesManager.getToken())
                    .asJsonObject()
                    .withResponse()
                    .get();
            if (response.getHeaders().code() == 200) {
                JsonArray jsonArray = response.getResult().get("lockedDays").getAsJsonArray();
                for (JsonElement day : jsonArray) {
                    Log.i(TAG, "onClick: day locked: " + day.getAsString());
                    lockedDays.add(day.getAsString());
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mDialogCaldroidFragment.setDisableDatesFromString(lockedDays);
        CaldroidListener listener = new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                //Toast.makeText(getApplicationContext(), date.toString(), Toast.LENGTH_SHORT).show();
                //get locked hour from server
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                ArrayList<String> lockedHours = new ArrayList<>();
                try {
                    Response<JsonObject> response = Ion.with(mCtx)
                            .load("GET", mUrl + "/getLockedHoursForDay/" + format.format(date) + "/serviceId/" + mServiceAuto.getServiceId())
                            .setHeader("Authorization", mPreferencesManager.getToken())
                            .asJsonObject()
                            .withResponse()
                            .get();
                    if (response.getHeaders().code() == 200) {
                        JsonArray jsonArray = response.getResult().get("lockedHours").getAsJsonArray();
                        for (JsonElement day : jsonArray) {
                            Log.i(TAG, "onClick: hour locked: " + day.getAsString());
                            lockedHours.add(day.getAsString());
                        }
                    }
                    showTimePicker(lockedHours,
                            date,
                            shortDescription,
                            checkRepair,
                            checkTire,
                            checkChassis,
                            checkItp);
                    mDialogCaldroidFragment.dismiss();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        mDialogCaldroidFragment.setCaldroidListener(listener);
        mDialogCaldroidFragment.show(getSupportFragmentManager(), "TAG");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (mDialogCaldroidFragment != null) {
                mDialogCaldroidFragment.dismiss();
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mDialogCaldroidFragment != null) {
                mDialogCaldroidFragment.dismiss();
            }
        }
    }

    private void showTimePicker(ArrayList<String> lockedHours,
                                final Date date,
                                final String shortDescription,
                                final boolean checkRepair,
                                final boolean checkTire,
                                final boolean checkChassis,
                                final boolean checkItp) {
        final ListView listView = new ListView(mCtx);
        ArrayList<String> availableHours = new ArrayList<>();
        //ToDo: for i=start program hour to end program hour if program will be available, probably no
        availableHours.add("08:00");
        availableHours.add("09:00");
        availableHours.add("10:00");
        availableHours.add("11:00");
        availableHours.add("12:00");
        availableHours.add("13:00");
        availableHours.add("14:00");
        availableHours.add("15:00");
        availableHours.add("16:00");
        availableHours.add("17:00");
        for (String elem : lockedHours) {
            availableHours.remove(elem);
        }
        TextView choseHourTitle = new TextView(mCtx);
        choseHourTitle.setText("Alege ora");
        choseHourTitle.setGravity(Gravity.CENTER);
        choseHourTitle.setPadding(10, 0, 10, 0);
        choseHourTitle.setTextSize(18);
        choseHourTitle.setTextColor(Color.DKGRAY);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mCtx, R.layout.time_list_element_layout, availableHours);
        listView.setAdapter(adapter);
        final AlertDialog timePickerDialog = new AlertDialog.Builder(ServiceAutoActivity.this)
                .setCustomTitle(choseHourTitle)
                .create();
        timePickerDialog.setView(listView, 5, 5, 5, 5);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String chosenHour = (String) listView.getItemAtPosition(position);
                //Toast.makeText(getApplicationContext(), chosenHour, Toast.LENGTH_SHORT).show();
                int type = 0;
                if (checkRepair) {
                    type |= 1;
                }
                if (checkTire) {
                    type |= 2;
                }
                if (checkChassis) {
                    type |= 4;
                }
                if (checkItp) {
                    type |= 8;
                }
                JsonObject jsonBody = new JsonObject();
                jsonBody.addProperty("serviceId", mServiceAuto.getServiceId());
                jsonBody.addProperty("ownerId", mPreferencesManager.getUserId());
                jsonBody.addProperty("day", new SimpleDateFormat("yyyy-MM-dd").format(date));
                jsonBody.addProperty("hour", chosenHour);
                jsonBody.addProperty("shortDescription", shortDescription);
                jsonBody.addProperty("scheduleType", type);

                Log.i(TAG, "onItemClick: shortDescription: " + shortDescription);
                try {
                    Response<JsonObject> response = Ion.with(getApplicationContext())
                            .load("POST", mUrl + "/addLockedPeriod")
                            .setHeader("Authorization", mPreferencesManager.getToken())
                            .setJsonObjectBody(jsonBody)
                            .asJsonObject()
                            .withResponse()
                            .get();
                    if (response.getHeaders().code() == 201) {
                        Log.i(TAG, "onItemClick: scheduled!");
                        Toast.makeText(getApplicationContext(), "Programare realizată cu succes!", Toast.LENGTH_SHORT).show();
                    } else {
                        if (response.getHeaders().code() == 403) {
                            TextView oncePerDayTitle = new TextView(mCtx);
                            oncePerDayTitle.setText("Incercare de spam!");
                            oncePerDayTitle.setGravity(Gravity.CENTER);
                            oncePerDayTitle.setPadding(10, 10, 10, 10);
                            oncePerDayTitle.setTextSize(18);
                            oncePerDayTitle.setTextColor(Color.DKGRAY);
                            TextView oncePerDayContent = new TextView(mCtx);
                            oncePerDayContent.setText("Ai voie sa te programezi o singură data pe zi la un service!");
                            oncePerDayContent.setGravity(Gravity.CENTER);
                            oncePerDayContent.setPadding(10, 10, 10, 10);
                            AlertDialog requestType = new AlertDialog.Builder(ServiceAutoActivity.this)
                                    .setCustomTitle(oncePerDayTitle)
                                    .setView(oncePerDayContent)
                                    .setPositiveButton("Am ințeles!", null)
                                    .create();
                            requestType.show();
                        } else {
                            if (response.getHeaders().code() == 409) {
                                Toast.makeText(getApplicationContext(), "Conflict in baza de date!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Error code: " + response.getHeaders().code(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                timePickerDialog.dismiss();
            }
        });
        timePickerDialog.show();
        timePickerDialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.6),
                (int) (getResources().getDisplayMetrics().heightPixels * 0.6));
    }

    private void setDefaultLockedDays(CaldroidFragment caldroidFragment, ArrayList<String> lockedDays) {
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.DATE, 1);
        caldroidFragment.setMinDate(minDate.getTime());

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 1; i < 366; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, i);
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                lockedDays.add(format.format(calendar.getTime()));
            }
        }

        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DATE, 365);
        caldroidFragment.setMaxDate(maxDate.getTime());
    }

    private void showPopUpNotLogged() {
        Log.i(TAG, "showPopUpNotLogged: ");
        TextView notLoggedContent = new TextView(mCtx);
        notLoggedContent.setText("Pentru a executa aceasta acțiune trebuie să fii logat. Vrei să te autentifici?");
        notLoggedContent.setGravity(Gravity.CENTER);
        notLoggedContent.setPadding(10, 10, 10, 10);
        TextView notLoggedTitle = new TextView(mCtx);
        notLoggedTitle.setText("Acțiune interzisă");
        notLoggedTitle.setGravity(Gravity.CENTER);
        notLoggedTitle.setPadding(10, 10, 10, 10);
        notLoggedTitle.setTextSize(18);
        notLoggedTitle.setTextColor(Color.DKGRAY);
        AlertDialog notLoggedPopUp = new AlertDialog.Builder(ServiceAutoActivity.this)
                .setCustomTitle(notLoggedTitle)
                .setView(notLoggedContent)
                .setPositiveButton("Autentifică-te", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "onClick: go to auth page");
                        Intent intent = new Intent(mCtx, AuthenticationActivity.class);
                        startActivityForResult(intent, 3);
                    }
                })
                .setNegativeButton("Anulează", null)
                .create();
        notLoggedPopUp.show();
    }

    private Bitmap createBitmapFromLocalImage(String name) {
        Log.i(TAG, "createBitmapFromLocalImage: ");
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(mCtx
                    .openFileInput(name));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @SuppressLint("DefaultLocale")
    private void populateActivity() {
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(String.format("%s - %s", mServiceAuto.getName(), mServiceAuto.getAddress()));
        getSupportActionBar().setTitle("");
        Glide.with(mCtx)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .load(mServiceAuto.getImage())
                .into(mLogoImage);
        mRatingBar.setRating(mServiceAuto.getRating());
        mDescription.setText(mServiceAuto.getDescription());
        mContactPhoneNumber.setText(mServiceAuto.getContactPhoneNumber());
        mContactEmail.setText(mServiceAuto.getContactEmail());
        if (ContextCompat.checkSelfPermission(mCtx, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "populateActivity: permission are granted");
            double distance = mServiceAuto.calculateDistance(mPreferencesManager.getUserLatitude(), mPreferencesManager.getUserLongitude());
            if (distance < 1) {
                mDistanceFromYou.setText(String.format("%d m", (int) (distance * 1000)));
            } else {
                mDistanceFromYou.setText(String.format("%.2f km", distance));
            }
        }
        StringBuilder offeredServices = new StringBuilder();
        if ((mServiceAuto.getType() & 1) == 1) {
            offeredServices.append("Service, ");
        }
        if ((mServiceAuto.getType() & 2) == 2) {
            offeredServices.append("Vulcanizare, ");
        }
        if ((mServiceAuto.getType() & 4) == 4) {
            offeredServices.append("Tinichigerie, ");
        }
        if ((mServiceAuto.getType() & 8) == 8) {
            offeredServices.append("ITP, ");
        }
        SpannableStringBuilder spannable = new SpannableStringBuilder(String.format("Servicii oferite: %s", offeredServices.subSequence(0, offeredServices.length() - 2)));
        ForegroundColorSpan color = new ForegroundColorSpan(Color.parseColor("#001952"));
        final StyleSpan style = new StyleSpan(Typeface.ITALIC);
        spannable.setSpan(color, 17, spannable.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannable.setSpan(style, 17, spannable.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mServiceType.setText(spannable);
        spannable = new SpannableStringBuilder(String.format("Brand-uri acceptate: %s", mServiceAuto.getAcceptedBrands()));
        spannable.setSpan(color, 19, spannable.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannable.setSpan(style, 19, spannable.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mServiceAcceptedBrands.setText(spannable);
        StringBuilder prices = new StringBuilder();
        if (mServiceAuto.getPriceService() != -1) {
            prices.append(String.format("Service: %s Lei,", mServiceAuto.getPriceService()));
        }
        if (mServiceAuto.getPriceTire() != -1) {
            prices.append(String.format(" Vulcanizare: %s Lei,", mServiceAuto.getPriceTire()));
        }
        if (mServiceAuto.getPriceChassis() != -1) {
            prices.append(String.format(" Tinichigerie: %s Lei,", mServiceAuto.getPriceChassis()));
        }
        if (mServiceAuto.getPriceItp() != -1) {
            prices.append(String.format(" ITP: %s Lei,", mServiceAuto.getPriceItp()));
        }
        spannable = new SpannableStringBuilder(String.format("Prețuri: %s", prices.subSequence(0, prices.length() - 1)));
        spannable.setSpan(color, 9, spannable.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannable.setSpan(style, 9, spannable.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mServicePrices.setText(spannable);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.openCommentsPage:
                seeComments();
                break;
            case R.id.leaveComment:
                leaveComment();
                break;
            case R.id.requestOffer:
                requestOffer();
                break;
            case R.id.addAppointment:
                addAppointment();
                break;
            case R.id.editService:
                editService();
                break;
            case R.id.openOfferRequestsPage:
                seeOfferRequests();
                break;
            case R.id.openTodaySchedulePage:
                seeTodaySchedule();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu: ");
        MenuInflater inflater = getMenuInflater();
        if (!isMyService) {
            inflater.inflate(R.menu.menu_service_auto, menu);
        } else {
            inflater.inflate(R.menu.menu_my_service_auto, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 3) {
            Log.i(TAG, "onActivityResult: out from login intent");

        }
        if (requestCode == 1) {
            Log.i(TAG, "onActivityResult: out from comments intent");
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "onActivityResult: Update after deletion");
                mRatingBar.setRating(data.getFloatExtra("deletionNewRating", 0));
                setResult(RESULT_OK, mReturnIntent);
                mReturnIntent.putExtra("creationDeletionNewRating", mRatingBar.getRating());
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAG, "onActivityResult: No deleted comment!");
            }
        }
        if (requestCode == 2) {
            if (data != null) {
                Uri chosenImageByUser = data.getData();
                mEditServiceImage.setImageURI(chosenImageByUser);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
