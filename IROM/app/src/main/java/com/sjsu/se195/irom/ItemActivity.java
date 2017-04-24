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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.api.services.vision.v1.model.WebDetection;
import com.google.api.services.vision.v1.model.WebEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sjsu.se195.irom.Classes.IROMazon;
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
    private DatabaseReference mItemDatabaseRef;
    private StorageReference mStorageRef;
    private DatabaseReference mIROMazonDatabaseRef;
    private FirebaseUser mUser;
    private ArrayList<IROMazon> IROMazonList;
    private ArrayList<IROMazon> entityR;
    private ArrayList<IROMazon> textR;

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
        ImageView imageHolder = (ImageView) findViewById(R.id.imageHolder);

        // Get current user
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        // Get database
        final FirebaseDatabase db = FirebaseDatabase.getInstance();
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        // Get ref
        mItemDatabaseRef = db.getReference("items");
        mStorageRef = storage.getReference("items/");
        mIROMazonDatabaseRef = db.getReference("IROMazon");

        // Fill out fields if coming from IROMazon search activity
        Intent i = getIntent();
        if (i.hasExtra("IROMazon")) {
            Bitmap image = i.getParcelableExtra("image");
            IROMazon iromazon = i.getParcelableExtra("IROMazon");
            imageHolder.setImageBitmap(image);
            mName.setText(iromazon.name);
        }

        // Get IROMazon data
        IROMazonList = new ArrayList<>();
        mIROMazonDatabaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Get each entry and store in the list
                IROMazon entry = dataSnapshot.getValue(IROMazon.class);
                IROMazonList.add(entry);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        // Button click functions
        submitItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check fields
                if(validityCheck()) {
                    // Create the item
                    Item newItem = new Item(
                            mUser.getUid(),
                            new Date(),
                            mName.getText().toString(),
                            Integer.parseInt(mQuantity.getText().toString()),
                            mNotes.getText().toString());
                    writeNewItemAndImage(newItem);
                }
                else{
                    Toast.makeText(ItemActivity.this, "Something wasn't filled", Toast.LENGTH_SHORT).show();
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
                            Feature imageProperties = new Feature();
                            imageProperties.setType("IMAGE_PROPERTIES");
                            imageProperties.setMaxResults(10);
                            Feature webEntities = new Feature();
                            webEntities.setType("WEB_DETECTION");
                            webEntities.setMaxResults(10);
                            add(labelDetection);
                            add(logoDetection);
                            add(textDetection);
                            add(imageProperties);
                            add(webEntities);

                        }});

                        // Add image list to request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);

                    // Protect against Vision API GZip bug
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "Created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();

                    // Use response to compare with IROMazon data
                    entityR = getIROMazon_Entity(response);
                    textR = getIROMazon_Text(response);
                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "Failed to make API request because: " + e.getContent());
                    return "Failed";
                } catch (IOException e) {
                    Log.d(TAG, "Failed to make API request because of other exception: " + e.getMessage());
                    return "Failed";
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                if (result != null) {
                    // Use result of comparison with IROMazon data to update field with suggested data
                    if (entityR != null && entityR.size() > 0) {
                        mName.setText(entityR.get(0).name);
                    } else if (textR != null && textR.size() > 0) {
                        mName.setText(textR.get(0).name);
                    } else {
                        Toast.makeText(ItemActivity.this, "No matches found in database", Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(ItemActivity.this, "Cloud Vision Request complete", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ItemActivity.this, "Cloud Vision Timeout", Toast.LENGTH_SHORT).show();
                }
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

    /**
     * Get any existing IROMazon data that matches the current Cloud Vision response web entities data
     * @param response Cloud Vision response data
     * @return List of matching IROMazon data
     */
    private ArrayList<IROMazon> getIROMazon_Entity(BatchAnnotateImagesResponse response) {
        ArrayList<IROMazon> result = new ArrayList<>();
        ArrayList<String> entityResults = new ArrayList<>();
        WebDetection webDetection = response.getResponses().get(0).getWebDetection();

        // Store Cloud Vision Web Entity Results into ArrayList<String>. Only return results with Score > 0.5
        if (webDetection != null) {
            for (WebEntity entity : webDetection.getWebEntities()) {
                if (entity.getDescription() != null && entity.getScore() >= 0.5) {
                    entityResults.add(entity.getDescription());
                }
            }

            for (IROMazon storedData : IROMazonList) {
                for (String newData : entityResults) {
                    if (storedData.name.equals(newData)) {
                        result.add(storedData);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Get any existing IROMazon data that matches the current Cloud Vision response text data
     * @param response Cloud Vision response data
     * @return List of matching IROMazon objects
     */
    private ArrayList<IROMazon> getIROMazon_Text(BatchAnnotateImagesResponse response) {
        ArrayList<IROMazon> result = new ArrayList<>();
        ArrayList<String> textResults = new ArrayList<>();
        List<EntityAnnotation> texts = response.getResponses().get(0).getTextAnnotations();

        if (texts != null) {
            for (EntityAnnotation text : texts) {
                if (text.getDescription() != null) {
                    textResults.add(text.getDescription());
                }
            }

            for (IROMazon storedData : IROMazonList) {
                if (storedData.text != null && getIROMazon_TLLScore(textResults, storedData.text) >= 0.5) {
                    result.add(storedData);
                }
            }
        }

        return result;
    }

    private double getIROMazon_TLLScore(ArrayList<String> a, ArrayList<String> b) {
        int found = 0;
        if(a.size() > 0 && b.size() > 0) {
            for(String text_b : b) {
                for (String text_a : a) {
                    //System.out.println("Checking Text/Label/Logo ");
                    //System.out.println(text_a);
                    if (text_b.contains(text_a)) {
                        //System.out.println("Found Text/Label/Logo ");
                        //System.out.println(text_a);
                        found += 1;
                    }
                }
            }
            //System.out.println("Score: ");
            //System.out.println(((double)found)/(((double)a.size())));
            return ((double) found) / (((double) a.size()));
        }
        else{
            return 0;
        }
    }

    private void writeNewItemAndImage(Item i) {
        // Upload item
        final String key = mItemDatabaseRef.child("items").push().getKey();
        i.setItemID(key);
        mItemDatabaseRef.child(key).setValue(i);

        // Upload image
        StorageReference imageUpload = mStorageRef.child(key);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        Bitmap image = ((BitmapDrawable) ((ImageView) findViewById(R.id.imageHolder)).getDrawable()).getBitmap();
        image = scaleBitmapDown(image, 1200);
        image.compress(Bitmap.CompressFormat.JPEG, 93, byteStream);
        byte[] data = byteStream.toByteArray();
        UploadTask uploadTask = imageUpload.putBytes(data);
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
            }
        });
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // On failure, do not want to keep item since we expect image for every item
                mItemDatabaseRef.child(key).removeValue();
                Toast.makeText(ItemActivity.this, "Image upload failed, item removed", Toast.LENGTH_SHORT).show();
            }
        });
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(ItemActivity.this, "Success!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Boolean validityCheck() {
        return individualCheck(mName) && individualCheck(mQuantity) && individualCheck(mNotes);
    }

    private Boolean individualCheck(EditText e) {
        if (TextUtils.isEmpty(e.getText().toString())) { // If field empty, return false and set error
            e.setError("Field cannot be empty.");
            return false;
        }
        return true; // Else true
    }
}
