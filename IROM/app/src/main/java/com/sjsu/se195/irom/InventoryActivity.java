package com.sjsu.se195.irom;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

/**
 * Created by Arthur on 11/9/2016.
 *
 * This is adapted from Krystle's code from 137 project
 */

public class InventoryActivity extends NavigationDrawerActivity {

    private FirebaseUser mUser;
    private RecyclerView itemRecyclerView;
    private ItemAdapter itemAdapter;
    private ArrayList<com.sjsu.se195.irom.Item> mItemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       //INSTEAD OF setContentView(R.layout.activity_welcome); USE NEXT 3 LINES IF YOU WANT TH NAV BAR TO WORK
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.item_list,null,false);
        this.drawer.addView(contentView,0);

       mUser = FirebaseAuth.getInstance().getCurrentUser();
       itemRecyclerView = (RecyclerView) findViewById(R.id.item_recycler_view);
       //this might be base parent?
       itemRecyclerView.setLayoutManager( new LinearLayoutManager(this) );
       //get items from database
       //TODO not use generated data.
       com.sjsu.se195.irom.Item tempItem = new com.sjsu.se195.irom.Item(mUser.getUid(),
               new Date(),
               "Horse",
               1,
               "my horse is amazing");
       tempItem.setForSale(true);
       mItemList.add(tempItem);
       tempItem = new com.sjsu.se195.irom.Item(mUser.getUid(),
               new Date(),
               "tea bag",
               12,
               "the green kind");
       mItemList.add(tempItem);
        this.setTitle("My Inventory");
       itemAdapter = new ItemAdapter(mItemList);
       itemRecyclerView.setAdapter(itemAdapter);
   }

    @Override public void onStart(){}

    private class ItemHolder extends RecyclerView.ViewHolder {

        com.sjsu.se195.irom.Item item;
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
        public void bindItem(com.sjsu.se195.irom.Item i) {
            //pss the object to the main activity so the individual item can be pulled
            item = i;
            //set the info for the current item
            itemName.setText(item.getName());
            itemQuantity.setText(item.getQuantity());
            itemForSale.setText(item.getForSale().toString());
            if(Objects.equals(itemForSale.getText().toString(), "true")){
                //is for sale, cannot be used
                itemForSale.setTextColor(Color.RED);
            } else{
                //not for sale, can be put up for sale
                itemForSale.setTextColor(Color.GREEN);
            }

        }


    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemHolder>{
        private ArrayList<com.sjsu.se195.irom.Item> mList;
        public ItemAdapter(ArrayList<com.sjsu.se195.irom.Item> list){
            mList = list;
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

            View view = layoutInflater.inflate(R.layout.item_list_item,parent,false);
            return new ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            com.sjsu.se195.irom.Item item = mList.get(position);
            holder.bindItem(item);
        }


        @Override
        public int getItemCount(){
            return mItemList.size();
        }

    }
}
