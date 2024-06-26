package com.example.animalhealth.adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.animalhealth.R
import com.example.animalhealth.activities.client.ClientClinicInfoActivity
import com.example.animalhealth.activities.common.MensajeActivity
import com.example.animalhealth.clases.Blocks
import com.example.animalhealth.clases.Clinic
import com.example.animalhealth.clases.User
import com.example.animalhealth.clases.Utilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resumeWithException


class ClinicAdapter(private val clinic_list: MutableList<Clinic>) : RecyclerView.Adapter<ClinicAdapter.ClinicViewHolder>(), Filterable {

    private lateinit var context: Context
    private var filter_list = clinic_list
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var navController: NavController
    private val adapterScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var dbReference: DatabaseReference
    private var block = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClinicViewHolder {
        val item_view = LayoutInflater.from(parent.context).inflate(R.layout.item_clinic, parent, false)
        context = parent.context
        sharedPreferences = context.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        navController = Navigation.findNavController(parent)
        dbReference = FirebaseDatabase.getInstance().reference
        return ClinicViewHolder(item_view)
    }

    override fun getItemCount(): Int = filter_list.size

    class ClinicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photo: ImageView = itemView.findViewById(R.id.clinicPhoto)
        val name: TextView = itemView.findViewById(R.id.clinicName)
        val address: TextView = itemView.findViewById(R.id.clinicLocation)
        val ratingBar: RatingBar = itemView.findViewById(R.id.clinicRate)
        val phone: TextView = itemView.findViewById(R.id.clinicPhone)
        val fav: ImageView = itemView.findViewById(R.id.favoriteButton)
        val booking: AppCompatButton = itemView.findViewById(R.id.reserveButton)
        val chatButton = itemView.findViewById<ImageView>(R.id.clinicChatButton)
    }

    override fun onBindViewHolder(holder: ClinicViewHolder, position: Int) {
        val actual_item = filter_list[position]
        holder.name.text = actual_item.name
        holder.address.text = actual_item.location
        holder.ratingBar.rating = actual_item.rate
        holder.phone.text = actual_item.phone

        holder.chatButton.setOnClickListener {
            Utilities.animation(it, 0.95f, 1.0f, 100,Runnable {
                dbReference.child("Clinics").get().addOnSuccessListener { clinicsSnapshot ->
                    clinicsSnapshot.children.forEach { clinicSnapshot ->
                        val clinic = clinicSnapshot.getValue(Clinic::class.java)
                        if (clinic?.id == actual_item.id) {
                            val vetId = clinic.vetId
                            dbReference.child("Users").child(vetId).child("Blocks").get()
                                .addOnSuccessListener { blocksSnapshot ->
                                    val blocks = blocksSnapshot.getValue(Blocks::class.java)
                                    if (blocks?.id == FirebaseAuth.getInstance().currentUser!!.uid) {
                                        // El usuario está bloqueado
                                        block = true
                                        Toast.makeText(
                                            context,
                                            "No puedes reservar en esta clínica porque has sido bloqueado",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        navController.navigate(R.id.action_clientAddBookingFragment_to_clientClinicsFragment)
                                    } else {
                                        // El usuario no está bloqueado
                                        block = false
                                    }
                                }
                        }
                    }
                    }
                    if (block){
                        Toast.makeText(context, "No puedes reservar en esta clinica porque has sido bloqueado", Toast.LENGTH_LONG).show()
                    }else {
                        var intent = Intent(context, MensajeActivity::class.java)
                        intent.putExtra("userId", actual_item.vetId)
                        context.startActivity(intent)
                    }
            })
        }

        holder.booking.setOnClickListener {
            Utilities.animation(it, 0.95f, 1.0f, 100,Runnable {
                dbReference.child("Clinics").get().addOnSuccessListener { clinicsSnapshot ->
                    clinicsSnapshot.children.forEach { clinicSnapshot ->
                        val clinic = clinicSnapshot.getValue(Clinic::class.java)
                        if (clinic?.id == actual_item.id) {
                            val vetId = clinic.vetId
                            dbReference.child("Users").child(vetId).child("Blocks").get().addOnSuccessListener { blocksSnapshot ->
                                val blocks = blocksSnapshot.getValue(Blocks::class.java)
                                if (blocks?.id == FirebaseAuth.getInstance().currentUser!!.uid) {
                                    // El usuario está bloqueado
                                    block = true
                                    Toast.makeText(context, "No puedes reservar en esta clínica porque has sido bloqueado", Toast.LENGTH_LONG).show()
                                    navController.navigate(R.id.action_clientAddBookingFragment_to_clientClinicsFragment)
                                } else {
                                    // El usuario no está bloqueado
                                    block = false
                                }
                            }
                        }
                    }
                    if (block){
                        Toast.makeText(context, "No puedes reservar en esta clinica porque has sido bloqueado", Toast.LENGTH_LONG).show()
                    }else {
                        navController.navigate(R.id.action_clientClinicsFragment_to_clientBookingFragment)
                        sharedPreferences.edit().putString("clinicId", actual_item.id).apply()
                    }
                }

            })
        }

        // Launching a coroutine to fetch favorite clinics
        adapterScope.launch {
            val fav = obtainFavClinics(FirebaseDatabase.getInstance().reference)
            val favs = fav.split(",")
            if (favs.contains(actual_item.id)) {
                holder.fav.setImageResource(R.drawable.baseline_favorite_24)
            } else {
                holder.fav.setImageResource(R.drawable.baseline_favorite_border_24)
            }

            holder.fav.setOnClickListener {
                Utilities.animation(it, 0.95f, 1.0f, 100,Runnable {
                    handleFavoriteClick(actual_item, holder, fav)
                })
            }
        }

        holder.itemView.setOnClickListener {
            Utilities.animation(it, 0.95f, 1.0f, 100,Runnable {
                val newIntent = Intent(context, ClientClinicInfoActivity::class.java)
                newIntent.putExtra("clinic", actual_item)
                context.startActivity(newIntent)
            })
        }

        val URL: String? = if (actual_item.photo.isEmpty()) null else actual_item.photo
        Glide.with(context).load(URL).apply(Utilities.glideOptions(context)).transition(Utilities.transition).into(holder.photo)
    }

    private fun handleFavoriteClick(actual_item: Clinic, holder: ClinicViewHolder, fav: String) {
        var updatedFav = fav
        val favs = updatedFav.split(",").toMutableList()

        if (favs.contains(actual_item.id)) {
            favs.remove(actual_item.id)
            holder.fav.setImageResource(R.drawable.baseline_favorite_border_24)
        } else {
            favs.add(actual_item.id)
            holder.fav.setImageResource(R.drawable.baseline_favorite_24)
        }

        val id = FirebaseAuth.getInstance().currentUser?.uid ?: return

        adapterScope.launch {
            val db_ref = FirebaseDatabase.getInstance().reference
            db_ref.child("Users").child(id).child("favClinics").setValue(favs.joinToString(","))
                .addOnSuccessListener {
                    // Actualizar la lista de favoritos localmente o notificar al adaptador
                    notifyDataSetChanged()
                    Toast.makeText(context, "Lista de favoritos actualizada correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al actualizar la lista de favoritos: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Manejar el error adecuadamente
                }
        }
    }


    private suspend fun obtainFavClinics(dbRef: DatabaseReference): String {
        return suspendCancellableCoroutine { continuation ->
            dbRef.child("Users").child(FirebaseAuth.getInstance().currentUser?.uid ?: return@suspendCancellableCoroutine)
                .child("favClinics")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val favClinics = snapshot.value.toString()
                        continuation.resume(favClinics) { throwable -> throwable.printStackTrace() }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val search = p0.toString().lowercase()
                filter_list = if (search.isEmpty()) {
                    clinic_list
                } else {
                    clinic_list.filter {
                        it.name.lowercase().contains(search) || it.location.lowercase().contains(search)
                    } as MutableList<Clinic>
                }

                val filterResults = FilterResults()
                filterResults.values = filter_list
                return filterResults
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                filter_list = p1?.values as MutableList<Clinic>
                notifyDataSetChanged()
            }
        }
    }
}