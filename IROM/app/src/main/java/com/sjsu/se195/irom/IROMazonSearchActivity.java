package com.sjsu.se195.irom;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Ricky on 4/3/2017.
 */

public class IROMazonSearchActivity extends NavigationDrawerActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_welcome, null, false);
        drawer.addView(contentView, 0);


    }
}
