package com.sjsu.se195.irom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sjsu.se195.irom.Classes.Item;
import com.sjsu.se195.irom.Classes.Listing;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arthur on 11/9/2016.
 */

public class ListingActivity extends NavigationDrawerActivity{
    private EditText lDescription;
    private EditText lPrice;
    private Button submitListingButton;
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;
    private Spinner itemSpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_listing, null, false);
        drawer.addView(contentView, 0);
        //initialize things
        //get current user
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        //get database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();
        initializeItems();

        fillInButtonlisteners();
        }

    private void fillInButtonlisteners() {
        submitListingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Item i = (Item) itemSpinner.getSelectedItem();
                //update that item is set for sale.
                i.setForSale(true);
                Listing newListing = new Listing(mUser.getUid(), i, lDescription.getText().toString(), Double.parseDouble(lPrice.getText().toString()));
                //TODO update item in database that forsale is true now
                String key = mDatabase.child("items").push().getKey();
                Map data = new HashMap();
                data.put(key,i);
                mDatabase.child("items").updateChildren(data);
                //send the listing off to the database
                key = mDatabase.child("listings").push().getKey();
                mDatabase.child("listings").child(key).setValue(newListing);
                //feedback for user
                Toast.makeText(ListingActivity.this,"listing created", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initializeItems() {
        lDescription = (EditText) findViewById(R.id.listing_description_text);
        lPrice = (EditText) findViewById(R.id.listing_price_text);
        submitListingButton = (Button) findViewById(R.id.listing_submit_button);
        itemSpinner = (Spinner) findViewById(R.id.listing_item_spinner);

        //hardcoded list TODO add in query to get your items.
        final ArrayList<Item> hardcodedItems = new ArrayList<Item>();
   //     Query myItemsQuery = mDatabase.child("items").equalTo(mUser.getUid());
   //     System.out.println( "MY ITEMS" + myItemsQuery.toString());
        Item i = new Item(mUser.getUid(),new Date(),"item 1",1,"hardcoded item 1");
        hardcodedItems.add(i);
        i = new Item(mUser.getUid(),new Date(),"item 2",1,"hardcoded item 2");
        hardcodedItems.add(i);

        // Creating adapter for spinner
        ArrayAdapter<Item> dataAdapter = new ArrayAdapter<Item>(this, android.R.layout.simple_spinner_item, hardcodedItems);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        // attaching data adapter to spinner
        itemSpinner.setAdapter(dataAdapter);



    }
}
