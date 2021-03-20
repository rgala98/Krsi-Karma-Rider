package com.krsikarma.app.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.krsikarma.app.R;
import com.krsikarma.app.Utility.Utils;
import com.ncorti.slidetoact.SlideToActView;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class PaymentActivity extends AppCompatActivity implements PaymentResultListener {

    public static final String TAG = "PaymentActivity";
    Utils utils;
    Checkout checkout;

    SlideToActView btn_pay;
    TextView tv_final_amount;
    ImageView img_back;

    String document_id;

    ListenerRegistration listenerRegistration;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;

    String order_amount;
    String order_id;
    String partner_id;
    String service_id;
    String razorpay_order_id;
    String razorpay_payment_id;

    RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        init();

        getData();

        btn_pay.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(SlideToActView slideToActView) {
                order_amount = order_amount + "00";
                getRequest(order_amount);
            }
        });

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private void init() {
        btn_pay = (SlideToActView) findViewById(R.id.btn_pay);
        tv_final_amount = (TextView) findViewById(R.id.tv_final_amount);
        img_back = (ImageView) findViewById(R.id.img_back);

        utils = new Utils();

        document_id = getIntent().getStringExtra("document_id");


        checkout.preload(getApplicationContext());

        // Instantiate the RequestQueue.
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();


    }

    private void getData() {

        if (document_id != null) {

            listenerRegistration = db.collection(getString(R.string.orders)).document(document_id)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Log.e(TAG, "Error", error);
                                return;
                            }

                            if (doc != null && doc.exists()) {

                                Double double_order_rate = null, double_order_quantity = null;

                                if (doc.get(getString(R.string.order_rate)) != null) {
                                    double_order_rate = doc.getDouble(getString(R.string.order_rate));
                                }

                                if (doc.get(getString(R.string.service_id)) != null) {
                                    service_id = doc.getString(getString(R.string.service_id));
                                }


                                if (doc.get(getString(R.string.order_quantity)) != null) {
                                    double_order_quantity = doc.getDouble(getString(R.string.order_quantity));

                                }

                                if (double_order_rate != null && double_order_quantity != null) {
                                    order_amount = String.valueOf(double_order_rate * double_order_quantity);
                                    order_amount = order_amount.substring(0, order_amount.length() - 2);
                                    tv_final_amount.setText("₹ " + order_amount);

                                }

                                if (doc.get(getString(R.string.partner_id)) != null) {
                                    partner_id = doc.getString(getString(R.string.partner_id));
                                }

                                if (doc.get(getString(R.string.order_id)) != null) {
                                    order_id = String.valueOf(doc.getLong(getString(R.string.order_id)));
                                }
                            }
                        }
                    });

        }

    }

    private void updateDriverAccounts() {

        Map<String, Object> data = new HashMap<>();
        data.put(getString(R.string.user_id), firebaseUser.getUid());
        data.put(getString(R.string.order_id), order_id);
        data.put(getString(R.string.order_document_id), document_id);
        data.put(getString(R.string.date_created), new Date());
        data.put(getString(R.string.order_amount), order_amount);
        data.put(getString(R.string.service_id), service_id);
        data.put(getString(R.string.razorpay_payment_id), razorpay_payment_id);

        if (partner_id != null) {
            db.collection(getString(R.string.partners))
                    .document(partner_id)
                    .collection(getString(R.string.accounts)).document()
                    .set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Log.i(TAG, "Driver data updated");
                            setResult(200);
                            finish();
                            finish();

                            //TODO: this finish is not working when payment gets completed.
                            // I think it is finishing to payment activity instead of going behind this activity. Just see
                            //and that setStatus code stuff wasnt working as well and i deleted it by mistake. okay bye.

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            btn_pay.resetSlider();
                            finish();

                        }
                    });

        }
    }

    //Test URL for volley
    //"https://jsonplaceholder.typicode.com/users"

    private void getRequest(String amount) {

        String url = "https://krsi-karma-payment.herokuapp.com/order?amount=" + amount;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.i(TAG, "RESPONSE IS " + response.toString());

                        try {
                            razorpay_order_id = response.getString("id");
                            Log.i(TAG, "razor pay order id is " + razorpay_order_id);

                            if(razorpay_order_id!=null){
                                startPayment();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error isn onErrorResponse ", error);
                        btn_pay.resetSlider();

                    }
                });

        requestQueue.add(jsonObjectRequest);


    }


    private void startPayment() {

        final Activity activity = this;
        Checkout checkout = new Checkout();
        checkout.setKeyID(getString(R.string.RAZOR_PAY_KEY_TEST_ID));

        try {
            JSONObject options = new JSONObject();

            options.put("name", getString(R.string.app_name));
            options.put("description", "Reference #" + order_id); //From firebase reference order id
            options.put("order_id", razorpay_order_id); //from Get request razorPay
            options.put("theme.color", getColor(R.color.brand_color));
            options.put("currency", "INR");
            options.put("amount", order_amount);//pass amount in currency subunits
            options.put("prefill.contact", firebaseUser.getPhoneNumber());

            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 3);
            options.put("retry", retryObj);

            checkout.open(activity, options);

        } catch (Exception e) {

            Log.e(TAG, "Error in starting Razorpay Checkout ", e);
            btn_pay.resetSlider();
        }


    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {

        razorpay_payment_id = razorpayPaymentID;
        Log.i(TAG, "payment successful " + razorpayPaymentID);


        try {
            //add data to firestore
            Map<String, Object> data = new HashMap<>();
            data.put(getString(R.string.order_status), getString(R.string.order_status_type_completed));
            data.put(getString(R.string.payment_mode), getString(R.string.online));

            db.collection(getString(R.string.orders)).document(document_id)
                    .update(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), "Payment Successful", Toast.LENGTH_SHORT).show();

                            updateDriverAccounts();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "Payment is completed but unable to add onto database." + e.getMessage());
                            showAlert(getString(R.string.error), getString(R.string.unable_to_add_onto_database));

                        }
                    });

            //

        } catch (Exception e) {
            Log.e(TAG, "Exception in onPaymentSuccess", e);
        }
    }

    @Override
    public void onPaymentError(int code, String response) {

        try {
            Log.i(TAG, "onPaymentError: Code: " + code + " Response: " + response);
            JSONObject jsonObject = new JSONObject(response);
            btn_pay.resetSlider();
            String message = getString(R.string.payment_failed_message) + " " + jsonObject.get("description") + " " + getString(R.string.please_try_again);
            showAlert(getString(R.string.payment_failed), message);
        } catch (Exception e) {
            Log.e(TAG, "Exception in onPaymentError", e);btn_pay.resetSlider();

        }
    }

    private void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this);
        builder.setMessage(message)
                .setCancelable(true)
                .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                        overridePendingTransition(0, 0);
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle(title);
        alert.show();
    }


    @Override
    protected void onStop() {
        super.onStop();
        Checkout.clearUserData(getApplicationContext());
        if (checkout != null) {
            checkout = null;
        }


        if (listenerRegistration != null) {
            listenerRegistration = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

