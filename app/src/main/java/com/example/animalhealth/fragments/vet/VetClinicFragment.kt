package com.example.animalhealth.fragments.vet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.example.animalhealth.R
import com.example.animalhealth.clases.Clinic
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class VetClinicFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_vet_clinic, container, false)

        val navController = findNavController()
        val dbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        val addClinic = view.findViewById<CardView>(R.id.addClinic)
        var clinic = Clinic()
        val sharedPreferences = requireActivity().getSharedPreferences("sharedPreferences", 0)

        addClinic.setOnClickListener {
            navController.navigate(R.id.vetClinicFragment_to_vetAddClinicFragment)
        }

        dbRef.child("Clinics").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { hijo: DataSnapshot?
                    ->
                    val pojo_clinic = hijo?.getValue(Clinic::class.java)
                    if (pojo_clinic?.vetId == FirebaseAuth.getInstance().currentUser!!.uid) {
                        clinic = pojo_clinic!!
                        sharedPreferences.edit().putString("clinicId", clinic.id).apply()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println(error.message)
            }
        })

        return view
    }
}