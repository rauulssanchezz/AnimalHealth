package com.example.animalhealth.fragments.client

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.cardview.widget.CardView
import com.example.animalhealth.R
import com.example.animalhealth.clases.Booking
import com.example.animalhealth.clases.Utilities
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ClientBookingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_client_booking, container, false)

        val dbReference = Firebase.database.reference
        val calendarView = view.findViewById<CalendarView>(R.id.bookingCalendar)
        var date = ""
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            date = selectedDate
        }

        val addPet = view.findViewById<CardView>(R.id.addBooking)

        addPet.setOnClickListener {
            var bookingId = dbReference.child("Bookings").push().key
            var booking = Booking(bookingId!!,"falta implementar",date,"falta implementar","falta implementar",FirebaseAuth.getInstance().uid.toString(),"falta implementar","falta implementar")
            GlobalScope.launch {
                Utilities.saveBooking(booking,dbReference)
            }
        }

    return view
    }

}