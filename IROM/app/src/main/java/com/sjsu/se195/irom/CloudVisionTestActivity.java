package com.sjsu.se195.irom;

import android.Manifest;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.api.services.vision.v1.model.WebDetection;
import com.google.api.services.vision.v1.model.WebEntity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sjsu.se195.irom.Classes.IROMazon;

public class CloudVisionTestActivity extends NavigationDrawerActivity {
    private static final String CLOUD_VISION_API_KEY = "AIzaSyAHnhDlz-V1OTUivtflxsQwFShuAzeh-6w";
    private static final String TAG = CloudVisionTestActivity.class.getSimpleName();
    private static final int GALLERY_IMAGE_REQUEST = 1;
    private static final int GALLERY_PERMISSIONS_REQUEST = 2;
    private static final int CAMERA_IMAGE_REQUEST = 3;
    private static final int CAMERA_PERMISSIONS_REQUEST = 4;
    private TextView resultField;
    private Uri currentPhotoURI;

    //IROMazon stuff
    private FirebaseDatabase cFirebaseEntry;
    private DatabaseReference cWebEntitySearch;
    private FirebaseStorage cStorageEntry;
    private StorageReference cImageUpload;
    private Bitmap imageToUpload;
    private ArrayList<IROMazon> cEntryList = new ArrayList<>();
    private boolean createNewIROMazon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // INSTEAD OF setContentView(R.layout.layout_name) USE NEXT 3 LINES
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_cloud_vision_test, null, false);
        drawer.addView(contentView, 0);

        // Set up buttons
        Button requestButton = (Button) findViewById(R.id.send_request_button);
        Button loadImageButton = (Button) findViewById(R.id.load_image_button);
        Button openCameraButton = (Button) findViewById(R.id.open_camera_button);
        resultField = (TextView) findViewById(R.id.resultsReplace);

        // Set up listeners.
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        loadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGalleryChooser();
            }
        });

        openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startCamera();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //IROMazon stuff
        cFirebaseEntry = FirebaseDatabase.getInstance();
        cWebEntitySearch = cFirebaseEntry.getReference().child("IROMazon");
        cStorageEntry = FirebaseStorage.getInstance();
        cImageUpload = cStorageEntry.getReference().child("IROMazon/");
        cWebEntitySearch.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                IROMazon entry = new IROMazon(); //dataSnapshot.getValue();
                entry.name = (String) dataSnapshot.child("name").getValue();
                entry.price = (Double) dataSnapshot.child("price").getValue();
                entry.key = dataSnapshot.getKey();
                for (DataSnapshot textSnapshot : dataSnapshot.child("text").getChildren()) {
                    entry.text.add(textSnapshot.getValue().toString());
                }
                for (DataSnapshot textSnapshot : dataSnapshot.child("logo").getChildren()) {
                    entry.logo.add(textSnapshot.getValue().toString());
                }
                for (DataSnapshot textSnapshot : dataSnapshot.child("label").getChildren()) {
                    entry.label.add(textSnapshot.getValue().toString());
                }
                cEntryList.add(entry);
                /*System.out.println("Key");
                System.out.println(entry.key);
                System.out.println("Object Name");
                System.out.println(entry.name);
                System.out.println("Price");
                System.out.println(String.format("%.2f",entry.price));
                if (entry.text.size() != 0) {
                    System.out.println("Text of Object");
                    System.out.println(entry.text.get(0));
                }
                if (entry.logo.size() != 0) {
                    System.out.println("Logo of Object");
                    System.out.println(entry.logo.get(0));
                }
                if(entry.label.size() != 0){
                    System.out.println("Label of Object");
                    System.out.println(entry.label.get(0));
                }*/
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
    }

    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, GALLERY_IMAGE_REQUEST);
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_IMAGE_REQUEST && data != null) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                try (Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null)) {
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);

                        ImageView imageView = (ImageView) findViewById(R.id.nav_bar_logo);
                        imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                    }
                } catch (java.lang.NullPointerException e) {
                    Log.d(TAG, "Null pointer exception with local image: " + e.getMessage());
                }
            }
            if (requestCode == CAMERA_IMAGE_REQUEST) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), currentPhotoURI);
                    ImageView imageView = (ImageView) findViewById(R.id.nav_bar_logo);
                    imageView.setImageBitmap(bitmap);
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
            ImageView imageView = (ImageView) findViewById(R.id.nav_bar_logo);
            Drawable drawable = imageView.getDrawable();
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            bitmap = scaleBitmapDown(bitmap, 1200);
            imageToUpload = bitmap;

            callCloudVision(bitmap);
        } catch (IOException e) {
            Log.d(TAG, "Image picking failed because " + e.getMessage());
        }
    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Change text to show loading
        resultField.setText(R.string.loading_text);

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

                    ArrayList<IROMazon> entityR = getIROMazon_Entity(response);
                    ArrayList<IROMazon> textR = getIROMazon_Text(response);
                    ArrayList<IROMazon> logolabelR = getIROMazon_Logo(response);
                    ArrayList<IROMazon> labelR = getIROMazon_Label(response);
                    createNewIROMazon = entityR.size() == 0 && textR.size() == 0;
                    //return convertResponseToString(response);
                    String result = resultstoString(entityR,textR,logolabelR,labelR);
                    result += "\n";
                    result += convertResponseToString(response);
                    return result;

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "Failed to make API request because: " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "Failed to make API request because of other exception: " + e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                resultField.setText(result);
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
       ArrayList<String> labelResults = new ArrayList<>();
       ArrayList<String> logoResults = new ArrayList<>();
       ArrayList<String> textResults = new ArrayList<>();
       ArrayList<String> entityResults = new ArrayList<>();
       String message = "I found these things:\n\n";

       message += "Labels:\n";
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message += String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription());
                message += "\n";
                if(label.getDescription() != null){
                    labelResults.add(label.getDescription());
                }
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
               if(logo.getDescription() != null){
                   logoResults.add(logo.getDescription());
               }
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
               if(text.getDescription() != null){
                    textResults.add(text.getDescription());
               }
           }
       } else {
           message += "Nothing";
       }

       message += "\nWeb Entities:\n";
       WebDetection webDetection = response.getResponses().get(0).getWebDetection();
       if(webDetection != null){
           for(WebEntity entity : webDetection.getWebEntities()){
               message += String.format(Locale.US, "%s: %.3f", entity.getDescription(), entity.getScore());
               message += "\n";
               if(entity.getDescription() != null){
                   entityResults.add(entity.getDescription());
               }
           }

           if(createNewIROMazon){
               IROMazon newEntry = new IROMazon(entityResults.get(0),textResults,logoResults,labelResults,19.95d);
               createIROMazonEntry(newEntry);
               createNewIROMazon = false;
           }
           }
        return message;
    }

    private void createIROMazonEntry(final IROMazon newEntry){
        System.out.println("Creating IROMazon Entry");
        System.out.println("\n");
        final String key = cWebEntitySearch.child("IROMazon").push().getKey();
        newEntry.key = key;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageToUpload.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        StorageReference upload = cImageUpload.child(key);
        UploadTask uploadTask = upload.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Handle unsucessful upload
                System.out.println("Failed to upload picture");
                System.out.println("\n");
                cWebEntitySearch.child(key).setValue(null);//deletes data
            }
        });
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("Upload Successful.");
                System.out.println("\n");
                cWebEntitySearch.child(key).setValue(newEntry);//sets data
            }
        });
        //String key = cWebEntitySearch.child("IROMazon").push().getKey();
        //cWebEntitySearch.child(key).setValue(newEntry);
    }

    private String resultstoString(ArrayList<IROMazon> entityResults,ArrayList<IROMazon> textResults,ArrayList<IROMazon> logolabelResults,ArrayList<IROMazon> labelResults){
        String result = "";
        result += "Entity Matches";
        result += "\n";
        for(int i = 0;i<entityResults.size();i++){
            result += i;
            result += ". ";
            result += entityResults.get(i).name;
            result += entityResults.get(i).key;
            result += "\n";
        }
        result += "\n";
        result += "Text Matches";
        result += "\n";
        for(int i = 0;i<textResults.size();i++){
            result += i;
            result += ". ";
            result += textResults.get(i).name;
            result += textResults.get(i).key;
            result += "\n";
        }
        result += "\n";
        result += "LogoLabel Matches";
        result += "\n";
        for(int i = 0;i<logolabelResults.size();i++){
            result += i;
            result += ". ";
            result += logolabelResults.get(i).name;
            result += logolabelResults.get(i).key;
            result += "\n";
        }
        result += "\n";
        result += "Label Matches";
        result += "\n";
        for(int i = 0;i<labelResults.size();i++){
            result += i;
            result += ". ";
            result += labelResults.get(i).name;
            result += labelResults.get(i).key;
            result += "\n";
        }
        return result;
    }

    private ArrayList<IROMazon> getIROMazon_Entity(BatchAnnotateImagesResponse response){
        ArrayList<IROMazon> result = new ArrayList<>();
        ArrayList<String> entityResults = new ArrayList<>();
        WebDetection webDetection = response.getResponses().get(0).getWebDetection();

        //Store Cloud Vision Web Entity Results into ArrayList<String>. Only return results with Score > 0.5
        if(webDetection != null) {
            for (WebEntity entity : webDetection.getWebEntities()) {
                if (entity.getDescription() != null && entity.getScore() >= 0.5) {
                    entityResults.add(entity.getDescription());
                    //System.out.println("Adding to entityResults");
                    //System.out.println(entity.getDescription());
                    //System.out.println("\n");
                }
            }
        }

        for(int i = 0;i<cEntryList.size();i++){
            for(int j = 0;j<entityResults.size();j++){
                if(cEntryList.get(i).name.equals(entityResults.get(j))){
                    result.add(cEntryList.get(i));
                    //System.out.println("Adding to result");
                    //System.out.println(cEntryList.get(i).name);
                    //System.out.println("\n");
                }
            }
        }

        return result;
    }

    private ArrayList<IROMazon> getIROMazon_Text(BatchAnnotateImagesResponse response){
        ArrayList<IROMazon> result = new ArrayList<>();
        ArrayList<String> textResults = new ArrayList<>();
        List<EntityAnnotation> texts = response.getResponses().get(0).getTextAnnotations();
        if (texts != null) {
            for (EntityAnnotation text : texts) {
               if(text.getDescription() != null){
                    textResults.add(text.getDescription());
               }
            }
        }
        for(int i = 0;i<cEntryList.size();i++){

                if(getIROMazon_TLLScore(textResults,cEntryList.get(i).text) >= 0.5){
                    result.add(cEntryList.get(i));
                }
        }
        return result;
    }

    private ArrayList<IROMazon> getIROMazon_Logo(BatchAnnotateImagesResponse response){
        ArrayList<IROMazon> result = new ArrayList<>();
        ArrayList<String> logoResults = new ArrayList<>();
        ArrayList<String> labelResults = new ArrayList<>();
        List<EntityAnnotation> logos = response.getResponses().get(0).getLogoAnnotations();
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (logos != null) {
            for (EntityAnnotation logo : logos) {
                if(logo.getDescription() != null){
                    logoResults.add(logo.getDescription());
                }
            }
        }
        if(labels != null){
            for (EntityAnnotation label : labels){
                if(label.getDescription() != null){
                    logoResults.add(label.getDescription());
                }
            }
        }

        for(int i = 0;i<cEntryList.size();i++){
            if(getIROMazon_TLLScore(logoResults,cEntryList.get(i).logo) >= 0.5){
                if(getIROMazon_TLLScore(labelResults,cEntryList.get(i).label) >= 0.5) {
                    result.add(cEntryList.get(i));
                }
            }
        }
        return result;
    }

    private ArrayList<IROMazon> getIROMazon_Label(BatchAnnotateImagesResponse response){
        ArrayList<IROMazon> result = new ArrayList<>();
        ArrayList<String> labelResults = new ArrayList<>();
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                if(label.getDescription() != null){
                    System.out.println("Adding to labelResults");
                    System.out.println(label.getDescription());
                    labelResults.add(label.getDescription());
                }
            }
        }
        for(int i = 0;i<cEntryList.size();i++){

            if(getIROMazon_TLLScore(labelResults,cEntryList.get(i).label) >= 0.5){
                result.add(cEntryList.get(i));
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
}

