package com.example.animalhealth.fragments.vet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.animalhealth.R
import com.example.animalhealth.adapters.BookingAdapter
import com.example.animalhealth.adapters.ClinicAdapter
import com.example.animalhealth.clases.Booking
import com.example.animalhealth.clases.Clinic
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class VetBookingFragment : Fragment() {
    private lateinit var dbRef : DatabaseReference
    private lateinit var list : MutableList<Booking>
    private lateinit var recycler : RecyclerView
    private lateinit var adapter : BookingAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_vet_booking, container, false)

        dbRef = FirebaseDatabase.getInstance().reference
        list = mutableListOf<Booking>()
        recycler = view.findViewById(R.id.vetBookingRecyclerView)

        list.clear()
        dbRef.child("Bookings")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { hijo: DataSnapshot?
                        ->
                        val pojo_clinic = hijo?.getValue(Booking::class.java)
                        list.add(pojo_clinic!!)
                    }
                    recycler.adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }
            })

        adapter = BookingAdapter(list)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(requireActivity())

        return view
    }

}