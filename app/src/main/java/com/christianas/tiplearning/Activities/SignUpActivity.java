package com.christianas.tiplearning.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.christianas.tiplearning.Utils.Utils;
import com.christianas.tiplearning.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.SimpleFormatter;


public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    private String email, firstname, lastname, password, conPass;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private static String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignUpActivity.super.onBackPressed();
            }
        });

        binding.signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstname = binding.firstname.getText().toString();
                lastname = binding.lastname.getText().toString();
                email = binding.email.getText().toString();
                password = binding.password.getText().toString();
                conPass = binding.conPass.getText().toString();

                if(!Utils.isEmailVerified(email)){
                    Utils.showSnackBar(binding.mainLayout,"Please enter a valid email");
                }else{
                    if(password.length()<6){
                        Utils.showSnackBar(binding.mainLayout,"Password should be at least 6 characters");
                    }
                    else if(!password.equals(conPass)){
                        Utils.showSnackBar(binding.mainLayout,"Password not match.");
                    }else{
                        Utils.initpDialog(SignUpActivity.this,"Please wait...");
                        Utils.showpDialog();
                        registerUser(email, password,firstname,lastname);
                    }
                }

            }
        });

    }

    private void registerUser(String email, String password, String firstname, String lastname) {

        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    //send verification email to the user
                    auth.getCurrentUser().sendEmailVerification();

                    String uid = task.getResult().getUser().getUid();
                    firestore = FirebaseFirestore.getInstance();

                    Map<String, Object> user = new HashMap<>();
                    user.put("id",uid);
                    user.put("first_name", firstname);
                    user.put("last_name", lastname);
                    user.put("email", email);
                    user.put("created_at",Utils.getCurrentTimeStamp());
                    user.put("startDate","");
                    user.put("endDate","");
                    user.put("type","");
                    user.put("price","");
                    user.put("currency","");
                    user.put("isSubscribed",false);
                    user.put("subscriptionCode","");

                    firestore.collection("users")
                            .document(uid)
                            .set(user)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Utils.hidepDialog();
                                    if(task.isSuccessful()){
                                        startActivity(new Intent(SignUpActivity.this,MainActivity.class));
                                        finish();
                                        finishAffinity();

                                    }else {
                                        Utils.showSnackBar(binding.mainLayout,"Some error occurred");
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Utils.hidepDialog();
                                    Utils.showSnackBar(binding.mainLayout,"Some error occurred");
                                    Log.w(TAG, "Error adding document", e);
                                }
                            });

                }else{
                    Utils.hidepDialog();
                    Utils.showSnackBar(binding.mainLayout,"Credentitals Already exists");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.hidepDialog();
                Log.i(TAG,"Some error occurred");
            }
        });

    }

}