package com.sjsu.se195.irom;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.irom.Classes.Listing;
import com.sjsu.se195.irom.Classes.Profile;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import java.util.ArrayList;
import java.util.Locale;

public class WelcomeActivity extends NavigationDrawerActivity {
    private static final String TAG = WelcomeActivity.class.getSimpleName();
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static Bus bus;
    private FirebaseUser mUser;
    private ListingAdapter listingAdapter;
    private ArrayList<ListingProfile> mListingList = new ArrayList<>();

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
        bus = new Bus(ThreadEnforcer.ANY);
        bus.register(this);

        // Get reference of listings from database
        DatabaseReference ref = database.getReference("listings");

        // Pull any changes as they are made, whether a new listing is added, changed, or removed.
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                Listing listing = dataSnapshot.getValue(Listing.class);
                if (!listing.creator.equals(mUser.getUid())) {
                    // Post to get profile
                    bus.post(listing);
                }
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
                // Get listing failed, log a message
                Toast.makeText(WelcomeActivity.this, "Cancelled. Refresh", Toast.LENGTH_SHORT).show();
            }
        });

        // Add listings to the view
        listingAdapter = new ListingAdapter(mListingList);
        listingRecyclerView.setAdapter(listingAdapter);
    }

    /**
     * Simple container class to allow passing of both the profile and the listing to the adapter.
     */
    private class ListingProfile {
        Listing listing;
        Profile profile;

        ListingProfile(Listing listing, Profile profile) {
            this.listing = listing;
            this.profile = profile;
        }
    }

    @Subscribe
    public void getProfile(final Listing listing) {
        DatabaseReference profileRef = database.getReference("profile/" + listing.creator);

        // Probably really inefficient to redo this every time but not sure how else to do it
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange:" + dataSnapshot.getKey());

                Profile profile = dataSnapshot.getValue(Profile.class);

                // Use the container class to store both the listing and the profile in the list
                mListingList.add(new ListingProfile(listing, profile));

                // Update adapter
                listingAdapter.mList = mListingList;
                listingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Get profile failed, log a message
                Toast.makeText(WelcomeActivity.this, "Cancelled. Refresh", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class ListingHolder extends RecyclerView.ViewHolder {
        Listing listing;
        Profile profile;
        Bundle bundle = new Bundle();

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
        void bindListing(ListingProfile l) {
            // Pass the object to the main activity so the individual listing can be pulled
            listing = l.listing;
            profile = l.profile;

            // Set listing details
            listingName.setText(listing.item.getName());
            listingPrice.setText(String.format(Locale.US, "$%.2f", listing.price));
            listingCreator.setText(profile.firstName + " " + profile.lastName);
            listingDescription.setText(listing.description);
        }
    }

    private class ListingAdapter extends RecyclerView.Adapter<ListingHolder> {
        ArrayList<ListingProfile> mList;

        ListingAdapter(ArrayList<ListingProfile> list) {
            mList = list;
        }

        @Override
        public ListingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

            View view = layoutInflater.inflate(R.layout.listing_list_item, parent, false);
            return new ListingHolder(view);
        }

        @Override
        public void onBindViewHolder(ListingHolder holder, int position) {
            ListingProfile listingProfile = mList.get(position);
            holder.bindListing(listingProfile);
        }


        @Override
        public int getItemCount() {
            return mListingList.size();
        }
    }
}
