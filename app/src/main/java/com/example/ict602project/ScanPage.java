package com.example.ict602project;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ict602project.CartItem;
import com.example.ict602project.CartListAdapter;
import com.example.ict602project.MainPage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.Locale;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import android.media.MediaPlayer;
import android.net.Uri;
public class ScanPage extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    private DatabaseReference databaseCart;
    private FirebaseAuth mAuth;
    private ArrayList<CartItem> cartItems;
    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
        mediaPlayer = MediaPlayer.create(this, R.raw.scan_sound);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String userEmail = user.getEmail().replace('.', ',');
            databaseCart = FirebaseDatabase.getInstance().getReference("carts").child(userEmail);
        } else {
            // Handle the case where the user is not signed in
        }

        cartItems = new ArrayList<>();
    }

    @Override
    public void handleResult(Result rawResult) {
        final int scannedBarcode = Integer.parseInt(rawResult.getText());

        if (mAuth.getCurrentUser() != null) {
            checkItemInDatabase(scannedBarcode);
            playScanSound();
        } else {

        }
    }
    private void playScanSound() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }
    private void checkItemInDatabase(int scannedBarcode) {
        DatabaseReference databaseItems = FirebaseDatabase.getInstance().getReference("items");
        databaseItems.child(String.valueOf(scannedBarcode)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    showButtons(scannedBarcode);
                } else {
                    // Item does not exist in the database
                    Log.v("TAG", "Item does not exist in the database: " + scannedBarcode);

                    AlertDialog.Builder builder = new AlertDialog.Builder(ScanPage.this);
                    builder.setTitle("Scan Result");
                    builder.setMessage("Item does not exist in the database: " + scannedBarcode);

                    builder.setPositiveButton("OK", (dialog, which) -> {
                        mScannerView.resumeCameraPreview(ScanPage.this);
                    });

                    AlertDialog alert1 = builder.create();
                    alert1.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "Error checking if item exists in the database", error.toException());
            }
        });
    }

    private void showButtons(int scannedBarcode) {
        DatabaseReference databaseItems = FirebaseDatabase.getInstance().getReference("items");
        databaseItems.child(String.valueOf(scannedBarcode)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String itemName = snapshot.child("itemName").getValue(String.class);
                    double itemPrice = snapshot.child("itemPrice").getValue(Double.class);

                    AlertDialog.Builder buttonBuilder = new AlertDialog.Builder(ScanPage.this);
                    buttonBuilder.setTitle("Scan Result");
                    buttonBuilder.setMessage("Item: " + itemName + "\nPrice: RM" + String.format(Locale.US, "%.2f", itemPrice));

                    buttonBuilder.setPositiveButton("Add to Cart", (dialog, which) -> {
                        addScannedItemToCart(scannedBarcode, itemName, itemPrice);
                    });

                    buttonBuilder.setNegativeButton("Close", (dialog, which) -> {
                        mScannerView.resumeCameraPreview(ScanPage.this);
                    });

                    AlertDialog buttonDialog = buttonBuilder.create();
                    buttonDialog.show();
                } else {
                    Log.v("TAG", "Item details not found in the database: " + scannedBarcode);

                    AlertDialog.Builder builder = new AlertDialog.Builder(ScanPage.this);
                    builder.setTitle("Scan Result");
                    builder.setMessage("Item details not found in the database: " + scannedBarcode);

                    builder.setPositiveButton("OK", (dialog, which) -> {
                        mScannerView.resumeCameraPreview(ScanPage.this);
                    });

                    AlertDialog alert1 = builder.create();
                    alert1.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "Error getting item details from the database", error.toException());
            }
        });
    }

    private void addScannedItemToCart(int scannedBarcode, String itemName, double itemPrice) {
        DatabaseReference userCartRef = databaseCart.child(itemName);

        userCartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot cartSnapshot) {
                if (cartSnapshot.exists()) {
                    // Item already in cart
                    Log.v("TAG", "Item already in cart: " + itemName);

                    int currentQuantity = cartSnapshot.child("quantity").getValue(Integer.class);
                    increaseCartItemQuantity(userCartRef, currentQuantity);

                    AlertDialog.Builder builder = new AlertDialog.Builder(ScanPage.this);
                    builder.setTitle("Scan Result");
                    builder.setMessage("Item already in cart: " + itemName + "\nQuantity: " + (currentQuantity + 1));

                    builder.setPositiveButton("OK", (dialog, which) -> {
                        mScannerView.resumeCameraPreview(ScanPage.this);
                    });

                    AlertDialog alert1 = builder.create();
                    alert1.show();
                } else {
                    // Item not in cart, add it with quantity 1
                    userCartRef.child("itemName").setValue(itemName);
                    userCartRef.child("itemPrice").setValue(itemPrice);
                    userCartRef.child("quantity").setValue(1);

                    cartItems.add(new CartItem(itemName, itemPrice, 1));

                    AlertDialog.Builder builder = new AlertDialog.Builder(ScanPage.this);
                    builder.setTitle("Scan Result");
                    builder.setMessage("Item added to cart: " + itemName + "\nQuantity: 1");

                    builder.setPositiveButton("OK", (dialog, which) -> {
                        new android.os.Handler().postDelayed(() -> {
                            mScannerView.resumeCameraPreview(ScanPage.this);
                        }, 500);
                    });

                    AlertDialog alert1 = builder.create();
                    alert1.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "Error checking if item exists in the cart", error.toException());
            }
        });
    }

    private void increaseCartItemQuantity(DatabaseReference userCartRef, int currentQuantity) {
        // Increase the quantity of the existing item in the cart
        userCartRef.child("quantity").setValue(currentQuantity + 1);
    }





    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ScanPage.this, MainPage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}
