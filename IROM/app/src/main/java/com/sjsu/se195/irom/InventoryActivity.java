package com.sjsu.se195.irom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Arthur on 11/9/2016.
 */

public class InventoryActivity extends NavigationDrawerActivity {

   @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       View contentView = inflater.inflate(R.layout.activity_inventory, null, false);
       drawer.addView(contentView, 0);
       TextView it = (TextView) findViewById(R.id.inventory_text);

   }


}
