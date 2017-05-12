package com.sjsu.se195.irom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sjsu.se195.irom.Classes.Listing;
import com.sjsu.se195.irom.Classes.Profile;

import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 * Shows detail for passed listing object
 */

public class ListingDetailActivity extends NavigationDrawerActivity {
    private static final String TAG = ListingDetailActivity.class.getSimpleName();
    private final long ONE_MEGABYTE = 1024 * 1024; // Max image download size to avoid issues
    private TextView listingName;
    private TextView listingCreator;
    private TextView listingDescription;
    private TextView listingDate;
    private TextView listingPrice;
    private TextView listingSold;
    private ImageView listingImage;
    private ImageView listingProfileImage;
    private Button purchaseButton;
    private View listingDivider;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState){
        // Drawer setup
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_listing_detail, null, false);
        this.drawer.addView(contentView, 0);

        // Set up layout items
        listingName = (TextView) findViewById(R.id.listing_detail_name);
        listingCreator = (TextView) findViewById(R.id.listing_detail_creator);
        listingDescription = (TextView) findViewById(R.id.listing_detail_description);
        listingDate = (TextView) findViewById(R.id.listing_detail_date);
        listingPrice = (TextView) findViewById(R.id.listing_detail_price);
        listingSold = (TextView) findViewById(R.id.listing_detail_sold);
        listingImage = (ImageView) findViewById(R.id.listing_detail_image);
        listingProfileImage = (ImageView) findViewById(R.id.listing_detail_profile_image);
        purchaseButton = (Button) findViewById(R.id.listing_detail_purchase);
        listingDivider = findViewById(R.id.listing_detail_divider);
        progressBar = (ProgressBar) findViewById(R.id.listing_detail_progressbar);

        // Start loading while image is downloaded
        startLoading();

        // Get data
        refreshData(getIntent());
    }

    private void startLoading() {
        listingName.setVisibility(View.INVISIBLE);
        listingCreator.setVisibility(View.INVISIBLE);
        listingDescription.setVisibility(View.INVISIBLE);
        listingDate.setVisibility(View.INVISIBLE);
        listingPrice.setVisibility(View.INVISIBLE);
        listingSold.setVisibility(View.INVISIBLE);
        listingImage.setVisibility(View.INVISIBLE);
        listingProfileImage.setVisibility(View.INVISIBLE);
        purchaseButton.setVisibility(View.INVISIBLE);
        listingDivider.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        listingName.setVisibility(View.VISIBLE);
        listingCreator.setVisibility(View.VISIBLE);
        listingDescription.setVisibility(View.VISIBLE);
        listingDate.setVisibility(View.VISIBLE);
        listingPrice.setVisibility(View.VISIBLE);
        listingSold.setVisibility(View.VISIBLE);
        listingImage.setVisibility(View.VISIBLE);
        listingProfileImage.setVisibility(View.VISIBLE);
        purchaseButton.setVisibility(View.VISIBLE);
        listingDivider.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onRestart(){
        super.onRestart();
        startLoading();
        refreshData(getIntent());
    }

    private void refreshData(final Intent intent) {
        if (intent.hasExtra("listing")) {
            // First get updated listing due to time sensitivity of marketplace, as well as image
            final Listing listing = intent.getParcelableExtra("listing");
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference ref = db.getReference().child("listings/").child(listing.getListID());
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Listing updatedListing = dataSnapshot.getValue(Listing.class);
                    // Get image so higher quality instead of passing through bundle
                    StorageReference imageRef = FirebaseStorage.getInstance().getReference("items/" + updatedListing.item.itemID);
                    imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            final Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            // Check if coming to page from marketplace or user's own inventory
                            if (intent.hasExtra("profile")) {
                                // Coming from marketplace
                                final Profile profile = intent.getParcelableExtra("profile");

                                // Get profile image if exists
                                StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("profile/" + profile.uID);
                                profileImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap profileImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        initializeFromMarketplace(updatedListing, profile, image, profileImage);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        initializeFromMarketplace(updatedListing, profile, image, null);
                                    }
                                });
                            } else {
                                // Coming from Item detail activity or direct listing creation
                                initializeFromItemDetail(updatedListing, image);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Something went wrong downloading the image!");
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "Error downloading listing: " + databaseError.toString());
                }
            });
        }
    }

    private void initializeFromMarketplace(final Listing listing, Profile profile, Bitmap image, Bitmap profileImage) {
        if (listing.isLive) { // Still for sale
            listingImage.setImageBitmap(image);
            listingName.setText(listing.item.name);
            listingCreator.setText("Posted By: " + profile.firstName + " " + profile.lastName);
            listingDescription.setText("Description: " + listing.description);
            listingPrice.setText(String.format(Locale.US, "$%.2f", listing.price));
            listingDate.setText(new SimpleDateFormat("'Created: 'MMM d, yyyy hh:mm a", Locale.US).format(listing.dateCreated));
            if (profileImage != null) {
                listingProfileImage.setImageBitmap(profileImage);
            }

            // Check if this listing is owned by current user or not
            if (profile.uID.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                stopLoading();
                purchaseButton.setVisibility(View.INVISIBLE);
                listingSold.setVisibility(View.GONE);
            } else {
                // Set up listener if for sale and not owned by current user
                purchaseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Intent intent = new Intent(getBaseContext(), PaymentTestActivity.class);
                        Bundle bundle = new Bundle();
                        String listing_id = listing.getListID();
                        Double price = listing.getPrice();
                        bundle.putString("listing_id", listing_id);
                        bundle.putDouble("price", price);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });
                stopLoading();
                listingSold.setVisibility(View.GONE);
            }
        } else { // No longer for sale
            listingImage.setImageBitmap(image);
            listingName.setText(listing.item.name);
            listingCreator.setText("Posted By: " + profile.firstName + " " + profile.lastName);
            listingDescription.setText("Description: " + listing.description);
            listingPrice.setText(String.format(Locale.US, "$%.2f", listing.price));
            listingDate.setText(new SimpleDateFormat("'Created: 'MMM d, yyyy hh:mm a", Locale.US).format(listing.dateCreated));
            listingSold.setText("Listing has been SOLD!");
            if (profileImage != null) {
                listingProfileImage.setImageBitmap(profileImage);
            }

            // Check if this listing is owned by current user or not
            if (profile.uID.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                stopLoading();
                purchaseButton.setVisibility(View.GONE);
            } else {
                stopLoading();
                purchaseButton.setEnabled(false);
            }
        }
    }

    private void initializeFromItemDetail(final Listing listing, final Bitmap image) {
        FirebaseDatabase.getInstance().getReference("profile/" + listing.creator).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                listingName.setText(listing.item.name);
                listingCreator.setText("Posted By: " + profile.firstName + " " + profile.lastName);
                listingDescription.setText("Description: " + listing.description);
                listingPrice.setText(String.format(Locale.US, "$%.2f", listing.price));
                listingDate.setText(new SimpleDateFormat("'Created: 'MMM d, yyyy hh:mm a", Locale.US).format(listing.dateCreated));
                listingImage.setImageBitmap(image);

                StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("profile/" + profile.uID);
                profileImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        listingProfileImage.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                        stopLoading();

                        // If coming from Item detail, can only be current user's own item
                        purchaseButton.setVisibility(View.GONE);

                        // Still can display sold/not sold
                        if (!listing.isLive) {
                            listingSold.setText("Listing has been SOLD!");
                        } else {
                            listingSold.setVisibility(View.GONE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        stopLoading();

                        // If coming from Item detail, can only be current user's own item
                        purchaseButton.setVisibility(View.GONE);

                        // Still can display sold/not sold
                        if (!listing.isLive) {
                            listingSold.setText("Listing has been SOLD!");
                        } else {
                            listingSold.setVisibility(View.GONE);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error downloading profile.");
            }
        });
    }
}
