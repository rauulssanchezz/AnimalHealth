package com.example.animalhealth.fragments.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.animalhealth.R
import com.example.animalhealth.clases.Clinic

class ClientClinicInfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_client_clinic_info, container, false)

        val data = arguments?.getParcelable<Clinic>("clinic")

        val nameEdit = view.findViewById<TextView>(R.id.clinicName)
        val addressEdit = view.findViewById<TextView>(R.id.clinicAddress)
        val ratingBar = view.findViewById<RatingBar>(R.id.clinicRating)
        val button = view.findViewById<TextView>(R.id.reserveButton)
        nameEdit.text = data?.name
        addressEdit.text = data?.location
        ratingBar.rating = data?.rate?.toFloat()!!

        button.setOnClickListener {

        }

        return view
    }
}