package com.sjsu.se195.irom;

import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Arthur on 11/9/2016.
 */

public class ListingActivity extends NavigationDrawerActivity{
    TextView lt;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        lt = (TextView) lt.findViewById(R.id.listing_text);
        super.onCreate(savedInstanceState);
    }
}
