package com.example.cabapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {

    Button driverLoginButton;
    Button customerLoginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        driverLoginButton=findViewById(R.id.welcome_driver);
        customerLoginButton=findViewById(R.id.welcome_customer);

        startService(new Intent(WelcomeActivity.this, onAppKilled.class));

        customerLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this,CustomerLoginRegisterActivity.class));
            }
        });
        driverLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this,DriverLoginRegisterActivity.class));
            }
        });
    }
}
