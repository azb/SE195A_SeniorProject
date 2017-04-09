package com.sjsu.se195.irom;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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


public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "emailpassword";
    private Button signInButton;
    private EditText email;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        //figure out later if we want toolbar in activity
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        //auth client for firebase user
        mAuth = FirebaseAuth.getInstance();
        //this is redundant because of the stuff in the buttons. i think.
        // TODO fix the redundancies in the sign in button and the auth user here
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }

            }
        };

        //set up edit texts (email and password fields)
        email = (EditText) findViewById(R.id.signin_email);
        //when you change stuff, remove that error
        email.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                email.setError(null);

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                email.setError(null);

            }

            });
        password = (EditText) findViewById(R.id.signin_password_text);
        //when you change stuff, remove that error
        password.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                password.setError(null);

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                password.setError(null);

            }

        });
        //set up button and button click function
        signInButton = (Button) findViewById(R.id.signin_button);
        signInButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //check email for being empty or not a valid email
                String em = email.getText().toString();
                String pw = password.getText().toString();
                if (em.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(em).matches()){
                    email.setError("a valid email is required");
                }else if(pw.isEmpty() || pw.length()<6){
                    password.setError("passwords should be 6 or more characters");
                }else{
                    email.setError(null);
                    password.setError(null);
                    signIn(email.getText().toString(), password.getText().toString());


                }
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

    private void signIn(String e, String p){
        Log.d(TAG, "signIn:" + e);

        signInButton.setEnabled(false);
        //maybe check validity here instead of oncreate...
        mAuth.signInWithEmailAndPassword(e,p).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                if (task.isSuccessful()){
                        Intent intent = new Intent( getBaseContext(), WelcomeActivity.class);
                        startActivity(intent);

                }
                //if the task isn't successful
                if(!task.isSuccessful()){
                    Log.w(TAG, "signInWithEmail:failed", task.getException());
                    signInButton.setEnabled(true);
                    Toast.makeText(SignInActivity.this, "Please enter a valid email and password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
