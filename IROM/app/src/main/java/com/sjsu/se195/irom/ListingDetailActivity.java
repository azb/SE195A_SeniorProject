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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sjsu.se195.irom.Classes.Listing;
import com.sjsu.se195.irom.Classes.NoodlioPayClass;
import com.sjsu.se195.irom.Classes.Profile;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Token;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Krystle on 3/29/2017.
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
    private Button purchaseButton;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState){
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
        purchaseButton = (Button) findViewById(R.id.listing_detail_purchase);
        progressBar = (ProgressBar) findViewById(R.id.listing_detail_progressbar);

        startLoading();

        final Intent i = getIntent();
        if (i.hasExtra("listing")) {
            // First get updated listing due to time sensitivity of marketplace, as well as image
            final Listing listing = i.getParcelableExtra("listing");
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
                            Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            // Check if coming to page from marketplace or user's own inventory
                            if (i.hasExtra("profile")) {
                                // Coming from marketplace
                                Profile profile = i.getParcelableExtra("profile");

                                initializeFromMarketplace(updatedListing, profile, image);
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

    private void startLoading() {
        listingName.setVisibility(View.INVISIBLE);
        listingCreator.setVisibility(View.INVISIBLE);
        listingDescription.setVisibility(View.INVISIBLE);
        listingDate.setVisibility(View.INVISIBLE);
        listingPrice.setVisibility(View.INVISIBLE);
        listingSold.setVisibility(View.INVISIBLE);
        listingImage.setVisibility(View.INVISIBLE);
        purchaseButton.setVisibility(View.INVISIBLE);
    }

    private void stopLoading() {
        listingName.setVisibility(View.VISIBLE);
        listingCreator.setVisibility(View.VISIBLE);
        listingDescription.setVisibility(View.VISIBLE);
        listingDate.setVisibility(View.VISIBLE);
        listingPrice.setVisibility(View.VISIBLE);
        listingSold.setVisibility(View.VISIBLE);
        listingImage.setVisibility(View.VISIBLE);
        purchaseButton.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onRestart(){
        super.onRestart();
        purchaseButton.setVisibility(View.INVISIBLE);
        listingName.setText("This item has been sold.");
        listingCreator.setText(null);
        listingDescription.setText(null);
        listingPrice.setText(null);
    }

    private void initializeFromMarketplace(final Listing listing, Profile profile, Bitmap image) {
        if (listing.isLive) { // Still for sale
            listingImage.setImageBitmap(image);
            listingName.setText(listing.item.name);
            listingCreator.setText(profile.firstName + " " + profile.lastName);
            listingDescription.setText(listing.description);
            listingPrice.setText(String.format(Locale.US, "$%.2f", listing.price));
            listingDate.setText(new SimpleDateFormat("MMM d, yyyy hh:mm a", Locale.US).format(listing.dateCreated));

            // Check if this listing is owned by current user or not
            if (profile.uID.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                stopLoading();
                purchaseButton.setVisibility(View.INVISIBLE);
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
            }
        } else { // No longer for sale
            listingImage.setImageBitmap(image);
            listingName.setText(listing.item.name);
            listingCreator.setText(profile.firstName + " " + profile.lastName);
            listingDescription.setText(listing.description);
            listingPrice.setText(String.format(Locale.US, "$%.2f", listing.price));
            listingDate.setText(new SimpleDateFormat("MMM d, yyyy hh:mm a", Locale.US).format(listing.dateCreated));
            listingSold.setText("Listing has been SOLD!");

            // Check if this listing is owned by current user or not
            if (profile.uID.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                stopLoading();
                purchaseButton.setVisibility(View.INVISIBLE);
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
                listingCreator.setText(profile.firstName + " " + profile.lastName);
                listingDescription.setText(listing.description);
                listingPrice.setText(String.format(Locale.US, "$%.2f", listing.price));
                listingDate.setText(new SimpleDateFormat("MMM d, yyyy hh:mm a", Locale.US).format(listing.dateCreated));
                listingImage.setImageBitmap(image);

                // If coming from Item detail, can only be current user's own item
                stopLoading();
                purchaseButton.setVisibility(View.INVISIBLE);

                // Still can display sold/not sold
                if (!listing.isLive) {
                    listingSold.setText("Listing has been SOLD!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error downloading profile.");
            }
        });
    }
}
