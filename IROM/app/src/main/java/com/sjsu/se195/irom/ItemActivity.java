package com.sjsu.se195.irom;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.Manifest;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.sjsu.se195.irom.Classes.Item;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private static final String CLOUD_VISION_API_KEY = "AIzaSyAHnhDlz-V1OTUivtflxsQwFShuAzeh-6w";
    private Uri currentPhotoURI;
    private RelativeLayout manAddItemForm;
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
        manAddItemForm = (RelativeLayout) findViewById(R.id.addItemForm);
        Button submitItemButton = (Button) findViewById(R.id.submitButton);
        mName = (EditText) findViewById(R.id.nameText);
        mQuantity = (EditText) findViewById(R.id.quantityText);
        mNotes = (EditText) findViewById(R.id.notesText);
        Button cameraButton = (Button) findViewById(R.id.cameraButton);
        Button galleryButton = (Button) findViewById(R.id.galleryButton);
        Button cloudVisionButton = (Button) findViewById(R.id.cloudVisionButton);

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
                if(validityCheck()) {
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
                    Toast.makeText(ItemActivity.this, "Something wasn't filled. Something broke", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startCamera();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGalleryChooser();
            }
        });

        cloudVisionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create a name first
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timestamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void startCamera() throws IOException {
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

    private void startGalleryChooser() {
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

                        ImageView imageHolder = (ImageView) findViewById(R.id.imageHolder);
                        imageHolder.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                        manAddItemForm.setVisibility(View.VISIBLE);
                    }
                } catch (java.lang.NullPointerException e) {
                    Log.d(TAG, "Null pointer exception with local image: " + e.getMessage());
                }
            }
            if (requestCode == CAMERA_IMAGE_REQUEST) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), currentPhotoURI);
                    ImageView imageHolder = (ImageView) findViewById(R.id.imageHolder);
                    imageHolder.setImageBitmap(bitmap);
                    manAddItemForm.setVisibility(View.VISIBLE);
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

    public void uploadImage() {
        try {
            ImageView imageView = (ImageView) findViewById(R.id.imageHolder);
            Drawable drawable = imageView.getDrawable();
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            bitmap = scaleBitmapDown(bitmap, 1200);

            callCloudVision(bitmap);
        } catch (IOException e) {
            Log.d(TAG, "Image picking failed because " + e.getMessage());
        }
    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Show loading
        Toast.makeText(ItemActivity.this, R.string.loading_text, Toast.LENGTH_SHORT).show();

        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(new VisionRequestInitializer(CLOUD_VISION_API_KEY));
                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add image
                        Image base64EncodedImage = new Image();
                        // Convert to JPEG
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Encode JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // Add features we want from Cloud Vision Request
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("LABEL_DETECTION");
                            labelDetection.setMaxResults(10);
                            Feature logoDetection = new Feature();
                            logoDetection.setType("LOGO_DETECTION");
                            logoDetection.setMaxResults(10);
                            Feature textDetection = new Feature();
                            textDetection.setType("TEXT_DETECTION");
                            textDetection.setMaxResults(10);
                            add(labelDetection);
                            add(logoDetection);
                            add(textDetection);
                        }});

                        // Add image list to request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);

                    // Protect against Vision API GZip bug
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "Created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);
                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "Failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "Failed to make API request because of other exception " + e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                Toast.makeText(ItemActivity.this, "Successful run!", Toast.LENGTH_SHORT).show();
            }
        }.execute();
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

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "I found these things:\n\n";

        message += "Labels:\n";
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message += String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription());
                message += "\n";
            }
        } else {
            message += "Nothing\n";
        }

        message += "\nLogos:\n";
        List<EntityAnnotation> logos = response.getResponses().get(0).getLogoAnnotations();
        if (logos != null) {
            for (EntityAnnotation logo : logos) {
                message += String.format(Locale.US, "%.3f: %s", logo.getScore(), logo.getDescription());
                message += "\n";
            }
        } else {
            message += "Nothing\n";
        }

        message += "\nTexts:\n";
        List<EntityAnnotation> texts = response.getResponses().get(0).getTextAnnotations();
        if (texts != null) {
            for (EntityAnnotation text : texts) {
                message += String.format(Locale.US, "%.3f: %s", text.getScore(), text.getDescription());
                message += "\n";
            }
        } else {
            message += "Nothing";
        }

        return message;
    }

    private void writeNewManualItem(Item i) {
        String key = mDatabaseRef.child("items").push().getKey();
        System.out.println(i.toAllString());
        mDatabaseRef.child(key).setValue(i);
        //updateChildren(data) is for updating an item
    }

    private Boolean validityCheck() {
        if (mName==null || mQuantity == null || mNotes == null) {
            mNotes.setError("fill all fields");
            return false;
        }
        return true;
    }
}
