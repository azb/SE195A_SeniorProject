package com.sjsu.se195.irom;

import android.os.Bundle;
import android.widget.TextView;

/**
 * This is to test adding an item to the database.
 * Created by Arthur on 11/9/2016.
 */

public class ItemActivity extends NavigationDrawerActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       TextView itemt = (TextView) findViewById(R.id.item_text);

    }


}
