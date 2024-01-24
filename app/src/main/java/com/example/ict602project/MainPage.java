package com.example.ict602project;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
public class MainPage extends AppCompatActivity {

    private TextView welcomeTextView;
    private BottomNavigationView bottomNavigationView;
    private ListView cartListView;
    private CartListAdapter cartListAdapter;

    //private List<CartItem> cartItems = new ArrayList<>();
    private TextView totalAmountTextView;
    private DatabaseReference databaseCart;
    private FirebaseAuth mAuth;
    Button paybutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        welcomeTextView = findViewById(R.id.welcomeTextView);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        cartListView = findViewById(R.id.cartListView);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        paybutton = findViewById(R.id.payButton);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // User is signed in, initialize the database reference for the user's cart
            String userEmail = user.getEmail().replace('.', ',');
            databaseCart = FirebaseDatabase.getInstance().getReference("carts").child(userEmail);
        } else {

        }
        paybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String formattedTotalAmount = totalAmountTextView.getText().toString();
                generateAndShowBarcodePopup(formattedTotalAmount);
            }
        });



        // Set up the CartListAdapter
        cartListAdapter = new CartListAdapter(this, new ArrayList<>());

        cartListView.setAdapter(cartListAdapter);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.nav_cart) {
                    // since its already in MainPage
                } else if (itemId == R.id.nav_scan) {
                    startActivity(new Intent(MainPage.this, ScanPage.class));
                } else if (itemId == R.id.nav_settings) {
                    startActivity(new Intent(MainPage.this, SettingPage.class));
                }

                return true;
            }
        });


        fetchUserCartItems();

    }



    private void updateTotalAmount() {
        double totalAmount = cartListAdapter.calculateTotalAmount();
        String formattedTotalAmount = String.format(Locale.US, "Total: RM %.2f", totalAmount);
        totalAmountTextView.setText(formattedTotalAmount);

        // Update the visibility of the "Pay" button based on the total amount
        Button payButton = findViewById(R.id.payButton);
        payButton.setVisibility(formattedTotalAmount.equals("Total: RM 0.00") ? View.GONE : View.VISIBLE);
    }

    private void fetchUserCartItems() {

        if (mAuth.getCurrentUser() != null) {
            DatabaseReference userCartRef = databaseCart;

            userCartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Clear existing items in the adapter
                        cartListAdapter.clear();

                        // Iterate through cart items
                        for (DataSnapshot cartItemSnapshot : snapshot.getChildren()) {
                            CartItem cartItem = cartItemSnapshot.getValue(CartItem.class);
                            if (cartItem != null) {
                                // Add the cart item to the adapter
                                cartListAdapter.add(cartItem);
                            }
                        }
                        updateTotalAmount();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle database error
                }
            });
        }
    }


    public void onDeleteButtonClick(View view) {

        int position = cartListView.getPositionForView((View) view.getParent());
        CartItem deletedItem = (CartItem) cartListAdapter.getItem(position);
       // cartListAdapter.removeItem(position);

        // Update the total amount after item removal
        updateTotalAmount();

        // Create an AlertDialog to confirm deletion
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to delete this item?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User confirmed deletion

                // Remove the item from the database
                if (mAuth.getCurrentUser() != null) {
                    String userEmail = mAuth.getCurrentUser().getEmail().replace('.', ',');
                    DatabaseReference userCartRef = FirebaseDatabase.getInstance().getReference("carts").child(userEmail);

                    // Find the item in the database and decrement its quantity
                    if (deletedItem != null && deletedItem.getItemName() != null) {
                        DatabaseReference itemRef = userCartRef.child(deletedItem.getItemName());
                        itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    CartItem existingItem = snapshot.getValue(CartItem.class);
                                    if (existingItem != null) {
                                        // Decrement the quantity by 1
                                        int newQuantity = existingItem.getQuantity() - 1;

                                        if (newQuantity > 0) {
                                            // Update the quantity in the database
                                            itemRef.child("quantity").setValue(newQuantity);
                                        } else {
                                            // If the new quantity is 0, remove the item from the database
                                            userCartRef.child(deletedItem.getItemName()).removeValue();
                                        }

                                        // Remove the item from the list after successful deletion
                                        cartListAdapter.removeItem(position);

                                        // Update the total amount after item removal
                                        updateTotalAmount();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Handle database error
                            }
                        });
                    }
                }
            }
        });


        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User canceled deletion
                dialog.dismiss();
            }
        });

        // Show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void generateAndShowBarcodePopup(String totalAmount) {
        // Generate random barcode data
        String barcodeData = generateRandomBarcodeData();

        // Generate barcode
        Bitmap barcodeBitmap = generateBarcode(barcodeData);

        // Show barcode in a popup with the total amount
        showBarcodePopup(barcodeBitmap, totalAmount);
    }


    private String generateRandomBarcodeData() {
        // Starting
        String prefix = "QS";

        // random numeric part
        String numericCharacters = "0123456789";

        // Set the length of the random numeric part
        int numericLength = 7;

        // Generate random numeric
        StringBuilder randomNumericData = new StringBuilder();
        for (int i = 0; i < numericLength; i++) {
            int index = (int) (Math.random() * numericCharacters.length());
            randomNumericData.append(numericCharacters.charAt(index));
        }

        // Combine the prefix and the random numeric part to form the complete barcode
        return prefix + randomNumericData.toString();
    }


    private Bitmap generateBarcode(String data) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(data, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(bitMatrix);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showBarcodePopup(Bitmap barcodeBitmap, String totalAmount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.barcode_popup, null);

        ImageView imageViewBarcode = view.findViewById(R.id.imageViewBarcode);
        imageViewBarcode.setImageBitmap(barcodeBitmap);

        TextView labelTextView = view.findViewById(R.id.labelTextView);
        TextView totalAmountTextView = view.findViewById(R.id.totalAmountTextView);

        // Set the label text
        labelTextView.setText("Go to Kiosk for payment");

        // Set the total amount text
        totalAmountTextView.setText(totalAmount);

        builder.setView(view);
        builder.setTitle("Payment Barcode");

        builder.setPositiveButton("OK", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public void onBackPressed() {
        int selectedItemId = bottomNavigationView.getSelectedItemId();
        if (selectedItemId != R.id.nav_cart) {
            bottomNavigationView.setSelectedItemId(R.id.nav_cart);
        } else {
            super.onBackPressed();
        }
    }
}
