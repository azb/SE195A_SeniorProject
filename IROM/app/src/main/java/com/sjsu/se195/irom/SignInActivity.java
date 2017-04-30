package com.sjsu.se195.irom;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import com.sjsu.se195.irom.signup.SignUpActivity;


public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "emailpassword";
    private Button signInButton;
    private EditText email;
    private EditText password;
    private TextView forgotPassword;
    private TextView signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        //auth client for firebase user
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Intent intent = new Intent( getBaseContext(), WelcomeActivity.class);
                    startActivity(intent);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }

            }
        };

        //set up edit texts (email and password fields)
        setUpFields();

    }

    private void setUpFields() {
        email = (EditText) findViewById(R.id.signin_email);
        //if focus isnt on email, close kb
        email.setOnFocusChangeListener(new View.OnFocusChangeListener(){

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(view);
                }else{
                    showKeyboard(view);
                }
            }
        });
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


            }

            @Override
            public void afterTextChanged(Editable s) {
                password.setError(null);

            }

        });
        password.setOnFocusChangeListener(new View.OnFocusChangeListener(){

            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    hideKeyboard(view);
                }else{
                    showKeyboard(view);
                }
            }
        });
        password.setOnEditorActionListener(new TextView.OnEditorActionListener(){

            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_DONE) {
                  /* Write your logic here that will be executed when user taps next button */
                    signInAttempt();

                    handled = true;
                }
                return handled;
            }
        });
        //set up button and button click function
        signInButton = (Button) findViewById(R.id.signin_button);
        signInButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //sign in attempt
                signInAttempt();
            }
        });
        //set up forgot password stuff
        forgotPassword = (TextView) findViewById(R.id.forgot_pw_text);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(SignInActivity.this);
                dialog.setContentView(R.layout.dialog_forgot_pw);
                dialog.setCanceledOnTouchOutside(true);
                dialog.setTitle("Password reset");

                Button button = (Button) dialog.findViewById(R.id.dialog_ok);
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        EditText edit=(EditText) dialog.findViewById(R.id.forgot_email);
                        String text=edit.getText().toString();
                        if(android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()){
                            mAuth.sendPasswordResetEmail(text)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                Log.d("forgotpwdialog", "Email sent.");
                                            }
                                        }
                                    });
                            Toast.makeText(SignInActivity.this, "Password request sent to "+text+".", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }


                    }
                });


                dialog.show();

            }
        });
        //set up sign up link
        signUp = (TextView) findViewById(R.id.sign_up_text);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( getBaseContext(), SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void signInAttempt() {
        //check email for being empty or not a valid email
        String em = email.getText().toString();
        String pw = password.getText().toString();
        if (em.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(em).matches()){
            email.requestFocus();
            email.setError("a valid email is required");
        }else if(pw.isEmpty() || pw.length()<6){
            password.requestFocus();
            password.setError("passwords should be 6 or more characters");
        }else{
            email.setError(null);
            password.setError(null);
            signIn(email.getText().toString(), password.getText().toString());


        }
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
