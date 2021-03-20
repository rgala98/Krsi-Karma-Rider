package com.krsikarma.app.Activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;

import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.krsikarma.app.Models.Service;
import com.krsikarma.app.R;
import com.krsikarma.app.Utility.DefaultTextConfig;
import com.krsikarma.app.Utility.Utils;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookAServiceActivity extends AppCompatActivity {

    public static final String TAG = "BookAServiceActivity";
    public static final int AUTOCOMPLETE_REQUEST_CODE = 10;
    private static final long twepoch = 1288834974657L;
    private static final long sequenceBits = 17;
    private static final long sequenceMax = 65536;
    private static volatile long lastTimestamp = -1L;
    private static volatile long sequence = 0L;

    RadioGroup date_radio_group;
    Spinner time_spinner;
    ImageView img_back;
    ImageView img_service;
    TextView tv_metric_rate;
    TextView tv_final_amount_1;
    TextView tv_final_amount_2;
    TextView tv_address;
    TextView tv_service_name;
    MaterialButton btn_book_now;
    ProgressBar progressBar;


    ArrayList<String> time_list;
    ArrayList<Service> serviceArrayList;
    ArrayList<String> storingTimeList;

    Double double_final_amount;
    Double double_quantity;
    String str_final_amount;
    String str_quantity;
    Service service;

    Timestamp date_timestamp;
    Boolean isDateSelected;

    PlacesClient placesClient;
    String places_api_key;

    String address;
    Double address_latitude;
    Double address_longitude;
    String user_name;

    Utils utils;
    private String postalCode;

    Boolean isOrderGoingOn = false;

    //firebase
    FirebaseFirestore db;
    ListenerRegistration listenerRegistration;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;


    String phone_language;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DefaultTextConfig.adjustFontScale(BookAServiceActivity.this);
        setContentView(R.layout.activity_book_a_service);

        init();

        tv_final_amount_1.setText("₹ " + str_final_amount);
        tv_final_amount_2.setText("₹ " + str_final_amount);

        if (serviceArrayList != null) {
            Glide.with(getApplicationContext())
                    .load(service.getService_image_url())
                    .centerCrop()
                    .into(img_service);

            if(phone_language.equals("hi")){
                utils.translateEnglishToHindi(service.getService_name(), tv_service_name);
            }else{
                tv_service_name.setText(service.getService_name());
            }


            String str_service_rate = service.getService_rate().toString().substring(0, service.getService_rate().toString().length() - 2);
            tv_metric_rate.setText(str_quantity + " " + getString(R.string.acres) + " x " + str_service_rate);


        }

        btn_book_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i(TAG, "is order going on" + isOrderGoingOn);
                if(!isOrderGoingOn) {
                    if (address == null || address.isEmpty()) {
                        utils.alertDialogOK(BookAServiceActivity.this, getString(R.string.error), getString(R.string.forgot_address));
                    } else if (getSelectedDate() == null) {
                        utils.alertDialogOK(BookAServiceActivity.this, getString(R.string.error), getString(R.string.forgot_date));
                    } else {
                        //Book the service now
                        addBookingDataToFirebase();
                    }

                }else{
                    utils.alertDialogOK(BookAServiceActivity.this, getString(R.string.error), getString(R.string.cannot_place_order));
                }

            }
        });


        date_radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                getSelectedDate();
                setTimeSpinner();
            }
        });


        img_back.setOnClickListener(view -> {
            onBackPressed();
        });

        tv_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "tv_address pressed");
                onSearchCalled();
            }
        });

        getUserName();
        checkIsOrderGoingOn();
        Log.i(TAG, "long id is " +generateLongId());

    }


    private void init() {

        date_radio_group = (RadioGroup) findViewById(R.id.date_radio_group);
        time_spinner = (Spinner) findViewById(R.id.time_spinner);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_service = (ImageView) findViewById(R.id.img_service);
        tv_address = (TextView) findViewById(R.id.tv_address);
        tv_service_name = (TextView) findViewById(R.id.tv_service_name);
        tv_metric_rate = (TextView) findViewById(R.id.tv_metric_rate);
        tv_final_amount_1 = (TextView) findViewById(R.id.tv_final_amount_1);
        tv_final_amount_2 = (TextView) findViewById(R.id.tv_final_amount_2);
        btn_book_now = (MaterialButton) findViewById(R.id.btn_book_now);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);


        time_list = new ArrayList<>();
        serviceArrayList = new ArrayList<>();
        storingTimeList = new ArrayList<>();

        utils = new Utils();

        str_quantity = getIntent().getStringExtra("quantity");
        if (str_quantity != null) {
            double_quantity = Double.parseDouble(str_quantity);
        }

        double_final_amount = getIntent().getDoubleExtra("final_amount", 0);
        str_final_amount = double_final_amount.toString().substring(0, double_final_amount.toString().length() - 2);

        serviceArrayList = getIntent().getParcelableArrayListExtra("serviceArrayList");
        if (serviceArrayList != null) {
            service = serviceArrayList.get(0);
        }

        //places
        places_api_key = getString(R.string.PLACES_API_KEY);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), places_api_key);
        }
        placesClient = Places.createClient(this);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        phone_language = Locale.getDefault().getLanguage();

        setDateRadioGroup();



    }


    private void setDateRadioGroup() {
        for (int i = 0; i < date_radio_group.getChildCount(); i++) {
            ((RadioButton) date_radio_group.getChildAt(i)).setText(getDate(i));
        }


    }

    private void setTimeSpinner() {

        setTimeList();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, time_list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        time_spinner.setAdapter(adapter);


    }

    private void setTimeList() {

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
        SimpleDateFormat timeInHoursFormat = new SimpleDateFormat("HH");
        SimpleDateFormat timeInMinsFormat = new SimpleDateFormat("mm");

        Calendar calendar = Calendar.getInstance();

        if (isDateSelected && getSelectedDate() != null) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date today_date = new Date();
            String str_today_date = dateFormat.format(today_date);
            try {
                Date new_format_Date = dateFormat.parse(str_today_date);
                Timestamp ts_today_date = new java.sql.Timestamp(new_format_Date.getTime());


                int value = getSelectedDate().compareTo(ts_today_date);
                if (value == 0) {
                    //Date selected is Today

                    //Now get the current time
                    int currentTimeInHours = Integer.parseInt(timeInHoursFormat.format(calendar.getTime()));
                    int currentTimeInMins = Integer.parseInt(timeInMinsFormat.format(calendar.getTime()));

                    if (currentTimeInHours >= 5) {

                        time_list.clear();
                        calendar.setTime(calendar.getTime());

                        if (currentTimeInMins < 20) {
                            //Person booking between 5 am to 8 pm will see all intervals on spinner one hour after current time.
                            //if he is 5:00, he will see from 6 am to 8 pm
                            // if he is booking at 7 pm, he will see only 8 pm available.

                            for (int i = currentTimeInHours; i < 20; i++) {
                                calendar.add(Calendar.HOUR, 1);
                                calendar.set(Calendar.MINUTE, 0);

                                time_list.add(timeFormat.format(toNearestWholeHour(calendar.getTime())));
                            }
                            Log.i(TAG, "time list is " + time_list);


                        } else {
                            //Since 20 mins of the hour are gone, he will see bookings 2 hrs + of current time and then all at one hour intervals
                            //He is booking at 5:30 am, he will see from (5+2=7) 7 am to 5 pm at 1 hr intervals

                            for (int i = currentTimeInHours; i < 19; i++) {
                                if (i == currentTimeInHours) {
                                    calendar.add(Calendar.HOUR, 2);

                                } else {
                                    calendar.add(Calendar.HOUR, 1);

                                }
                                calendar.set(Calendar.MINUTE, 0);
                                time_list.add(timeFormat.format(toNearestWholeHour(calendar.getTime())));

                            }
                            Log.i(TAG, "time list in past 20 min block is " + time_list);


                        }


                    } else if (currentTimeInHours > 19) {

                        if (currentTimeInMins > 20) {
                            //Person is booking after 7:20 pm of today's date.
                            //No time slot available for today
                            // will show a text you to book another date.
                        } else {
                            //show 8 pm available

                            time_list.clear();
                            calendar.set(Calendar.HOUR_OF_DAY, 20);
                            calendar.set(Calendar.MINUTE, 0);
                            time_list.add(timeFormat.format(toNearestWholeHour(calendar.getTime())));
                        }


                    }
                } else if (value > 0) {
                    //Date selected is after today
                    //Show all time slots from 6 am to 5 pm

                    time_list.clear();

                    calendar.set(Calendar.HOUR_OF_DAY, 5);
                    calendar.set(Calendar.MINUTE, 0);

                    for (int i = 5; i < 20; i++) {
                        calendar.add(Calendar.HOUR, 1);
                        calendar.set(Calendar.MINUTE, 0);

                        Log.i(TAG, "in another day " + calendar.getTime());

                        time_list.add(timeFormat.format(toNearestWholeHour(calendar.getTime())));
                    }
                    Log.i(TAG, "time list for another day is " + time_list);


                } else {
                    //Date selected is less than today (this case never will happen for us)
                }


            } catch (ParseException e) {
                e.printStackTrace();
            }


        }

    }


    private String getDate(int increment) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE\ndd\nMMM");
        SimpleDateFormat timeInHours = new SimpleDateFormat("HH");
        Calendar calendar = Calendar.getInstance();

        // After 8 PM Change the date to next day
        if (Integer.parseInt(timeInHours.format(calendar.getTime())) >= 19) {
            calendar.add(Calendar.DATE, increment + 1);
        } else {
            calendar.add(Calendar.DATE, increment);
        }

        //For Storage
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        storingTimeList.add(dateFormat1.format(calendar.getTime()));


        return dateFormat.format(calendar.getTime());
    }


    private Timestamp getSelectedDate() {
        int radioButtonID = date_radio_group.getCheckedRadioButtonId();
        View radioButton = date_radio_group.findViewById(radioButtonID);
        int idx = date_radio_group.indexOfChild(radioButton);

        if (radioButtonID != -1) {
            isDateSelected = true;
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date parsedDate = dateFormat.parse(storingTimeList.get(idx));
                date_timestamp = new java.sql.Timestamp(parsedDate.getTime());
                return date_timestamp;

            } catch (Exception e) {
                Log.i(TAG, "Error in timestamp date: " + e.getLocalizedMessage());
            }
        } else {
            isDateSelected = false;
        }

        return null;
    }

    static Date toNearestWholeHour(Date d) {
        Calendar c = new GregorianCalendar();
        c.setTime(d);

        if (c.get(Calendar.MINUTE) >= 30)
            c.add(Calendar.HOUR, 1);

        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        return c.getTime();
    }

    public void onSearchCalled() {
        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields).setCountry("IN")
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);

                address = place.getAddress();
                address_latitude = place.getLatLng().latitude;
                address_longitude = place.getLatLng().longitude;
                tv_address.setText(address);

                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(address_latitude, address_longitude, 1);
                    postalCode = addresses.get(0).getPostalCode();
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String knownName = addresses.get(0).getFeatureName();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void addBookingDataToFirebase() {
        progressBar.setVisibility(View.VISIBLE);
        btn_book_now.setEnabled(false);
        Calendar calendar = Calendar.getInstance();

        String selected_time = time_spinner.getSelectedItem().toString();
        SimpleDateFormat only_date_format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat order_date_time_format = new SimpleDateFormat("yyyy-MM-dd hh:mm aa");
        String str_order_Date_time = only_date_format.format(getSelectedDate()) + " " + selected_time;

        Date date_order_date_time = null;
        try {
            date_order_date_time = order_date_time_format.parse(str_order_Date_time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Long order_id = generateLongId();
        Map<String, Object> data = new HashMap<>();
        data.put(getString(R.string.user_id), FirebaseAuth.getInstance().getCurrentUser().getUid());
        data.put(getString(R.string.service_id), service.getService_id());
        data.put(getString(R.string.service_name), service.getService_name());
        data.put(getString(R.string.user_name), user_name);
        data.put(getString(R.string.order_date_time), date_order_date_time);
        data.put(getString(R.string.order_status), getString(R.string.order_status_type_requested));
        data.put(getString(R.string.order_address), address);
        data.put(getString(R.string.order_quantity), double_quantity);
        data.put(getString(R.string.order_rate), service.getService_rate());
        data.put(getString(R.string.order_metric), service.getService_metric());
        data.put(getString(R.string.date_created), calendar.getTime());
        data.put(getString(R.string.order_latitude), address_latitude);
        data.put(getString(R.string.order_longitude), address_longitude);
        data.put(getString(R.string.image_url), service.getService_image_url());
        data.put(getString(R.string.order_id), order_id);

        db.collection(getString(R.string.orders)).document()
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.GONE);
                        Log.i(TAG, "Order Placed");
                        setResult(69);
                        finish();
                        Intent intent = new Intent(getApplicationContext(), OrderDetailsActivity.class);
                        intent.putExtra("order_id", order_id);
                        startActivity(intent);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        btn_book_now.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                        Log.i(TAG, "An error occurred in placing order " + e.getLocalizedMessage());
                    }
                });


    }

    private void getUserName(){
        listenerRegistration = db.collection(getString(R.string.users)).document(firebaseUser.getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                        if(error!=null){
                            Log.e(TAG,"error",error);
                            return;
                        }

                        if(snapshot!=null && snapshot.exists()){
                            if(snapshot.getString(getString(R.string.first_name))!=null && snapshot.getString(getString(R.string.last_name))!=null){
                                user_name = snapshot.getString(getString(R.string.first_name)) + " " + snapshot.getString(getString(R.string.last_name));

                            }
                        }
                    }
                });
    }

    private void checkIsOrderGoingOn(){
        listenerRegistration = db.collection(getString(R.string.orders))
                .orderBy(getString(R.string.date_created), Query.Direction.DESCENDING)
                .whereEqualTo(getString(R.string.user_id), firebaseUser.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if(value!=null) {

                            for (QueryDocumentSnapshot doc : value) {

                                String order_status;
                                if (doc.get(getString(R.string.order_status)) != null) {
                                    order_status = doc.getString(getString(R.string.order_status));
                                    Log.i(TAG, "order status is " + order_status);
                                    if (order_status.equals(getString(R.string.order_status_type_requested)) || order_status.equals(getString(R.string.order_status_type_ongoing)) || order_status.equals(getString(R.string.order_status_type_payment_pending)) || order_status.equals(getString(R.string.order_status_type_driver_assigned))) {
                                        isOrderGoingOn = true;
                                        return;
                                    }

                                }

                            }
                        }
                    }
                });
    }

    private Long generateLongId() {
        long timestamp = System.currentTimeMillis();
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) % sequenceMax;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = timestamp;
        Long id = ((timestamp - twepoch) << sequenceBits) | sequence;
        return id;
    }

    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(listenerRegistration!=null){
            listenerRegistration=null;
        }

        utils.closeTranslator();
    }
}
