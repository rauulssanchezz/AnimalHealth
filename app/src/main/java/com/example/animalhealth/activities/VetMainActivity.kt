package com.example.animalhealth.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.animalhealth.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class VetMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE)
        val darkMode = sharedPreferences.getInt("Theme", AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setDefaultNightMode(darkMode)
        setContentView(R.layout.activity_vet_main)

        val navHostFragment=
            supportFragmentManager.findFragmentById(R.id.vet_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavigationView.setupWithNavController(navController)
    }
}