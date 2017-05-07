package com.sjsu.se195.irom;

import android.content.Context;
import android.os.Bundle;

import android.support.design.widget.NavigationView; //added by Arthur

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot; //added by Arthur
import com.google.firebase.database.DatabaseError; //added by Arthur

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener; //added by Arthur
import com.sjsu.se195.irom.Classes.Profile;

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
    private EditText mEmailEditText;
    private Spinner mCurrencySpinner;
    private TextView mFirstNameTextView;
    private TextView mLastNameTextView;
    private TextView mEmailTextView;

    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();//added by Arthur



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

        //Get text input fields IDs
        mLastNameEditText = (EditText) findViewById(R.id.last_name_edit_text);
        mFirstNameEditText = (EditText) findViewById(R.id.first_name_edit_text);
        mEmailEditText = (EditText) findViewById(R.id.email_edit_text);
        //Get text labels IDs
        mFirstNameTextView = (TextView) findViewById(R.id.first_name_label);
        mLastNameTextView = (TextView) findViewById(R.id.last_name_label);
        mEmailTextView = (TextView) findViewById(R.id.email_label);
        //TODO PULL FROM PROFILE FOR NAMES

        //set up spinner
        mCurrencySpinner = (Spinner) findViewById(R.id.currency_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currency_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mCurrencySpinner.setAdapter(adapter);

        mFirstNameTextView.setText("First Name");
        mLastNameTextView.setText("Last Name");
        mEmailTextView.setText("Email");

        //////////////added by Arthur///////////////////////////////////////////////////////////////////////////
        //NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //navigationView.setNavigationItemSelectedListener(this);
        //View header=navigationView.getHeaderView(0);

        usr = FirebaseAuth.getInstance().getCurrentUser();

        if(usr!=null){
            final DatabaseReference userprofileref = database.getReference("profile/"+usr.getUid());
            userprofileref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot ds) {
                    String fn=ds.child("firstName").getValue().toString();
                    String ln=ds.child("lastName").getValue().toString();

                    mFirstNameEditText.setText(fn);
                    mLastNameEditText.setText(ln);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        //////////////////////////////////////////////////////////////////////////////////////////
    }

    private void setupClickListeners() {
        //Edit Profile Button
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open or close the form.
                if(editForm.getVisibility()==View.INVISIBLE){
                    editForm.setVisibility(View.VISIBLE);
                }else{
                    //set invisible
                    editForm.setVisibility(View.INVISIBLE);
                }
            }
        });

        //Save Changes Button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fn = mFirstNameEditText.getText().toString();
                String ln =  mLastNameEditText.getText().toString();
                Profile profile = new Profile(mUser.getUid(), fn, ln, mCurrencySpinner.getSelectedItem().toString());
                writeProfile(profile);
                Toast.makeText(ProfileActivity.this, "you clicked save!", Toast.LENGTH_SHORT).show();
                //update labels
                mFirstNameTextView.setText( "first name: " + fn);
                mLastNameTextView.setText("last name: " + ln );
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
