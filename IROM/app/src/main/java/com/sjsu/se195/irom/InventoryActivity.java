package com.sjsu.se195.irom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sjsu.se195.irom.Classes.Item;

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
    private ArrayList<Item> mItemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //INSTEAD OF setContentView(R.layout.activity_welcome); USE NEXT 3 LINES IF YOU WANT TH NAV BAR TO WORK
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.item_list, null, false);
        this.drawer.addView(contentView, 0);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        itemRecyclerView = (RecyclerView) findViewById(R.id.item_recycler_view);
        //this might be base parent?
        itemRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //get reference of items from database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        // everything under items is in ref
        DatabaseReference ref = database.getReference("items");

        //pull any changes as they are made, whether a new item is added, changed, or removed.
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Item item = dataSnapshot.getValue(Item.class);
                if(item.getuID().equals(mUser.getUid())){
                    mItemList.add(item);
                }

                // Update the adapter
                itemAdapter.mList = mItemList;
                itemAdapter.notifyDataSetChanged();
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

        //add your items to the view
        itemAdapter = new ItemAdapter(mItemList);
        itemRecyclerView.setAdapter(itemAdapter);
    }

    private class ItemHolder extends RecyclerView.ViewHolder {

        Item item;
        Bundle bundle = new Bundle();

        //set up layout of each thing on a row for an item
        ImageView itemImage;
        TextView itemName;
        TextView itemQuantity;
        TextView itemForSale;


        public ItemHolder(View itemView) {
            super(itemView);

            // Initialize name/q/forsale status/item image

            itemImage = (ImageView) itemView.findViewById(R.id.item_list_item_picture);
            itemName = (TextView) itemView.findViewById(R.id.item_list_item_name);
            itemQuantity = (TextView) itemView.findViewById(R.id.item_list_item_quantity);
            itemForSale = (TextView) itemView.findViewById(R.id.item_list_item_forSale);
        }

        // Bind item to the holder and set name accordingly
        public void bindItem(Item i) {
            //pss the object to the main activity so the individual item can be pulled
            item = i;
            //set the info for the current item
            itemName.setText(item.getName());
            itemQuantity.setText(item.getQuantity().toString());
            itemForSale.setText("for sale: " + item.getForSale().toString());
            if (Objects.equals(itemForSale.getText().toString(), "true")) {
                //is for sale, cannot be used
                itemForSale.setTextColor(Color.RED);
            } else {
                //not for sale, can be put up for sale
                itemForSale.setTextColor(Color.GREEN);
            }

        }


    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemHolder> {
        ArrayList<Item> mList;

        public ItemAdapter(ArrayList<Item> list) {
            mList = list;
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

            View view = layoutInflater.inflate(R.layout.item_list_item, parent, false);
            return new ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            Item item = mList.get(position);
            holder.bindItem(item);
        }


        @Override
        public int getItemCount() {
            return mItemList.size();
        }
    }
}

