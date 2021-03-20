package com.krsikarma.app.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.krsikarma.app.Models.Service;
import com.krsikarma.app.R;
import com.krsikarma.app.Utility.DefaultTextConfig;
import com.krsikarma.app.Utility.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ServiceDescriptionActivity extends AppCompatActivity {

    public static final String TAG = "ServiceDescriptionActiv";
    public static final int BOOK_SERVICE = 69;
    MaterialButton btn_book_now;
    ImageView img_service;
    ImageView img_back;
    TextView tv_service_name;
    TextView tv_metric_rate_final_amount;
    TextView tv_final_amount;
    TextView tv_description;
    EditText et_quantity;

    FirebaseFirestore db;
    ListenerRegistration listenerRegistration;

    Utils utils;
    ArrayList<Service> serviceArrayList;

    String service_id;
    String str_final_amount;
    Double double_final_amount;

    String phone_language;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == BOOK_SERVICE){
            onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DefaultTextConfig.adjustFontScale(ServiceDescriptionActivity.this);
        setContentView(R.layout.activity_service_description);

        init();

        getServiceData();




        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        et_quantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(charSequence.length()>0){
                    double_final_amount = Double.parseDouble(charSequence.toString()) * serviceArrayList.get(0).getService_rate();

                    str_final_amount = double_final_amount.toString().substring(0,double_final_amount.toString().length()-2);

                    tv_final_amount.setText("₹ "+ str_final_amount);
                    btn_book_now.setEnabled(true);
                }else{
                    tv_final_amount.setText("₹ 0000");
                    btn_book_now.setEnabled(false);


                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btn_book_now.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), BookAServiceActivity.class);
            intent.putExtra("quantity", et_quantity.getText().toString().trim());
            intent.putExtra("final_amount", double_final_amount);
            intent.putExtra("serviceArrayList", serviceArrayList);

            startActivityForResult(intent,BOOK_SERVICE);
//            startActivity(intent);
        });




    }

    private void init(){
        btn_book_now = (MaterialButton) findViewById(R.id.btn_book_now);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_service = (ImageView) findViewById(R.id.img_service);
        tv_service_name = (TextView) findViewById(R.id.tv_service_name);
        tv_metric_rate_final_amount = (TextView) findViewById(R.id.tv_metric_rate_final_amount);
        tv_description = (TextView) findViewById(R.id.tv_description);
        et_quantity = (EditText) findViewById(R.id.et_quantity);
        tv_final_amount = (TextView) findViewById(R.id.tv_final_amount);

        et_quantity.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        btn_book_now.setEnabled(false);


        utils = new Utils();
        serviceArrayList = new ArrayList<>();

        db = FirebaseFirestore.getInstance();

        service_id = getIntent().getStringExtra(getString(R.string.service_id));
        phone_language = Locale.getDefault().getLanguage();


    }

    private void getServiceData(){

        if(service_id!=null){
            DocumentReference doc_ref = db.collection(getString(R.string.services)).document(service_id);
            listenerRegistration = doc_ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException e) {
                    if(e!=null){
                        Log.e(TAG,"Error in getServiceData Function ", e);
                        return;
                    }

                    if(doc!=null && doc.exists()) {
                        String service_id;
                        String service_name = "";
                        Double service_rate = 0.0;
                        String service_metric = "";
                        String service_image_url = "";
                        String service_description = "";
                        String service_date_created = "";


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

                        tv_service_name.setText(service_name);
                        tv_description.setText(service_description);

                        String str_service_rate = service_rate.toString().substring(0,service_rate.toString().length()-2);
                        tv_metric_rate_final_amount.setText(getString(R.string.acres) + " X ₹" + str_service_rate);

                        Glide.with(getApplicationContext())
                                .load(service_image_url)
                                .centerCrop()
                                .into(img_service);


                        if(phone_language.equals("hi")){
                            utils.translateEnglishToHindi(serviceArrayList.get(0).getService_name(), tv_service_name);
                            utils.translateEnglishToHindi(serviceArrayList.get(0).getService_description(), tv_description);
                        }
                    }
                }
            });

        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        if(listenerRegistration!=null){
            listenerRegistration=null;
        }

        utils.closeTranslator();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}