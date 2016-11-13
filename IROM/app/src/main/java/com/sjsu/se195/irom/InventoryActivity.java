package com.sjsu.se195.irom;

import android.os.Bundle;
import android.view.Menu;
import android.widget.SearchView;
import android.widget.TextView;

/**
 * Created by Arthur on 11/9/2016.
 */

public class InventoryActivity extends NavigationDrawerActivity {
<<<<<<< HEAD
   @Override
    protected void onCreate(Bundle savedInstanceState) {
       TextView  it = (TextView) findViewById(R.id.inventory_text);
=======

   @Override
    protected void onCreate(Bundle savedInstanceState) {
>>>>>>> 601392dbd5ff0c395115ac07c00a78f11be0506b
        super.onCreate(savedInstanceState);
       TextView it = (TextView) findViewById(R.id.inventory_text);

   }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    /*
        SearchView searchView = (SearchView) menu.findItem(R.id.inventory_search).getActionView();
        //searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
    */
        return super.onCreateOptionsMenu(menu);

    }
}
