package com.example.ict602project;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
public class Splash extends AppCompatActivity {



    LottieAnimationView lottie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_page);



        lottie = findViewById(R.id.lottie);


       //appname.animate().setDuration(2000).start();
        lottie.animate().start();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(getApplicationContext(), LoginPage.class);
                startActivity(i);
                finish(); //nak stop kan animation
            }
        }, 3000);
    }
}