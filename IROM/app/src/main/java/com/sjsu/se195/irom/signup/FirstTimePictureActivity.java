package com.sjsu.se195.irom.signup;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.io.File;
import java.io.IOException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sjsu.se195.irom.PermissionUtils;
import com.sjsu.se195.irom.R;
import com.sjsu.se195.irom.WelcomeActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Krystle on 4/10/2017.
 */

public class FirstTimePictureActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private Button yesButton;
    private Button noButton;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    private static final int GALLERY_PERMISSIONS_REQUEST = 2;
    private static final int CAMERA_IMAGE_REQUEST = 3;
    private static final int CAMERA_PERMISSIONS_REQUEST = 4;
    private static final String TAG ="firsttimephoto";
    private Uri currentPhotoURI;

    //ask the user to take a picture for their profile here!
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        setContentView(R.layout.activity_first_time_picture);

        setupButton();
    }



    private void setupButton() {
        yesButton = (Button) findViewById(R.id.yespleasebutton);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    startCamera();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        noButton = (Button) findViewById(R.id.skip_button);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getBaseContext(), WelcomeActivity.class);
                startActivity(i);
            }
        });

    }


    public void startCamera() throws IOException {
        if (PermissionUtils.requestPermission(this, CAMERA_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Make sure camera activity available
            if (intent.resolveActivity(getPackageManager()) != null) {
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
                    Uri photoURI = FileProvider.getUriForFile(this, "com.sjsu.se195.irom.fileprovider", photoFile);
                    currentPhotoURI = photoURI;
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
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

    public File createImageFile() throws IOException {
        // Create a name first
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timestamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
}
