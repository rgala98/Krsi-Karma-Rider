package com.krsikarma.app.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.krsikarma.app.Adapters.OrdersRecyclerAdapter;
import com.krsikarma.app.Models.Order;
import com.krsikarma.app.R;
import com.krsikarma.app.Utility.DefaultTextConfig;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class YourOrdersActivity extends AppCompatActivity {

    public static final String TAG = "YourOrdersActivity";
    RecyclerView recycler_view;
    OrdersRecyclerAdapter ordersRecyclerAdapter;
    ArrayList<Order> orderArrayList;

    FirebaseFirestore db;
    ListenerRegistration listenerRegistration;

    ImageView img_back;

    String phone_language;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(YourOrdersActivity.this);
        setContentView(R.layout.activity_your_orders);

        init();

        img_back.setOnClickListener(view -> {
            onBackPressed();
        });


        getOrders();

    }

    private void init() {

        img_back = (ImageView) findViewById(R.id.img_back);
        recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
        orderArrayList = new ArrayList<>();

        db = FirebaseFirestore.getInstance();

        phone_language = Locale.getDefault().getLanguage();


    }


    private void getOrders() {
        listenerRegistration = db.collection(getString(R.string.orders))
                .whereEqualTo(getString(R.string.user_id), FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderBy(getString(R.string.order_date_time), Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        String service_id = "";
                        String service_name = "";
                        String service_image_url = "";
                        String order_id = "";
                        String user_id = "";
                        String partner_id = "";
                        String order_date = "";
                        String order_time = "";
                        String order_status = "";
                        String order_address = "";
                        String order_quantity = "";
                        String order_rate = "";
                        String order_metric = "";
                        String order_amount = "";
                        String date_created = "";


                        orderArrayList.clear();
                        for (QueryDocumentSnapshot doc : value) {


                            if (doc.get(getString(R.string.order_id)) != null) {
                                order_id = String.valueOf(doc.getLong(getString(R.string.order_id)));
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

                            if (doc.get(getString(R.string.partner_id)) != null) {
                                partner_id = doc.getString(getString(R.string.partner_id));
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

                            orderArrayList.add(new Order(
                                    service_id,
                                    service_name,
                                    service_image_url,
                                    order_id,
                                    user_id,
                                    partner_id,
                                    order_date,
                                    order_time,
                                    order_status,
                                    order_address,
                                    order_quantity,
                                    order_rate,
                                    order_metric,
                                    order_amount,
                                    date_created,
                                    doc.getId()

                            ));


                        }


                        ordersRecyclerAdapter = new OrdersRecyclerAdapter(YourOrdersActivity.this, orderArrayList, phone_language);
                        recycler_view.setAdapter(ordersRecyclerAdapter);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
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