package com.sjsu.se195.irom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import java.util.Locale;

public class WelcomeActivity extends NavigationDrawerActivity {
    private static final String TAG = WelcomeActivity.class.getSimpleName();
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final long ONE_MEGABYTE = 1024 * 1024; // Max image download size to avoid issues
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
                    getProfile(listing);
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
        listingAdapter = new ListingAdapter(mListingList, new OnItemClickListener() {
            @Override
            public void onItemClick(ListingProfile listingProfile) {
                // Move to listing detail page
                Intent i = new Intent(WelcomeActivity.this, ListingDetailActivity.class);
                // Create bundle to hold everything
                Bundle b = new Bundle();
                b.putParcelable("listing", listingProfile.listing);
                b.putParcelable("profile", listingProfile.profile);
                // Get image and scale for Parcel
                if (listingProfile.image != null) {
                    Bitmap image = listingProfile.image;
                    image = Bitmap.createScaledBitmap(image, (image.getWidth() / 4), (image.getHeight() / 4), true);
                    b.putParcelable("image", image);
                }
                // Add into intent
                i.putExtras(b);
                startActivity(i);
            }
        });
        listingRecyclerView.setAdapter(listingAdapter);
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

    public void getProfile(final Listing listing) {
        DatabaseReference profileRef = database.getReference("profile/" + listing.creator);

        // Probably really inefficient to redo this every time but not sure how else to do it
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange:" + dataSnapshot.getKey());

                Profile profile = dataSnapshot.getValue(Profile.class);
                ListingProfile listingProfile = new ListingProfile(listing, profile);

                if (listing.item.itemID == null) { // Temporary measure while there's items without itemID
                    // Use the container class to store both the listing and the profile in the list
                    mListingList.add(listingProfile);

                    // Update adapter
                    listingAdapter.mList = mListingList;
                    listingAdapter.notifyDataSetChanged();
                } else { // There is an itemID, can try to get image
                    getImage(listingProfile);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Get profile failed, log a message
                Toast.makeText(WelcomeActivity.this, "Cancelled. Refresh", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getImage(final ListingProfile listingProfile) {
        // Set up the storage ref
        StorageReference imageRef = FirebaseStorage.getInstance().getReference("items/" + listingProfile.listing.item.itemID);

        // Get the image
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                listingProfile.image = Bitmap.createScaledBitmap(bmp, (bmp.getWidth() / 4), (bmp.getHeight() / 4), true);
                mListingList.add(listingProfile);
                listingAdapter.mList = mListingList;
                listingAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Something went wrong downloading the image!");
                // Still want to put the listing up since quite a few still do not have images currently
                mListingList.add(listingProfile);
                listingAdapter.mList = mListingList;
                listingAdapter.notifyDataSetChanged();
            }
        });

        // If no image, continue anyway for handling the items we have that don't currently have images
    }

    private class ListingHolder extends RecyclerView.ViewHolder {
        Listing listing;
        Profile profile;
        Bitmap image;
        Bundle bundle = new Bundle();
        View listingView;

        // Set up layout of each part of the listing
        ImageView listingImage;
        TextView listingName;
        TextView listingPrice;
        TextView listingCreator;
        TextView listingDescription;


        ListingHolder(View listingView) {
            super(listingView);
            this.listingView = listingView;

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
            listingView.setOnClickListener(new View.OnClickListener() {
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
        ArrayList<ListingProfile> mList;
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
            ListingProfile listingProfile = mList.get(position);
            holder.bindListing(listingProfile, listener);
        }


        @Override
        public int getItemCount() {
            return mListingList.size();
        }
    }
}
