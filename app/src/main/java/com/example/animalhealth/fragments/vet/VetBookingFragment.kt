package com.example.animalhealth.fragments.vet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.animalhealth.R
import com.example.animalhealth.adapters.BookingAdapter
import com.example.animalhealth.clases.Booking
import com.example.animalhealth.clases.Clinic
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class VetBookingFragment : Fragment() {
    private lateinit var dbRef : DatabaseReference
    private lateinit var bookingList : MutableList<Booking>
    private lateinit var recycler : RecyclerView
    private lateinit var adapter : BookingAdapter
    private var clinic : Clinic?=null
    private lateinit var dateText : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_vet_booking, container, false)

        val date = Calendar.getInstance()
        val actualDate = "${date.get(Calendar.DAY_OF_MONTH)}/${date.get(Calendar.MONTH) + 1}/${date.get(
            Calendar.YEAR)}"

        val dateCardView = view.findViewById<CardView>(R.id.bookingCalendar)
        dateText = view.findViewById<TextView>(R.id.date)
        val calendarView = view.findViewById<CalendarView>(R.id.bookingCalendarView)

        calendarView.visibility = View.GONE

        dateText.text = actualDate

        dateCardView.setOnClickListener {
            Log.d("Calendar", "Clicked")
            if (calendarView.visibility == View.VISIBLE) {
                calendarView.visibility = View.GONE
                recycler.visibility = View.VISIBLE
                Log.d("Calendar", "Gone")
            } else {
                calendarView.visibility = View.VISIBLE
                recycler.visibility = View.GONE
                Log.d("Calendar", "Visible")
            }
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val newDate = "$dayOfMonth/${month + 1}/$year"
            dateText.text = newDate
            loadBookings(newDate)
        }

        dbRef = FirebaseDatabase.getInstance().reference
        bookingList = mutableListOf()

        recycler = view.findViewById(R.id.vetBookingRecyclerView)
        adapter = BookingAdapter(bookingList)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(requireActivity())

        lifecycleScope.launch {
            clinic = getClinicForUser(dbRef, FirebaseAuth.getInstance().currentUser!!.uid)
            loadBookings(actualDate)
        }

        return view
    }

    private suspend fun getClinicForUser(db_ref: DatabaseReference, id: String): Clinic? {
        return suspendCancellableCoroutine { continuation ->
            val clinicsRef = db_ref.child("Clinics")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var clinic: Clinic? = null
                    snapshot.children.forEach { child: DataSnapshot? ->
                        val pojo_clinic = child?.getValue(Clinic::class.java)
                        if (pojo_clinic?.vetId == id) {
                            clinic = pojo_clinic
                        }
                    }
                    continuation.resume(clinic)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(error.toException())
                }
            }
            clinicsRef.addListenerForSingleValueEvent(listener)

            continuation.invokeOnCancellation {
                clinicsRef.removeEventListener(listener)
            }
        }
    }

    private fun loadBookings(date: String) {
        dbRef.child("Bookings")
            .orderByChild("clinicId")
            .equalTo(clinic?.id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    bookingList.clear()
                    snapshot.children.forEach { child: DataSnapshot? ->
                        val booking = child?.getValue(Booking::class.java)
                        if (booking?.date == date) {
                            bookingList.add(booking!!)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", error.message)
                }
            })
    }
}