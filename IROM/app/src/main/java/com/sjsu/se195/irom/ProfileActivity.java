package com.sjsu.se195.irom;

import android.content.Context;
import android.icu.util.Currency;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Set;

/**
 * Created by Arthur on 11/9/2016.
 */

public class ProfileActivity extends NavigationDrawerActivity {

    private Spinner currency;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //INSTEAD OF setContentView(R.layout.activity_welcome); USE NEXT 3 LINES IF YOU WANT TH NAV BAR TO WORK
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_profile, null, false);
        drawer.addView(contentView, 0);

        //initialize
            //yay you can use get available currencies

        currency = (Spinner) findViewById(R.id.currency_spinner);

       TextView ppt = (TextView) findViewById(R.id.profile_page_text);
    }

public Set getCurrencies(){
    Set currencySet = java.util.Currency.getAvailableCurrencies();
    return currencySet;
}
}
