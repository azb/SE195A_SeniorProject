package com.sjsu.se195.irom;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Button logOutButton = (Button) findViewById(R.id.log_out_button);
        TextView email = (TextView) findViewById(R.id.currentEmailPasteHere);

        email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        //set up log out button and function
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user==null){
                    //correctly logged out
                    Intent intent = new Intent( getBaseContext(), SignUpActivity.class);
                    startActivity(intent);
                }else{
                    //there was a problem
                    Toast.makeText(WelcomeActivity.this, "Log out failed", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
