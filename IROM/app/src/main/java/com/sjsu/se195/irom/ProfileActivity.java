package com.sjsu.se195.irom;

import android.content.Context;
import android.icu.util.Currency;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sjsu.se195.irom.Profile;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Arthur on 11/9/2016.
 */

public class ProfileActivity extends NavigationDrawerActivity {
    private Button editButton;
    private Button saveButton;
    private TextView userEmailWelcome;
    private ScrollView editForm;
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;
    private EditText mFirstNameEditText;
    private EditText mLastNameEditText;
    private Spinner mCurrencySpinner;



    //private Spinner currency;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //INSTEAD OF setContentView(R.layout.activity_welcome); USE NEXT 3 LINES IF YOU WANT TH NAV BAR TO WORK
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_profile, null, false);
        drawer.addView(contentView, 0);
        //initialize
        initializeAllTheThings();
        //get current user
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        //get database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //get current user to set up welcome
        String profileName = mUser.getEmail() + " profile";
        userEmailWelcome.setText(profileName);

        setupClickListeners();




//TODO currency spinner
  //      currency = (Spinner) findViewById(R.id.currency_spinner);

    }

    private void initializeAllTheThings() {
        editButton = (Button) findViewById(R.id.profile_edit_form_button);
        saveButton = (Button) findViewById(R.id.profile_save_button);
        userEmailWelcome = (TextView) findViewById(R.id.profile_page__user_welcome);
        editForm = (ScrollView) findViewById(R.id.profile_edit_form);
        mLastNameEditText = (EditText) findViewById(R.id.last_name_edit_text);
        mFirstNameEditText = (EditText) findViewById(R.id.first_name_edit_text);
        //set up spinner
        mCurrencySpinner = (Spinner) findViewById(R.id.currency_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currency_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mCurrencySpinner.setAdapter(adapter);
    }

    private void setupClickListeners() {
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open or close the form.
                if(editForm.getVisibility()==View.INVISIBLE){
                    editForm.setVisibility(View.VISIBLE);
                }else{
                    //clear fields
                    mLastNameEditText.getText().clear();
                    mFirstNameEditText.getText().clear();
                    //set invisible
                    editForm.setVisibility(View.INVISIBLE);
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Profile profile = new Profile(mUser.getUid(),
                        mFirstNameEditText.getText().toString(),
                        mLastNameEditText.getText().toString(),
                        mCurrencySpinner.getSelectedItem().toString());
                writeProfile(profile);
                Toast.makeText(ProfileActivity.this, "you clicked save!", Toast.LENGTH_SHORT).show();
            }

            private void writeProfile(Profile profile) {
                Map<String, Object> data = new HashMap<>();
                data.put(mUser.getUid(),profile);

                mDatabase.child("profile").updateChildren(data);
            }
        });
    }

    public Set getCurrencies(){
    Set currencySet = java.util.Currency.getAvailableCurrencies();
    return currencySet;
}
}
