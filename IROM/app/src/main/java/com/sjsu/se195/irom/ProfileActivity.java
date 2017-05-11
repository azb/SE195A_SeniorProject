package com.sjsu.se195.irom;

import android.*;
import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot; //added by Arthur
import com.google.firebase.database.DatabaseError; //added by Arthur

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener; //added by Arthur
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sjsu.se195.irom.Classes.Profile;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickClick;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Arthur on 11/9/2016.
 */

public class ProfileActivity extends NavigationDrawerActivity {
    private Button editButton;
    private Button saveButton;
    private TextView userEmailWelcome;
    private ScrollView editForm;
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;
    private EditText mFirstNameEditText;
    private EditText mLastNameEditText;
    private EditText mEmailEditText;
    private TextView mFirstNameTextView;
    private TextView mLastNameTextView;
    private TextView mEmailTextView;
    private ImageView profilePicture;
    private PickImageDialog dialog;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    private static final int GALLERY_PERMISSIONS_REQUEST = 2;
    private static final int CAMERA_IMAGE_REQUEST = 3;
    private static final int CAMERA_PERMISSIONS_REQUEST = 4;
    private static final int CROP_IMAGE = 5;
    private static final String TAG = ProfileActivity.class.getSimpleName();
    private final long ONE_MEGABYTE = 1024 * 1024; // Max image download size to avoid issues
    private Uri currentPhotoURI;
    private StorageReference storageRef;

    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();//added by Arthur



