package com.example.animalhealth.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.animalhealth.R
import com.example.animalhealth.clases.Pet
import com.example.animalhealth.clases.Utilities

class PetsAdapter(private var pets_list:MutableList<Pet>): RecyclerView.Adapter<PetsAdapter.PetsViewHolder>(),
    Filterable {
    private lateinit var context: Context
    private var filter_list = pets_list


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetsViewHolder {
        val item_view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_pet, parent, false)
        context = parent.context
        return PetsViewHolder(item_view)
    }

    override fun getItemCount(): Int = filter_list.size

    class PetsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photo: ImageView = itemView.findViewById(R.id.petPhoto)
        val name: TextView = itemView.findViewById(R.id.petName)
        val type : TextView = itemView.findViewById(R.id.petType)
        val breed : TextView = itemView.findViewById(R.id.petBreed)
        val age : TextView = itemView.findViewById(R.id.petAge)
        val weight : TextView = itemView.findViewById(R.id.petWeight)
        val ilness : TextView = itemView.findViewById(R.id.petIlness)
        val vacunes : TextView = itemView.findViewById(R.id.petVacunes)
    }


    override fun onBindViewHolder(holder: PetsViewHolder, position: Int) {
        val actual_item = filter_list[position]
        holder.name.text = actual_item.name
        holder.type.text = actual_item.type
        holder.breed.text = actual_item.breed
        holder.age.text = "Edad: " + actual_item.age
        holder.weight.text = "Peso: "+actual_item.weight
        holder.ilness.text ="Enfermedades: " + actual_item.ilness
        holder.vacunes.text = "Vacunas:\n" +actual_item.vacunes

        val URL: String? = when (actual_item.photo) {
            "" -> null
            else -> actual_item.photo
        }

        Glide.with(context).load(URL).apply(Utilities.glideOptions(context))
            .transition(Utilities.transition).into(holder.photo)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                filter_list = pets_list
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