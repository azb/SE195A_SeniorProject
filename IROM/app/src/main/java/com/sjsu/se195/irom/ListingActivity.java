package com.sjsu.se195.irom;

import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Arthur on 11/9/2016.
 */

public class ListingActivity extends NavigationDrawerActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        TextView lt = (TextView) findViewById(R.id.listing_text);

    }
}
