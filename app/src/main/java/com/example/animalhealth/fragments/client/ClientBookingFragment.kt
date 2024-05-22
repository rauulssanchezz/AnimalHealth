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

        val dbRef = Firebase.database.reference
        val recycler = view.findViewById<RecyclerView>(R.id.bookingRecyclerViewClient)
        val bookingList = mutableListOf<Booking>()
        val adapter = BookingAdapter(bookingList)

        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter

        // Leer los datos desde Firebase y agregarlos a bookingList
        dbRef.child("Bookings").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookingList.clear()
                snapshot.children.forEach { child ->
                    val booking = child.getValue(Booking::class.java)
                    if (booking?.ownerId == FirebaseAuth.getInstance().currentUser!!.uid) {
                        booking?.let {
                            bookingList.add(it)
                        }
                    }
                }
                // Ordenar las reservas por fecha en formato "YYYY-MM-DD"
                bookingList.sortByDescending { it.date }

                // Notificar al adaptador sobre los cambios en la lista
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", error.message)
            }
        })

        // Inflate the layout for this fragment
        return view
    }



}