package com.sjsu.se195.irom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.sjsu.se195.irom.Classes.Item;

import java.util.ArrayList;

/**
 * Created by Arthur on 11/9/2016.
 *
 * This is adapted from Krystle's code from 137 project (the adapter, and holder)
 */

public class InventoryActivity extends NavigationDrawerActivity {
    private FirebaseUser mUser;
    private RecyclerView itemRecyclerView;
    private ItemAdapter itemAdapter;
    private ArrayList<ItemImage> mItemList = new ArrayList<>();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final long ONE_MEGABYTE = 1024 * 1024; // Max image download size to avoid issues
    private static final String TAG = InventoryActivity.class.getSimpleName();
    private int currentLoadedCount;
    private int totalToLoadCount;
    private SwipeRefreshLayout swipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //INSTEAD OF setContentView(R.layout.activity_welcome); USE NEXT 3 LINES IF YOU WANT TH NAV BAR TO WORK
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.item_list, null, false);
        this.drawer.addView(contentView, 0);

        // Variables
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        itemRecyclerView = (RecyclerView) findViewById(R.id.inventory_recycler_view);
        itemRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Set up the database ref
        final DatabaseReference ref = database.getReference("items");

        // Set up refresh listener
        swipeLayout = ((SwipeRefreshLayout) findViewById(R.id.inventory_swipe_layout));
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

        // Set up adapter
        itemAdapter = new ItemAdapter(mItemList, new OnItemClickListener() {
            @Override
            public void onItemClick(ItemImage itemimage) {
                //you want to make a new activity
                Intent i = new Intent(InventoryActivity.this, InventoryItemDetailActivity.class);
                //you need to put info into that new activity, here is a bundle to store it in
                Bundle b = new Bundle();
                //put your custom thing in the holder
                b.putParcelable("item", itemimage.item);
                // Add image as well
                b.putParcelable("image", itemimage.image);
                //stuff your holder into the new intent to start an activity
                i.putExtras(b);
                //actually follow through with your intent. you can now fill in details about the item.
                startActivity(i);
            }
        });
        itemRecyclerView.setAdapter(itemAdapter);

        // Initial load
        refreshItems(ref);
    }

    private void refreshItems(DatabaseReference ref) {
        // Initial setup
        mItemList = new ArrayList<>();
        currentLoadedCount = 0;
        // Set refreshing
        swipeLayout.setRefreshing(true);

        // Using Value Listeners for a list stores all results in the single DataSnapshot
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                totalToLoadCount = (int) dataSnapshot.getChildrenCount();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    Item item = itemSnapshot.getValue(Item.class);
                    if (item.uID.equals(mUser.getUid())) {
                        if (item.itemID == null) { // Update itemID for items that don't have it yet
                            item.itemID = itemSnapshot.getKey();
                        }

                        // Get image now too
                        getImage(new ItemImage(item));
                    } else {
                        // Not an item we're getting
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
                // Get items failed, log a message
                Toast.makeText(InventoryActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
                // Stop refresh
                swipeLayout.setRefreshing(false);
            }
        });
    }

    private void onLoadComplete() {
        if (currentLoadedCount == totalToLoadCount) {
            // Update adapter
            itemAdapter.mList = mItemList;
            itemAdapter.notifyDataSetChanged();
            // Stop refresh
            swipeLayout.setRefreshing(false);
        }
    }

    private class ItemImage {
        Item item;
        Bitmap image;

        ItemImage(Item item) {
            this.item = item;
        }

        ItemImage(Item item, Bitmap image) {
            this.item = item;
            this.image = image;
        }
    }

    public void getImage(final ItemImage itemimage) {
        // Set up the storage ref
        StorageReference imageRef = storage.getReference("items/" + itemimage.item.itemID);

        // Get the image
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                itemimage.image = Bitmap.createScaledBitmap(bmp, (bmp.getWidth() / 4), (bmp.getHeight() / 4), true);
                mItemList.add(itemimage);
                currentLoadedCount++;
                onLoadComplete();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Something went wrong downloading the image!");
                // Still want to put the item up since quite a few still do not have images currently
                mItemList.add(itemimage);
                currentLoadedCount++;
                onLoadComplete();
            }
        });
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        Item item;
        Bitmap image;

        //set up layout of each thing on a row for an item
        ImageView itemImage;
        TextView itemName;
        TextView itemQuantity;
        TextView itemForSale;


        public ItemHolder(View itemView) {
            super(itemView);

            // Initialize name/q/forsale status/item image

            itemImage = (ImageView) itemView.findViewById(R.id.item_list_item_image);
            itemName = (TextView) itemView.findViewById(R.id.item_list_item_name);
            itemQuantity = (TextView) itemView.findViewById(R.id.item_list_item_quantity);
            itemForSale = (TextView) itemView.findViewById(R.id.item_list_item_forSale);

        }

        // Bind item to the holder and set name accordingly
        public void bindItem(ItemImage i, final OnItemClickListener listener) {
            // Pass the object to the main activity so the individual item can be pulled
            item = i.item;
            image = i.image;

            // Set the info for the current item
            itemName.setText(item.getName());
            itemQuantity.setText(item.getQuantity().toString());
            itemForSale.setText("for sale: " + item.getForSale().toString());
            if (image != null) {
                itemImage.setImageBitmap(image);
            }

            if (itemForSale.getText().toString().contains("true")) {
                //is for sale, cannot be used
                itemForSale.setTextColor(Color.RED);
            } else {
                //not for sale, can be put up for sale
                itemForSale.setTextColor(Color.GREEN);
            }

            //set up custom click listener
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    listener.onItemClick(new ItemImage(item, image));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ItemImage itemimage);
    }


    public class ItemAdapter extends RecyclerView.Adapter<ItemHolder> {
        private ArrayList<ItemImage> mList;
        private final OnItemClickListener listener;

        public ItemAdapter(ArrayList<ItemImage> list, OnItemClickListener listener) {
            this.mList = list;
            this.listener = listener;
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

            View view = layoutInflater.inflate(R.layout.item_list_item, parent, false);
            return new ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            final ItemImage itemImage = mList.get(position);
            holder.bindItem(itemImage, listener);

        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }
}

