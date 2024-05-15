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
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.animalhealth.R
import com.example.animalhealth.adapters.PetSpinnerAdapter
import com.example.animalhealth.clases.Booking
import com.example.animalhealth.clases.Pet
import com.example.animalhealth.clases.User
import com.example.animalhealth.clases.Utilities
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_client_add_booking, container, false)
        val sharedPreferences = requireActivity().getSharedPreferences("sharedPreferences",Context.MODE_PRIVATE)
        var startHour = ""
        val dbReference = Firebase.database.reference
        val backButton = view.findViewById<ImageView>(R.id.bookingBackButton)
        val calendarView = view.findViewById<CalendarView>(R.id.bookingCalendar)
        var bookingDate = ""
        var reason = ""
        var navController = findNavController()
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
            Log.d("DATE", bookingDate)
            if (actualDate >= selectedDate){
                Toast.makeText(context, "Hay que reservar con al menos un dia de antelación", Toast.LENGTH_SHORT).show()
            }else{
                bookingDate = selectedDate
            }
        }

        backButton.setOnClickListener {
            navController.navigate(R.id.action_clientAddBookingFragment_to_clientClinicsFragment)
            Log.d("BACK", "Back button clicked")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                user = Utilities.obtainUser(dbReference)!!

                bookingList = getBookingsForClinic(dbReference)
                Log.d("BOOKINGGlobalScope", bookingList.toString())

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
                        Log.d("PET", pet.toString())
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        pet = Pet()
                    }
                }

                Log.d("BOOKING", bookingList.toString())
                for (booking in bookingList) {
                    ocupatedHours.add(booking.startHour)
                }

                val hours = resources.getStringArray(R.array.booking_hours_array)
                val flterList = mutableListOf<String>()
                for (hour in hours) {
                    if (!ocupatedHours.contains(hour)) {
                        flterList.add(hour)
                    }
                }
                val hoursAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, flterList)
                hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                startHourSpinner.adapter = hoursAdapter

                startHourSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (ocupatedHours.contains(parent?.getItemAtPosition(position).toString())) {
                            Toast.makeText(context, "Esta hora ya está reservada", Toast.LENGTH_SHORT).show()
                            return
                        }
                        startHour = parent?.getItemAtPosition(position).toString()
                        Log.d("HOUR", startHour)
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
                        Log.d("REASON", reason)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        reason = ""
                    }
                }

            } catch (e: Exception) {
                Log.e("ERROR", e.message.toString())
            }
        }

        val addPet = view.findViewById<AppCompatButton>(R.id.addBookingButton)
        addPet.setOnClickListener {
            if (bookingDate == "" || startHour == "" || pet.id == "" || reason == "") {
                Toast.makeText(context, "Por favor llene todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                val bookingId = dbReference.child("Bookings").push().key
                val booking = Booking(bookingId!!, reason, bookingDate, startHour, clinicId, FirebaseAuth.getInstance().uid.toString(), pet.id, user.img)
                viewLifecycleOwner.lifecycleScope.launch {
                    Utilities.saveBooking(booking, dbReference)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Reserva realizada con éxito", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    return view
    }

    suspend fun getBookingsForClinic(dbReference: DatabaseReference): MutableList<Booking> {
        return suspendCancellableCoroutine { continuation ->
            val bookingsRef = dbReference.child("Bookings")

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var bookingList = mutableListOf<Booking>()
                    if (snapshot.exists()) {
                        bookingList = snapshot.children.mapNotNull { it.getValue(Booking::class.java) }.toMutableList()
                    }
                    continuation.resume(bookingList)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(error.toException())
                }
            }

            bookingsRef.addListenerForSingleValueEvent(listener)

            continuation.invokeOnCancellation {
                bookingsRef.removeEventListener(listener)
            }
        }
    }
}