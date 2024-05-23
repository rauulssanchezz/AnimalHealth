package com.example.animalhealth.activities.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.animalhealth.R
import com.example.animalhealth.clases.Chat
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE)
        val darkMode = sharedPreferences.getInt("Theme", AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setDefaultNightMode(darkMode)
        setContentView(R.layout.activity_login)


    }
}