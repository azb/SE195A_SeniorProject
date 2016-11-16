package com.sjsu.se195.irom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Arthur on 11/9/2016.
 */

public class ListingActivity extends NavigationDrawerActivity{
    private EditText lDescription;
    private EditText lPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);

        //initialize things
        lDescription = (EditText) findViewById(R.id.listing_description_text);
        lPrice = (EditText) findViewById(R.id.listing_price_text);
        Button submitListingButton = (Button) findViewById(R.id.listing_submit_button);

        submitListingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ListingActivity.this,lDescription.getText().toString(), Toast.LENGTH_LONG).show();
            }
        });
        }
}
