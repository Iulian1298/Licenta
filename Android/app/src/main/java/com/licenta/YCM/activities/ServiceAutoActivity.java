package com.licenta.YCM.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.licenta.YCM.R;
import com.licenta.YCM.SharedPreferencesManager;
import com.licenta.YCM.models.ServiceAuto;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static java.security.AccessController.getContext;

public class ServiceAutoActivity extends AppCompatActivity {
    private static final String TAG = "ServiceAutoActivity";

    private SharedPreferencesManager mPreferencesManager;
    private ServiceAuto mServiceAuto;
    private ImageView mLogoImage;
    private TextView mName;
    private TextView mAddress;
    private RatingBar mRatingBar;
    //private TextView mWorkingProgram;
    private TextView mDescription;
    private TextView mContactPhoneNumber;
    private TextView mContactEmail;
    private TextView mDistanceFromYou;
    private Button mDialButton;
    private Button mSendMailButton;
    private Button mViewComments;
    private Button mLeaveComment;
    private Button mScheduleToService;

    private Context mCtx;
    private String mAddCommentURL;
    private Intent mReturnIntent;
    private boolean mIsLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_auto);
        mCtx = getApplication();
        mPreferencesManager = SharedPreferencesManager.getInstance(mCtx);
        try {
            mIsLoggedIn = mPreferencesManager.isLoggedIn();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        init();
    }

    private void init() {
        Log.i(TAG, "init: ");
        mLogoImage = findViewById(R.id.logoImageFull);
        mName = findViewById(R.id.serviceNameFull);
        mAddress = findViewById(R.id.addressFull);
        mRatingBar = findViewById(R.id.ratingFull);
        mDescription = findViewById(R.id.serviceDescriptionFull);
        mContactPhoneNumber = findViewById(R.id.contactPhoneNumbeFullInfo);
        mContactEmail = findViewById(R.id.contactEmailFullInfo);
        mDistanceFromYou = findViewById(R.id.distanceFromYouFull);
        if (!mIsLoggedIn) {
            mDistanceFromYou.setVisibility(View.GONE);
        }
        mDialButton = findViewById(R.id.dialButton);
        mSendMailButton = findViewById(R.id.sendMailButton);
        mViewComments = findViewById(R.id.viewComments);
        mLeaveComment = findViewById(R.id.leaveComment);
        mScheduleToService = findViewById(R.id.scheduleToService);

        mAddCommentURL = "http://10.0.2.2:5000/comments/addComment";
        Intent intent = getIntent();
        mServiceAuto = new ServiceAuto(
                intent.getStringExtra("serviceId"),
                stringToBitmap(intent.getStringExtra("logoImage")),
                intent.getStringExtra("serviceName"),
                intent.getStringExtra("description"),
                intent.getStringExtra("address"),
                intent.getFloatExtra("rating", 0),
                intent.getStringExtra("contactPhoneNumber"),
                intent.getStringExtra("contactEmail"), intent.getDoubleExtra("latitude", 0),
                intent.getDoubleExtra("longitude", 0)
        );
        Objects.requireNonNull(getSupportActionBar()).setTitle(mServiceAuto.getName());

        populateActivity();
        mReturnIntent = new Intent();
        setResult(RESULT_CANCELED, mReturnIntent);

        mDialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "onClick() -> dial number: " + mServiceAuto.getContactPhoneNumber());
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel: +4" + mServiceAuto.getContactPhoneNumber()));
                startActivity(dialIntent);
            }
        });
        mSendMailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "onClick() -> send mail to: " + mServiceAuto.getContactEmail());
                Intent sendMessageIntent = new Intent(Intent.ACTION_SENDTO);
                sendMessageIntent.setData(Uri.parse("mailto: " + mServiceAuto.getContactEmail()));
                startActivity(Intent.createChooser(sendMessageIntent,
                        "Trimite mail-ul cu: "));
            }
        });
        mViewComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "onClick() -> open comments page ");
                Intent intentViewComments = new Intent(getApplicationContext(), CommentsActivity.class);
                intentViewComments.putExtra("serviceId", mServiceAuto.getServiceId());
                startActivityForResult(intentViewComments, 1);
            }
        });
        mLeaveComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "onClick() -> show leave comment pop-up ");
                if (!mIsLoggedIn) {
                    showPopUpNotLogged();
                } else {
                    final View leaveCommentView = getLayoutInflater().inflate(R.layout.leave_comment_popup_layout, null);
                    final EditText givenComment = leaveCommentView.findViewById(R.id.givenComment);
                    final RatingBar givenRating = leaveCommentView.findViewById(R.id.givenRating);
                    TextView addCommentTitle = new TextView(getApplicationContext());
                    addCommentTitle.setText("Lasa un comentariu!");
                    addCommentTitle.setGravity(Gravity.CENTER);
                    addCommentTitle.setPadding(10, 10, 10, 10);
                    addCommentTitle.setTextSize(18);
                    addCommentTitle.setTextColor(Color.DKGRAY);

                    AlertDialog addCommentPopUp = new AlertDialog.Builder(ServiceAutoActivity.this)
                            .setCustomTitle(addCommentTitle)
                            .setView(leaveCommentView)
                            .setPositiveButton("Adauga", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i(TAG, "onCick() -> onClick() -> Comentariu: " + givenComment.getText());
                                    Log.i(TAG, "onCick() -> onClick() -> Rating: " + givenRating.getRating());
                                    if (givenComment.getText().toString().trim().isEmpty() || givenRating.getRating() == 0) {
                                        Toast.makeText(getApplicationContext(), "Completeaza toate campurile", Toast.LENGTH_SHORT).show();
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
                                                    .load("POST", mAddCommentURL)
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
                            .setNegativeButton("Anuleaza", null)
                            .create();
                    addCommentPopUp.show();
                }
            }
        });
        mScheduleToService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: schedule clicked");
                if (!mIsLoggedIn) {
                    showPopUpNotLogged();
                } else {
                    //ToDo: show time/date picker an schedule to a service
                }

            }
        });
    }

    private void showPopUpNotLogged() {
        Log.i(TAG, "showPopUpNotLogged: ");
        TextView notLoggedContent = new TextView(mCtx);
        notLoggedContent.setText("Pentru a executa aceasta actiune trebuie sa fii logat. Vrei sa te autentifici?");
        notLoggedContent.setGravity(Gravity.CENTER);
        notLoggedContent.setPadding(10, 10, 10, 10);
        TextView notLoggedTitle = new TextView(getApplicationContext());
        notLoggedTitle.setText("Actiune interzisa!");
        notLoggedTitle.setGravity(Gravity.CENTER);
        notLoggedTitle.setPadding(10, 10, 10, 10);
        notLoggedTitle.setTextSize(18);
        notLoggedTitle.setTextColor(Color.DKGRAY);
        AlertDialog notLoggedPopUp = new AlertDialog.Builder(ServiceAutoActivity.this)
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

    @SuppressLint("DefaultLocale")
    private void populateActivity() {
        mLogoImage.setImageBitmap(mServiceAuto.getImage());
        mName.setText(mServiceAuto.getName());
        mAddress.setText(mServiceAuto.getAddress());
        mRatingBar.setRating(mServiceAuto.getRating());
        mDescription.setText(mServiceAuto.getDescription());
        mContactPhoneNumber.setText(mServiceAuto.getContactPhoneNumber());
        mContactEmail.setText(mServiceAuto.getContactEmail());
        mDistanceFromYou.setText(String.format("%s %.2f", "Distanta fata de tine:", mServiceAuto.calculateDistance(0, 0)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 3) {
            Log.i(TAG, "onActivityResult: out from login intent");
            try {
                mIsLoggedIn = mPreferencesManager.isLoggedIn();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (mIsLoggedIn) {
                mDistanceFromYou.setVisibility(View.VISIBLE);
            }
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
}
