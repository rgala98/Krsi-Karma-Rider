package com.krsikarma.app.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.krsikarma.app.Adapters.SettingsRecyclerAdapter;
import com.krsikarma.app.R;
import com.krsikarma.app.Utility.DefaultTextConfig;
import com.krsikarma.app.Utility.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SettingsActivity extends AppCompatActivity {

    public static final int RESULT_LOAD_IMAGE_MULTIPLE = 18;
    public static final int RESULT_CAMERA = 23;
    public static final String TAG = "SettingsActivity";

    ImageView img_back;
    ImageView img_profile;
    RecyclerView settings_recycler_view;
    SettingsRecyclerAdapter settingsRecyclerAdapter;
    ProgressBar progressBar;

    ArrayList<String> settingNameArrayList;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    StorageTask uploadTask;
    Bitmap bitmapImage;


    Utils utils;

    String profile_photo_url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(SettingsActivity.this);
        setContentView(R.layout.activity_settings);

        init();

        setSettingsRecyclerAdapter();

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        img_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPermissionListeners();
            }
        });

        if (profile_photo_url != null) {
            Glide.with(getApplicationContext())
                    .load(profile_photo_url)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(img_profile);
        }

    }

    private void init() {
        img_back = (ImageView) findViewById(R.id.img_back);
        img_profile = (ImageView) findViewById(R.id.img_profile);
        settings_recycler_view = (RecyclerView) findViewById(R.id.settings_recycler_view);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        settingNameArrayList = new ArrayList<>();
        utils = new Utils();

        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        profile_photo_url = getIntent().getStringExtra("profile_photo_url");

//        //for camera intent
//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());


    }

    private void setSettingsRecyclerAdapter() {
        settingNameArrayList.add(getString(R.string.account));
        settingNameArrayList.add(getString(R.string.your_orders));
        settingNameArrayList.add(getString(R.string.privacy_policy));
        settingNameArrayList.add(getString(R.string.terms_of_use));
        settingNameArrayList.add(getString(R.string.logout));

        settingsRecyclerAdapter = new SettingsRecyclerAdapter(SettingsActivity.this, settingNameArrayList);
        settings_recycler_view.setAdapter(settingsRecyclerAdapter);


    }

    private void createPermissionListeners() {

        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {

                if (report.areAllPermissionsGranted()) {
                    setProfilePicture();
                }

                // check for permanent denial of any permission
                else if (report.isAnyPermissionPermanentlyDenied()) {
                    Log.i(TAG, "Permissions permanently denied. Open Settings");


                    AlertDialog.Builder builder;
                    AlertDialog alert;

                    builder = new AlertDialog.Builder(SettingsActivity.this);
                    builder.setMessage(getString(R.string.open_settings_permission))
                            .setCancelable(true)
                            .setPositiveButton(getString(R.string.open_settings), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    startActivity(intent);

                                }
                            });
                    alert = builder.create();
                    alert.setTitle(getString(R.string.error));
                    alert.show();
                }

            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }

        }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {
                Toast.makeText(getApplicationContext(), "Error occurred! " + error.toString(), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Error Occurred" + error.toString());
            }
        }).check();

    }

    private void setProfilePicture() {

        final String takePhoto = getString(R.string.take_photo);
        final String chooseFromLibrary = getString(R.string.choose_from_gallery);


        final CharSequence[] items = {takePhoto, chooseFromLibrary};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {


                if (items[item].equals(chooseFromLibrary)) {

                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE_MULTIPLE);

                } else if (items[item].equals(takePhoto)) {


                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, RESULT_CAMERA);

                }


            }
        });
        builder.show();


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RESULT_LOAD_IMAGE_MULTIPLE && resultCode == Activity.RESULT_OK) {

            img_profile.setImageBitmap(null);
            if (data.getData() != null) {
                Uri mImageUri = data.getData();
                try {
                    bitmapImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri);
                    uploadPhoto();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }


        }

        if (requestCode == RESULT_CAMERA && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            bitmapImage = (Bitmap) extras.get("data");
            uploadPhoto();
        }

    }

    private void uploadPhoto() {
        progressBar.setVisibility(View.VISIBLE);
        //delete previous photo from storage
        if (profile_photo_url != null) {
            StorageReference photoRef = firebaseStorage.getReferenceFromUrl(profile_photo_url);
            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Log.d(TAG, "onSuccess: deleted file");

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Log.d(TAG, "onFailure: did not delete file");
                }
            });
        }

        //now upload new file
        if (bitmapImage != null) {
            final StorageReference ref = storageReference.child("profile_images/" + UUID.randomUUID().toString());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream); // bmp is bitmap from user image file
            bitmapImage.recycle();
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            uploadTask = ref.putBytes(byteArray);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();

                }
            }).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        String new_profile_image_url = task.getResult().toString();
                        profile_photo_url = new_profile_image_url;

                        bitmapImage = null;
                        Glide.with(getApplicationContext())
                                .load(new_profile_image_url)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .into(img_profile);

                        progressBar.setVisibility(View.GONE);
                        //now update reference on firestore
                        Map<String, Object> data = new HashMap<>();
                        data.put(getString(R.string.profile_img_url), new_profile_image_url);
                        db.collection(getString(R.string.users)).document(firebaseUser.getUid())
                                .update(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(), getString(R.string.profile_pic_updated), Toast.LENGTH_SHORT).show();


                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                                Log.i(TAG, "An error occurred " + e.getLocalizedMessage());
                                progressBar.setVisibility(View.GONE);


                            }
                        });

                    } else {
                        Log.i(TAG, "An error occurred in uploading image" + task.getException().getLocalizedMessage());
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }


    }

    @Override
    public void onBackPressed() {
        finish();
    }
}