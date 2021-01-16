package com.example.cabapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

public class DriverLoginRegisterActivity extends AppCompatActivity {

    Button loginButton,registerButton;
    EditText emailDriver, pswdDriver;
    private ProgressDialog loadingBar;
    private FirebaseAuth m_Auth;
    private String onlineDriverID;
    private DatabaseReference driverDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_register);
        loginButton=findViewById(R.id.loginDriver);
        registerButton=findViewById(R.id.registerDriver);
        emailDriver =findViewById(R.id.emailDriver);
        pswdDriver =findViewById(R.id.pswdDriver);
        m_Auth=FirebaseAuth.getInstance();
        loadingBar=new ProgressDialog(this);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=emailDriver.getText().toString();
                String password=pswdDriver.getText().toString();
                registerDriver(email,password);
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=emailDriver.getText().toString();
                String password=pswdDriver.getText().toString();
                loginDriver(email,password);
            }
        });
    }
    private void registerDriver(@NotNull String email, String password) {
        if(email.isEmpty())
        {
            Toast.makeText(this, "Email should not be empty", Toast.LENGTH_SHORT).show();
        }
        else if(password.isEmpty())
        {
            Toast.makeText(this, "Password should be empty", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Driver Registration");
            loadingBar.setMessage("Please wait while we are authenticating");
            loadingBar.show();
            m_Auth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {

                                onlineDriverID =m_Auth.getCurrentUser().getUid();
                                driverDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(onlineDriverID);
                                driverDatabaseReference.setValue(true);

                                Intent intent = new Intent(DriverLoginRegisterActivity.this, SettingsActivity.class);
                                intent.putExtra("type", "Drivers");
                                startActivity(intent);
                            }
                            else {
                                loadingBar.dismiss();
                                Toast.makeText(DriverLoginRegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void loginDriver(@NotNull String email, String password) {
        if(email.isEmpty())
        {
            Toast.makeText(this, "Email should not be empty", Toast.LENGTH_SHORT).show();
        }
        else if(password.isEmpty())
        {
            Toast.makeText(this, "Password should not be empty", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Driver Login");
            loadingBar.setMessage("Please wait while we are authenticating");
            loadingBar.show();
            m_Auth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                loadingBar.dismiss();
                                Toast.makeText(DriverLoginRegisterActivity.this, "Driver Login Successfully", Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(DriverLoginRegisterActivity.this,DriverMapActivity.class);
                                startActivity(intent);
                            }
                            else {
                                loadingBar.dismiss();
                                Toast.makeText(DriverLoginRegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
