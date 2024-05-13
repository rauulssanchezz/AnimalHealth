package com.example.animalhealth.fragments.client

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import com.example.animalhealth.R
import com.example.animalhealth.clases.Booking
import com.example.animalhealth.clases.Utilities
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class ClientBookingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_client_booking, container, false)
        val sharedPreferences = requireActivity().getSharedPreferences("sharedPreferences",Context.MODE_PRIVATE)
        var startHour = ""
        var endHour = ""
        val dbReference = Firebase.database.reference
        val calendarView = view.findViewById<CalendarView>(R.id.bookingCalendar)
        var bookingDate = ""
        var clinicId = sharedPreferences.getString("clinicId","").toString()
        var actualUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        var startHourSpinner = view.findViewById<Spinner>(R.id.bookingStartHourSpinner)
        var endHourSpinner = view.findViewById<Spinner>(R.id.bookingEndHourSpinner)
        var reasonSpinner = view.findViewById<Spinner>(R.id.bookingReasonSpinner)

        val date = Calendar.getInstance()
        val actualDate = "${date.get(Calendar.DAY_OF_MONTH)}/${date.get(Calendar.MONTH) + 1}/${date.get(Calendar.YEAR)}"

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            bookingDate = selectedDate
            if (actualDate > bookingDate){
                Toast.makeText(context, "No puedes reservar antes de hoy", Toast.LENGTH_SHORT).show()
            }
        }

        val addPet = view.findViewById<AppCompatButton>(R.id.addBookingButton)

        addPet.setOnClickListener {
            if (bookingDate == "" || startHour == "" || endHour == "") {
                Toast.makeText(context, "Por favor llene todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                var bookingId = dbReference.child("Bookings").push().key
                var booking = Booking(bookingId!!,"falta implementar",bookingDate,"falta implementar","falta implementar",FirebaseAuth.getInstance().uid.toString(),"falta implementar","falta implementar")
                GlobalScope.launch {
                    Utilities.saveBooking(booking,dbReference)
                }
            }
        }

    return view
    }

}