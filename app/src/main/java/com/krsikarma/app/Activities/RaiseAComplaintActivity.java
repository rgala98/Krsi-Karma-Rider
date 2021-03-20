package com.krsikarma.app.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.krsikarma.app.Models.Order;
import com.krsikarma.app.R;
import com.krsikarma.app.Utility.DefaultTextConfig;
import com.krsikarma.app.Utility.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RaiseAComplaintActivity extends AppCompatActivity {


    public static final String TAG = "RaiseAComplaintActivity";
    // From imported Layout
    View view_order;
    Button btn_raise_complaint;
    ArrayList<Order> orderArrayList;
    ImageView img_back;
    EditText et_complaint;
    ImageView img_service;
    TextView tv_service_name;
    TextView tv_final_amount;
    TextView tv_metric_rate;
    TextView tv_address;
    TextView tv_order_date_time;
    TextView tv_order_id;
    TextView tv_status;
    Button btn_submit;
    Utils utils;

    FirebaseFirestore db;
    int position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(RaiseAComplaintActivity.this);
        setContentView(R.layout.activity_raise_a_complaint);

        init();

        img_back.setOnClickListener(view -> {
            onBackPressed();
        });

        if (orderArrayList!=null && !orderArrayList.isEmpty()) {
            Order order = orderArrayList.get(position);
            Activity mActivity = RaiseAComplaintActivity.this;

            Glide.with(getApplicationContext())
                    .load(order.getService_image_url())
                    .into(img_service);
            tv_service_name.setText(order.getService_name());
            tv_final_amount.setText("₹ " + order.getOrder_amount());
            tv_metric_rate.setText(order.getOrder_quantity() + " " + order.getOrder_metric() + " x ₹ " + order.getOrder_rate());
            tv_address.setText(order.getOrder_address());
            tv_order_id.setText("#" + order.getOrder_id());
            tv_order_date_time.setText(order.getOrder_date() + " at " + order.getOrder_time());
            tv_status.setText(order.getOrder_status());

            if (order.getOrder_status().equals(mActivity.getString(R.string.order_status_type_requested))) {
                tv_status.setBackgroundColor(mActivity.getColor(R.color.light_orange));
                tv_status.setTextColor(mActivity.getColor(R.color.orange));

            } else if (order.getOrder_status().equals(mActivity.getString(R.string.order_status_type_completed))) {
                tv_status.setBackgroundColor(mActivity.getColor(R.color.light_green));
                tv_status.setTextColor(mActivity.getColor(R.color.green));

            } else if (order.getOrder_status().equals(mActivity.getString(R.string.order_status_type_ongoing))) {
                tv_status.setBackgroundColor(mActivity.getColor(R.color.light_yellow));
                tv_status.setTextColor(mActivity.getColor(R.color.yellow));

            } else if (order.getOrder_status().equals(mActivity.getString(R.string.order_status_type_cancelled))) {
                tv_status.setBackgroundColor(mActivity.getColor(R.color.brand_color_light));
                tv_status.setTextColor(mActivity.getColor(R.color.brand_color));
            }

        }
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!et_complaint.getText().toString().trim().isEmpty()) {
                    addComplaint();
                } else {
                    utils.alertDialogOK(RaiseAComplaintActivity.this, getString(R.string.error), getString(R.string.complete_all_fields));
                }
            }
        });
    }


    private void init() {

        img_back = (ImageView) findViewById(R.id.img_back);
        view_order = findViewById(R.id.view_order);
        btn_raise_complaint = (Button) view_order.findViewById(R.id.btn_raise_complaint);
        btn_raise_complaint.setVisibility(View.GONE);
        img_service = (ImageView) view_order.findViewById(R.id.img_service);
        tv_service_name = (TextView) view_order.findViewById(R.id.tv_service_name);
        tv_final_amount = (TextView) view_order.findViewById(R.id.tv_final_amount);
        tv_metric_rate = (TextView) view_order.findViewById(R.id.tv_metric_rate);
        tv_address = (TextView) view_order.findViewById(R.id.tv_address);
        tv_order_date_time = (TextView) view_order.findViewById(R.id.tv_order_date_time);
        tv_order_id = (TextView) view_order.findViewById(R.id.tv_order_id);
        tv_status = (TextView) view_order.findViewById(R.id.tv_status);

        et_complaint = (EditText) findViewById(R.id.et_complaint);
        btn_submit = (Button) findViewById(R.id.btn_submit);

        db = FirebaseFirestore.getInstance();

        utils = new Utils();
        orderArrayList = new ArrayList<>();
        orderArrayList = getIntent().getParcelableArrayListExtra("order_list");
        position = getIntent().getIntExtra("pos", 0);
        Log.i(TAG, "orderArrayList is " +orderArrayList.get(position));


    }

    private void addComplaint() {
        if(orderArrayList!=null && !orderArrayList.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put(getString(R.string.user_id), FirebaseAuth.getInstance().getCurrentUser().getUid());
            data.put(getString(R.string.date_created), new Date());
            data.put(getString(R.string.complaint_message), et_complaint.getText().toString().trim());
            data.put(getString(R.string.service_id), orderArrayList.get(0).getService_id());
            data.put(getString(R.string.order_id), orderArrayList.get(0).getOrder_id());
            data.put(getString(R.string.order_document_id), orderArrayList.get(0).getOrder_document_id());
            data.put(getString(R.string.user_type),"user");
            data.put(getString(R.string.order_status), orderArrayList.get(0).getOrder_status());


            db.collection(getString(R.string.complaints)).document().set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "Data is added");
                            Toast.makeText(getApplicationContext(), getString(R.string.complaint_successful_message), Toast.LENGTH_LONG).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "An error occurred " + e.getLocalizedMessage());
                        }
                    });
        }else{
            Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}