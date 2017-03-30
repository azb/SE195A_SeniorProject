package com.sjsu.se195.irom;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;
import android.Manifest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.sjsu.se195.irom.Classes.Item;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This is to test adding an item to the database.
 * Created by Arthur on 11/9/2016.
 */

public class ItemActivity extends NavigationDrawerActivity{
    private static final int GALLERY_IMAGE_REQUEST = 1;
    private static final int GALLERY_PERMISSIONS_REQUEST = 2;
    private static final int CAMERA_IMAGE_REQUEST = 3;
    private static final int CAMERA_PERMISSIONS_REQUEST = 4;
    private static final String TAG = ItemActivity.class.getSimpleName();
    private Uri currentPhotoURI;
    private ScrollView manAddItemForm;
    private EditText mName;
    private EditText mQuantity;
    private EditText mNotes;
    private DatabaseReference mDatabaseRef;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // INSTEAD OF setContentView(R.layout.activity_welcome); USE NEXT 3 LINES IF YOU WANT TH NAV BAR TO WORK
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_item, null, false);
        drawer.addView(contentView, 0);

        // Initialize add item buttons and input fields
        manAddItemForm = (ScrollView) findViewById(R.id.manually_add_item_form);
        Button submitItemButton = (Button) findViewById(R.id.submit_item_button);
        mName = (EditText) findViewById(R.id.item_name_text);
        mQuantity = (EditText) findViewById(R.id.item_quantity_text);
        mNotes = (EditText) findViewById(R.id.item_note_text);
        FloatingActionButton cameraItemButton = (FloatingActionButton) findViewById(R.id.add_item_from_camera_button);
        FloatingActionButton galleryItemButton = (FloatingActionButton) findViewById(R.id.add_item_from_gallery_button);

        // Get current user
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        // Get database
        final FirebaseDatabase db = FirebaseDatabase.getInstance();
        // Get ref
        mDatabaseRef = db.getReference("items");
        Query myItemsQuery = mDatabaseRef.equalTo(mUser.getUid());
        System.out.println(myItemsQuery.toString());

        // Button click functions
        submitItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check fields
                if(validityCheck()){
                    // Manually create items
                    Item newItem = new Item(
                            mUser.getUid(),
                            new Date(),
                            mName.getText().toString(),Integer.parseInt(mQuantity.getText().toString()),
                            mNotes.getText().toString());
                    writeNewManualItem(newItem);
                    Toast.makeText(ItemActivity.this, "You made an item", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(ItemActivity.this, "something wasnt filled. something broke", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cameraItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startCamera();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        galleryItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGalleryChooser();
            }
        });
    }

    public File createImageFile() throws IOException {
        // Create a name first
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timestamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(imageFileName, ".jpg", storageDir);
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

    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, GALLERY_IMAGE_REQUEST);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_IMAGE_REQUEST && data != null) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                try (Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null)) {
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);

                        ImageView imageView = (ImageView) findViewById(R.id.imageView2);
                        imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                        manAddItemForm.setVisibility(View.VISIBLE);
                    }
                } catch (java.lang.NullPointerException e) {
                    Log.d(TAG, "Null pointer exception with local image: " + e.getMessage());
                }
            }
            if (requestCode == CAMERA_IMAGE_REQUEST) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), currentPhotoURI);
                    ImageView imageView = (ImageView) findViewById(R.id.imageView2);
                    imageView.setImageBitmap(bitmap);
                    manAddItemForm.setVisibility(View.VISIBLE);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.loading_text);
                    AlertDialog dialogue = builder.create();
                    dialogue.show();
                } catch (java.io.IOException e) {
                    Log.d(TAG, "Image selection failed: " + e.getMessage());
                }
            }
        } else {
            Log.d(TAG, "Result is not ok for some reason.");
        }
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

    private void writeNewManualItem(Item i) {
        String key = mDatabaseRef.child("items").push().getKey();
        i.setItemID(key);
        mDatabaseRef.child(key).setValue(i);
        //updateChildren(data) is for updating an item
    }

    private Boolean validityCheck() {
        if (mName==null || mQuantity == null || mNotes == null){
            mNotes.setError("fill all fields");
            return false;
        }
        return true;
    }
}
