package com.example.animalhealth.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.animalhealth.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class ClientMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_main)

        val navController = findNavController(R.id.nav_host_fragment_activity_navigation_client)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.client_bottom_navigation_view)
        bottomNavigationView.setupWithNavController(navController)

    }
}