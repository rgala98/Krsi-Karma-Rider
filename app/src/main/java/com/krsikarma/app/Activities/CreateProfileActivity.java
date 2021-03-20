package com.krsikarma.app.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;


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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
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
import com.krsikarma.app.R;
import com.krsikarma.app.Utility.DefaultTextConfig;
import com.krsikarma.app.Utility.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class CreateProfileActivity extends AppCompatActivity {

    public static final String TAG = "CreateProfileActivity";
    public static final int RESULT_LOAD_IMAGE_MULTIPLE = 18;
    public static final int RESULT_CAMERA = 23;


    ImageView img_profile;
    EditText et_first_name;
    EditText et_last_name;
    TextView tv_phone;
    Button btn_register;
    ProgressBar progressBar;

    FirebaseFirestore db;
    ListenerRegistration listenerRegistration;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    StorageTask uploadTask;
    Bitmap bitmapImage;


    String phone_number;
    String first_name;
    String last_name;
    String mCurrentPhotoPath;


    Uri file_camera_uri;

    Utils utils;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTextConfig.adjustFontScale(CreateProfileActivity.this);
        setContentView(R.layout.activity_create_profile);

        init();




        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_register.setEnabled(false);

                first_name = utils.capitalizeString(et_first_name.getText().toString().trim());
                last_name = utils.capitalizeString(et_last_name.getText().toString().trim());

                if(!utils.isStringNull(first_name) && !utils.isStringNull(last_name) && !first_name.isEmpty() && !last_name.isEmpty()){
                    progressBar.setVisibility(View.VISIBLE);
                    if(utils.isInternetAvailable(CreateProfileActivity.this)){
                        addData();
                    }else{
                        btn_register.setEnabled(true);
                    }

                }else{
                    progressBar.setVisibility(View.GONE);
                    btn_register.setEnabled(true);
                    utils.alertDialogOK(CreateProfileActivity.this,getString(R.string.error),getString(R.string.complete_all_fields));
                }



            }
        });

        img_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                createPermissionListeners();
            }
        });




    }

    private void init(){

        img_profile = (ImageView) findViewById(R.id.img_profile);
        et_first_name = (EditText) findViewById(R.id.et_first_name);
        et_last_name = (EditText) findViewById(R.id.et_last_name);
        tv_phone = (TextView) findViewById(R.id.tv_phone);
        btn_register = (Button) findViewById(R.id.btn_register);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);



        utils = new Utils();


        phone_number = getIntent().getStringExtra("phone_number");
        if(phone_number!=null){
            tv_phone.setText(phone_number.substring(3));
        }

        //for camera intent
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();
    }

    private void addData(){
        progressBar.setVisibility(View.VISIBLE);
        if(bitmapImage!=null){
            final StorageReference ref = storageReference.child("profile_images/"+ UUID.randomUUID().toString());

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream); // bmp is bitmap from user image file
            bitmapImage.recycle();
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            uploadTask=ref.putBytes(byteArray);

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
                    if(task.isSuccessful()){

                        String profile_image_url= task.getResult().toString();


                        //adding data to firestore
                        Map<String, Object> data = new HashMap<>();
                        data.put(getString(R.string.user_id), firebaseUser.getUid());
                        data.put(getString(R.string.first_name),first_name);
                        data.put(getString(R.string.last_name), last_name);
                        data.put(getString(R.string.phone_number), phone_number);
                        data.put(getString(R.string.profile_img_url), profile_image_url);
                        data.put(getString(R.string.date_created), new Date());

                        db.collection(getString(R.string.users)).document(firebaseUser.getUid())
                                .set(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressBar.setVisibility(View.GONE);
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                        finishAffinity();
                                        Intent intent = new Intent (getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });


                    }else{
                        Log.i(TAG,"An Error Occurred in uploading image" +task.getException().getMessage());
                    }
                }
            });
        }else{
            //adding data to firestore
            Map<String, Object> data = new HashMap<>();
            data.put(getString(R.string.user_id), firebaseUser.getUid());
            data.put(getString(R.string.first_name),first_name);
            data.put(getString(R.string.last_name), last_name);
            data.put(getString(R.string.phone_number), phone_number);
            data.put(getString(R.string.date_created), new Date());


            db.collection(getString(R.string.users)).document(firebaseUser.getUid())
                    .set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                            progressBar.setVisibility(View.GONE);
                            finishAffinity();
                            Intent intent = new Intent (getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private void setProfilePicture(){

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
                       startActivityForResult(Intent.createChooser(intent,""), RESULT_LOAD_IMAGE_MULTIPLE);

               }
               else if (items[item].equals(takePhoto)) {



                       Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                       try {
                           file_camera_uri =  FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", createImageFile());
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                       intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                       intent.putExtra(MediaStore.EXTRA_OUTPUT,file_camera_uri );
                       startActivityForResult(intent, RESULT_CAMERA);
                   }



           }
       });
       builder.show();



   }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE_MULTIPLE && resultCode == Activity.RESULT_OK) {

                if(data.getData()!=null) {

                    Uri mImageUri = data.getData();

                    try {
                        bitmapImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri);
                        img_profile.setImageBitmap(bitmapImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


            }


        }

        if (requestCode == RESULT_CAMERA && resultCode == Activity.RESULT_OK) {

            Log.i(TAG,"Here");
            if(file_camera_uri!=null) {
                Log.i(TAG, "Here 2");
                try {
                    bitmapImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), file_camera_uri);
                    img_profile.setImageBitmap(bitmapImage);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
    }

    private void createPermissionListeners() {

        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {

                if (report.areAllPermissionsGranted()) {
                    setProfilePicture();
                }

                // check for permanent denial of any permission
                if (report.isAnyPermissionPermanentlyDenied()) {
                    Log.i(TAG,"Permissions permanently denied. Open Settings");


                    AlertDialog.Builder builder;
                    AlertDialog alert;

                    builder = new AlertDialog.Builder(CreateProfileActivity.this);
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
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
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

    @Override
    public void onBackPressed() {
        finishAffinity();
        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(listenerRegistration!=null){
            listenerRegistration.remove();
            listenerRegistration=null;
        }
    }
}