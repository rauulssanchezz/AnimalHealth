package com.example.animalhealth.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.animalhealth.R
import com.example.animalhealth.clases.Clinic
import com.example.animalhealth.clases.Utilities
import com.example.animalhealth.fragments.client.ClientClinicInfoFragment


class ClinicAdapter(private val clinics:MutableList<Clinic>, private val navController: NavController): RecyclerView.Adapter<ClinicAdapter.ClinicViewHolder>(){
    private lateinit var context: Context
    private var filter_list=clinics

    class ClinicViewHolder(item: View): RecyclerView.ViewHolder(item) {
        val photo = item.findViewById<ImageView>(R.id.clinicPhoto)
        val name = item.findViewById<TextView>(R.id.clinicName)
        val address = item.findViewById<TextView>(R.id.clinicLocation)
        val rate = item.findViewById<TextView>(R.id.clinicRate)

    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ClinicAdapter.ClinicViewHolder {
        val item_view = LayoutInflater.from(parent.context).inflate(R.layout.item_clinic, parent, false)
        context = parent.context.applicationContext
        return ClinicViewHolder(item_view)
    }
    override fun onBindViewHolder(holder: ClinicAdapter.ClinicViewHolder, position: Int) {
        val actual_item=filter_list[position]
        holder.name.text=actual_item.name
        holder.address.text=actual_item.location
        holder.rate.text=actual_item.rate.toDouble().toString()

        val URL:String? = when (actual_item.photo){
            ""->null
            else->actual_item.photo
        }
        Glide.with(context).load(URL).apply(Utilities.glideOptions(context)).transition(Utilities.transition).into(holder.photo)

        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable("clinic", actual_item)
            val fragment = ClientClinicInfoFragment()
            fragment.arguments = bundle

            navController.navigate(R.id.action_clientClinicsFragment_to_clientClinicInfoFragment)
        }

        holder.itemView.findViewById<Button>(R.id.reserveButton).setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable("clinic", actual_item)
            val fragment = ClientClinicInfoFragment()
            fragment.arguments = bundle

            navController.navigate(R.id.action_clientClinicsFragment_to_clientBookingFragment)
        }
    }

    override fun getItemCount(): Int =filter_list.size

    fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                filter_list = if (charSearch.isEmpty()) {
                    clinics
                } else {
                    val resultList = mutableListOf<Clinic>()
                    for (row in clinics) {
                        if (row.name.toLowerCase().contains(charSearch.toLowerCase())) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filter_list
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filter_list = results?.values as MutableList<Clinic>
                notifyDataSetChanged()
            }
        }
    }
}