package com.krsikarma.app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.krsikarma.app.R;
import com.krsikarma.app.Utility.DefaultTextConfig;
import com.krsikarma.app.Utility.Utils;


import java.util.concurrent.TimeUnit;

public class EnterOtpActivity extends AppCompatActivity {

    public static final String TAG = "EnterOTPActivity";
    private static final String KEY_VERIFICATION_ID = "key_verification_id";
    Button btn_Verify;
    EditText et_number_1;
    EditText et_number_2;
    EditText et_number_3;
    EditText et_number_4;
    EditText et_number_5;
    EditText et_number_6;
    TextView tv_phone_number;

    String otp;
    String phone_number;
    TextView tv_resend_code;
    ProgressBar progress_bar;
    Utils utils;

    //firebase
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String mVerificationId;
    private boolean verificationSuccessful;
    FirebaseFirestore db;
    ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(EnterOtpActivity.this);
        setContentView(R.layout.activity_enter_otp);

        init();

        if(phone_number!=null){
            getOtp();
            tv_phone_number.setText(phone_number);
        }


        tv_resend_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendVerificationCode(phone_number, mResendToken);
            }
        });

        btn_Verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                otp = et_number_1.getText().toString().trim() + et_number_2.getText().toString().trim()+et_number_3.getText().toString().trim()
                        +et_number_4.getText().toString().trim()+et_number_5.getText().toString().trim()+et_number_6.getText().toString().trim();


                if(otp == null || otp.isEmpty() || otp.length() != 6){

                    btn_Verify.setEnabled(true);
                    utils.alertDialogOK(EnterOtpActivity.this,getString(R.string.error),getResources().getString(R.string.Please_enter_correct_OTP));
                }else{

                    if (mVerificationId == null && savedInstanceState != null) {
                        onRestoreInstanceState(savedInstanceState);

                    }
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
                    signInWithPhoneAuthCredential(credential);

                }


            }
        });


        shiftEditTextFocus();
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_VERIFICATION_ID,mVerificationId);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationId = savedInstanceState.getString(KEY_VERIFICATION_ID);
    }

    private void init(){

        btn_Verify = (Button) findViewById(R.id.btn_verify);
        et_number_1 = (EditText) findViewById(R.id.et_number_1);
        et_number_2 = (EditText) findViewById(R.id.et_number_2);
        et_number_3 = (EditText) findViewById(R.id.et_number_3);
        et_number_4 = (EditText) findViewById(R.id.et_number_4);
        et_number_5 = (EditText) findViewById(R.id.et_number_5);
        et_number_6 = (EditText) findViewById(R.id.et_number_6);

        tv_resend_code = (TextView) findViewById(R.id.tv_resend_code);
        tv_phone_number=(TextView) findViewById(R.id.tv_phone_number);

        phone_number = getIntent().getStringExtra("phone_number");

        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        utils = new Utils();

        btn_Verify.setEnabled(false);

    }

    private void shiftEditTextFocus(){
        et_number_1.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start,int before, int count)
            {

                if(et_number_1.getText().toString().length()==1)     //size as per your requirement
                {
                    et_number_2.requestFocus();
                }
            }
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {


            }

            public void afterTextChanged(Editable s) {

            }

        });

        et_number_2.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start,int before, int count)
            {
                if(et_number_2.getText().toString().length()==1)     //size as per your requirement
                {
                    et_number_3.requestFocus();
                }

                if(TextUtils.isEmpty(et_number_2.getText().toString())){
                    et_number_1.requestFocus();
                }
            }
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            public void afterTextChanged(Editable s) {

            }

        });

        et_number_3.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start,int before, int count)
            {
                if(et_number_3.getText().toString().length()==1)     //size as per your requirement
                {
                    et_number_4.requestFocus();
                }

                if(TextUtils.isEmpty(et_number_3.getText().toString())){
                    et_number_2.requestFocus();
                }
            }
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            public void afterTextChanged(Editable s) {

            }

        });
        et_number_4.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start,int before, int count)
            {
                if(et_number_4.getText().toString().length()==1)     //size as per your requirement
                {
                    et_number_5.requestFocus();
                }

                if(TextUtils.isEmpty(et_number_4.getText().toString())){
                    et_number_3.requestFocus();
                }
            }
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            public void afterTextChanged(Editable s) {

            }

        });

        et_number_5.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start,int before, int count)
            {
                if(et_number_5.getText().toString().length()==1)     //size as per your requirement
                {
                    et_number_6.requestFocus();
                }

                if(TextUtils.isEmpty(et_number_5.getText().toString())){
                    et_number_4.requestFocus();
                }
            }
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            public void afterTextChanged(Editable s) {

            }

        });

        et_number_6.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start,int before, int count)
            {

                if(TextUtils.isEmpty(et_number_6.getText().toString())){
                    et_number_5.requestFocus();
                }
            }
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            public void afterTextChanged(Editable s) {

            }

        });

    }

    private void getOtp() {

        Log.i(TAG,"I am here");

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(final PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
//                mVerificationInProgress = false;

                //for phones that automatically detect otp and move ahead
                verificationSuccessful=true;


                if(verificationSuccessful && credential!=null){
                    signInWithPhoneAuthCredential(credential);
                }else{
                    utils.alertDialogOK(EnterOtpActivity.this,getString(R.string.error),getResources().getString(R.string.Please_enter_correct_OTP));
                }


            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                Log.w(TAG, "onVerificationFailed"+ e.getLocalizedMessage());



                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    utils.alertDialogOK(EnterOtpActivity.this,"Oops!",getResources().getString(R.string.phone_number_is_valid));
                    finish(); //go to previous activity to re enter phone number


                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.too_many_requests),Toast.LENGTH_LONG).show();
                    Log.i(TAG," here have been too many requests sent on this number");
                }else{
                    utils.alertDialogOK(EnterOtpActivity.this,getString(R.string.error),getResources().getString(R.string.Please_enter_correct_OTP));
                }

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                Log.d("Enter", "onCodeSent:" + verificationId);
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.otp_sent),Toast.LENGTH_SHORT).show();
                mVerificationId = verificationId;
                mResendToken = token;
                btn_Verify.setVisibility(View.VISIBLE);
                btn_Verify.setEnabled(true);

            }
        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone_number,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);


    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    //For auto Login
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        progress_bar.setVisibility(View.VISIBLE);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            progress_bar.setVisibility(View.GONE);
                            FirebaseUser user = task.getResult().getUser();


                                DocumentReference doc_ref= FirebaseFirestore.getInstance().collection(getString(R.string.users)).document(mAuth.getCurrentUser().getUid());

                                doc_ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            finishAffinity();
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(intent);

                                        }else{
                                            Intent intent = new Intent(getApplicationContext(), CreateProfileActivity.class);
                                            intent.putExtra("phone_number", phone_number);
                                            startActivity(intent);
                                        }
                                    }
                                });






                        }else{
                            progress_bar.setVisibility(View.GONE);
                            utils.alertDialogOK(EnterOtpActivity.this, getString(R.string.error), getString(R.string.Please_enter_correct_OTP));
                            Log.i(TAG,"Sign in not successful" + task.getException().getLocalizedMessage());
                        }
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
}