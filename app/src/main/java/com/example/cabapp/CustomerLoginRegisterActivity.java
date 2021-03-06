package com.example.cabapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
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

public class CustomerLoginRegisterActivity extends AppCompatActivity {

    private TextView customerLoginText,forgotPswd;
    private Button loginButton,registerButton;
    private EditText emailCustomer,pswdCustomer;
    private FirebaseAuth m_Auth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login_register);
        customerLoginText =findViewById(R.id.customerLoginText);
        forgotPswd=findViewById(R.id.frgtpswdCustomer);
        loginButton=findViewById(R.id.loginBtnCustomer);
        registerButton=findViewById(R.id.registerBtnCustomer);
        emailCustomer=findViewById(R.id.emailCustomer);
        pswdCustomer=findViewById(R.id.passwordCustomer);
        m_Auth=FirebaseAuth.getInstance();
        loadingBar=new ProgressDialog(this);

        registerButton.setVisibility(View.GONE);
        forgotPswd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customerLoginText.setText("Customer's Reg");
                loginButton.setVisibility(View.GONE);
                forgotPswd.setVisibility(View.GONE);
                registerButton.setVisibility(View.VISIBLE);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=emailCustomer.getText().toString();
                String password=pswdCustomer.getText().toString();
                registerCustomer(email,password);
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=emailCustomer.getText().toString();
                String password=pswdCustomer.getText().toString();
                loginCustomer(email,password);
            }
        });
    }

    private void registerCustomer(@NotNull String email, String password) {
        if(email.isEmpty())
        {
            Toast.makeText(this, "Email should not be empty", Toast.LENGTH_SHORT).show();
        }
        else if(password.isEmpty())
        {
            Toast.makeText(this, "Password should be empty", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Customer Registration");
            loadingBar.setMessage("Please wait while we are authenticating");
            loadingBar.show();
            m_Auth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                loadingBar.dismiss();
                                Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Reg Successfully", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                loadingBar.dismiss();
                                Toast.makeText(CustomerLoginRegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
            loadingBar.setTitle("Customer Login");
            loadingBar.setMessage("Please wait while we are authenticating");
            loadingBar.show();
            m_Auth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                loadingBar.dismiss();
                                Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Login Successfully", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                loadingBar.dismiss();
                                Toast.makeText(CustomerLoginRegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
