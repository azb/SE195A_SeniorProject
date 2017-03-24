package com.sjsu.se195.irom;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    protected FirebaseUser usr;
    protected DrawerLayout drawer;
    protected TextView navHeaderEmail;
    protected TextView navHeaderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header=navigationView.getHeaderView(0);

        usr = FirebaseAuth.getInstance().getCurrentUser();
        navHeaderEmail = (TextView) header.findViewById(R.id.nav_header_user_email);
        navHeaderName = (TextView) header.findViewById(R.id.nav_header_user_name);
        if(usr!=null){
            navHeaderEmail.setText(usr.getEmail());
            String name = usr.getDisplayName();
            navHeaderName.setText(name);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_logout){
          logout();
        }

        return super.onOptionsItemSelected(item);
    }

    public void logout(){
        FirebaseAuth.getInstance().signOut();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null){
            //correctly logged out
            Intent intent = new Intent( getBaseContext(), SignUpActivity.class);
            startActivity(intent);
        }else{
            //there was a problem
            Toast.makeText(getBaseContext(), "Log out failed", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_item) {
            //handle item activity
            Intent intent = new Intent(getBaseContext(), ItemActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_inventory) {
            //go to inventory activity
            Intent intent = new Intent(getBaseContext(), InventoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_listing) {
            //go to listing activity
            Intent intent = new Intent(getBaseContext(), ListingActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_manage) {
            //TODO amazon activity intent
            Toast.makeText(NavigationDrawerActivity.this,"This is currently under construction",Toast.LENGTH_SHORT).show();
            //Intent intent = new Intent(getBaseContext(), AmazonActivity.class);
            //startActivity(intent);
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
            startActivity(intent);
        } else if(id == R.id.nav_vision){
            Intent intent = new Intent(getBaseContext(), CloudVisionTestActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
