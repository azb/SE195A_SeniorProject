package com.sjsu.se195.irom;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpActivity extends AppCompatActivity {
    private EditText email;
    private EditText password1;
    private EditText password2;
    private EditText firstName;
    private EditText lastName;
    private Button signUpButton;
    private Button signInButton;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "emailpassword";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setUpFieldsAndButtons();

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    //logged in already, MOVE ALONG!
                    //TODO make this work!
                    //I think auth is supposed to be for 24 hrs, so I am not sure why this isnt working on launch for my phone.
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
        password2 = (EditText) findViewById(R.id.password_2_edit_text);
        firstName = (EditText) findViewById(R.id.first_name_edit_text);
        lastName = (EditText) findViewById(R.id.last_name_edit_text);
        signUpButton = (Button) findViewById(R.id.sign_up_button);
        signInButton = (Button) findViewById(R.id.sign_in_button);

        signUpButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                //check validity of input
                if(isInputValid()){
                    //then sign up your heart away
                    mAuth.createUserWithEmailAndPassword(email.getText().toString(), password1.getText().toString())
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(SignUpActivity.this, R.string.auth_failed,
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    // ...

                                }
                            });

                }

            }
        });
        signInButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                Intent intent = new Intent( getBaseContext(), SignInActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean isInputValid() {
        //do wanted validity checks here

        if(email.getText().toString().isEmpty() || password1.getText().toString().isEmpty()|| password2.getText().toString().isEmpty()){
            //all fields listed as if null are required fields
            email.setError("please input all * fields");
            return false;
        }
        if(!password1.getText().toString().matches(password2.getText().toString())){
            //if the two password fields don't match do the exclamation mark error
            password1.setError("must match passwords");
            return false;
        }
        return true;
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


}
