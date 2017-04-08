package com.sjsu.se195.irom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends NavigationDrawerActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //INSTEAD OF setContentView(R.layout.activity_welcome); USE NEXT 3 LINES IF YOU WANT TH NAV BAR TO WORK
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_welcome, null, false);
        drawer.addView(contentView, 0);

        //set up buttons n stuff
        //Button logOutButton = (Button) findViewById(R.id.log_out_button);
        TextView email = (TextView) findViewById(R.id.currentEmailPasteHere);
        //Button cloudVisionTestButton = (Button) findViewById(R.id.visionTestButton);
        //Button gotoInventory = (Button) findViewById(R.id.goto_inventory);


        email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
/*
        //set up log out button and function
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             //see nav drawer activity for logout funct
               logout();
            }
        });

        //set up log out button and function
        gotoInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), InventoryActivity.class);
                startActivity(intent);
            }
        });

        // Set up temp Vision Test button
        cloudVisionTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), CloudVisionTestActivity.class);
                startActivity(intent);
            }
        });
        */
    }
}
