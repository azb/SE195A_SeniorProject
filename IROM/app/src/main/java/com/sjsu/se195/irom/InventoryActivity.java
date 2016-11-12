package com.sjsu.se195.irom;

import android.os.Bundle;
import android.view.Menu;
import android.widget.SearchView;

/**
 * Created by Arthur on 11/9/2016.
 */

public class InventoryActivity extends NavigationDrawerActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
