package com.sjsu.se195.irom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjsu.se195.irom.Classes.Item;


public class InventoryItemDetailActivity extends NavigationDrawerActivity {

    private Item item;
    private ImageView itemImage;
    private TextView itemName;
    private TextView itemDesc;
    private TextView itemCreatedDate;
    private TextView itemNote;
    private TextView quantity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        item = getIntent().getParcelableExtra("item");
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_inventory_item_detail, null, false);
        this.drawer.addView(contentView, 0);
        //make the textviews and stuff and fill in its info from the passed in item
        initializeAndFillInInfo(item);



    }

    private void initializeAndFillInInfo(Item item) {
        itemImage = (ImageView) findViewById(R.id.item_detail_picture);
        //TODO make image work
        itemName = (TextView) findViewById(R.id.item_detail_name);
        System.out.print("KRYSTLE  " + item.toAllString());

        itemName.setText(item.getName());
        itemDesc = (TextView) findViewById(R.id.item_detail_desc);
        System.out.print("KRYSTLE  " + item.toAllString());

        itemDesc.setText(item.getNote());


    }


}
