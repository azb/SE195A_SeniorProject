package com.sjsu.se195.irom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.irom.Classes.Item;
import com.sjsu.se195.irom.Classes.Listing;

import java.util.Locale;

public class InventoryItemDetailActivity extends NavigationDrawerActivity {
    private static final String TAG = InventoryItemDetailActivity.class.getSimpleName();
    private Item item;
    private Bitmap image;
    private ImageView itemImage;
    private TextView itemName;
    private TextView itemDesc;
    private TextView itemCreatedDateLabel;
    private TextView itemCreatedDate;
    private Button itemListingButton;
    private TextView itemQuantity;
    private DatabaseReference mDatabaseRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        item = getIntent().getParcelableExtra("item");
        image = getIntent().getParcelableExtra("image");
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_inventory_item_detail, null, false);
        this.drawer.addView(contentView, 0);

        initializeAndFillInInfo(item);
    }

    private void initializeAndFillInInfo(final Item item) {
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        itemImage = (ImageView) findViewById(R.id.item_detail_picture);
        itemImage.setImageBitmap(image);

        itemName = (TextView) findViewById(R.id.item_detail_name);
        itemName.setText(item.getName());

        itemDesc = (TextView) findViewById(R.id.item_detail_desc);
        itemDesc.setText(item.getNote());

        itemCreatedDateLabel = (TextView) findViewById(R.id.item_detail_date_label);

        itemCreatedDate = (TextView) findViewById(R.id.item_detail_date);
        itemCreatedDate.setText(item.getDateAdded().toString());

        itemQuantity = (TextView) findViewById(R.id.item_detail_quantity);
        itemQuantity.setText(String.valueOf(item.getQuantity()));

        itemListingButton = (Button) findViewById(R.id.item_detail_button);
        if (item.getForSale()) {
            //do the things if it is already for sale
            itemListingButton.setText("View Listing");

        } else {
            //do the things if not for sale
            itemListingButton.setText("Create Listing");
        }

        itemListingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.getForSale()) {
                    if (item.listingID != null) {
                        FirebaseDatabase.getInstance().getReference("listings/" + item.listingID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Listing listing = dataSnapshot.getValue(Listing.class);
                                // Move to Listing detail activity
                                Intent i = new Intent(InventoryItemDetailActivity.this, ListingDetailActivity.class);
                                // Add to bundle
                                Bundle b = new Bundle();
                                b.putParcelable("listing", listing);
                                b.putParcelable("image", ((BitmapDrawable) itemImage.getDrawable()).getBitmap());
                                i.putExtras(b);
                                startActivity(i);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                } else {
                    // If not for sale, want to make listing
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(R.layout.create_listing_popup, null);

                    // Create popup
                    PopupWindow listingPopup = new PopupWindow();
                    listingPopup.setContentView(layout);
                    listingPopup.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
                    listingPopup.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
                    listingPopup.setFocusable(true);
                    listingPopup.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

                    // Display in center
                    listingPopup.showAtLocation(layout, Gravity.CENTER, 0, 0);

                    // Set listener
                    Button submit = (Button) layout.findViewById(R.id.submit_button);
                    final EditText descriptionField = (EditText) layout.findViewById(R.id.description);
                    final EditText priceField = (EditText) layout.findViewById(R.id.price);
                    if (item.savedDescription != null) {
                        descriptionField.setText(item.savedDescription);
                        priceField.setText(String.format(Locale.US, "$%.2f", item.savedPrice));
                    }
                    submit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!(TextUtils.isEmpty(descriptionField.getText()) && TextUtils.isEmpty(priceField.getText()))) {
                                createListing(item, descriptionField.getText().toString(), Double.parseDouble(priceField.getText().toString()));
                            } else {
                                Toast.makeText(InventoryItemDetailActivity.this, "Fill in the fields!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void createListing(final Item item, final String description, final double price) {
        // Get key first and set to current Item object
        DatabaseReference itemsReference = FirebaseDatabase.getInstance().getReference("items/");
        final DatabaseReference listingsReference = FirebaseDatabase.getInstance().getReference("listings/");
        final String key = listingsReference.push().getKey();

        // Update Item first
        item.listingID = key;
        item.forSale = true;
        itemsReference.child(item.itemID).setValue(item).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Create and upload listing
                final Listing listing = new Listing(item.uID, item, description, price);
                // TODO: Set date once that's figured out
                listingsReference.child(key).setValue(listing).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Finally, go to listing detail view on success
                        Intent i = new Intent(InventoryItemDetailActivity.this, ListingDetailActivity.class);
                        // Bundle stuff
                        Bundle b = new Bundle();
                        b.putParcelable("listing", listing);
                        b.putParcelable("image", ((BitmapDrawable) (itemImage).getDrawable()).getBitmap());
                        i.putExtras(b);
                        startActivity(i);
                    }
                });
            }
        });
    }
}
