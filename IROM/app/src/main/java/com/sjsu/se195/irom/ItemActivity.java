package com.sjsu.se195.irom;

import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Arthur on 11/9/2016.
 */

public class ItemActivity extends NavigationDrawerActivity{
    TextView itemt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        itemt = (TextView) itemt.findViewById(R.id.item_text);
        super.onCreate(savedInstanceState);
    }

}
