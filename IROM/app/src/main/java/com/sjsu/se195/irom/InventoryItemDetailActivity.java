package com.sjsu.se195.irom;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sjsu.se195.irom.Classes.Item;
import com.sjsu.se195.irom.Classes.Listing;

import java.util.HashMap;
import java.util.Map;


public class InventoryItemDetailActivity extends NavigationDrawerActivity {

    private Item item;
    private ImageView itemImage;
    private TextView itemName;
    private TextView itemDesc;
    private TextView itemCreatedDate;
    private Button itemListingButton;
    private TextView itemQuantity;
    private DatabaseReference mDatabaseRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        item = getIntent().getParcelableExtra("item");
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_inventory_item_detail, null, false);
        this.drawer.addView(contentView, 0);
        //make the textviews and stuff and fill in its info from the passed in item
        initializeAndFillInInfo(item);



    }

    private void initializeAndFillInInfo(final Item item) {
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        itemImage = (ImageView) findViewById(R.id.item_detail_picture);
        //TODO make image work
        itemName = (TextView) findViewById(R.id.item_detail_name);
        itemName.setText(item.getName());

        itemDesc = (TextView) findViewById(R.id.item_detail_desc);
        itemDesc.setText(item.getNote());
//
//        itemCreatedDate = (TextView) findViewById(R.id.item_detail_date);
//        itemCreatedDate.setText(item.getDateAdded().toString());

        itemQuantity = (TextView) findViewById(R.id.item_detail_quantity);
        itemQuantity.setText(String.valueOf(item.getQuantity()));

        itemListingButton = (Button) findViewById(R.id.item_detail_button);
        if(item.getForSale()){
            //do the things if it is already for sale
            itemListingButton.setText("View Listing");

        }else{
            //do the things if not for sale
            itemListingButton.setText("Create Listing");
        }
        itemListingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(InventoryItemDetailActivity.this,"let's make this a listing", Toast.LENGTH_LONG).show();
                //set up next page
                Intent i = new Intent(InventoryItemDetailActivity.this,ListingDetailActivity.class);
                //push the item through
                Bundle b = new Bundle();
                //put your custom thing in the holder
                b.putParcelable("item",item);
                //stuff your holder into the new intent to start an activity
                i.putExtras(b);
                startActivity(i);

            }
        });

    }


}
