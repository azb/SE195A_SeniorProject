package com.sjsu.se195.irom;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This is to test adding an item to the database.
 * Created by Arthur on 11/9/2016.
 */

public class ItemActivity extends NavigationDrawerActivity{
    private ScrollView manAddItemForm;
    private EditText mName;
    private EditText mQuantity;
    private EditText mNotes;
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //INSTEAD OF setContentView(R.layout.activity_welcome); USE NEXT 3 LINES IF YOU WANT TH NAV BAR TO WORK
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_item, null, false);
        drawer.addView(contentView, 0);

        //initialize things
        Button manuallyAddItemButton = (Button) findViewById(R.id.manually_add_item_button);
        manAddItemForm = (ScrollView) findViewById(R.id.manually_add_item_form);
       Button submitItemButton = (Button) findViewById(R.id.submit_item_button);
        mName = (EditText) findViewById(R.id.item_name_text);
        mQuantity = (EditText) findViewById(R.id.item_quantity_text);
        mNotes = (EditText) findViewById(R.id.item_note_text);

        //get current user
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        //get database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //button click functions
        manuallyAddItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (manAddItemForm.getVisibility()== View.VISIBLE){
                    //if visible make invisible
                    mName.setText("");
                    mNotes.setText("");
                    mQuantity.setText("");
                    manAddItemForm.setVisibility(View.INVISIBLE);
                }else{
                    //it is invisible so make visible
                    manAddItemForm.setVisibility(View.VISIBLE);
                }
            }
        });
        submitItemButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //check fields
                if(validityCheck()){
                    //manually create items.
                    com.sjsu.se195.irom.Item newItem = new com.sjsu.se195.irom.Item(
                            mUser.getUid(),
                            new Date(),
                            mName.getText().toString(),
                            Integer.getInteger(mQuantity.getText().toString()),
                            mNotes.getText().toString());
                    writeNewManualItem(newItem);
                }
                else{
                    Toast.makeText(ItemActivity.this, "something wasnt filled. something broke", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void writeNewManualItem(com.sjsu.se195.irom.Item i) {
        String key = mDatabase.child("items").push().getKey();
        System.out.println(i.toString());
        mDatabase.child("items").child(key).setValue(i);
        //updateChildren(data) is for updating an item
    }

    private Boolean validityCheck() {
        if (mName==null || mQuantity == null || mNotes == null){
            mNotes.setError("fill all fields");
            return false;
        }
        return true;
    }


}
