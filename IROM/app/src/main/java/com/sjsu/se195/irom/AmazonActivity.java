package com.sjsu.se195.irom;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Ricky on 11/12/2016.
 */

public class AmazonActivity extends NavigationDrawerActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_amazon, null, false);
        drawer.addView(contentView, 0);
        Button searchAmazonButton = (Button) findViewById(R.id.amazon_button);
        final EditText searchAmazonEditText = (EditText) findViewById(R.id.amazon_text);

        searchAmazonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //send it off to amazon
                Toast.makeText(AmazonActivity.this, searchAmazonEditText.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