    //private Spinner currency;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //INSTEAD OF setContentView(R.layout.activity_marketplace); USE NEXT 3 LINES IF YOU WANT TH NAV BAR TO WORK
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_profile, null, false);
        drawer.addView(contentView, 0);
        //initialize
        initializeAllTheThings();
        //get current user
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        //get database reference
        storageRef = FirebaseStorage.getInstance().getReference("profile/"+mUser.getUid());

        mDatabase = FirebaseDatabase.getInstance().getReference();
        //get current user to set up welcome
        String profileName = mUser.getEmail() + " profile";
        userEmailWelcome.setText(profileName);

        setupClickListeners();
    }

    private void initializeAllTheThings() {
        editButton = (Button) findViewById(R.id.profile_edit_form_button);
        saveButton = (Button) findViewById(R.id.profile_save_button);
        userEmailWelcome = (TextView) findViewById(R.id.profile_page__user_welcome);
        editForm = (ScrollView) findViewById(R.id.profile_edit_form);

        //Get text input fields IDs
        mLastNameEditText = (EditText) findViewById(R.id.last_name_edit_text);
        mFirstNameEditText = (EditText) findViewById(R.id.first_name_edit_text);
        mEmailEditText = (EditText) findViewById(R.id.email_edit_text);
        //Get text labels IDs
        mFirstNameTextView = (TextView) findViewById(R.id.first_name_label);
        mLastNameTextView = (TextView) findViewById(R.id.last_name_label);
        mEmailTextView = (TextView) findViewById(R.id.email_label);

        profilePicture = (ImageView) findViewById(R.id.profile_picture);
        //set up spinner
//        mCurrencySpinner = (Spinner) findViewById(R.id.currency_spinner);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//                R.array.currency_array, android.R.layout.simple_spinner_item);
//        // Specify the layout to use when the list of choices appears
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        // Apply the adapter to the spinner
//        mCurrencySpinner.setAdapter(adapter);

        mFirstNameTextView.setText("First Name");
        mLastNameTextView.setText("Last Name");
        mEmailTextView.setText("Email");

        //////////////added by Arthur///////////////////////////////////////////////////////////////////////////
        //NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //navigationView.setNavigationItemSelectedListener(this);
        //View header=navigationView.getHeaderView(0);

        usr = FirebaseAuth.getInstance().getCurrentUser();

        if(usr!=null){
            final DatabaseReference userprofileref = database.getReference("profile/"+usr.getUid());
            userprofileref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot ds) {
                    String fn=ds.child("firstName").getValue().toString();
                    String ln=ds.child("lastName").getValue().toString();

                    mFirstNameEditText.setText(fn);
                    mLastNameEditText.setText(ln);

                    storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            profilePicture.setImageBitmap(bmp);
                            navheaderProfilePic.setImageBitmap(bmp);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Something went wrong downloading the image!");
                            // Still want to put the listing up since quite a few still do not have images currently
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        //////////////////////////////////////////////////////////////////////////////////////////
    }

    private void setupClickListeners() {
        //pick a new picture for profile when clicking on the image
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickSetup setup = new PickSetup().setCameraIcon(R.drawable.camera).setGalleryIcon(R.drawable.gallery);
                dialog = PickImageDialog.build(setup).setOnClick(new IPickClick() {
                    @Override
                    public void onGalleryClick() {
                        startGalleryChooser();
                    }

                    @Override
                    public void onCameraClick() {
                        try {
                            startCamera();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                dialog.show(getSupportFragmentManager());
            }
        });

        //Edit Profile Button
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open or close the form.
                if(editForm.getVisibility()==View.INVISIBLE){
                    editForm.setVisibility(View.VISIBLE);
                }else{
                    //set invisible
                    editForm.setVisibility(View.INVISIBLE);
                }
            }
        });

        //Save Changes Button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fn = mFirstNameEditText.getText().toString();
                String ln =  mLastNameEditText.getText().toString();
                Profile profile = new Profile(mUser.getUid(), fn, ln);
                writeProfile(profile);
               // Toast.makeText(ProfileActivity.this, "you clicked save!", Toast.LENGTH_SHORT).show();
                //update labels
                mFirstNameTextView.setText( "first name: " + fn);
                mLastNameTextView.setText("last name: " + ln );
            }

            private void writeProfile(Profile profile) {
                Map<String, Object> data = new HashMap<>();
                data.put(mUser.getUid(),profile);

                mDatabase.child("profile").updateChildren(data);
            }
        });
    }

    private void startGalleryChooser() {

        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            dialog.dismiss();
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, GALLERY_IMAGE_REQUEST);
        }
    }

    private void startCamera() throws IOException {


        if (PermissionUtils.requestPermission(this, CAMERA_PERMISSIONS_REQUEST, android.Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            dialog.dismiss();
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Make sure camera activity available
            if (i.resolveActivity(getPackageManager()) != null) {
                // Create the file
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    // Error in file creation
                    e.printStackTrace();
                }
                // If file created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this, "com.sjsu.se195.irom.com.vansuita.pickimage.provider", photoFile);
                    currentPhotoURI = photoURI;
                    i.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(i, CAMERA_IMAGE_REQUEST);
                } else {
                    Log.d(TAG, "File not created.");
                }
            } else {
                Log.d(TAG, "Not resolving camera activity.");
            }
        } else {
            Log.d(TAG, "Permissions incorrect.");
        }
    }

    private File createImageFile() throws IOException {
        // Create a name first
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timestamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File temp = File.createTempFile(imageFileName, ".jpg", storageDir);


        // Delete file on exit of app so we don't waste user storage
        temp.deleteOnExit();

        return temp;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_IMAGE_REQUEST && data != null ) {

                currentPhotoURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    if(bitmap!=null){
                        profilePicture.setImageBitmap(bitmap);
                        uploadImage(bitmap);
                    }

                } catch (java.io.IOException e) {
                    Log.d(TAG, "Image selection failed: " + e.getMessage());
                }catch (java.lang.NullPointerException e){
                    Log.d(TAG, "image selection failed - null pointer " + e.getMessage());
                }
            }
            if (requestCode == CAMERA_IMAGE_REQUEST) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), currentPhotoURI);

                    /* ExifInterface relies on paths, but paths are heavily discouraged, should use URIs, so lots of weird issues
                    /  http://stackoverflow.com/questions/34696787/a-final-answer-on-how-to-get-exif-data-from-uri
                    /  Apparently, to deal with this, in API 24 and up, Android allowed ExifInterfaces to use InputStreams, which works
                    /  for people with Nougat, but still has a problem for us since we're using content:// URIs for ALL API levels.
                    /  https://android-developers.googleblog.com/2016/12/introducing-the-exifinterface-support-library.html
                    /  Thus, solution from one StackOverflow answer for lower API levels is used */
                    int orientation;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        InputStream in = getContentResolver().openInputStream(currentPhotoURI);
                        ExifInterface ei = new ExifInterface(in);
                        orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                    } else {
                        String path = null;

                        String[] proj = { MediaStore.Images.Media.DATA };
                        Cursor cursor = getApplicationContext().getContentResolver().query(currentPhotoURI, proj, null, null, null);
                        if (cursor.moveToFirst()) {
                            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                            path = cursor.getString(column_index);
                        }
                        cursor.close();

                        ExifInterface ei = new ExifInterface(path);
                        orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                    }

                    switch (orientation) {

                        case ExifInterface.ORIENTATION_ROTATE_90:
                            profilePicture.setImageBitmap(rotateImage(bitmap, 90));
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            profilePicture.setImageBitmap(rotateImage(bitmap, 180));
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            profilePicture.setImageBitmap(rotateImage(bitmap, 270));
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:

                        default:
                            profilePicture.setImageBitmap(bitmap);
                    }

                    uploadImage(bitmap);

                } catch (java.io.IOException e) {
                    Log.d(TAG, "Image selection failed: " + e.getMessage());
                }catch (java.lang.NullPointerException e){
                    Log.d(TAG, "image selection failed - null pointer " + e.getMessage());
                }
            }

        } else {
            Log.d(TAG, "Result is not ok for some reason.");
        }
    }
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
            startGalleryChooser();
        }
        if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
            try {
                startCamera();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void uploadImage(Bitmap b){
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        Bitmap image = b;
        image = scaleBitmapDown(image, 1200);
        image.compress(Bitmap.CompressFormat.JPEG, 93, byteStream);
        byte[] d = byteStream.toByteArray();
        UploadTask uploadTask = storageRef.putBytes(d);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Upload failed
                Toast.makeText(ProfileActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
            }
        });
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Toast.makeText(ProfileActivity.this, "Image upload success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }




}
