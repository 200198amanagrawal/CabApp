package com.example.cabapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

public class DriverLoginRegisterActivity extends AppCompatActivity {

    TextView driverLoginText,forgotPswd;
    Button loginButton,registerButton;
    EditText emailDriver, pswdDriver;
    private ProgressDialog loadingBar;
    private FirebaseAuth m_Auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_register);
        driverLoginText=findViewById(R.id.driverLoginText);
        forgotPswd=findViewById(R.id.forgotPswdDriver);
        loginButton=findViewById(R.id.loginDriver);
        registerButton=findViewById(R.id.registerDriver);
        emailDriver =findViewById(R.id.emailDriver);
        pswdDriver =findViewById(R.id.pswdDriver);
        m_Auth=FirebaseAuth.getInstance();
        loadingBar=new ProgressDialog(this);
        registerButton.setVisibility(View.GONE);
        forgotPswd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverLoginText.setText("Driver's Reg");
                loginButton.setVisibility(View.GONE);
                forgotPswd.setVisibility(View.GONE);
                registerButton.setVisibility(View.VISIBLE);
            }
        });
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
                loginCustomer(email,password);
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
                                loadingBar.dismiss();
                                Toast.makeText(DriverLoginRegisterActivity.this, "Customer Reg Successfully", Toast.LENGTH_SHORT).show();
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

    private void loginCustomer(@NotNull String email, String password) {
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
