package com.example.runningtracker.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.runningtracker.db.*;

import android.os.Bundle;

import com.example.runningtracker.R;
import com.example.runningtracker.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            switch(item.getItemId()){
                case R.id.navHome:
                    replaceFragment(new HomeFragment());
                    break;
                case R.id.navRun:
                    replaceFragment(new RunFragment());
                    break;
                case R.id.navMetrics:
                    replaceFragment(new MetricsFragment());
                    break;
            }

            return true;
        });
    }

    private void replaceFragment (Fragment fragment){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}