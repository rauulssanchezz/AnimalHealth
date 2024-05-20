package com.example.animalhealth.fragments.client

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.animalhealth.R
import com.example.animalhealth.adapters.PetSpinnerAdapter
import com.example.animalhealth.clases.Booking
import com.example.animalhealth.clases.Pet
import com.example.animalhealth.clases.User
import com.example.animalhealth.clases.Utilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ClientAddBookingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_client_add_booking, container, false)
        val sharedPreferences = requireActivity().getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        var startHour = ""
        val dbReference = Firebase.database.reference
        val backButton = view.findViewById<ImageView>(R.id.bookingBackButton)
        val calendarView = view.findViewById<CalendarView>(R.id.bookingCalendar)
        var bookingDate = ""
        var reason = ""
        val navController = findNavController()
        val clinicId = sharedPreferences.getString("clinicId", "").toString()
        var pet = Pet()
        val actualUser = FirebaseAuth.getInstance().currentUser?.uid.toString()
        var user = User()
        val startHourSpinner = view.findViewById<Spinner>(R.id.bookingStartHourSpinner)
        val reasonSpinner = view.findViewById<Spinner>(R.id.bookingReasonSpinner)
        val petSelectedSpinner = view.findViewById<Spinner>(R.id.bookingPetSpinner)

        val ocupatedHours = mutableListOf<String>()
        var bookingList = mutableListOf<Booking>()

        val date = Calendar.getInstance()
        val actualDate = "${date.get(Calendar.DAY_OF_MONTH)}/${date.get(Calendar.MONTH) + 1}/${date.get(Calendar.YEAR)}"

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            if (actualDate >= selectedDate) {
                Toast.makeText(context, "Hay que reservar con al menos un dia de antelación", Toast.LENGTH_SHORT).show()
            } else {
                bookingDate = selectedDate
                loadBookingsForDate(dbReference, clinicId, bookingDate, ocupatedHours, startHourSpinner)
            }
        }

        backButton.setOnClickListener {
            navController.navigate(R.id.action_clientAddBookingFragment_to_clientClinicsFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                user = Utilities.obtainUser(dbReference)!!

                val petsSnapshot = dbReference.child("Pets").child(actualUser).get().await()
                if (petsSnapshot.exists()) {
                    val pets = petsSnapshot.children.map { it.getValue(Pet::class.java)!! }
                    val adapter = PetSpinnerAdapter(requireContext(), R.layout.item_pet, pets)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    petSelectedSpinner.adapter = adapter
                }

                petSelectedSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        pet = parent?.getItemAtPosition(position) as Pet
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        pet = Pet()
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

            } catch (e: Exception) {
                Log.e("ERROR", e.message.toString())
            }
        }

        val addBookingButton = view.findViewById<AppCompatButton>(R.id.addBookingButton)
        addBookingButton.setOnClickListener {
            if (bookingDate.isEmpty() || startHour.isEmpty() || pet.id.isEmpty() || reason.isEmpty()) {
                Toast.makeText(context, "Por favor llene todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                val bookingId = dbReference.child("Bookings").push().key
                val booking = Booking(
                    bookingId!!, reason, bookingDate, startHour, clinicId, FirebaseAuth.getInstance().uid.toString(), pet.id, user.img
                )
                viewLifecycleOwner.lifecycleScope.launch {
                    Utilities.saveBooking(booking, dbReference)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Reserva realizada con éxito", Toast.LENGTH_SHORT).show()
                        navController.navigate(R.id.action_clientAddBookingFragment_to_clientClinicsFragment)
                    }
                }
            }
        }

        return view
    }

    private fun loadBookingsForDate(
        dbReference: DatabaseReference,
        clinicId: String,
        bookingDate: String,
        ocupatedHours: MutableList<String>,
        startHourSpinner: Spinner
    ) {
        dbReference.child("Bookings")
            .orderByChild("clinicId")
            .equalTo(clinicId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    ocupatedHours.clear()
                    snapshot.children.forEach { child ->
                        val booking = child.getValue(Booking::class.java)
                        if (booking?.date == bookingDate) {
                            ocupatedHours.add(booking.startHour)
                        }
                    }
                    updateHoursSpinner(startHourSpinner, ocupatedHours)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", error.message)
                }
            })
    }

    private fun updateHoursSpinner(startHourSpinner: Spinner, ocupatedHours: List<String>) {
        val hours = resources.getStringArray(R.array.booking_hours_array)
        val availableHours = hours.filter { !ocupatedHours.contains(it) }
        val hoursAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, availableHours)
        hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        startHourSpinner.adapter = hoursAdapter

        startHourSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedHour = parent?.getItemAtPosition(position).toString()
                if (ocupatedHours.contains(selectedHour)) {
                    Toast.makeText(context, "Esta hora ya está reservada", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}
