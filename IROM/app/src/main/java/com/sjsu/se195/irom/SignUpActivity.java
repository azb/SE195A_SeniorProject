package com.sjsu.se195.irom;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignUpActivity extends AppCompatActivity {
    private EditText email;
    private EditText password1;
    private EditText password2;
    private EditText firstName;
    private EditText lastName;
    private Button signUpButton;
    private Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setUpFieldsAndButtons();

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
                //all fields listed as if null are required fields
                if(email.getText().toString().isEmpty() || password1.getText().toString().isEmpty()|| password2.getText().toString().isEmpty() || firstName.getText().toString().isEmpty() || lastName.getText().toString().isEmpty()){
                    Toast.makeText(v.getContext(), "enter all * fields", Toast.LENGTH_SHORT).show();

                }else if(!password1.getText().toString().matches(password2.getText().toString())){
                    //if the two password fields don't match do the exclamation mark error
                    password1.setError("must match passwords");
                }else{
                    Toast.makeText(v.getContext(), "you clicked sign up successfully!", Toast.LENGTH_SHORT).show();
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

}
