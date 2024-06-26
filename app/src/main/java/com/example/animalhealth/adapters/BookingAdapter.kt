package com.example.animalhealth.adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.animalhealth.R
import com.example.animalhealth.activities.client.ClientClinicInfoActivity
import com.example.animalhealth.clases.Blocks
import com.example.animalhealth.clases.Booking
import com.example.animalhealth.clases.Clinic
import com.example.animalhealth.clases.Pet
import com.example.animalhealth.clases.User
import com.example.animalhealth.clases.Utilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BookingAdapter(private val books_list:MutableList<Booking>): RecyclerView.Adapter<BookingAdapter.BookingViewHolder>(),
    Filterable {

    private lateinit var context: Context
    private var filter_list=books_list
    private lateinit var  sharedPreferences : SharedPreferences
    private lateinit var dbRef : DatabaseReference


    private lateinit var navController: NavController


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val item_view= LayoutInflater.from(parent.context).inflate(R.layout.item_booking,parent,false)
        context=parent.context
        sharedPreferences = context.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        navController = Navigation.findNavController(parent)
        dbRef = FirebaseDatabase.getInstance().reference
        return BookingViewHolder(item_view)
    }

    override fun getItemCount(): Int = filter_list.size

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val ownerName: TextView = itemView.findViewById(R.id.itemBookingUserName)
        val petBreed: TextView = itemView.findViewById(R.id.itemBookingPetBreed)
        val petName: TextView = itemView.findViewById(R.id.itemBookingPetName)
        val time: TextView =itemView.findViewById(R.id.itemBookingTime)
        val photo : ImageView = itemView.findViewById(R.id.itemBookingUserPhoto)
        val reason : TextView = itemView.findViewById(R.id.itemBookingReason)
        val date : TextView = itemView.findViewById(R.id.itemBookingDate)
        val clinic : TextView = itemView.findViewById(R.id.itemBookingClinic)
        val cancel : AppCompatButton = itemView.findViewById(R.id.itemBookingCancelButton)
        val status : TextView = itemView.findViewById(R.id.itemBookingStatus)
        val block : ImageView = itemView.findViewById(R.id.itemBookingBlockUser)
    }


    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val actual_item = filter_list[position]
        val lifecycleScope = (holder.itemView.context as? FragmentActivity)?.lifecycleScope
        var user = User()
        lifecycleScope?.launch {
            try {
                user = Utilities.obtainUser(dbRef)!!
                if (user?.type == "Veterinario") {
                    holder.block.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Log.e("BookingAdapter", "Error al obtener usuario: ${e.message}")
            }
        }

        if (user?.type == "Veterinario"){
            holder.block.visibility = View.VISIBLE
        }

        holder.block.setOnClickListener {
            var block = Blocks(actual_item.ownerId)
            Utilities.animation(it, 0.95f, 1.0f, 100,Runnable {
                dbRef.child("Users").child(user!!.id).child("Blocks").setValue(block)
                dbRef.child("Bookings").get().addOnSuccessListener { snapshot ->
                    snapshot.children.forEach { child ->
                        val booking = child.getValue(Booking::class.java)
                        if (booking?.ownerId == actual_item.ownerId) {
                            dbRef.child("Bookings").child(booking.id).removeValue()
                        }
                    }
                }
                filter_list.removeAt(position)
                notifyItemRemoved(position)
            })

        }

        // Establecer valores iniciales o por defecto
        holder.time.text = actual_item.startHour
        holder.reason.text = actual_item.bookingReason
        holder.date.text = actual_item.date

        // Verificar si la fecha de la reserva es posterior a ayer
        if (Utilities.isDateBeforeToday(actual_item.date)) {
            holder.status.text = "Terminada"
            holder.cancel.visibility = View.GONE // Ocultar el botón de cancelar
            holder.status.visibility = View.VISIBLE // Mostrar el texto de estado
        } else {
            holder.status.visibility = View.GONE // Ocultar el texto de estado
            holder.cancel.visibility = View.VISIBLE // Mostrar el botón de cancelar
            holder.cancel.setOnClickListener {
                Utilities.animation(it, 0.95f, 1.0f, 100, Runnable {
                    dbRef.child("Bookings").child(actual_item.id).removeValue()
                    filter_list.removeAt(position)
                    notifyItemRemoved(position)
                })
            }
        }

        // Obtener y establecer el nombre del dueño
        dbRef.child("Users").child(actual_item.ownerId).get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            holder.ownerName.text = user?.name.toString()
        }

        // Obtener y establecer el nombre de la clínica
        dbRef.child("Clinics").child(actual_item.clinicId).get().addOnSuccessListener { snapshot ->
            val clinic = snapshot.getValue(Clinic::class.java)
            holder.clinic.text = clinic?.name
        }

        // Obtener y establecer los detalles de la mascota
        dbRef.child("Pets").child(actual_item.ownerId).child(actual_item.petId).get().addOnSuccessListener { snapshot ->
            val pet = snapshot.getValue(Pet::class.java)
            holder.petName.text = pet?.name
            holder.petBreed.text = pet?.breed
        }

        // Cargar la imagen del dueño usando Glide
        val photoUrl = if (actual_item.ownerPhoto.isNullOrEmpty()) null else actual_item.ownerPhoto
        Glide.with(context)
            .load(photoUrl)
            .apply(Utilities.glideOptions(context))
            .transition(Utilities.transition)
            .into(holder.photo)
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                filterResults.values = filter_list
                return filterResults
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                notifyDataSetChanged()
            }
        }
    }
}