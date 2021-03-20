package com.krsikarma.app.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.krsikarma.app.Activities.ServiceDescriptionActivity;
import com.krsikarma.app.Models.Service;
import com.krsikarma.app.R;
import com.krsikarma.app.Utility.Utils;

import java.util.ArrayList;

public class ServicesGridAdapter extends BaseAdapter {

    public static final String TAG = "ServiceGridAdapter";
    private ArrayList<Service> serviceArrayList;
    private Activity mActivity;
    private LayoutInflater inflater;



    Utils utils;
    Boolean isTranslateModelDownloaded = false;
    Translator englishHindiTranslator;


    public ServicesGridAdapter(ArrayList<Service> serviceArrayList, Activity mActivity) {
        this.serviceArrayList = serviceArrayList;
        this.mActivity = mActivity;

        utils = new Utils();
    }

    @Override
    public int getCount() {
        return serviceArrayList.size();
    }

    @Override
    public Object getItem(int i) {

        Object object = new Object(){
            String service_name = serviceArrayList.get(i).getService_name();


        };
        return object;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Service service = serviceArrayList.get(i);
        if(view==null){

            inflater=(LayoutInflater) mActivity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view=inflater.inflate(R.layout.row_service_grid,null);
        }

        TextView tv_service_name = (TextView) view.findViewById(R.id.tv_service_name);
        ImageView img_service = (ImageView) view.findViewById(R.id.img_service);


        Glide.with(mActivity.getApplicationContext())
                .load(service.getService_image_url())
                .centerCrop()
                .into(img_service);


        tv_service_name.setText(service.getService_name());
        translateEnglishToHindi(service.getService_name(), tv_service_name);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mActivity.getApplicationContext(), ServiceDescriptionActivity.class);
                intent.putExtra(mActivity.getString(R.string.service_id),service.getService_id());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.getApplicationContext().startActivity(intent);
            }
        });


        return view;
    }

    public void updateList(ArrayList<Service> list){
        serviceArrayList = list;
        notifyDataSetChanged();
    }

    public void translateEnglishToHindi(String text, TextView textView){
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.HINDI)
                        .build();
        englishHindiTranslator =
                Translation.getClient(options);

        //check if translation model exists in device

        DownloadConditions conditions = new DownloadConditions.Builder()
                .build();
        englishHindiTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Model downloaded successfully. Okay to start translating.
                        // (Set a flag, unhide the translation UI, etc.)
                        isTranslateModelDownloaded = true;
                        Log.i(TAG, "Translator is downloaded");

                        englishHindiTranslator.translate(text)
                                .addOnSuccessListener(new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(String s) {
                                        Log.i(TAG, "hindi description is " + s);
                                        textView.setText(text + " { " + s + " }");
                                    }
                                })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Log.i(TAG, "could not translate " + e.getLocalizedMessage());
                                            }
                                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Model couldn’t be downloaded or other internal error.
                        Log.i(TAG, "Translator could not be downloaded " +e.getLocalizedMessage() );
                    }
                });


    }




}
