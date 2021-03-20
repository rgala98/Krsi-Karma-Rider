package com.krsikarma.app.Activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.krsikarma.app.R;


import java.util.Locale;

public class GetStartedActivity extends AppCompatActivity {


    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    Button btn_get_started;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_KrsiKarma);
        setContentView(R.layout.activity_get_started);

        init();

        btn_get_started.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class );
            startActivity(intent);

        });
    }


    private void init(){
        mAuth= FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();

        btn_get_started = (Button) findViewById(R.id.btn_get_started);
    }




    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}