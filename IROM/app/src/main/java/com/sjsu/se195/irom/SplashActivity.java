package com.sjsu.se195.irom;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * This is a splash screen followed by:
 * https://www.bignerdranch.com/blog/splash-screens-the-right-way/
 * Created by Krystle on 11/12/2016.
 */
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }
}
