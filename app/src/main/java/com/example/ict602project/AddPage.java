package com.example.ict602project;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddPage extends AppCompatActivity {
    EditText etName, etBarcode, etPrice;
    Button addBtn;
    DatabaseReference databaseItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_page);

        FirebaseApp.initializeApp(this);

        databaseItems = FirebaseDatabase.getInstance().getReference("items");


        etName = findViewById(R.id.etName);
        etBarcode = findViewById(R.id.etBarcode);
        etPrice = findViewById(R.id.etPrice);

        addBtn = findViewById(R.id.addBtn);


        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem();
            }
        });
    }

    private void addItem() {
        String name = etName.getText().toString().trim();
        String barcodeString = etBarcode.getText().toString().trim();
        String priceString = etPrice.getText().toString().trim();


        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(barcodeString) && !TextUtils.isEmpty(priceString)) {
            // Convert barcode to an int
            int barcode = Integer.parseInt(barcodeString);

            // Convert price to a float
            double price = Double.parseDouble(priceString);



            Item item = new Item(barcode, name, price);
            databaseItems.child(String.valueOf(barcode)).setValue(item);

            Toast.makeText(this, "Item added", Toast.LENGTH_LONG).show();
            recreate();
        } else {

            Toast.makeText(this, "Please enter all details", Toast.LENGTH_LONG).show();
        }
    }

}
