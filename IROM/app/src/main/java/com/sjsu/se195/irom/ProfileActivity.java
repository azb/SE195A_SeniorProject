package com.sjsu.se195.irom;

import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Arthur on 11/9/2016.
 */

public class ProfileActivity extends NavigationDrawerActivity {
    TextView ppt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ppt = (TextView) ppt.findViewById(R.id.profile_page_text);


    }


}
