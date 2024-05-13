package com.example.animalhealth.fragments.client

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CalendarView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import com.example.animalhealth.R
import com.example.animalhealth.adapters.PetSpinnerAdapter
import com.example.animalhealth.clases.Booking
import com.example.animalhealth.clases.Pet
import com.example.animalhealth.clases.User
import com.example.animalhealth.clases.Utilities
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
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
        val dbReference = Firebase.database.reference
        val calendarView = view.findViewById<CalendarView>(R.id.bookingCalendar)
        var bookingDate = ""
        var reason = ""
        var clinicId = sharedPreferences.getString("clinicId","").toString()
        var pet = Pet()
        var actualUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        var user = User()
        GlobalScope.launch {
            user = Utilities.obtainUser(dbReference)!!
        }
        var startHourSpinner = view.findViewById<Spinner>(R.id.bookingStartHourSpinner)
        var reasonSpinner = view.findViewById<Spinner>(R.id.bookingReasonSpinner)
        var petSelectedSpinner = view.findViewById<Spinner>(R.id.bookingPetSpinner)

        var ocupatedHours = mutableListOf<String>()
        var bookingList = mutableListOf<Booking>()

        val date = Calendar.getInstance()
        val actualDate = "${date.get(Calendar.DAY_OF_MONTH)}/${date.get(Calendar.MONTH) + 1}/${date.get(Calendar.YEAR)}"

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            bookingDate = selectedDate
            if (actualDate > bookingDate){
                Toast.makeText(context, "No puedes reservar antes de hoy", Toast.LENGTH_SHORT).show()
            }
        }

        GlobalScope.launch {
            bookingList = Utilities.getBooking(dbReference,clinicId)
            Log.d("BookingList", bookingList.toString())
            dbReference.child("Pets").child(actualUser).get().addOnSuccessListener {
                if (it.exists()){
                    val pets = it.children.map { it.getValue(Pet::class.java)!! }
                    val adapter = PetSpinnerAdapter(requireContext(), R.layout.item_pet, pets)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    petSelectedSpinner.adapter = adapter
                }
            }.addOnFailureListener {
                Log.e("ERROR", it.message.toString())
            }
        }

        for (booking in bookingList){
            ocupatedHours.add(booking.startHour)
        }

        petSelectedSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                pet = parent?.getItemAtPosition(position) as Pet
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                pet = Pet()
            }
        }

        var hours = resources.getStringArray(R.array.booking_hours_array)
        var flterList = mutableListOf<String>()
        for (hour in hours){
            if (!ocupatedHours.contains(hour)){
                flterList.add(hour)
                Log.d("HOUR", hour)
            }
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, flterList)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        startHourSpinner.adapter = adapter

        startHourSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (ocupatedHours.contains(parent?.getItemAtPosition(position).toString())){
                    Toast.makeText(context, "Esta hora ya está reservada", Toast.LENGTH_SHORT).show()
                    return
                }
                startHour = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                startHour = ""
            }
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.booking_types_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            reasonSpinner.adapter = adapter
        }

        reasonSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                reason = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                reason = ""
            }
        }

        val addPet = view.findViewById<AppCompatButton>(R.id.addBookingButton)

        addPet.setOnClickListener {
            if (bookingDate == "" || startHour == "" || pet.id == "" || reason == ""){
                Toast.makeText(context, "Por favor llene todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                var bookingId = dbReference.child("Bookings").push().key
                var booking = Booking(bookingId!!,reason,bookingDate,startHour,clinicId,FirebaseAuth.getInstance().uid.toString(),pet.id,user.img)
                GlobalScope.launch {
                    Utilities.saveBooking(booking,dbReference)
                    GlobalScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "Reserva realizada con éxito", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    return view
    }

}