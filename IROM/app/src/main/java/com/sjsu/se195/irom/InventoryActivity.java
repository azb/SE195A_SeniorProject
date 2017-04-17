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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sjsu.se195.irom.Classes.Item;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import java.util.ArrayList;
import java.util.Objects;

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
    public static Bus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //INSTEAD OF setContentView(R.layout.activity_welcome); USE NEXT 3 LINES IF YOU WANT TH NAV BAR TO WORK
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.item_list, null, false);
        this.drawer.addView(contentView, 0);

        // Variables
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        itemRecyclerView = (RecyclerView) findViewById(R.id.item_recycler_view);
        itemRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        bus = new Bus(ThreadEnforcer.ANY);
        bus.register(this);

        // Set up the database ref
        DatabaseReference ref = database.getReference("items");

        //pull any changes as they are made, whether a new item is added, changed, or removed.
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                
                Item item = dataSnapshot.getValue(Item.class);
                if(item.getuID().equals(mUser.getUid())){
                    if (item.itemID == null) {
                        item.itemID = dataSnapshot.getKey();
                    }
                    // Post to get the accompanying image
                    bus.post(new ItemImage(item));
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //item to be removed
                Item item = dataSnapshot.getValue(Item.class);
                //iterate through the list, remove the one that is the same as item
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //get item failed, log a message
                Toast.makeText(InventoryActivity.this, "Cancelled. Refresh", Toast.LENGTH_SHORT).show();
            }
        });

        //add your items to the view and set up listener for when you click on an item
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

    @Subscribe
    public void getImage(final ItemImage itemimage) {
        // Set up the storage ref
        StorageReference imageRef = storage.getReference("items/" + itemimage.item.itemID);

        // Get the image
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                itemimage.image = Bitmap.createScaledBitmap(bmp, (bmp.getWidth() / 4), (bmp.getHeight() / 4), true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Something went wrong downloading the image!");
            }
        });

        // If no image, continue anyway for handling the items we have that don't currently have images
        mItemList.add(itemimage);
        itemAdapter.mList = mItemList;
        itemAdapter.notifyDataSetChanged();
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

            if (Objects.equals(itemForSale.getText().toString(), "true")) {
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

