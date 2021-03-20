package com.krsikarma.app.Activities;

import android.content.Intent;
import android.content.IntentSender;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.krsikarma.app.Adapters.ServicesGridAdapter;
import com.krsikarma.app.Models.Service;
import com.krsikarma.app.R;
import com.krsikarma.app.Utility.DefaultTextConfig;
import com.krsikarma.app.Utility.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private static final int RC_APP_UPDATE = 11;

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore db;
    ListenerRegistration listenerRegistration;


    ImageView img_user;
    GridView service_grid_view;
    ConstraintLayout main_constraint;
    ConstraintLayout pending_order_view;
    EditText et_search;
    TextView tv_service;
    TextView tv_final_amount;
    TextView tv_order_status;

    String phone_language;

    Utils utils;
    ArrayList<Service> serviceArrayList;
    ServicesGridAdapter servicesGridAdapter;
    String profile_img_url;
    String first_name;
    Long current_order_id;
    TextView tv_user_greetings;
    InstallStateUpdatedListener installStateUpdatedListener;
    private AppUpdateManager mAppUpdateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_KrsiKarma);

        DefaultTextConfig.adjustFontScale(MainActivity.this);
        setContentView(R.layout.activity_main);

        init();

        checkForLogOut();
        setNotificationToken();
        checkUserDetails();
        getServiceData();
        getCurrentOrder();


        img_user.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            intent.putExtra("profile_photo_url", profile_img_url);
            startActivity(intent);

        });

        pending_order_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), OrderDetailsActivity.class);
                intent.putExtra("order_id", current_order_id);
                startActivity(intent);
            }
        });

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString().toLowerCase());
            }
        });

    }


    private void init() {
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();


        db = FirebaseFirestore.getInstance();

        img_user = (ImageView) findViewById(R.id.img_user);
        service_grid_view = (GridView) findViewById(R.id.service_grid_view);
        main_constraint = (ConstraintLayout) findViewById(R.id.main_constraint);
        pending_order_view = (ConstraintLayout) findViewById(R.id.pending_order_view);
        et_search = (EditText) findViewById(R.id.et_search);
        tv_user_greetings = (TextView) findViewById(R.id.tv_user_greetings);
        tv_final_amount = (TextView) findViewById(R.id.tv_final_amount);
        tv_order_status = (TextView) findViewById(R.id.tv_order_status);
        tv_service = (TextView) findViewById(R.id.tv_service);

        phone_language = Locale.getDefault().getLanguage();

        utils = new Utils();
        serviceArrayList = new ArrayList<>();

    }

    private void filter(String text) {
        ArrayList<Service> temp = new ArrayList();

        for (Service d : serviceArrayList) {

            if (d.getService_name().toLowerCase().contains(text.toLowerCase())) {
                temp.add(d);

            }
        }
        if(temp!=null) {
            servicesGridAdapter.updateList(temp);
        }

    }

    private void popupSnackbarForCompleteUpdate() {

        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.main_constraint),
                        getString(R.string.update_app),
                        Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction(getString(R.string.install_now), view -> {
            if (mAppUpdateManager != null) {
                mAppUpdateManager.completeUpdate();
            }
        });


        snackbar.setActionTextColor(getResources().getColor(R.color.white));
        snackbar.show();
    }

    private void getServiceData() {
        listenerRegistration = db.collection(getString(R.string.services))
                .orderBy(getString(R.string.name))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        String service_id;
                        String service_name = "";
                        Double service_rate = 0.0;
                        String service_metric = "";
                        String service_image_url = "";
                        String service_description = "";
                        String service_date_created = "";

                        serviceArrayList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            service_id = doc.getId();
                            if (doc.get(getString(R.string.name)) != null) {
                                service_name = doc.getString(getString(R.string.name));
                            }

                            if (doc.get(getString(R.string.metric)) != null) {
                                service_metric = doc.getString(getString(R.string.metric));
                            }

                            if (doc.get(getString(R.string.image_url)) != null) {
                                service_image_url = doc.getString(getString(R.string.image_url));
                            }

                            if (doc.get(getString(R.string.service_description)) != null) {
                                service_description = doc.getString(getString(R.string.service_description));
                            }

                            if (doc.get(getString(R.string.rate)) != null) {
                                service_rate = doc.getDouble(getString(R.string.rate));
                            }

                            if (doc.get(getString(R.string.date_created)) != null) {
                                Timestamp date_created_ts = doc.getTimestamp(getString(R.string.date_created));
                                SimpleDateFormat sfd_viewFormat = new SimpleDateFormat("d MMMM yyyy");
                                service_date_created = sfd_viewFormat.format(date_created_ts.toDate());
                            }

                            serviceArrayList.add(new Service(
                                    service_id,
                                    service_name,
                                    service_rate,
                                    service_metric,
                                    service_image_url,
                                    service_description,
                                    service_date_created

                            ));

                        }

                        servicesGridAdapter = new ServicesGridAdapter(serviceArrayList, MainActivity.this);
                        service_grid_view.setAdapter(servicesGridAdapter);


                    }
                });
    }

    private void checkUserDetails() {

        if (firebaseUser != null) {
            DocumentReference doc_ref = FirebaseFirestore.getInstance().collection(getString(R.string.users)).document(firebaseUser.getUid());
            listenerRegistration = doc_ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        if (!utils.isStringNull(snapshot.getString(getString(R.string.profile_img_url)))) {
                            profile_img_url = snapshot.getString(getString(R.string.profile_img_url));


                            Glide.with(getApplicationContext())
                                    .load(profile_img_url)
                                    .placeholder(R.drawable.ic_user)
                                    .centerCrop()
                                    .into(img_user);
                        }
                        if (!utils.isStringNull(snapshot.getString(getString(R.string.first_name)))) {
                            first_name = snapshot.getString(getString(R.string.first_name));
                            tv_user_greetings.setText(getString(R.string.greetings) + " " + first_name);
                        }


                    } else {


                        Log.d(TAG, "Current data: null");

                    }
                }
            });
        }


    }

    private void checkForLogOut() {

        if (firebaseUser != null) {
            DocumentReference doc_ref = FirebaseFirestore.getInstance().collection(getString(R.string.users)).document(firebaseUser.getUid());

            doc_ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        finishAffinity();
                        Intent intent = new Intent(getApplicationContext(), GetStartedActivity.class);
                        startActivity(intent);

                    }
                }
            });
        }


    }

    private void getCurrentOrder() {

        if (firebaseUser != null) {
            listenerRegistration = db.collection(getString(R.string.orders))
                    .whereEqualTo(getString(R.string.user_id), firebaseUser.getUid())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen failed.", e);
                                return;
                            }


                            String service_name = "";
                            String order_status = "";
                            String order_quantity = "";
                            String order_rate = "";
                            Double double_order_quantity = 0.0;
                            Double double_order_rate = 0.0;


                            Boolean isOrderExisting = false;

                            for (QueryDocumentSnapshot doc : value) {


                                if (doc.get(getString(R.string.order_status)) != null) {
                                    order_status = doc.getString(getString(R.string.order_status));


                                    if (order_status.equals(getString(R.string.order_status_type_ongoing)) ||
                                            order_status.equals(getString(R.string.order_status_type_payment_pending)) ||
                                            order_status.equals(getString(R.string.order_status_type_requested)) ||
                                            order_status.equals(getString(R.string.order_status_type_driver_assigned))  ) {

                                        isOrderExisting = true;
                                        pending_order_view.setVisibility(View.VISIBLE);
                                        if (doc.get(getString(R.string.service_name)) != null) {
                                            service_name = doc.getString(getString(R.string.service_name));
                                            tv_service.setText(service_name);

                                        }


                                        if (doc.get(getString(R.string.order_quantity)) != null) {
                                            double_order_quantity = doc.getDouble(getString(R.string.order_quantity));
                                            order_quantity = String.valueOf(double_order_quantity);
                                            order_quantity = order_quantity.substring(0, order_quantity.length() - 2);

                                        }

                                        if (doc.get(getString(R.string.order_rate)) != null) {
                                            double_order_rate = doc.getDouble(getString(R.string.order_rate));
                                            order_rate = String.valueOf(double_order_rate);
                                            order_rate = order_rate.substring(0, order_rate.length() - 2);

                                        }


                                        if (doc.get(getString(R.string.order_id)) != null) {
                                            current_order_id = doc.getLong(getString(R.string.order_id));
                                        }


                                        tv_final_amount.setText(order_quantity + " " + getString(R.string.acres) + " x ₹ " + order_rate);


                                        tv_order_status.setText(order_status);

                                        if (order_status.equals(getString(R.string.order_status_type_requested))) {
                                            tv_order_status.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.light_orange)));
                                            tv_order_status.setTextColor(getColor(R.color.orange));

                                        } else if (order_status.equals(getString(R.string.order_status_type_completed))) {
                                            tv_order_status.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.light_green)));
                                            tv_order_status.setTextColor(getColor(R.color.green));

                                        } else if (order_status.equals(getString(R.string.order_status_type_ongoing))) {
                                            tv_order_status.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.light_yellow)));
                                            tv_order_status.setTextColor(getColor(R.color.yellow));

                                        } else if (order_status.equals(getString(R.string.order_status_type_cancelled))) {
                                            tv_order_status.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.brand_color_light)));
                                            tv_order_status.setTextColor(getColor(R.color.brand_color));

                                        } else if (order_status.equals(getString(R.string.payment_pending))) {
                                            tv_order_status.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.light_blue)));
                                            tv_order_status.setTextColor(getColor(R.color.blue));
                                        }
                                    } else if (order_status.equals(getString(R.string.order_status_type_driver_assigned))) {
                                        tv_order_status.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.light_yellow)));
                                        tv_order_status.setTextColor(getColor(R.color.yellow));


                                    }
                                }
                            }

                            if (!isOrderExisting) {
                                pending_order_view.setVisibility(View.GONE);
                            }

                        }
                    });
        }


    }

    private void setNotificationToken(){

        if(firebaseUser!=null) {

            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                return;
                            }

                            // Get new FCM registration token
                            String token = task.getResult();

                            final DocumentReference doc_id = db.collection(getString(R.string.users)).document(firebaseUser.getUid());


                            doc_id.update("token", FieldValue.arrayUnion(token)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.i(TAG, "Token updated");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i(TAG, "An error occurred : " + e.getMessage());
                                }
                            });


                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (firebaseUser == null) {
            finishAffinity();
            Intent intent = new Intent(getApplicationContext(), GetStartedActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration = null;
        }

        utils.closeTranslator();
    }

    @Override
    protected void onStart() {
        super.onStart();

        installStateUpdatedListener = new InstallStateUpdatedListener() {
            @Override
            public void onStateUpdate(InstallState state) {
                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                    //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                    popupSnackbarForCompleteUpdate();
                } else if (state.installStatus() == InstallStatus.INSTALLED) {
                    if (mAppUpdateManager != null) {
                        mAppUpdateManager.unregisterListener(installStateUpdatedListener);
                    }

                } else {
                    Log.i(TAG, "InstallStateUpdatedListener: state: " + state.installStatus());
                }
            }
        };

        mAppUpdateManager = AppUpdateManagerFactory.create(this);

        mAppUpdateManager.registerListener(installStateUpdatedListener);

        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE /*AppUpdateType.IMMEDIATE*/)) {

                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo, AppUpdateType.FLEXIBLE /*AppUpdateType.IMMEDIATE*/, MainActivity.this, RC_APP_UPDATE);

                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }

            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                popupSnackbarForCompleteUpdate();
            } else {
                Log.e(TAG, "checkForAppUpdateAvailability: something else");
            }
        });
    }

}