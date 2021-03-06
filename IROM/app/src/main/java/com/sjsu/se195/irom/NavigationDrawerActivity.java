package com.sjsu.se195.irom;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    protected FirebaseUser usr;
    protected DrawerLayout drawer;
    protected TextView navHeaderEmail;
    protected TextView navHeaderName;
    protected ImageView navheaderProfilePic;
    private final long ONE_MEGABYTE = 1024 * 1024; // Max image download size to avoid issues
    //protected TextView navHeaderLastName;
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();
    private static final String TAG = NavigationDrawerActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header=navigationView.getHeaderView(0);

        usr = FirebaseAuth.getInstance().getCurrentUser();
        navHeaderEmail = (TextView) header.findViewById(R.id.nav_header_user_email);
        navHeaderName = (TextView) header.findViewById(R.id.nav_bar_name);
        navheaderProfilePic = (ImageView) header.findViewById(R.id.nav_bar_header_profile_pic);
        //navHeaderLastName = (TextView) header.findViewById(R.id.nav_bar_last_name);

        if(usr!=null){
            final DatabaseReference userprofileref = database.getReference("profile/"+usr.getUid());
            final StorageReference userProfilePicRef = storage.getReference("profile/"+usr.getUid());
            userprofileref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot ds) {
                    String fn=ds.child("firstName").getValue().toString();
                    String ln=ds.child("lastName").getValue().toString();
                    navHeaderName.setText(fn +" "+ ln);
                    //navHeaderLastName.setText(ln);

                    navHeaderEmail.setText(usr.getEmail());
                    // Get the image
                    final Bitmap[] b = new Bitmap[1];
                    userProfilePicRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            navheaderProfilePic.setImageBitmap(Bitmap.createScaledBitmap(bmp, (bmp.getWidth() / 4), (bmp.getHeight() / 4), true));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Something went wrong downloading the image!");
                            // Still want to put the listing up since quite a few still do not have images currently
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


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
        if (id == R.id.action_logout){
          logout();
        }

        return super.onOptionsItemSelected(item);
    }

    public void logout(){
        FirebaseAuth.getInstance().signOut();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null){
            //correctly logged out
            Intent intent = new Intent( getBaseContext(), SignInActivity.class);
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

//        if (id == R.id.nav_item) {
//            //handle item activity
//            Intent intent = new Intent(getBaseContext(), ItemActivity.class);
//            startActivity(intent);
//        }
        if (id == R.id.nav_inventory) {
            //go to inventory activity
            Intent intent = new Intent(getBaseContext(), InventoryActivity.class);
            startActivity(intent);
        }
//        else if (id == R.id.nav_listing) {
//            //go to listing activity
//            Intent intent = new Intent(getBaseContext(), ListingActivity.class);
//            startActivity(intent);
//        }
// else if (id == R.id.nav_manage) {
//            //TODO amazon activity intent
//            Toast.makeText(NavigationDrawerActivity.this,"This is currently under construction",Toast.LENGTH_SHORT).show();
//            //Intent intent = new Intent(getBaseContext(), AmazonActivity.class);
//            //startActivity(intent);
//        }
        else if (id == R.id.nav_profile) {
            Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
            startActivity(intent);
        //} else if (id == R.id.nav_vision) {
        //    Intent intent = new Intent(getBaseContext(), CloudVisionTestActivity.class);
        //    startActivity(intent);
        } else if (id == R.id.nav_marketplace) {
            Intent intent = new Intent(getBaseContext(), MarketplaceActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_cloud_vision) {
            Intent intent = new Intent(getBaseContext(), IROMazonSearchActivity.class);
            startActivity(intent);
        }

//        else if(id == R.id.nav_pay){
//            Intent intent = new Intent(getBaseContext(), PaymentTestActivity.class);
//            Bundle bundle = new Bundle();
//            String listing_id = "-KiqEnj9EPavAqF117kT";
//            Double price = 100.00;
//            bundle.putString("listing_id",listing_id);
//            bundle.putDouble("price",price);
//            intent.putExtras(bundle);
//            startActivity(intent);
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
