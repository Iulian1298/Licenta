package com.licenta.YCM.activities;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;


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

    private Context mCtx;
    private String mAddCommentURL;
    private Intent mReturnIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_auto);
        mCtx = getApplicationContext();
        mPreferencesManager = SharedPreferencesManager.getInstance(mCtx);

        init();
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (!mPreferencesManager.getPermissionLocation()) {
            mDistanceFromYou.setVisibility(View.GONE);
        }
        mDialButton = findViewById(R.id.dialButton);
        mSendMailButton = findViewById(R.id.sendMailButton);

        mAddCommentURL = "http://10.0.2.2:5000/comments/addComment";
        Intent intent = getIntent();
        mServiceAuto = new ServiceAuto(
                intent.getStringExtra("serviceId"),
                createBitmapFromLocalImage(intent.getStringExtra("logoImage")),
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.service_auto_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean isLoggedIn = false;
        try {
            isLoggedIn = mPreferencesManager.isLoggedIn();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        switch (item.getItemId()) {
            case R.id.viewComments:
                Log.v(TAG, "onClick() -> open comments page ");
                Intent intentViewComments = new Intent(getApplicationContext(), CommentsActivity.class);
                intentViewComments.putExtra("serviceId", mServiceAuto.getServiceId());
                startActivityForResult(intentViewComments, 1);
                break;
            case R.id.leaveComment:
                Log.v(TAG, "onClick() -> show leave comment pop-up ");
                if (!isLoggedIn) {
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
                break;
            case R.id.scheduleToService:
                Log.i(TAG, "onClick: schedule clicked");
                //ToDo: before date time picker ask user type of scheule (constatare, vulcanizare,
                //ToDo: tinichigerie)de adaugat in bd tipul iar la interogare trebuie vazut
                //ToDo: ce tip vrea userul (tipul trebuie adaugat la time si facut cheie multipla cu dayId ul si time ul
                if (!isLoggedIn) {
                    showPopUpNotLogged();
                } else {
                    final CaldroidFragment dialogCaldroidFragment = CaldroidFragment.newInstance("Alege data", Calendar.getInstance().get(Calendar.MONTH) + 1, Calendar.getInstance().get(Calendar.YEAR));
                    ArrayList<String> lockedDays = new ArrayList<>();
                    dialogCaldroidFragment.setBackgroundDrawableForDate(new ColorDrawable(Color.parseColor("#3385ff")), Calendar.getInstance().getTime());
                    dialogCaldroidFragment.setTextColorForDate(R.color.colorWhite, Calendar.getInstance().getTime());
                    //set default locked days (SUNDAY, SATURDAY and days after current date + one year)
                    setDefaultLockedDays(dialogCaldroidFragment, lockedDays);
                    //get locked days from server
                    try {
                        Response<JsonObject> response = Ion.with(mCtx)
                                .load("GET", "http://10.0.2.2:5000/getLockedDays/" + mServiceAuto.getServiceId())
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
                    dialogCaldroidFragment.setDisableDatesFromString(lockedDays);
                    CaldroidListener listener = new CaldroidListener() {
                        @Override
                        public void onSelectDate(Date date, View view) {
                            Toast.makeText(getApplicationContext(), date.toString(),
                                    Toast.LENGTH_SHORT).show();
                            //get locked hour from server
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                            ArrayList<String> lockedHours = new ArrayList<>();
                            try {
                                Response<JsonObject> response = Ion.with(mCtx)
                                        .load("GET", "http://10.0.2.2:5000/getLockedHoursForDay/" + format.format(date) + "/serviceId/" + mServiceAuto.getServiceId())
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
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            showTimePicker(lockedHours, date);
                            dialogCaldroidFragment.dismiss();
                        }
                    };
                    dialogCaldroidFragment.setCaldroidListener(listener);
                    dialogCaldroidFragment.show(getSupportFragmentManager(), "TAG");
                }
                break;
            case R.id.requestOffer:
                if (!isLoggedIn) {
                    showPopUpNotLogged();
                } else {
                    TextView requestTypeTitle = new TextView(mCtx);
                    requestTypeTitle.setText("Tipul de problema!");
                    requestTypeTitle.setGravity(Gravity.CENTER);
                    requestTypeTitle.setPadding(10, 10, 10, 10);
                    requestTypeTitle.setTextSize(18);
                    requestTypeTitle.setTextColor(Color.DKGRAY);
                    TextView requestTypeContent = new TextView(mCtx);
                    requestTypeContent.setText("Apasa \"DA\" daca vrei o oferta pentru rezolvarea unei probleme mecanice sau \"NU\" altfel!");
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
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
        requestOfferTitle.setText("Filtrare avansata!");
        requestOfferTitle.setGravity(Gravity.CENTER);
        requestOfferTitle.setPadding(10, 10, 10, 10);
        requestOfferTitle.setTextSize(18);
        requestOfferTitle.setTextColor(Color.DKGRAY);
        AlertDialog requestOfferPopUp = new AlertDialog.Builder(ServiceAutoActivity.this)
                .setCustomTitle(requestOfferTitle)
                .setView(requestOffer)
                .setPositiveButton("Confirma", new DialogInterface.OnClickListener() {
                    //ToDo: ca in login sa nu se inchida dialogul daca ceva nu e valid
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (requestText.getText().toString().trim().isEmpty() ||
                                carType.getText().toString().trim().isEmpty() ||
                                carModel.getText().toString().trim().isEmpty() ||
                                carYear.getText().toString().trim().isEmpty() ||
                                carVin.getText().toString().trim().isEmpty()) {
                            Toast.makeText(mCtx, "Trebuie completate toate campurile cererii!", Toast.LENGTH_SHORT).show();
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
                                            .load("POST", "http://10.0.2.2:5000/addRequestedOffer")
                                            .setJsonObjectBody(requestDetails)
                                            .asJsonObject()
                                            .withResponse()
                                            .get();
                                    if (response.getHeaders().code() == 201) {
                                        Log.i(TAG, "onClick: requestAdded");
                                        Toast.makeText(getApplicationContext(), "Cerere realizata cu succes!", Toast.LENGTH_SHORT).show();
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
                .setNegativeButton("Anuleaza", null)
                .create();
        requestOfferPopUp.show();
    }


    private void showTimePicker(ArrayList<String> lockedHours, final Date date) {
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
        choseHourTitle.setText("Alege ora!");
        choseHourTitle.setGravity(Gravity.CENTER);
        choseHourTitle.setPadding(10, 10, 10, 10);
        choseHourTitle.setTextSize(18);
        choseHourTitle.setTextColor(Color.DKGRAY);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mCtx, R.layout.time_list_element_layout, availableHours);
        listView.setAdapter(adapter);
        final AlertDialog timePickerDialog = new AlertDialog.Builder(ServiceAutoActivity.this)
                .setCustomTitle(choseHourTitle)
                .create();
        timePickerDialog.setView(listView, 50, 50, 50, 50);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String chosenHour = (String) listView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), chosenHour,
                        Toast.LENGTH_SHORT).show();
                JsonObject jsonBody = new JsonObject();
                jsonBody.addProperty("serviceId", mServiceAuto.getServiceId());
                jsonBody.addProperty("ownerId", mPreferencesManager.getUserId());
                jsonBody.addProperty("day", new SimpleDateFormat("yyyy-MM-dd").format(date));
                jsonBody.addProperty("hour", chosenHour);
                try {
                    Response<JsonObject> response = Ion.with(getApplicationContext())
                            .load("POST", "http://10.0.2.2:5000/addLockedPeriod")
                            .setHeader("Authorization", mPreferencesManager.getToken())
                            .setJsonObjectBody(jsonBody)
                            .asJsonObject()
                            .withResponse()
                            .get();
                    if (response.getHeaders().code() == 201) {
                        Log.i(TAG, "onItemClick: scheduled!");
                        Toast.makeText(getApplicationContext(), "Programare realizata cu succes!", Toast.LENGTH_SHORT).show();
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
                timePickerDialog.dismiss();
            }
        });
        timePickerDialog.show();
        timePickerDialog.getWindow().setLayout(600, 800);
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
        if (mPreferencesManager.getPermissionLocation()) {
            double distance = mServiceAuto.calculateDistance(mPreferencesManager.getUserLatitude(), mPreferencesManager.getUserLongitude());
            if (distance < 1) {
                mDistanceFromYou.setText(String.format("La aproximativ: %d m de tine", (int) (distance * 1000)));
            } else {
                mDistanceFromYou.setText(String.format("La aproximativ: %.2f km de tine", distance));
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 3) {
            Log.i(TAG, "onActivityResult: out from login intent");
            if (mPreferencesManager.getPermissionLocation()) {
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
