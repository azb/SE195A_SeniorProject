package com.sjsu.se195.irom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sjsu.se195.irom.Classes.Item;
import com.sjsu.se195.irom.Classes.Listing;

/**
 * Created by Krystle on 3/29/2017.
 */

public class ListingDetailActivity extends NavigationDrawerActivity {
    private Item item;
    private TextView textView;
    private Button buyButton;
    private FirebaseUser mUser;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //retrieve passed info
        item = getIntent().getParcelableExtra("item");
        //setup layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_listing_detail, null, false);
        this.drawer.addView(contentView, 0);
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        initializePage(item);


    }

    private void initializePage(Item item) {
        textView = (TextView) findViewById(R.id.listing_detail_text);
        textView.setText("let's make a listing here");
        buyButton = (Button) findViewById(R.id.listing_detail_buy_button);


        if(mUser.getUid().equals(item.getuID())){
            //this is your item! cant buy!
            buyButton.setVisibility(View.INVISIBLE);
            //make a setup listing button visible instead
        }
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ListingDetailActivity.this, "arthur put your buy stuff here", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
