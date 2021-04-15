package com.christianas.tiplearning.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import com.christianas.tiplearning.Utils.Utils;
import com.christianas.tiplearning.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    private String email, password;

    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser()!=null){
            goToMain();
        }

        binding.forgotPw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(),0);
                email = binding.email.getText().toString();
                password = binding.password.getText().toString();

                if(email.length()==0 || password.length()==0){
                    Snackbar.make(binding.mainLayout,"All fields are required",Snackbar.LENGTH_LONG)
                            .show();
                }
                else if(!Utils.isEmailVerified(email)){
                    Snackbar.make(binding.mainLayout,"Please enter a valid email",Snackbar.LENGTH_LONG)
                            .show();
                }else{
                    Utils.initpDialog(LoginActivity.this,"Please wait...");
                    Utils.showpDialog();
                    loginUser(email, password);
                }
            }
        });

        binding.signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,SignUpActivity.class));
            }
        });

    }

    private void goToMain(){
        startActivity(new Intent(LoginActivity.this,MainActivity.class));
        finish();
        finishAffinity();
    }

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Utils.hidepDialog();
                    goToMain();
                }else{
                    Utils.hidepDialog();
                    Utils.showSnackBar(binding.mainLayout,"Some error occurred");
                }
            }
        });
    }
}