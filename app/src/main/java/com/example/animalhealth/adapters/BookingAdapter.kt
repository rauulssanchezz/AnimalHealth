package com.example.animalhealth.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.animalhealth.R
import com.example.animalhealth.clases.Booking

class BookingAdapter(private var bookings:List<Booking>):RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {
    private var filter_list=bookings
    private lateinit var context: Context
    class BookingViewHolder(item : View):RecyclerView.ViewHolder(item) {
        var reason = item.findViewById<TextView>(R.id.bookingReason)
        var time = item.findViewById<TextView>(R.id.bookingTime)
        var userName = item.findViewById<TextView>(R.id.itemBookingUserName)
        var petName = item.findViewById<TextView>(R.id.itemBookingPetName)
        var petBreed = item.findViewById<TextView>(R.id.itemBookingPetBreed)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BookingAdapter.BookingViewHolder {
        val item_view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        context = parent.context.applicationContext
        return BookingAdapter.BookingViewHolder(item_view)
    }

    override fun onBindViewHolder(holder: BookingAdapter.BookingViewHolder, position: Int) {
        val actual_item=filter_list[position]
        holder.reason.text=actual_item.bookingReason
        holder.time.text=actual_item.time
        holder.userName.text=actual_item.ownerId
        holder.petName.text=actual_item.petId
    }

    override fun getItemCount(): Int =filter_list.size
}