package com.example.animalhealth.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.animalhealth.VetMainActivity
import com.example.animalhealth.R
import com.example.animalhealth.clases.User
import com.example.animalhealth.clases.Utilities
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoadingFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_loading, container, false)

        var db_ref = Firebase.database.reference
        var user:User
        val sharedPreferences = requireContext().getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        CoroutineScope(Dispatchers.IO).launch {
            user = Utilities.obtainUser(db_ref)
            with(sharedPreferences.edit()){
                putString("Name",user.name)
                putString("Email",user.email)
                putString("Password",user.password)
                putString("Type",user.type)
                putString("Img",user.img)
                apply()
            }
            withContext(Dispatchers.Main){
                val newIntent = Intent(requireActivity(),VetMainActivity::class.java)
                startActivity(newIntent)
            }
        }

        return view
    }
}