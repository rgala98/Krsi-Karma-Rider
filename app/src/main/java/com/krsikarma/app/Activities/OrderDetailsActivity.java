package com.krsikarma.app.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.krsikarma.app.Models.Order;
import com.krsikarma.app.R;
import com.krsikarma.app.Utility.DefaultTextConfig;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDetailsActivity extends AppCompatActivity {

    public static final String TAG = "OrderDetailsActivity";
    public static final int PAYMENT_REQUEST_CODE = 200;
    LottieAnimationView animationView;

    ImageView img_back;
    TextView tv_service_name;
    TextView tv_metric_rate;
    TextView tv_final_amount;
    TextView tv_order_number;
    TextView tv_date;
    TextView tv_address;
    TextView tv_partner_name;
    TextView tv_partner_phone;
    TextView tv_partner_details_text;
    TextView tv_order_status_text;
    TextView tv_payment_text;
    TextView tv_payment;
    TextView tv_otp;
    ImageView img_user;
    Button btn_cancel;
    CardView card_user;

    FirebaseFirestore db;
    ListenerRegistration listenerRegistration;

    Long order_id;
    String partner_id;
    String phone_number;
    String document_id;
    String otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(OrderDetailsActivity.this);
        setContentView(R.layout.activity_order_details);

        init();

        img_back.setOnClickListener(view -> onBackPressed());



        getData();

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(btn_cancel.getText().equals(getString(R.string.cancel))) {
                    Map<String, Object> data = new HashMap<>();
                    data.put(getString(R.string.order_status), getString(R.string.order_status_type_cancelled));

                    AlertDialog.Builder builder;
                    AlertDialog alert;

                    builder = new AlertDialog.Builder(OrderDetailsActivity.this);
                    builder.setMessage(getString(R.string.are_you_sure_cancel))
                            .setCancelable(true)
                            .setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    db.collection(getString(R.string.orders)).document(document_id)
                                            .update(data)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i(TAG, "User has cancelled this order");

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Log.i(TAG, "An error occurred " + e.getLocalizedMessage());

                                                }
                                            });
                                }
                            });
                    alert = builder.create();
                    alert.setTitle("");
                    alert.show();

                }else if(btn_cancel.getText().equals(getString(R.string.proceed_to_pay))){
                    Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                    intent.putExtra("order_id", order_id);
                    intent.putExtra("document_id", document_id);
                    startActivityForResult(intent, PAYMENT_REQUEST_CODE);

                }

            }
        });



    }

    private void init() {

        img_back = (ImageView) findViewById(R.id.img_back);
        animationView = (LottieAnimationView) findViewById(R.id.animation_view);
        tv_service_name = (TextView) findViewById(R.id.tv_service_name);
        tv_metric_rate = (TextView) findViewById(R.id.tv_metric_rate);
        tv_final_amount = (TextView) findViewById(R.id.tv_final_amount_1);
        tv_order_number = (TextView) findViewById(R.id.tv_order_number);
        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_address = (TextView) findViewById(R.id.tv_address);
        tv_partner_name = (TextView) findViewById(R.id.tv_partner_name);
        tv_partner_phone = (TextView) findViewById(R.id.tv_partner_phone);
        tv_partner_details_text = (TextView) findViewById(R.id.tv_partner_details_text);
        tv_order_status_text = (TextView) findViewById(R.id.tv_order_status_text);
        tv_payment_text = (TextView) findViewById(R.id.tv_payment_text);
        tv_payment = (TextView) findViewById(R.id.tv_payment);
        tv_otp = (TextView) findViewById(R.id.tv_otp);

        card_user = (CardView) findViewById(R.id.card_user);
        img_user = (ImageView) findViewById(R.id.img_user);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);

        db = FirebaseFirestore.getInstance();

        order_id = getIntent().getLongExtra("order_id", 0);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == PAYMENT_REQUEST_CODE){
            // PAYMENT SUCCESSFUL SHOW PAYMENT STATUS NOW
            btn_cancel.setVisibility(View.GONE);
        }

    }

    private void getData() {
        if (order_id != 0) {
            listenerRegistration = db.collection(getString(R.string.orders))
                    .whereEqualTo(getString(R.string.order_id), order_id)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                            if (error != null) {
                                Log.e(TAG, "Error is ", error);
                                return;
                            }

                            String service_id = "";
                            String service_name = "";
                            String service_image_url = "";
                            String order_id = "";
                            String user_id = "";
                            String order_date = "";
                            String order_time = "";
                            String order_status = "";
                            String order_address = "";
                            String order_quantity = "";
                            String order_rate = "";
                            String order_metric = "";
                            String order_amount = "";
                            String date_created = "";


                            for (QueryDocumentSnapshot doc : value) {
                                document_id = doc.getId();
                                if (doc.get(getString(R.string.order_id)) != null) {
                                    order_id = String.valueOf(doc.getLong(getString(R.string.order_id)));
                                    tv_order_number.setText(order_id);
                                    otp =  order_id.substring(order_id.length() - 4);
                                    tv_otp.setText("OTP " + otp);

                                }

                                if (doc.get(getString(R.string.service_id)) != null) {
                                    service_id = doc.getString(getString(R.string.service_id));
                                }

                                if (doc.get(getString(R.string.service_name)) != null) {
                                    service_name = doc.getString(getString(R.string.service_name));

                                }

                                if (doc.get(getString(R.string.image_url)) != null) {
                                    service_image_url = doc.getString(getString(R.string.image_url));

                                }

                                if (doc.get(getString(R.string.user_id)) != null) {
                                    user_id = doc.getString(getString(R.string.user_id));
                                }

                                if (doc.get(getString(R.string.order_status)) != null) {
                                    order_status = doc.getString(getString(R.string.order_status));
                                }

                                if (doc.get(getString(R.string.order_metric)) != null) {
                                    order_metric = doc.getString(getString(R.string.order_metric));
                                }


                                Double double_order_rate = null, double_order_quantity = null;
                                if (doc.get(getString(R.string.order_rate)) != null) {
                                    double_order_rate = doc.getDouble(getString(R.string.order_rate));
                                    order_rate = String.valueOf(double_order_rate);
                                    order_rate = order_rate.substring(0, order_rate.length() - 2);

                                }

                                if (doc.get(getString(R.string.order_quantity)) != null) {
                                    double_order_quantity = doc.getDouble(getString(R.string.order_quantity));
                                    order_quantity = String.valueOf(double_order_quantity);
                                    order_quantity = order_quantity.substring(0, order_quantity.length() - 2);

                                }

                                if (double_order_rate != null && double_order_quantity != null) {
                                    order_amount = String.valueOf(double_order_rate * double_order_quantity);
                                    order_amount = order_amount.substring(0, order_amount.length() - 2);
                                }

                                if (doc.get(getString(R.string.order_address)) != null) {
                                    order_address = doc.getString(getString(R.string.order_address));
                                }

                                if (doc.get(getString(R.string.partner_id)) != null) {
                                    partner_id = doc.getString(getString(R.string.partner_id));
                                    getDriverData();
                                }

                                if (doc.get(getString(R.string.date_created)) != null) {
                                    Timestamp ts_date = doc.getTimestamp(getString(R.string.date_created));
                                    SimpleDateFormat sfd_viewFormat = new SimpleDateFormat("d MMMM, yyyy");
                                    date_created = sfd_viewFormat.format(ts_date.toDate());

                                }

                                if (doc.get(getString(R.string.order_date_time)) != null) {
                                    Timestamp ts_date = doc.getTimestamp(getString(R.string.order_date_time));
                                    SimpleDateFormat sfd_viewFormat = new SimpleDateFormat("d MMMM, yyyy");
                                    order_date = sfd_viewFormat.format(ts_date.toDate());


                                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
                                    order_time = timeFormat.format(ts_date.toDate());


                                }
                                tv_service_name.setText(service_name);
                                tv_metric_rate.setText(order_quantity + " " + order_metric + " x ₹ " + order_rate);
                                tv_final_amount.setText("₹ " + order_amount);
                                tv_date.setText(order_date + " at " + order_time);
                                tv_address.setText(order_address);

                                if (order_status.equals(getString(R.string.order_status_type_requested))) {
                                    tv_order_status_text.setText(getString(R.string.order_requested_status));
                                    tv_order_status_text.setTextColor(getColor(R.color.orange));
                                    animationView.setAnimation(R.raw.requested_animation);
                                    animationView.playAnimation();
                                    tv_partner_name.setVisibility(View.GONE);
                                    tv_partner_phone.setVisibility(View.GONE);
                                    card_user.setVisibility(View.GONE);
                                    tv_partner_details_text.setVisibility(View.GONE);
                                    tv_payment_text.setVisibility(View.GONE);
                                    tv_payment.setVisibility(View.GONE);
                                    btn_cancel.setText(getString(R.string.cancel));

                                }

                                else if (order_status.equals(getString(R.string.order_status_type_cancelled))) {
                                    tv_order_status_text.setText(getString(R.string.order_cancelled_status));
                                    tv_order_status_text.setTextColor(getColor(R.color.brand_color));
                                    animationView.setAnimation(R.raw.cancel_animation);
                                    animationView.playAnimation();
                                    tv_partner_name.setVisibility(View.GONE);
                                    tv_partner_phone.setVisibility(View.GONE);
                                    card_user.setVisibility(View.GONE);
                                    tv_partner_details_text.setVisibility(View.GONE);
                                    btn_cancel.setVisibility(View.GONE);
                                    tv_otp.setVisibility(View.GONE);
                                }

                                else if (order_status.equals(getString(R.string.order_status_type_completed))) {
                                    tv_order_status_text.setText(getString(R.string.order_completed_status));
                                    tv_order_status_text.setTextColor(getColor(R.color.green));
                                    animationView.setAnimation(R.raw.check_animation);
                                    animationView.playAnimation();
                                    btn_cancel.setVisibility(View.GONE);
                                    tv_payment_text.setVisibility(View.VISIBLE);
                                    tv_payment.setVisibility(View.VISIBLE);
                                    tv_otp.setVisibility(View.GONE);

                                    if(doc.get(getString(R.string.payment_mode))!=null){
                                        tv_payment.setText(getString(R.string.paid_via) + " " + doc.getString(getString(R.string.payment_mode)));
                                    }


                                }

                                else if (order_status.equals(getString(R.string.order_status_type_ongoing))) {
                                    tv_order_status_text.setText(getString(R.string.order_ongoing_status));
                                    tv_order_status_text.setTextColor(getColor(R.color.yellow));
                                    animationView.setAnimation(R.raw.on_going_animation);
                                    animationView.playAnimation();
                                    tv_payment_text.setVisibility(View.GONE);
                                    tv_payment.setVisibility(View.GONE);
                                    tv_otp.setVisibility(View.GONE);
                                    btn_cancel.setVisibility(View.GONE);


                                }

                                else if (order_status.equals(getString(R.string.order_status_type_payment_pending))) {
                                    tv_order_status_text.setText(getString(R.string.order_pending_payment_status));
                                    tv_order_status_text.setTextColor(getColor(R.color.blue));
                                    tv_payment_text.setVisibility(View.VISIBLE);
                                    tv_payment.setVisibility(View.VISIBLE);
                                    tv_payment.setText(getString(R.string.payment_pending));
                                    animationView.setAnimation(R.raw.payment_pending_animation);
                                    animationView.playAnimation();
                                    btn_cancel.setText(getString(R.string.proceed_to_pay));
                                    btn_cancel.setVisibility(View.VISIBLE);
                                    tv_otp.setVisibility(View.GONE);

                                }

                                else if (order_status.equals(getString(R.string.order_status_type_driver_assigned))) {
                                    tv_order_status_text.setText(getString(R.string.order_driving_assigned_status));
                                    tv_order_status_text.setTextColor(getColor(R.color.yellow));
                                    tv_payment_text.setVisibility(View.GONE);
                                    tv_payment.setVisibility(View.GONE);
                                    animationView.setAnimation(R.raw.on_going_animation);
                                    animationView.playAnimation();
                                    btn_cancel.setText(getString(R.string.cancel));
                                    tv_otp.setVisibility(View.VISIBLE);

                                }



                            }
                        }
                    });

        }
    }



    private void getDriverData(){

        if(partner_id!=null){
            listenerRegistration = db.collection(getString(R.string.partners)).document(partner_id)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                            if(error!=null){
                                Log.e(TAG, "Error is " , error);
                                return;
                            }

                            String first_name = "";
                            String last_name = "";
                            String full_name;

                            if (snapshot.get(getString(R.string.first_name)) != null) {
                                first_name = snapshot.getString(getString(R.string.first_name));
                            }

                            if (snapshot.get(getString(R.string.last_name)) != null) {
                                last_name = snapshot.getString(getString(R.string.last_name));
                            }

                            full_name = first_name + " " + last_name;
                            tv_partner_name.setText(full_name);

                            if (snapshot.get(getString(R.string.phone_number)) != null) {
                                phone_number = snapshot.getString(getString(R.string.phone_number));
                            }

                            tv_partner_phone.setText("Call (+91 " + phone_number.substring(3) + ")");

                            if (snapshot.get(getString(R.string.profile_img_url)) != null) {

                                String profile_image = snapshot.getString(getString(R.string.profile_img_url));
                                Glide.with(getApplicationContext())
                                        .load(profile_image)
                                        .into(img_user);
                            }
                        }
                    });

        }

        tv_partner_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               createPermissionListeners();
            }
        });
    }

    private void createPermissionListeners() {

        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CALL_PHONE
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {

                if (report.areAllPermissionsGranted()) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone_number));
                    startActivity(intent);
                }

                // check for permanent denial of any permission
                if (report.isAnyPermissionPermanentlyDenied()) {
                    Log.i(TAG,"Permissions permanently denied. Open Settings");


                    AlertDialog.Builder builder;
                    AlertDialog alert;

                    builder = new AlertDialog.Builder(OrderDetailsActivity.this);
                    builder.setMessage(getString(R.string.open_settings_permission))
                            .setCancelable(true)
                            .setPositiveButton(getString(R.string.open_settings), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    startActivity(intent);

                                }
                            });
                    alert = builder.create();
                    alert.setTitle(getString(R.string.error));
                    alert.show();
                }

            }
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }

        }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {
                Toast.makeText(getApplicationContext(), "Error occurred! " + error.toString(), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Error Occurred" + error.toString());
            }
        }).check();
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
    }
}