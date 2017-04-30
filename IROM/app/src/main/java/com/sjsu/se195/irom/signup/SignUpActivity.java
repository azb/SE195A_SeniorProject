package com.sjsu.se195.irom.signup;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sjsu.se195.irom.Classes.Profile;
import com.sjsu.se195.irom.R;
import com.sjsu.se195.irom.SignInActivity;
import com.sjsu.se195.irom.WelcomeActivity;

public class SignUpActivity extends AppCompatActivity {
    private EditText email;
    private EditText password1;
    private EditText password2;
    private EditText firstName;
    private EditText lastName;
    private Button signUpButton;
    private TextView signInText;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private static final String TAG = "signup";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setUpFieldsAndButtons();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    //logged in already, MOVE ALONG!
                    Intent intent = new Intent( getBaseContext(), WelcomeActivity.class);
                    startActivity(intent);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

    }

    private void setUpFieldsAndButtons() {
        email = (EditText) findViewById(R.id.email_edit_text);
        password1 = (EditText) findViewById(R.id.password_edit_text);
      //  password2 = (EditText) findViewById(R.id.password_2_edit_text);
        firstName = (EditText) findViewById(R.id.first_name_edit_text);
        lastName = (EditText) findViewById(R.id.last_name_edit_text);
        signUpButton = (Button) findViewById(R.id.sign_up_button);
        signInText = (TextView) findViewById(R.id.sign_in_text);

        //if not clicked on these things get rid of the keyboard!
        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    hideKeyboard(view);
                }else{
                    showKeyboard(view);
                }
            }
        });
        password1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    hideKeyboard(view);
                }else{
                    showKeyboard(view);
                }
            }
        });
        firstName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    hideKeyboard(view);
                }else{
                    showKeyboard(view);
                }
            }
        });
        lastName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    hideKeyboard(view);
                }else{
                    showKeyboard(view);
                }
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                //hide the keyboard
                hideKeyboard(v);

                //check validity of input
                if(email.getText().toString().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
                    //all fields listed as if null are required fields
                    email.requestFocus();
                    email.setError("a valid email is required");
                }
                else if(password1.getText().toString().isEmpty() || password1.length()<6){
                    //if the two password fields don't match do the exclamation mark error
                    password1.requestFocus();
                    password1.setError("passwords should be 6 or more characters");
                }else if(firstName.getText().toString().isEmpty()){
                    firstName.requestFocus();
                    firstName.setError("please enter a first name");
                }else if(lastName.getText().toString().isEmpty()){
                    lastName.requestFocus();
                    lastName.setError("please enter a last name");
                }
                else{
                    //then sign up your heart away
                    email.setError(null);
                    password1.setError(null);
                    firstName.setError(null);
                    lastName.setError(null);
                    mAuth.createUserWithEmailAndPassword(email.getText().toString(), password1.getText().toString())
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(SignUpActivity.this, "There was an error making an account.",
                                                Toast.LENGTH_SHORT).show();
                                    }else{
                                        Profile p = new Profile(mAuth.getCurrentUser().getUid(),firstName.getText().toString(),lastName.getText().toString());
                                        mDatabase.child("profile").child(mAuth.getCurrentUser().getUid()).setValue(p);
                                        Intent intent = new Intent( getBaseContext(), FirstTimePictureActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            });
                }
            }
        });

        //go to sign in page
        signInText.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                Intent intent = new Intent( getBaseContext(), SignInActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    //these are used for hiding and showing the keyboard
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    private void showKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, 0);
    }
}
