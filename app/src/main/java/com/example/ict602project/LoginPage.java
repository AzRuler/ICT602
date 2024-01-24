package com.example.ict602project;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginPage extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button loginButton;
    private TextView registerText ,forgotText;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        loginButton = findViewById(R.id.loginButton);
        registerText = findViewById(R.id.registerTextView);
        forgotText= findViewById(R.id.txtForgot);

        fAuth = FirebaseAuth.getInstance();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                login();
            }
        });

        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openRegisterActivity();
            }
        });

        forgotText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginPage.this, ForgotPage.class));
            }
        });

    }


    private void login() {
        // Retrieve user inputs
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        //  basic validation
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        fAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login successful
                        Toast.makeText(LoginPage.this, "Login successful", Toast.LENGTH_SHORT).show();


                        Intent intent = new Intent(LoginPage.this, MainPage.class);
                        startActivity(intent);
                    } else {

                        if (task.getException() != null) {
                            String errorMessage = task.getException().getMessage();


                            if (errorMessage.contains("password")) {
                                // Incorrect password
                                Toast.makeText(LoginPage.this, "Incorrect password.", Toast.LENGTH_SHORT).show();
                            } else if (errorMessage.contains("no user")) {
                                // User is not registered
                                Toast.makeText(LoginPage.this, "User not registered.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Other authentication errors
                                Toast.makeText(LoginPage.this, "Authentication failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

    }


    private void openRegisterActivity() {
        Intent intent = new Intent(this, RegisterPage.class);
        startActivity(intent);
    }
}
