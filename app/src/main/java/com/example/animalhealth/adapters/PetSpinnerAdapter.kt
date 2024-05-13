package com.example.animalhealth.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.animalhealth.R
import com.example.animalhealth.clases.Pet
import com.example.animalhealth.clases.Utilities

class PetSpinnerAdapter(
    context: Context,
    resource: Int,
    private val petList: List<Pet>
) : ArrayAdapter<Pet>(context, resource, petList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_pet, parent, false)
        val pet = petList[position]
        val photo: ImageView = view.findViewById(R.id.petPhoto)
        val name: TextView = view.findViewById(R.id.petName)
        val type : TextView = view.findViewById(R.id.petType)
        val breed : TextView = view.findViewById(R.id.petBreed)
        val age : TextView = view.findViewById(R.id.petAge)
        val weight : TextView = view.findViewById(R.id.petWeight)
        val ilness : TextView = view.findViewById(R.id.petIlness)
        val vacunes : TextView = view.findViewById(R.id.petVacunes)
        name.text = pet.name
        type.text = pet.type
        breed.text = pet.breed
        age.text = "Edad: " + pet.age
        weight.text = "Peso: "+pet.weight
        ilness.text ="Enfermedades: " + pet.ilness
        vacunes.text = "Vacunas:\n" +pet.vacunes

        val URL: String? = when (pet.photo) {
            "" -> null
            else -> pet.photo
        }

        Glide.with(context).load(URL).apply(Utilities.glideOptions(context))
            .transition(Utilities.transition).into(photo)
        return view
    }
}