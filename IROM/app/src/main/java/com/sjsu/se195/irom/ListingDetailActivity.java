package com.sjsu.se195.irom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjsu.se195.irom.Classes.Listing;
import com.sjsu.se195.irom.Classes.Profile;

/**
 * Created by Krystle on 3/29/2017.
 */

public class ListingDetailActivity extends NavigationDrawerActivity {
    private TextView listingName;
    private TextView listingCreator;
    private TextView listingDescription;
    private TextView listingPrice;
    private ImageView listingImage;

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

        // Check if coming to page from marketplace or user's own inventory
        Intent i = getIntent();
        if (i.hasExtra("listing")) {
            // Coming from marketplace
            Listing listing = i.getParcelableExtra("listing");
            Profile profile = i.getParcelableExtra("profile");
            if (i.hasExtra("image")) {
                Bitmap image = i.getParcelableExtra("image");
                initializeFromMarketplace(listing, profile, image);
            } else {
                initializeFromMarketplace(listing, profile);
            }
        } else {
            // TODO: Handle coming from inventory, get listing based on item, don't show purchase button
        }
    }

    private void initializeFromMarketplace(Listing listing, Profile profile, Bitmap image) {
        listingName.setText(listing.item.name);
        listingCreator.setText(profile.firstName + " " + profile.lastName);
        listingDescription.setText(listing.description);
        listingPrice.setText(listing.price.toString());
        listingImage.setImageBitmap(image);
    }

    private void initializeFromMarketplace(Listing listing, Profile profile) {
        listingName.setText(listing.item.name);
        listingCreator.setText(profile.firstName + " " + profile.lastName);
        listingDescription.setText(listing.description);
        listingPrice.setText(listing.price.toString());
    }
}
