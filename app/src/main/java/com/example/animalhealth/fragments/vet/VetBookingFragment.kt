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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.animalhealth.R
import com.example.animalhealth.adapters.BookingAdapter
import com.example.animalhealth.clases.Booking
import com.example.animalhealth.clases.Clinic
import com.example.animalhealth.clases.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.GlobalScope
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
        val dateText = view.findViewById<TextView>(R.id.date)
        val calendarView = view.findViewById<CalendarView>(R.id.bookingCalendarView)

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

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val newDate = "$dayOfMonth/${month + 1}/$year"
            dateText.text = newDate
        }

        dbRef = FirebaseDatabase.getInstance().reference
        bookingList = mutableListOf()
        var clinic : Clinic?=null
        GlobalScope.launch {
            clinic = getClinicForUser(dbRef, FirebaseAuth.getInstance().currentUser!!.uid)

            Log.d("ClinicId", clinic?.id.toString())
            dbRef.child("Bookings")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        bookingList.clear()
                        snapshot.children.forEach { hijo: DataSnapshot?
                            ->
                            val pojo_clinic = hijo?.getValue(Booking::class.java)
                            Log.d("Booking", pojo_clinic.toString())
                            if (pojo_clinic?.clinicId == clinic?.id && pojo_clinic?.date == dateText.text.toString()) {
                                bookingList.add(pojo_clinic!!)
                            }
                        }
                        recycler.adapter?.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.message)
                    }
                })
        }

        recycler = view.findViewById(R.id.vetBookingRecyclerView)
        adapter = BookingAdapter(bookingList)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(requireActivity())

        return view
    }
    private suspend fun getClinicForUser(db_ref: DatabaseReference, id : String): Clinic? {
        return suspendCancellableCoroutine { continuation ->
            val clinicsRef = db_ref.child("Clinics")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var clinic: Clinic? = null
                    snapshot.children.forEach { hijo: DataSnapshot? ->
                        val pojo_clinic = hijo?.getValue(Clinic::class.java)
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
}