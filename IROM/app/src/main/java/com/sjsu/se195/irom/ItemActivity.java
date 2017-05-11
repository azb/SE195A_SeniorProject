package com.sjsu.se195.irom;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sjsu.se195.irom.Classes.IROMazon;
import com.sjsu.se195.irom.Classes.Item;
import com.sjsu.se195.irom.Classes.Listing;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Locale;

/**
 * This is to test adding an item to the database.
 * Created by Arthur on 11/9/2016.
 */

public class ItemActivity extends NavigationDrawerActivity{
    private static final String TAG = ItemActivity.class.getSimpleName();
    private EditText mName;
    private EditText mQuantity;
    private EditText mNotes;
    private DatabaseReference mItemDatabaseRef;
    private StorageReference mStorageRef;
    private DatabaseReference mListingDatabaseRef;
    private FirebaseUser mUser;
    private IROMazon passedIROMazon;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // INSTEAD OF setContentView(R.layout.activity_marketplace); USE NEXT 3 LINES IF YOU WANT TH NAV BAR TO WORK
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_item, null, false);
        drawer.addView(contentView, 0);

        // Initialize add item buttons and input fields
        Button submitItemButton = (Button) findViewById(R.id.submitButton);
        mName = (EditText) findViewById(R.id.nameText);
        mQuantity = (EditText) findViewById(R.id.quantityText);
        mNotes = (EditText) findViewById(R.id.notesText);
        Button createListingButton = (Button) findViewById(R.id.createListingButton);
        ImageView imageHolder = (ImageView) findViewById(R.id.imageHolder);

        // Set up progress dialog
        progressDialog = new ProgressDialog(ItemActivity.this, R.style.AppTheme_Dialog);
        progressDialog.setIndeterminate(true);

        // Get current user
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        // Get database
        final FirebaseDatabase db = FirebaseDatabase.getInstance();
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        // Get ref
        mItemDatabaseRef = db.getReference("items");
        mStorageRef = storage.getReference("items/");
        mListingDatabaseRef = db.getReference("listings/");

        // Fill out fields if coming from IROMazon search activity
        Intent i = getIntent();
        if (i.hasExtra("IROMazon")) {
            Uri imageURI = i.getParcelableExtra("imageURI");
            IROMazon iromazon = i.getParcelableExtra("IROMazon");
            // Set image
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageURI);
                imageHolder.setImageBitmap(bitmap);
            } catch (java.io.IOException e) {
                Log.d(TAG, "Image selection failed: " + e.getMessage());
            }

            // Set fields
            mName.setText(iromazon.name);
            if (iromazon.description != null) {
                mNotes.setText(iromazon.description);
            }

            passedIROMazon = iromazon;
        }

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
                    if (passedIROMazon != null) {
                        newItem.savedDescription = passedIROMazon.description;
                        newItem.savedPrice = passedIROMazon.price;
                    }
                    writeNewItemAndImage(newItem);
                } else {
                    Toast.makeText(ItemActivity.this, "Something wasn't filled", Toast.LENGTH_SHORT).show();
                }
            }
        });

        createListingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check fields
                if (validityCheck()) {
                    final Item newItem = new Item(
                            mUser.getUid(),
                            new Date(),
                            mName.getText().toString(),
                            Integer.parseInt(mQuantity.getText().toString()),
                            mNotes.getText().toString());

                    // Get extra listing input
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.create_listing_popup, null);

                    // Set up view and listener
                    Button submit = (Button) layout.findViewById(R.id.submit_button);
                    final EditText descriptionField = (EditText) layout.findViewById(R.id.description);
                    final EditText priceField = (EditText) layout.findViewById(R.id.price);
                    submit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!(TextUtils.isEmpty(descriptionField.getText()) && TextUtils.isEmpty(priceField.getText()))) {
                                writeNewItemImageAndListing(newItem, descriptionField.getText().toString(), Double.parseDouble(priceField.getText().toString()));
                            } else {
                                Toast.makeText(ItemActivity.this, "Fill in the fields!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    // Set IROMazon data if present
                    if (passedIROMazon != null) {
                        if (passedIROMazon.description != null) {
                            descriptionField.setText(passedIROMazon.description);
                        }
                        priceField.setText(String.format(Locale.US, "%.2f", passedIROMazon.price));
                    }

                    // Create dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(ItemActivity.this);
                    builder.setView(layout);
                    builder.show();
                } else {
                    Toast.makeText(ItemActivity.this, "Something wasn't filled", Toast.LENGTH_SHORT).show();
                }
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

    private void writeNewItemAndImage(final Item item) {
        // Show dialog
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        // Upload item
        final String key = mItemDatabaseRef.child("items").push().getKey();
        item.itemID = key;
        mItemDatabaseRef.child(key).setValue(item);

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
        final Bitmap finalImage = image;
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(ItemActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                // Set up intent
                Intent i = new Intent(ItemActivity.this, InventoryItemDetailActivity.class);
                Bundle b = new Bundle();
                b.putParcelable("item", item);
                Bitmap image = Bitmap.createScaledBitmap(finalImage, (finalImage.getWidth() / 4), (finalImage.getHeight() / 4), true);
                b.putParcelable("image", image);
                i.putExtras(b);

                // Close dialog
                progressDialog.hide();

                // Change to Item Detail activity
                startActivity(i);
            }
        });
    }

    private void writeNewItemImageAndListing(Item item, String description, double price) {
        // Show dialog
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        // Update and upload item
        final String itemKey = mItemDatabaseRef.push().getKey();
        item.itemID = itemKey;
        final String listingKey = mListingDatabaseRef.push().getKey();
        item.listingID = listingKey;
        item.forSale = true;
        mItemDatabaseRef.child(itemKey).setValue(item);

        // Create and upload listing
        final Listing listing = new Listing(item.uID, item, description, price);
        listing.listID = listingKey;
        mListingDatabaseRef.child(listingKey).setValue(listing);

        // Now upload image
        StorageReference imageUpload = mStorageRef.child(itemKey);
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
                mItemDatabaseRef.child(itemKey).removeValue();
                mListingDatabaseRef.child(listingKey).removeValue();
                Toast.makeText(ItemActivity.this, "Image upload failed, item removed", Toast.LENGTH_SHORT).show();
            }
        });
        final Bitmap finalImage = image;
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(ItemActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                // Set up intent
                Intent i = new Intent(ItemActivity.this, ListingDetailActivity.class);
                Bundle b = new Bundle();
                b.putParcelable("listing", listing);
                Bitmap image = Bitmap.createScaledBitmap(finalImage, (finalImage.getWidth() / 4), (finalImage.getHeight() / 4), true);
                b.putParcelable("image", image);
                i.putExtras(b);

                // Close dialog
                progressDialog.hide();

                // Change to Listing Detail activity
                startActivity(i);
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
