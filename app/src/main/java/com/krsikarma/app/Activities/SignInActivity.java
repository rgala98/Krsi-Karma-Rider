package com.krsikarma.app.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.krsikarma.app.R;
import com.krsikarma.app.Utility.DefaultTextConfig;
import com.krsikarma.app.Utility.Utils;


public class SignInActivity extends AppCompatActivity {

    EditText et_phone;
    Button btn_sign_in;
    String phone_number;
    Utils utils;
    ImageView img_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(SignInActivity.this);
        setContentView(R.layout.activity_sign_in);

        init();


        btn_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (et_phone==null || et_phone.getText().toString().isEmpty() || et_phone.getText().toString().trim().length()!=10){
                    utils.alertDialogOK(SignInActivity.this,getResources().getString(R.string.error),getResources().getString(R.string.enter_10_digit_number));
                }else if(et_phone.getText().toString().charAt(0) != '9' &&
                        et_phone.getText().toString().charAt(0) != '8'  &&
                        et_phone.getText().toString().charAt(0) != '7'  &&
                        et_phone.getText().toString().charAt(0) != '6'){
                    utils.alertDialogOK(SignInActivity.this,getString(R.string.error),getString(R.string.enter_valid_number));
                }else {
                    phone_number = "+91"+et_phone.getText().toString().trim();
                    showMessageOptions(getString(R.string.otp_sent_to) +" "+ phone_number, getString(R.string.app_name)+" "+getResources().getString(R.string.otp_warning));

                }


            }
        });

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private void init(){
        et_phone = (EditText) findViewById(R.id.et_phone);
        btn_sign_in = (Button) findViewById(R.id.btn_sign_in);
        utils = new Utils();
        img_back = (ImageView) findViewById(R.id.img_back);
    }

    public void showMessageOptions(String title, String message){
        final AlertDialog.Builder builder=new AlertDialog.Builder(SignInActivity.this);
        builder.setCancelable(true);

        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                Intent intent = new Intent (getApplicationContext(), EnterOtpActivity.class);
                intent.putExtra("phone_number",phone_number);
                startActivity(intent);

            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}