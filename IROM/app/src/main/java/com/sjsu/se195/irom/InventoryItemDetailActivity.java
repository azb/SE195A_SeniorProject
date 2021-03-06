package com.sjsu.se195.irom;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sjsu.se195.irom.Classes.Item;
import com.sjsu.se195.irom.Classes.Listing;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class InventoryItemDetailActivity extends NavigationDrawerActivity {
    private static final String TAG = InventoryItemDetailActivity.class.getSimpleName();
    private final long ONE_MEGABYTE = 1024 * 1024; // Max image download size to avoid issues
    private TextView itemName;
    private TextView itemNotes;
    private TextView itemDateLabel;
    private TextView itemDate;
    private TextView itemQuantity;
    private TextView itemSold;
    private ImageView itemImage;
    private Button itemListingButton;
    private Button itemListingDeleteButton;
    private View itemDivider;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Drawer setup
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_inventory_item_detail, null, false);
        this.drawer.addView(contentView, 0);

        // Set up layout items
        itemName = (TextView) findViewById(R.id.item_detail_name);
        itemNotes = (TextView) findViewById(R.id.item_detail_notes);
        itemDateLabel = (TextView) findViewById(R.id.item_detail_date_label);
        itemDate = (TextView) findViewById(R.id.item_detail_date);
        itemQuantity = (TextView) findViewById(R.id.item_detail_quantity);
        itemSold = (TextView) findViewById(R.id.item_detail_sold);
        itemImage = (ImageView) findViewById(R.id.item_detail_image);
        itemListingButton = (Button) findViewById(R.id.item_detail_button);
        itemListingDeleteButton = (Button) findViewById(R.id.item_detail_delete_button);
        itemDivider = findViewById(R.id.item_detail_divider);
        progressBar = (ProgressBar) findViewById(R.id.item_detail_progressbar);

        // Set up progress dialog
        progressDialog = new ProgressDialog(InventoryItemDetailActivity.this, R.style.AppTheme_Dialog);
        progressDialog.setIndeterminate(true);

        // Start loading while image is downloaded
        startLoading();

        // Get data
        refreshData(getIntent());
    }

    private void startLoading() {
        itemName.setVisibility(View.INVISIBLE);
        itemNotes.setVisibility(View.INVISIBLE);
        itemDateLabel.setVisibility(View.INVISIBLE);
        itemDate.setVisibility(View.INVISIBLE);
        itemQuantity.setVisibility(View.INVISIBLE);
        itemSold.setVisibility(View.INVISIBLE);
        itemImage.setVisibility(View.INVISIBLE);
        itemListingButton.setVisibility(View.INVISIBLE);
        itemListingDeleteButton.setVisibility(View.INVISIBLE);
        itemDivider.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        itemName.setVisibility(View.VISIBLE);
        itemNotes.setVisibility(View.VISIBLE);
        itemDateLabel.setVisibility(View.VISIBLE);
        itemDate.setVisibility(View.VISIBLE);
        itemQuantity.setVisibility(View.VISIBLE);
        itemSold.setVisibility(View.VISIBLE);
        itemImage.setVisibility(View.VISIBLE);
        itemListingButton.setVisibility(View.VISIBLE);
        itemListingDeleteButton.setVisibility(View.VISIBLE);
        itemDivider.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onRestart(){
        super.onRestart();
        startLoading();
        refreshData(getIntent());
    }

    private void refreshData(final Intent intent) {
        if (intent.hasExtra("item")) {
            // First get updated listing due to time sensitivity of marketplace, as well as image
            final Item item = intent.getParcelableExtra("item");
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference ref = db.getReference().child("items/").child(item.itemID);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Item updatedItem = dataSnapshot.getValue(Item.class);
                    // Get image so higher quality instead of passing through bundle
                    StorageReference imageRef = FirebaseStorage.getInstance().getReference("items/" + updatedItem.itemID);
                    imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            // Check to see if there's a listing we need to handle too
                            if (updatedItem.forSale) {
                                initializeWithListing(updatedItem, image);
                            } else {
                                // No listing has been made for the item yet
                                initializeWithoutListing(updatedItem, image);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Something went wrong downloading the image!");
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "Error downloading item: " + databaseError.toString());
                }
            });
        }
    }

    private void initializeWithListing(final Item item, final Bitmap image) {
        // First, get the up-to-date listing as well
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference().child("listings/").child(item.listingID);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Listing updatedListing = dataSnapshot.getValue(Listing.class);

                // Now, fill out all the views
                itemName.setText(item.name);
                itemNotes.setText("Notes: " + item.note);
                itemDate.setText(new SimpleDateFormat("MMM d, yyyy hh:mm a", Locale.US).format(item.dateAdded));
                itemQuantity.setText(String.format(Locale.US, "Quantity: %d", item.quantity));
                itemImage.setImageBitmap(image);
                itemListingButton.setText("View Listing");
                itemListingDeleteButton.setText("Remove Listing");

                // Set up button handlers
                itemListingButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Move to Listing detail activity
                        Intent i = new Intent(InventoryItemDetailActivity.this, ListingDetailActivity.class);
                        // Add to bundle
                        Bundle b = new Bundle();
                        b.putParcelable("listing", updatedListing);
                        i.putExtras(b);
                        startActivity(i);
                    }
                });

                itemListingDeleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteListing(item);
                    }
                });

                stopLoading();

                if (!updatedListing.isLive) {
                    itemSold.setText("Listing has been SOLD!");
                } else {
                    itemSold.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error downloading listing: " + databaseError.toString());
            }
        });
    }

    private void initializeWithoutListing(final Item item, Bitmap image) {
        // No listing to get, just fill out views
        itemName.setText(item.name);
        itemNotes.setText("Notes: " + item.note);
        itemDate.setText(new SimpleDateFormat("MMM d, yyyy hh:mm a", Locale.US).format(item.dateAdded));
        itemQuantity.setText(String.format(Locale.US, "Quantity: %d", item.quantity));
        itemImage.setImageBitmap(image);
        itemListingButton.setText("Create Listing");
        itemListingDeleteButton.setText("Delete");

        // Set up button handlers
        itemListingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create listing setup
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.create_listing_popup, null);

                // Set up view and listener
                Button submit = (Button) layout.findViewById(R.id.submit_button);
                final EditText descriptionField = (EditText) layout.findViewById(R.id.description);
                final EditText priceField = (EditText) layout.findViewById(R.id.price);
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

                // Set saved IROMazon data if present
                if (item.savedDescription != null) {
                    descriptionField.setText(item.savedDescription);
                    priceField.setText(String.format(Locale.US, "%.2f", item.savedPrice));
                }

                // Create dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(InventoryItemDetailActivity.this);
                builder.setView(layout);
                builder.show();
            }
        });

        itemListingDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem(item);
            }
        });

        stopLoading();

        // No listing that could possibly be sold yet
        itemSold.setVisibility(View.GONE);
    }

    private void createListing(final Item item, final String description, final double price) {
        // Show dialog
        progressDialog.setMessage("Creating...");
        progressDialog.show();

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
                listing.listID = key;
                listingsReference.child(key).setValue(listing).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Finally, go to listing detail view on success
                        Intent i = new Intent(InventoryItemDetailActivity.this, ListingDetailActivity.class);
                        // Bundle stuff
                        Bundle b = new Bundle();
                        b.putParcelable("listing", listing);
                        i.putExtras(b);

                        // Hide progress dialog
                        progressDialog.hide();

                        startActivity(i);
                    }
                });
            }
        });
    }

    private void deleteItem(final Item item) {
        // Show progress dialog
        progressDialog.setMessage("Deleting...");
        progressDialog.show();

        // First delete the item
        DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("items/" + item.itemID);
        itemRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Then if successful delete the image
                StorageReference imageRef = FirebaseStorage.getInstance().getReference("items/" + item.itemID);
                imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Successfully deleted, move back to inventory
                        Intent i = new Intent(InventoryItemDetailActivity.this, InventoryActivity.class);

                        // Hide progress dialog
                        progressDialog.hide();

                        startActivity(i);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Something went wrong!
                        progressDialog.hide();
                        Toast.makeText(InventoryItemDetailActivity.this, "Error deleting image", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Error deleting item image: " + e);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Something went wrong!
                progressDialog.hide();
                Toast.makeText(InventoryItemDetailActivity.this, "Error deleting item", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Error deleting item: " + e);
            }
        });
    }

    private void deleteListing(final Item item) {
        // Show progress dialog
        progressDialog.setMessage("Deleting...");
        progressDialog.show();

        // First delete listing
        DatabaseReference listingRef = FirebaseDatabase.getInstance().getReference("listings/" + item.listingID);
        listingRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Then, need to update item data to reflect that
                item.forSale = false;
                item.listingID = null;

                DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("items/" + item.itemID);
                itemRef.setValue(item).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Hide progress dialog
                        progressDialog.hide();

                        // Item successfully updated, refresh the page
                        onRestart();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Something went wrong!
                        progressDialog.hide();
                        Toast.makeText(InventoryItemDetailActivity.this, "Error updating item", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Error updating item: " + e);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Something went wrong!
                progressDialog.hide();
                Toast.makeText(InventoryItemDetailActivity.this, "Error deleting listing", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Error deleting listing: " + e);
            }
        });
    }
}
