package com.example.animalhealth.fragments.client

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.animalhealth.R
import com.example.animalhealth.adapters.BookingAdapter
import com.example.animalhealth.clases.Booking
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.ktx.database


class ClientBookingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_client_booking, container, false)

        var bookingList = mutableListOf<Booking>()
        val recycler = view.findViewById<RecyclerView>(R.id.bookingRecyclerViewClient)
        val dbRef = Firebase.database.reference

        dbRef.child("Bookings")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    bookingList.clear()
                    snapshot.children.forEach { hijo: DataSnapshot?
                        ->
                        val pojo_clinic = hijo?.getValue(Booking::class.java)
                        Log.d("Booking", pojo_clinic.toString())
                        if (pojo_clinic?.ownerId == FirebaseAuth.getInstance().currentUser!!.uid) {
                            bookingList.add(pojo_clinic!!)
                        }
                    }
                    recycler.adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }
            })

        val adapter = BookingAdapter(bookingList)
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter


        // Inflate the layout for this fragment
        return view
    }

}