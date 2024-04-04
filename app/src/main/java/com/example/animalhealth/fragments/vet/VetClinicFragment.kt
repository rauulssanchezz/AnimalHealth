package com.example.animalhealth.fragments.vet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.example.animalhealth.R

class VetClinicFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_vet_clinic, container, false)

        val navController = findNavController()

        val addClinic = view.findViewById<CardView>(R.id.addClinic)

        addClinic.setOnClickListener {
            navController.navigate(R.id.vetClinicFragment_to_vetAddClinicFragment)
        }

        return view
    }
}