package com.christianas.tiplearning.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.christianas.tiplearning.Fragments.HomeFragment;
import com.christianas.tiplearning.Fragments.PurchasedFragment;
import com.christianas.tiplearning.Fragments.TutorialsFragment;
import com.christianas.tiplearning.R;
import com.christianas.tiplearning.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

import me.ibrahimsn.lib.OnItemSelectedListener;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, new HomeFragment());
        transaction.commit();

        binding.bottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int i) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                switch (i) {
                    case 0:
                        getSupportActionBar().setTitle("Dashboard");
                        transaction.replace(R.id.content, new HomeFragment());
                        transaction.commit();
                        break;
                    case 1:
                        getSupportActionBar().setTitle("Tutorials");
                        transaction.replace(R.id.content, new TutorialsFragment());
                        transaction.commit();
                        break;
                    case 2:
                        getSupportActionBar().setTitle("Purchased Tutorials");
                        transaction.replace(R.id.content, new PurchasedFragment());
                        transaction.commit();
                        break;

                }
                return false;
            }
        });

    }
}