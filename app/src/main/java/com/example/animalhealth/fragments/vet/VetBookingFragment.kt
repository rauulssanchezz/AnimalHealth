package com.example.animalhealth.fragments.vet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.animalhealth.R
import com.example.animalhealth.clases.Booking
import com.google.firebase.database.DatabaseReference

class VetBookingFragment : Fragment() {
    private lateinit var dbRef : DatabaseReference
    private lateinit var list : MutableList<Booking>
    private lateinit var recycler : RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_vet_booking, container, false)

        return view
    }

}