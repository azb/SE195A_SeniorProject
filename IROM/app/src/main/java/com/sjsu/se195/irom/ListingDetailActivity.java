package com.sjsu.se195.irom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.irom.Classes.Listing;
import com.sjsu.se195.irom.Classes.Profile;

import java.util.Locale;

/**
 * Created by Krystle on 3/29/2017.
 */

public class ListingDetailActivity extends NavigationDrawerActivity {
    private TextView listingName;
    private TextView listingCreator;
    private TextView listingDescription;
    private TextView listingPrice;
    private ImageView listingImage;
    private Button purchaseButton;

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
        listingPrice = (TextView) findViewById(R.id.listing_detail_price);
        listingImage = (ImageView) findViewById(R.id.listing_detail_image);
        purchaseButton = (Button) findViewById(R.id.listing_detail_purchase);

        // Check if coming to page from marketplace or user's own inventory
        Intent i = getIntent();
        if (i.hasExtra("profile")) {
            // Coming from marketplace
            Listing listing = i.getParcelableExtra("listing");
            Profile profile = i.getParcelableExtra("profile");
            if (i.hasExtra("image")) {
                Bitmap image = i.getParcelableExtra("image");

                initializeFromMarketplaceBase(listing, profile);
                // Also do image
                listingImage.setImageBitmap(image);
            } else {
                // No image
                initializeFromMarketplaceBase(listing, profile);
            }
        } else {
            // Coming from Item detail activity or direct listing creation
            Listing listing = i.getParcelableExtra("listing");
            Bitmap image = i.getParcelableExtra("image");

            initializeFromItemDetail(listing, image);
        }
    }

    private void initializeFromMarketplaceBase(Listing listing, Profile profile) {
        listingName.setText(listing.item.name);
        listingCreator.setText(profile.firstName + " " + profile.lastName);
        listingDescription.setText(listing.description);
        listingPrice.setText(String.format(Locale.US, "$%.2f", listing.price));

        // Check if this listing is owned by current user or not
        if (profile.uID.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            purchaseButton.setVisibility(View.INVISIBLE);
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
                listingImage.setImageBitmap(image);

                // If coming from Item detail, can only be current user's own item
                purchaseButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
