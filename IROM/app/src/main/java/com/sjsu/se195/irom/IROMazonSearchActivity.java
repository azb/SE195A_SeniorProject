package com.sjsu.se195.irom;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sjsu.se195.irom.Classes.IROMazon;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickClick;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Main activity for handling Cloud Vision data
 */

public class IROMazonSearchActivity extends NavigationDrawerActivity {
    private static final int GALLERY_IMAGE_REQUEST = 1;
    private static final int GALLERY_PERMISSIONS_REQUEST = 2;
    private static final int CAMERA_IMAGE_REQUEST = 3;
    private static final int CAMERA_PERMISSIONS_REQUEST = 4;
    private static final String TAG = IROMazonSearchActivity.class.getSimpleName();
    private static final String CLOUD_VISION_API_KEY = "AIzaSyAHnhDlz-V1OTUivtflxsQwFShuAzeh-6w";
    private Uri currentPhotoURI;
    private File currentPhotoFile;
    private final long ONE_MEGABYTE = 1024 * 1024; // Max image download size to avoid issues
    private DatabaseReference IROMazonDatabaseRef;
    private StorageReference storageRef;
    private ArrayList<IROMazon> IROMazonList;
    private ArrayList<IROMazonImage> entityR;
    private ArrayList<IROMazonImage> textR;
    private ArrayList<IROMazonImage> logolabelR;
    private ArrayList<IROMazonImage> labelR;
    private ArrayList<IROMazonImage> IROMazonImageList = new ArrayList<>();
    private IROMazonAdapter iromazonAdapter;
    private ArrayList<ArrayList<String>> IROMazonStringLists;
    private PickImageDialog dialog;
    private ProgressDialog progressDialog;
    private int currentLoadedCount;
    private int totalToLoadCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_iromazonsearch, null, false);
        drawer.addView(contentView, 0);

        // Layout items
        ImageView addImageView = (ImageView) findViewById(R.id.imageHolder);
        final Button submitIROMazonButton = (Button) findViewById(R.id.submitIROMazon);
        final EditText submitIROMazonName = (EditText) findViewById(R.id.nameText);
        final EditText submitIROMazonDescription = (EditText) findViewById(R.id.descriptionText);
        final EditText submitIROMazonPrice = (EditText) findViewById(R.id.priceText);
        RecyclerView IROMazonRecyclerView = (RecyclerView) findViewById(R.id.IROMazon_recycler_view);
        //dialog setup for choosing gallery/camera
        PickSetup setup = new PickSetup().setCameraIcon(R.drawable.camera).setGalleryIcon(R.drawable.gallery);
        dialog = PickImageDialog.build(setup).setOnClick(new IPickClick() {
            @Override
            public void onGalleryClick() {
                startGalleryChooser();
                submitIROMazonButton.setEnabled(false);
                submitIROMazonButton.setBackground(ContextCompat.getDrawable(getBaseContext(),R.drawable.disabled_green_button_shape));

            }

            @Override
            public void onCameraClick() {
                try {
                    startCamera();
                    submitIROMazonButton.setEnabled(false);
                    submitIROMazonButton.setBackground(ContextCompat.getDrawable(getBaseContext(),R.drawable.disabled_green_button_shape));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Firebase references
        IROMazonDatabaseRef = FirebaseDatabase.getInstance().getReference("IROMazon");
        storageRef = FirebaseStorage.getInstance().getReference("IROMazon/");

        // Button listeners
        addImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               dialog.show(getSupportFragmentManager());
            }
        });

        // Progress dialogue setup
        progressDialog = new ProgressDialog(IROMazonSearchActivity.this, R.style.AppTheme_Dialog);
        progressDialog.setIndeterminate(true);

        submitIROMazonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check to see if a name first
                if (!(TextUtils.isEmpty(submitIROMazonName.getText()) && TextUtils.isEmpty(submitIROMazonDescription.getText())
                        && TextUtils.isEmpty(submitIROMazonPrice.getText()))) {
                    createIROMazonEntry();
                } else {
                    Toast.makeText(IROMazonSearchActivity.this, "No name provided", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up adapter for RecyclerView
        IROMazonRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        iromazonAdapter = new IROMazonAdapter(IROMazonImageList, new OnItemClickListener() {
            @Override
            public void onItemClick(IROMazonImage itemimage) {
                // Move to add item activity
                Intent i = new Intent(IROMazonSearchActivity.this, ItemActivity.class);
                // Create bundle to hold everything
                Bundle b = new Bundle();
                b.putParcelable("IROMazon", itemimage.iromazon);
                // Include image URI for ItemActivity
                b.putParcelable("imageURI", currentPhotoURI);
                // Add into intent
                i.putExtras(b);
                startActivity(i);
            }
        });
        IROMazonRecyclerView.setAdapter(iromazonAdapter);

        // Get IROMazon data
        IROMazonList = new ArrayList<>();
        IROMazonDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot IROMazonSnapshot : dataSnapshot.getChildren()) {
                    IROMazon temp = IROMazonSnapshot.getValue(IROMazon.class);
                    if (temp.key == null) { // Fill out key for entries created without it
                        temp.key = IROMazonSnapshot.getKey();
                    }
                    IROMazonList.add(temp);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Get entries failed, log a message
                Toast.makeText(IROMazonSearchActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        // Delete temp image file if not already deleted when this activity is fully killed
        if (currentPhotoFile != null && currentPhotoFile.exists()) {
            try {
                if (currentPhotoFile.delete()) {
                    Log.d(TAG, "Temp file deleted");
                }
            } catch (Exception e) {
                Log.d(TAG, "Error deleting file: " + e);
            }
        }

        super.onDestroy();
    }

    private void searchIROMazon() {
        progressDialog.setMessage("Searching...");
        progressDialog.show();

        // Reset recycler view
        IROMazonImageList = new ArrayList<>();
        iromazonAdapter.mList = IROMazonImageList;
        iromazonAdapter.notifyDataSetChanged();

        uploadImage();
    }

    private void createIROMazonEntry() {
        // Create IROMazon object
        EditText IROMazonName = (EditText) findViewById(R.id.nameText);
        EditText IROMazonDescription = (EditText) findViewById(R.id.descriptionText);
        EditText IROMazonPrice = (EditText) findViewById(R.id.priceText);
        final IROMazon newEntry = new IROMazon(IROMazonName.getText().toString(), IROMazonStringLists.get(0), IROMazonStringLists.get(1),
                                               IROMazonStringLists.get(2), IROMazonStringLists.get(3), Double.parseDouble(IROMazonPrice.getText().toString()));
        newEntry.description = IROMazonDescription.getText().toString();

        // Get key for entry
        final String key = IROMazonDatabaseRef.child("IROMazon").push().getKey();
        newEntry.key = key;

        // Handle image upload first
        StorageReference imageUpload = storageRef.child(key);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        Bitmap image = ((BitmapDrawable) ((ImageView) findViewById(R.id.imageHolder)).getDrawable()).getBitmap();
        image = scaleBitmapDown(image, 1200);
        image.compress(Bitmap.CompressFormat.JPEG, 93, byteStream);
        byte[] data = byteStream.toByteArray();
        UploadTask uploadTask = imageUpload.putBytes(data);

        progressDialog.setMessage("Uploading...");
        progressDialog.show();
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Upload failed
                progressDialog.dismiss();
                Toast.makeText(IROMazonSearchActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
            }
        });
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Upload successful, can add to database as well
                progressDialog.dismiss();
                IROMazonDatabaseRef.child(key).setValue(newEntry);
                Toast.makeText(IROMazonSearchActivity.this, "Successfully uploaded to database!", Toast.LENGTH_SHORT).show();
                // Move to add item activity
                Intent i = new Intent(IROMazonSearchActivity.this, ItemActivity.class);
                // Create bundle to hold everything
                Bundle b = new Bundle();
                b.putParcelable("IROMazon", newEntry);
                // Include image URI for ItemActivity
                b.putParcelable("imageURI", currentPhotoURI);
                // Add into intent
                i.putExtras(b);
                startActivity(i);
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create a name first
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timestamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Delete existing file if one already present
        if (currentPhotoFile != null && currentPhotoFile.exists()) {
            try {
                if (currentPhotoFile.delete()) {
                    Log.d(TAG, "Temp file deleted");
                }
            } catch (Exception e) {
                Log.d(TAG, "Error deleting file: " + e);
            }
        }

        // Create new file
        currentPhotoFile = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Set this up just in case? But apparently doesn't ensure files are actually deleted due to how
        // apps work with VM termination
        // https://developer.android.com/reference/java/io/File.html#deleteOnExit()
        currentPhotoFile.deleteOnExit();

        return currentPhotoFile;
    }

    private void startCamera() throws IOException {
        if (PermissionUtils.requestPermission(this, CAMERA_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            dialog.dismiss();

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
                    Uri photoURI = FileProvider.getUriForFile(this, "com.sjsu.se195.irom.com.vansuita.pickimage.provider", photoFile);
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
            dialog.dismiss();
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, GALLERY_IMAGE_REQUEST);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_IMAGE_REQUEST && data != null) {
                currentPhotoURI = data.getData();
                try {
                    // Set image to ImageView
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), currentPhotoURI);
                    ImageView imageHolder = (ImageView) findViewById(R.id.imageHolder);

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
                            imageHolder.setImageBitmap(rotateImage(bitmap, 90));
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            imageHolder.setImageBitmap(rotateImage(bitmap, 180));
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            imageHolder.setImageBitmap(rotateImage(bitmap, 270));
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:

                        default:
                            imageHolder.setImageBitmap(bitmap);
                    }

                    // Search IROMazon database automatically
                    searchIROMazon();
                } catch (java.io.IOException e) {
                    Log.d(TAG, "Image selection failed: " + e.getMessage());
                }
            }
            if (requestCode == CAMERA_IMAGE_REQUEST) {
                try {
                    // Set image to ImageView
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), currentPhotoURI);
                    ImageView imageHolder = (ImageView) findViewById(R.id.imageHolder);

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
                            imageHolder.setImageBitmap(rotateImage(bitmap, 90));
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            imageHolder.setImageBitmap(rotateImage(bitmap, 180));
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            imageHolder.setImageBitmap(rotateImage(bitmap, 270));
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:

                        default:
                            imageHolder.setImageBitmap(bitmap);
                    }

                    // Search IROMazon database automatically
                    searchIROMazon();
                } catch (java.io.IOException e) {
                    Log.d(TAG, "Image selection failed: " + e.getMessage());
                }
            }
        } else {
            Log.d(TAG, "Result is not ok for some reason.");
        }
    }

    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private void uploadImage() {
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
                    logolabelR = getIROMazon_Logo(response);
                    labelR = getIROMazon_Label(response);

                    // Get data in case creating new IROMazon entry
                    convertResponseToString(response);
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
                    // Use result of comparison with IROMazon data to update field with potential matches
                    ArrayList<IROMazonImage> potentialMatches = new ArrayList<>();

                    // Loop through each result list, avoiding duplicates
                    for (IROMazonImage current : entityR) {
                        if (!potentialMatches.contains(current)) {
                            potentialMatches.add(current);
                            Log.d(TAG, "Added in entityR: " + current.iromazon.name);
                        } else {
                            // If already in the matches list, check if this has higher score and update so it shows higher
                            IROMazonImage existingMatch = potentialMatches.get(potentialMatches.indexOf(current));
                            if (existingMatch.score < current.score) {
                                existingMatch.score = current.score;
                            }
                        }
                    }
                    for (IROMazonImage current : textR) {
                        if (!potentialMatches.contains(current)) {
                            potentialMatches.add(current);
                            Log.d(TAG, "Added in textR: " + current.iromazon.name);
                        } else {
                            // If already in the matches list, check if this has higher score and update so it shows higher
                            IROMazonImage existingMatch = potentialMatches.get(potentialMatches.indexOf(current));
                            if (existingMatch.score < current.score) {
                                existingMatch.score = current.score;
                            }
                        }
                    }
                    for (IROMazonImage current : logolabelR) {
                        if (!potentialMatches.contains(current)) {
                            potentialMatches.add(current);
                            Log.d(TAG, "Added in logolabelR: " + current.iromazon.name);
                        } else {
                            // If already in the matches list, check if this has higher score and update so it shows higher
                            IROMazonImage existingMatch = potentialMatches.get(potentialMatches.indexOf(current));
                            if (existingMatch.score < current.score) {
                                existingMatch.score = current.score;
                            }
                        }
                    }
                    for (IROMazonImage current : labelR) {
                        if (!potentialMatches.contains(current)) {
                            potentialMatches.add(current);
                            Log.d(TAG, "Added in labelR: " + current.iromazon.name);
                        } else {
                            // If already in the matches list, check if this has higher score and update so it shows higher
                            IROMazonImage existingMatch = potentialMatches.get(potentialMatches.indexOf(current));
                            if (existingMatch.score < current.score) {
                                existingMatch.score = current.score;
                            }
                        }
                    }

                    // Handle case that there were no matches found, no items added to ArrayList
                    if (potentialMatches.size() == 0) {
                        // Remove dialog
                        progressDialog.dismiss();

                        Toast.makeText(IROMazonSearchActivity.this, "No matches found in database", Toast.LENGTH_SHORT).show();
                    } else {
                        currentLoadedCount = 0;
                        totalToLoadCount = potentialMatches.size();
                        getImages(potentialMatches);
                    }

                    findViewById(R.id.submitIROMazon).setEnabled(true);
                    findViewById(R.id.submitIROMazon).setBackground(ContextCompat.getDrawable(getBaseContext(),R.drawable.green_button_shape));
                } else {
                    Toast.makeText(IROMazonSearchActivity.this, "Cloud Vision Timeout", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void convertResponseToString(BatchAnnotateImagesResponse response) {
        ArrayList<String> labelResults = new ArrayList<>();
        ArrayList<String> logoResults = new ArrayList<>();
        ArrayList<String> textResults = new ArrayList<>();
        ArrayList<String> entityResults = new ArrayList<>();

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                if(label.getDescription() != null){
                    labelResults.add(label.getDescription());
                }
            }
        }

        List<EntityAnnotation> logos = response.getResponses().get(0).getLogoAnnotations();
        if (logos != null) {
            for (EntityAnnotation logo : logos) {
                if(logo.getDescription() != null){
                    logoResults.add(logo.getDescription());
                }
            }
        }

        List<EntityAnnotation> texts = response.getResponses().get(0).getTextAnnotations();
        if (texts != null) {
            for (EntityAnnotation text : texts) {
                if(text.getDescription() != null){
                    textResults.add(text.getDescription());
                }
            }
        }

        WebDetection webDetection = response.getResponses().get(0).getWebDetection();
        if(webDetection != null){
            for(WebEntity entity : webDetection.getWebEntities()){
                if(entity.getDescription() != null){
                    entityResults.add(entity.getDescription());
                }
            }
        }

        // Store for if used to create a new IROMazon entry
        IROMazonStringLists = new ArrayList<>();
        IROMazonStringLists.add(entityResults);
        IROMazonStringLists.add(textResults);
        IROMazonStringLists.add(logoResults);
        IROMazonStringLists.add(labelResults);
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
    private ArrayList<IROMazonImage> getIROMazon_Entity(BatchAnnotateImagesResponse response) {
        ArrayList<IROMazonImage> result = new ArrayList<>();
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
                if (storedData.entity.size() != 0) {
                    double score = getIROMazon_TLLScore(entityResults, storedData.entity);
                    if (score >= 0.5) {
                        result.add(new IROMazonImage(storedData, score));
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
    private ArrayList<IROMazonImage> getIROMazon_Text(BatchAnnotateImagesResponse response) {
        ArrayList<IROMazonImage> result = new ArrayList<>();
        ArrayList<String> textResults = new ArrayList<>();
        List<EntityAnnotation> texts = response.getResponses().get(0).getTextAnnotations();

        if (texts != null) {
            for (EntityAnnotation text : texts) {
                if (text.getDescription() != null) {
                    textResults.add(text.getDescription());
                }
            }

            for (IROMazon storedData : IROMazonList) {
                if (storedData.text.size() != 0) {
                    double score = getIROMazon_TLLScore(textResults, storedData.text);
                    if (score >= 0.5) {
                        result.add(new IROMazonImage(storedData, score));
                    }
                }
            }
        }

        return result;
    }

    private ArrayList<IROMazonImage> getIROMazon_Logo(BatchAnnotateImagesResponse response){
        ArrayList<IROMazonImage> result = new ArrayList<>();
        ArrayList<String> logoResults = new ArrayList<>();
        ArrayList<String> labelResults = new ArrayList<>();
        List<EntityAnnotation> logos = response.getResponses().get(0).getLogoAnnotations();
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();

        if (logos != null) {
            for (EntityAnnotation logo : logos) {
                if(logo.getDescription() != null) {
                    logoResults.add(logo.getDescription());
                }
            }

            if (labels != null) {
                for (EntityAnnotation label : labels) {
                    if(label.getDescription() != null) {
                        logoResults.add(label.getDescription());
                    }
                }

                for (IROMazon storedData : IROMazonList) {
                    if (storedData.logo.size() != 0) {
                        double score = getIROMazon_TLLScore(logoResults, storedData.logo);
                        if (score >= 0.5) {
                            if (storedData.label.size() != 0 && getIROMazon_TLLScore(labelResults, storedData.label) >= 0.5) {
                                result.add(new IROMazonImage(storedData, score));
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    private ArrayList<IROMazonImage> getIROMazon_Label(BatchAnnotateImagesResponse response){
        ArrayList<IROMazonImage> result = new ArrayList<>();
        ArrayList<String> labelResults = new ArrayList<>();
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();

        if (labels != null) {
            for (EntityAnnotation label : labels) {
                if(label.getDescription() != null) {
                    labelResults.add(label.getDescription());
                }
            }

            for (IROMazon storedData : IROMazonList) {
                if (storedData.label.size() != 0) {
                    double score = getIROMazon_TLLScore(labelResults, storedData.label);
                    if (score >= 0.5) {
                        result.add(new IROMazonImage(storedData, score));
                    }
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
                    if (text_b.contains(text_a)) {
                        found += 1;
                    }
                }
            }
            return ((double) found) / ((double) a.size());
        }
        else{
            return 0;
        }
    }

    private void getImages(ArrayList<IROMazonImage> IROMazonList) {
        // Get image for each IROMazon match, add to the RecyclerView
        for (final IROMazonImage potentialMatch : IROMazonList) {
            // Set up the storage ref
            StorageReference imageRef = storageRef.child(potentialMatch.iromazon.key);
            Log.d(TAG, "ImageRef: " + imageRef.toString());

            // Get the image
            imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    bmp = Bitmap.createScaledBitmap(bmp, (bmp.getWidth() / 4), (bmp.getHeight() / 4), true);

                    // Update RecyclerView
                    potentialMatch.image = bmp;
                    IROMazonImageList.add(potentialMatch);
                    currentLoadedCount++;
                    onLoadComplete();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "Something went wrong downloading the image!");
                }
            });
        }
    }

    private void onLoadComplete() {
        if (currentLoadedCount == totalToLoadCount) {
            // First sort so highest scoring items should be at the top
            Collections.sort(IROMazonImageList, new Comparator<IROMazonImage>() {
                @Override
                public int compare(IROMazonImage o1, IROMazonImage o2) {
                    Log.d(TAG, "o1 (" + o1.iromazon.name + ") score: " + o1.score);
                    Log.d(TAG, "o2 (" + o2.iromazon.name + ") score: " + o2.score);
                    if (o1.score > o2.score) {
                        return -1;
                    } else if (o2.score > o1.score) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
            // Update adapter
            iromazonAdapter.mList = IROMazonImageList;
            iromazonAdapter.notifyDataSetChanged();

            // Remove dialog
            progressDialog.dismiss();
        }
    }

    private class IROMazonImage {
        IROMazon iromazon;
        Bitmap image;
        double score;

        IROMazonImage(IROMazon iromazon, double score) {
            this.iromazon = iromazon;
            this.score = score;
        }

        IROMazonImage(IROMazon iromazon, Bitmap image) {
            this.iromazon = iromazon;
            this.image = image;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof IROMazonImage) && (this.iromazon.equals(((IROMazonImage) obj).iromazon));
        }
    }

    private class IROMazonHolder extends RecyclerView.ViewHolder {
        IROMazon iromazon;
        Bitmap image;

        //set up layout of each thing on a row for an item
        ImageView IROMazonImage;
        TextView IROMazonName;
        TextView IROMazonDescription;
        TextView IROMazonPrice;


        IROMazonHolder(View IROMazonView) {
            super(IROMazonView);

            // Initialize name/q/forsale status/item image
            IROMazonImage = (ImageView) IROMazonView.findViewById(R.id.IROMazon_list_item_image);
            IROMazonName = (TextView) IROMazonView.findViewById(R.id.IROMazon_list_item_name);
            IROMazonDescription = (TextView) IROMazonView.findViewById(R.id.IROMazon_list_item_description);
            IROMazonPrice = (TextView) IROMazonView.findViewById(R.id.IROMazon_list_item_price);
        }

        // Bind item to the holder and set name accordingly
        void bindItem(IROMazonImage i, final OnItemClickListener listener) {
            // Pass the object to the main activity so the individual item can be pulled
            iromazon = i.iromazon;
            image = i.image;

            // Set the info for the current item
            IROMazonName.setText(iromazon.name);
            if (image != null) {
                IROMazonImage.setImageBitmap(image);
            }
            if (iromazon.description != null) {
                IROMazonDescription.setText(iromazon.description);
                IROMazonPrice.setText(String.format(Locale.US, "$%.2f", iromazon.price));
            } else {
                IROMazonDescription.setText(null);
                IROMazonPrice.setText(null);
            }

            // Set up custom click listener
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    listener.onItemClick(new IROMazonImage(iromazon, image));
                }
            });
        }
    }

    interface OnItemClickListener {
        void onItemClick(IROMazonImage itemimage);
    }


    private class IROMazonAdapter extends RecyclerView.Adapter<IROMazonHolder> {
        private ArrayList<IROMazonImage> mList;
        private final OnItemClickListener listener;

        IROMazonAdapter(ArrayList<IROMazonImage> list, OnItemClickListener listener) {
            this.mList = list;
            this.listener = listener;
        }

        @Override
        public IROMazonHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

            View view = layoutInflater.inflate(R.layout.iromazon_list_item, parent, false);
            return new IROMazonHolder(view);
        }

        @Override
        public void onBindViewHolder(IROMazonHolder holder, int position) {
            final IROMazonImage IROMazonImage = mList.get(position);
            holder.bindItem(IROMazonImage, listener);

        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }
}
