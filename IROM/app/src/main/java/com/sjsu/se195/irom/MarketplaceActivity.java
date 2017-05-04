package com.sjsu.se195.irom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SearchView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sjsu.se195.irom.Classes.Listing;
import com.sjsu.se195.irom.Classes.Profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;

public class MarketplaceActivity extends NavigationDrawerActivity {
    private static final String TAG = MarketplaceActivity.class.getSimpleName();
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final long ONE_MEGABYTE = 1024 * 1024; // Max image download size to avoid issues
    private FirebaseUser mUser;
    private ListingAdapter listingAdapter;
    private ArrayList<ListingProfile> mListingList = new ArrayList<>();
    private int currentLoadedCount;
    private int totalToLoadCount;
    private SwipeRefreshLayout swipeLayout;
    private String queryText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //INSTEAD OF setContentView(R.layout.activity_welcome); USE NEXT 3 LINES IF YOU WANT TH NAV BAR TO WORK
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_welcome, null, false);
        drawer.addView(contentView, 0);

        // Set up variables
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        RecyclerView listingRecyclerView = (RecyclerView) findViewById(R.id.listing_recycler_view);
        listingRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get reference of listings from database
        final DatabaseReference ref = database.getReference("listings/");

        // Set up refresh listener
        swipeLayout = ((SwipeRefreshLayout) findViewById(R.id.listing_swipe_layout));
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems(ref);
            }
        });
        // Set refresh spinner to be below action bar
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        swipeLayout.setProgressViewOffset(false, 0, actionBarHeight);

        // Set up SearchView
        SearchView searchView = (SearchView) findViewById(R.id.listing_search_view);
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                queryText = query;
                refreshItems(ref);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<ListingProfile> quickResults = new ArrayList<>();
                newText = newText.toLowerCase();
                for (ListingProfile current : mListingList) {
                    if (current.listing.item.name.toLowerCase().contains(newText) || current.listing.description.toLowerCase().contains(newText)) {
                        quickResults.add(current);
                    }
                }
                listingAdapter.mList = quickResults;
                listingAdapter.notifyDataSetChanged();
                return true;
            }
        });

        // Set up adapter
        listingAdapter = new ListingAdapter(mListingList, new OnItemClickListener() {
            @Override
            public void onItemClick(ListingProfile listingProfile) {
                // Move to listing detail page
                Intent i = new Intent(MarketplaceActivity.this, ListingDetailActivity.class);
                // Create bundle to hold everything
                Bundle b = new Bundle();
                b.putParcelable("listing", listingProfile.listing);
                b.putParcelable("profile", listingProfile.profile);
                // Get image and scale for Parcel
                if (listingProfile.image != null) {
                    Bitmap image = listingProfile.image;
                    b.putParcelable("image", image);
                }
                // Add into intent
                i.putExtras(b);
                startActivity(i);
            }
        });
        listingRecyclerView.setAdapter(listingAdapter);

        // Initial load of listings
        refreshItems(ref);
    }

    private void refreshItems(DatabaseReference ref) {
        // Initial setup
        mListingList = new ArrayList<>();
        currentLoadedCount = 0;
        // Set refreshing
        swipeLayout.setRefreshing(true);

        // Using Value Listeners for a list stores all results in the single DataSnapshot
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                totalToLoadCount = (int) dataSnapshot.getChildrenCount();
                for (DataSnapshot listingSnapshot : dataSnapshot.getChildren()) {
                    Listing listing = listingSnapshot.getValue(Listing.class);
                    if (!listing.creator.equals(mUser.getUid()) && listing.isLive) {
                        // Next get profile
                        getProfile(listing);
                    } else {
                        // Not a listing we're getting
                        totalToLoadCount--;
                    }
                }

                // Handle case where no listings to be shown
                if (totalToLoadCount == 0) {
                    swipeLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Get listings failed, log a message
                Toast.makeText(MarketplaceActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
                // Stop refresh
                swipeLayout.setRefreshing(false);
            }
        });
    }

    private void onLoadComplete() {
        if (currentLoadedCount == totalToLoadCount) {
            // Update adapter
            Collections.sort(mListingList, new Comparator<ListingProfile>() {
                @Override
                public int compare(ListingProfile o1, ListingProfile o2) {
                    return o1.listing.price.compareTo(o2.listing.price);
                }
            });
            if (queryText != null) {
                Log.d(TAG, queryText);
                queryText = queryText.toLowerCase();
                for (Iterator<ListingProfile> iterator = mListingList.iterator(); iterator.hasNext();) {
                    ListingProfile current = iterator.next();
                    if (!current.listing.item.name.toLowerCase().contains(queryText) && !current.listing.description.toLowerCase().contains(queryText)) {
                        iterator.remove();
                    }
                }
                queryText = null;
            }
            listingAdapter.mList = mListingList;
            listingAdapter.notifyDataSetChanged();
            // Stop refresh
            swipeLayout.setRefreshing(false);
        }
    }

    /**
     * Simple container class to allow passing of both the profile and the listing to the adapter.
     */
    private class ListingProfile {
        Listing listing;
        Profile profile;
        Bitmap image;

        ListingProfile(Listing listing, Profile profile) {
            this.listing = listing;
            this.profile = profile;
        }

        ListingProfile(Listing listing, Profile profile, Bitmap image) {
            this.listing = listing;
            this.profile = profile;
            this.image = image;
        }
    }

    private void getProfile(final Listing listing) {
        DatabaseReference profileRef = database.getReference("profile/" + listing.creator);

        // Probably really inefficient to redo this every time but not sure how else to do it
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.getKey());

                Profile profile = dataSnapshot.getValue(Profile.class);
                ListingProfile listingProfile = new ListingProfile(listing, profile);

                if (listing.item.itemID == null) { // Temporary measure while there's items without itemID
                    // Use the container class to store both the listing and the profile in the list
                    mListingList.add(listingProfile);
                    currentLoadedCount++;
                    onLoadComplete();
                } else { // There is an itemID, can try to get image
                    getImage(listingProfile);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Get profile failed, log a message
                Toast.makeText(MarketplaceActivity.this, "Cancelled. Refresh", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getImage(final ListingProfile listingProfile) {
        // Set up the storage ref
        StorageReference imageRef = FirebaseStorage.getInstance().getReference("items/" + listingProfile.listing.item.itemID);
        Log.d(TAG, "Trying to get image for: " + listingProfile.listing.item.itemID);

        // Get the image
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                listingProfile.image = Bitmap.createScaledBitmap(bmp, (bmp.getWidth() / 4), (bmp.getHeight() / 4), true);
                mListingList.add(listingProfile);
                currentLoadedCount++;
                onLoadComplete();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Something went wrong downloading the image!");
                // Still want to put the listing up since quite a few still do not have images currently
                mListingList.add(listingProfile);
                currentLoadedCount++;
                onLoadComplete();
            }
        });
    }

    private class ListingHolder extends RecyclerView.ViewHolder {
        Listing listing;
        Profile profile;
        Bitmap image;

        // Set up layout of each part of the listing
        ImageView listingImage;
        TextView listingName;
        TextView listingPrice;
        TextView listingCreator;
        TextView listingDescription;


        ListingHolder(View listingView) {
            super(listingView);

            // Initialize layout elements
            listingImage = (ImageView) listingView.findViewById(R.id.listing_list_item_image);
            listingName = (TextView) listingView.findViewById(R.id.listing_list_item_name);
            listingPrice = (TextView) listingView.findViewById(R.id.listing_list_item_price);
            listingCreator = (TextView) listingView.findViewById(R.id.listing_list_item_creator);
            listingDescription = (TextView) listingView.findViewById(R.id.listing_list_item_description);
        }

        // Bind listing to the holder and set details accordingly
        void bindListing(ListingProfile l, final OnItemClickListener listener) {
            // Pass the object to the main activity so the individual listing can be pulled
            listing = l.listing;
            profile = l.profile;
            image = l.image;

            // Set listing details
            listingName.setText(listing.item.getName());
            listingPrice.setText(String.format(Locale.US, "$%.2f", listing.price));
            listingCreator.setText(profile.firstName + " " + profile.lastName);
            listingDescription.setText(listing.description);
            if (image != null) {
                listingImage.setImageBitmap(image);
            }

            // Set up listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(new ListingProfile(listing, profile, image));
                }
            });
        }
    }

    interface OnItemClickListener {
        void onItemClick(ListingProfile listingProfile);
    }

    private class ListingAdapter extends RecyclerView.Adapter<ListingHolder> {
        private ArrayList<ListingProfile> mList;
        private final OnItemClickListener listener;

        ListingAdapter(ArrayList<ListingProfile> list, OnItemClickListener listener) {
            this.mList = list;
            this.listener = listener;
        }

        @Override
        public ListingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

            View view = layoutInflater.inflate(R.layout.listing_list_item, parent, false);
            return new ListingHolder(view);
        }

        @Override
        public void onBindViewHolder(ListingHolder holder, int position) {
            if (position < mList.size()) {
                ListingProfile listingProfile = mList.get(position);
                holder.bindListing(listingProfile, listener);
            }
        }


        @Override
        public int getItemCount() {
            return mList.size();
        }
    }
}
