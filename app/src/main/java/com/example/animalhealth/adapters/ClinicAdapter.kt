package com.example.animalhealth.adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.animalhealth.R
import com.example.animalhealth.activities.client.ClientClinicInfoActivity
import com.example.animalhealth.clases.Clinic
import com.example.animalhealth.clases.User
import com.example.animalhealth.clases.Utilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ClinicAdapter(private val clinic_list:MutableList<Clinic>): RecyclerView.Adapter<ClinicAdapter.ClinicViewHolder>(),
    Filterable {

        private lateinit var context: Context
        private var filter_list=clinic_list
        private lateinit var  sharedPreferences : SharedPreferences


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClinicViewHolder {
            val item_view= LayoutInflater.from(parent.context).inflate(R.layout.item_clinic,parent,false)
            context=parent.context
            sharedPreferences = context.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
            return ClinicViewHolder(item_view)
        }

        override fun getItemCount(): Int = filter_list.size

        class ClinicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            val photo: ImageView = itemView.findViewById(R.id.clinicPhoto)
            val name: TextView = itemView.findViewById(R.id.clinicName)
            val address: TextView = itemView.findViewById(R.id.clinicLocation)
            val ratingBar: RatingBar =itemView.findViewById(R.id.clinicRate)
            val phone : TextView = itemView.findViewById(R.id.clinicPhone)
            val fav : ImageView = itemView.findViewById(R.id.favoriteButton)
            val booking: AppCompatButton = itemView.findViewById(R.id.reserveButton)
        }


        override fun onBindViewHolder(holder: ClinicViewHolder, position: Int) {
            val actual_item=filter_list[position]
            holder.name.text=actual_item.name
            holder.address.text=actual_item.location
            holder.ratingBar.rating=actual_item.rate
            holder.phone.text=actual_item.phone

            var fav: String = sharedPreferences.getString("favClinic", " ")!!
            var favs = fav.split(",")

            if (favs.contains(actual_item.id)) {
                holder.fav.setImageResource(R.drawable.baseline_favorite_24)
            }

            holder.fav.setOnClickListener {

                if (fav == "null"){
                    fav = "$actual_item.id,"
                }

                if (!favs.contains(actual_item.id)) {
                    fav = fav.plus(actual_item.id).plus(",")
                    fav = fav.trim()
                    holder.fav.setImageResource(R.drawable.baseline_favorite_24)

                    val editor = sharedPreferences.edit()
                    editor.putString("favClinic", fav)
                    editor.apply()

                    val id = FirebaseAuth.getInstance().currentUser?.uid
                    var name = sharedPreferences.getString("Name", "")
                    var email = sharedPreferences.getString("Email", "")
                    var password = sharedPreferences.getString("Password", "")
                    var type = sharedPreferences.getString("Type", "")
                    var img = sharedPreferences.getString("Img", "")

                    GlobalScope.launch {
                        val db_ref = FirebaseDatabase.getInstance().reference
                        db_ref.child("Users").child(id!!)
                            .setValue(User(id, name!!, email!!, password!!, type!!, img!!, fav))
                    }
                }else{
                    fav = fav.replace(actual_item.id, "")
                    fav = fav.replace(",,", ",")
                    fav = fav.trim()

                    if (fav == ","){
                        fav = "null"
                    }

                    holder.fav.setImageResource(R.drawable.baseline_favorite_border_24)

                    val editor = sharedPreferences.edit()
                    editor.putString("favClinic", fav)
                    editor.apply()

                    val id = FirebaseAuth.getInstance().currentUser?.uid
                    var name = sharedPreferences.getString("Name", "")
                    var email = sharedPreferences.getString("Email", "")
                    var password = sharedPreferences.getString("Password", "")
                    var type = sharedPreferences.getString("Type", "")
                    var img = sharedPreferences.getString("Img", "")

                    GlobalScope.launch {
                        val db_ref = FirebaseDatabase.getInstance().reference
                        db_ref.child("Users").child(id!!)
                            .setValue(User(id, name!!, email!!, password!!, type!!, img!!, fav))
                    }

                }
            }
            holder.itemView.setOnClickListener {
                var newIntent=Intent(context,ClientClinicInfoActivity::class.java)
                newIntent.putExtra("clinic",actual_item)
                context.startActivity(newIntent)
            }

            val URL:String? = when (actual_item.photo){
                ""->null
                else->actual_item.photo
            }

            Glide.with(context).load(URL).apply(Utilities.glideOptions(context)).transition(Utilities.transition).into(holder.photo)
        }

        override fun getFilter(): Filter {
            return  object : Filter(){
                override fun performFiltering(p0: CharSequence?): FilterResults {
                    val search = p0.toString().lowercase()

                    if (search.isEmpty()){
                        filter_list = clinic_list
                    }else {
                        filter_list = (clinic_list.filter {
                            if (it.name.toString().lowercase().contains(search)){
                                it.name.toString().lowercase().contains(search)
                            }else{
                                it.location.toString().lowercase().contains(search)
                            }
                        }) as MutableList<Clinic>
                    }

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