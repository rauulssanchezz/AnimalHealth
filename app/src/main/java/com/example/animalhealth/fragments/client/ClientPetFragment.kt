package com.example.animalhealth.fragments.client

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.example.animalhealth.R

class ClientPetFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_client_pet, container, false)
        val navController=findNavController()
        val addPet = view?.findViewById<CardView>(R.id.addPet)
        Log.d("Boton","listo")
        addPet!!.setOnClickListener {
            Log.d("Boton","pulsado")
            navController.navigate(R.id.action_clientPetFragment_to_clientAddPetFragment)
        }
        return view
    }
}