package com.sjsu.se195.irom;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

    private EditText email;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        //figure out later if we want toolbar in activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //auth client for firebase user
        mAuth = FirebaseAuth.getInstance();
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
        password = (EditText) findViewById(R.id.signin_password_text);
        //set up button and button click function
        Button signInButton = (Button) findViewById(R.id.signin_button);
        signInButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (email.getText().toString().isEmpty() || password.getText().toString().isEmpty()){
                    Toast.makeText(view.getContext(),"enter email and password",Toast.LENGTH_SHORT).show();
                }else{
                    signIn(email.getText().toString(), password.getText().toString());
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user!=null){
                        Intent intent = new Intent( getBaseContext(), WelcomeActivity.class);
                        startActivity(intent);
                    }

                }
                Toast.makeText(view.getContext(),"you clicked sign in",Toast.LENGTH_SHORT).show();
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
        //maybe check validity here instead of oncreate...
        mAuth.signInWithEmailAndPassword(e,p).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                if (task.isSuccessful()){
                }
                //if the task isn't successful
                if(!task.isSuccessful()){
                    Log.w(TAG, "signInWithEmail:failed", task.getException());
                    Toast.makeText(SignInActivity.this,"auth failed",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
