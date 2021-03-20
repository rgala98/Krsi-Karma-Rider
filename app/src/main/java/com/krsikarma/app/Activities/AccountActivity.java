package com.krsikarma.app.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.krsikarma.app.Utility.DefaultTextConfig;
import com.krsikarma.app.Utility.Utils;

import java.util.HashMap;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {

    public static final String TAG = "AccountActivity";
    public static final String CHANNEL_ID = "NOTIF";
    public static final String CHANNEL_NAME = "Notifications";
    public static final String CHANNEL_DESC = "This channel is for all notifications";

    Button btn_submit;
    TextView tv_edit;
    TextView tv_phone;
    EditText et_first_name;
    EditText et_last_name;
    ImageView img_back;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore db;
    ListenerRegistration listenerRegistration;

    Utils utils;


    String first_name;
    String last_name;
    String phone_number;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(AccountActivity.this);
        setContentView(R.layout.activity_account);

        init();

        getData();


        tv_edit.setOnClickListener(view -> {
            // Make the First name and last name editable
            btn_submit.setVisibility(View.VISIBLE);
            tv_edit.setVisibility(View.GONE);
            et_last_name.setEnabled(true);
            et_first_name.setEnabled(true);
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!et_first_name.getText().toString().trim().isEmpty() && !et_last_name.getText().toString().trim().isEmpty()){
                    btn_submit.setEnabled(false);
                    updateData();
                }

            }
        });



        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }

    }

    private void init(){

        btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_submit.setVisibility(View.GONE);

        et_first_name = (EditText) findViewById(R.id.et_first_name);
        et_last_name = (EditText) findViewById(R.id.et_last_name);
        et_first_name.setEnabled(false);
        et_last_name.setEnabled(false);

        tv_phone = (TextView) findViewById(R.id.tv_phone);
        tv_edit = (TextView) findViewById(R.id.tv_edit);

        img_back = (ImageView) findViewById(R.id.img_back);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        utils = new Utils();

    }

    private void getData(){
        if(firebaseUser!=null){
            listenerRegistration = db.collection(getString(R.string.users)).document(firebaseUser.getUid())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {

                            if(error!=null){
                                Log.i(TAG, "An error occurred " + error.getLocalizedMessage());
                                return;
                            }

                            if(snapshot!=null && snapshot.exists()){

                                if(!utils.isStringNull(snapshot.getString(getString(R.string.first_name)))){
                                    first_name = snapshot.getString(getString(R.string.first_name));
                                    et_first_name.setText(first_name);
                                }

                                if(!utils.isStringNull(snapshot.getString(getString(R.string.last_name)))){
                                    last_name = snapshot.getString(getString(R.string.last_name));
                                    et_last_name.setText(last_name);
                                }

                                if(!utils.isStringNull(snapshot.getString(getString(R.string.phone_number)))){
                                    phone_number = snapshot.getString(getString(R.string.phone_number));
                                    tv_phone.setText(phone_number.substring(3));
                                }


                            }
                        }
                    });
        }

    }

    private void updateData(){
        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> data = new HashMap<>();
        data.put(getString(R.string.first_name),et_first_name.getText().toString().trim());
        data.put(getString(R.string.last_name), et_last_name.getText().toString().trim());

        db.collection(getString(R.string.users)).document(firebaseUser.getUid())
                .update(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Date is updated");
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), getString(R.string.updated), Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Log.i(TAG, "An error occurred " + e.getLocalizedMessage());
                        Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                        btn_submit.setEnabled(true);
                    }
                });


    }


    @Override
    protected void onStop() {
        super.onStop();
        if(listenerRegistration!=null){
            listenerRegistration=null;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}