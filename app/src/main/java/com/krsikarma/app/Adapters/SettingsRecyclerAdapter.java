package com.krsikarma.app.Adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.krsikarma.app.Activities.AccountActivity;
import com.krsikarma.app.Activities.CreateProfileActivity;
import com.krsikarma.app.Activities.GetStartedActivity;
import com.krsikarma.app.Activities.RaiseAComplaintActivity;
import com.krsikarma.app.Activities.YourOrdersActivity;
import com.krsikarma.app.R;
import com.krsikarma.app.Utility.Utils;


import java.util.ArrayList;

public class SettingsRecyclerAdapter extends RecyclerView.Adapter<SettingsRecyclerAdapter.ViewHolder> {

    public static final String TAG = "SettingsRecyclerAdapter";
    ArrayList<String> settingNameArrayList;
    Activity mActivity;

    Utils utils;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;

    public SettingsRecyclerAdapter(Activity mActivity, ArrayList<String> settingNameArrayList) {
        this.mActivity = mActivity;
        this.settingNameArrayList = settingNameArrayList;

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        utils = new Utils();
    }

    @NonNull
    @Override
    public SettingsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater= LayoutInflater.from(mActivity.getApplicationContext());
        View view=layoutInflater.inflate(R.layout.row_settings,null);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsRecyclerAdapter.ViewHolder holder, int position) {
        holder.tv_settings_name.setText(settingNameArrayList.get(position));

        if(position == 4){
            holder.tv_settings_name.setTextColor(mActivity.getColor(R.color.brand_color));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (position){
                    case 0: //Account

                        Intent accountIntent = new Intent(mActivity, AccountActivity.class);
                        accountIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mActivity.startActivity(accountIntent);
                        break;

                    case 1: // Your Orders

                        Intent orders_intent = new Intent(mActivity, YourOrdersActivity.class);
                        orders_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mActivity.startActivity(orders_intent);
                        break;

                    case 2: //Privacy Policy




                    case 3: //Terms
                        break;
                    case 4:
                        //logout

                        AlertDialog.Builder builder;
                        AlertDialog alert;

                        builder = new AlertDialog.Builder(mActivity);
                        builder.setMessage(mActivity.getString(R.string.are_you_sure_logout))
                                .setCancelable(true)
                                .setPositiveButton(mActivity.getString(R.string.logout), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

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

                                                        final DocumentReference doc_id = FirebaseFirestore.getInstance().collection(mActivity.getString(R.string.users)).document(firebaseUser.getUid());


                                                        doc_id.update("token", FieldValue.arrayRemove(token)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.i(TAG,"Token Removed");
                                                                mAuth.signOut();
                                                                mActivity.finishAffinity();
                                                                Intent intent = new Intent(mActivity, GetStartedActivity.class);
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                mActivity.startActivity(intent);
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.i(TAG,"An error occurred : "+e.getMessage());
                                                            }
                                                        });


                                                    }
                                                });




                                    }
                                });
                        alert = builder.create();
                        alert.setTitle("");
                        alert.show();
                        break;
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return settingNameArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView tv_settings_name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_settings_name = (TextView) itemView.findViewById(R.id.tv_settings_name);
        }
    }
}
