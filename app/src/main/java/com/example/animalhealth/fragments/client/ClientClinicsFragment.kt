package com.example.animalhealth.fragments.client

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.animalhealth.R
import com.example.animalhealth.adapters.ClinicAdapter
import com.example.animalhealth.clases.Clinic
import com.example.animalhealth.clases.Utilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ClientClinicsFragment : Fragment() {
    private lateinit var dbRef : DatabaseReference
    private lateinit var list : MutableList<Clinic>
    private lateinit var recycler : RecyclerView
    private lateinit var adapter : ClinicAdapter
    private lateinit var filters : ImageView
    private lateinit var search : SearchView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var newList : MutableList<Clinic>
    private lateinit var favClinics : String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_client_clinics, container, false)

        dbRef = FirebaseDatabase.getInstance().reference
        list = mutableListOf<Clinic>()
        recycler = view.findViewById(R.id.clinicRecyclerView)
        filters = view.findViewById(R.id.filters)
        search = view.findViewById(R.id.searchView)
        sharedPreferences = requireActivity().getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        newList = mutableListOf()
        favClinics = ""

        obtainClinics()

        adapter = ClinicAdapter(list)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(requireActivity())

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })

        filters.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)

            popupMenu.inflate(R.menu.client_clinics_filters_menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when(item.itemId) {

                    R.id.fav ->{
                        Log.d("Fav", "Entra en fav")
                        Utilities.obtainFavClinics(dbRef)
                        Log.d("Fav", favClinics.toString())
                        if (favClinics == "null") {
                            Log.d("Fav", "No hay favoritos")
                            Toast.makeText(requireContext(), "No hay clínicas favoritas", Toast.LENGTH_SHORT).show()
                        }else{
                            Log.d("Fav", "Hay favoritos")
                            for (clinic in list) {
                                Log.d("Fav", clinic.id)
                                Log.d("Fav", favClinics.toString())
                                if (favClinics.contains(clinic.id)) {
                                    Log.d("Fav", "Añade a la lista")
                                    newList.add(clinic)
                                    Log.d("Fav", newList.toString())
                                }
                            }
                            list.clear()
                            list.addAll(newList)
                            newList.clear()
                            Log.d("Fav", list.toString())
                            adapter.notifyDataSetChanged()
                            Log.d("Fav", "Notifica")
                        }
                        true
                    }

                    R.id.name -> {
                        list.sortBy { it.name }
                        adapter.notifyDataSetChanged()
                        true
                    }
                    R.id.ubication -> {
                        val latitud = sharedPreferences.getString("latitud", "0.0")!!.toDouble()
                        val longitud = sharedPreferences.getString("longitud", "0.0")!!.toDouble()
                        Log.d("Latitud", latitud.toString())
                        for (clinic in list) {
                            val distancia = Utilities.calcularDistancia(latitud, longitud, clinic.latitude, clinic.longitude)
                            if (distancia < 30) {
                                newList.add(clinic)
                            }
                        }
                        if (newList.isEmpty()) {
                            Toast.makeText(requireContext(), "No hay clínicas cercanas", Toast.LENGTH_SHORT).show()
                        }else{
                            list.clear()
                            list.addAll(newList)
                            newList.clear()
                            adapter.notifyDataSetChanged()
                        }
                        Log.d("Lista", newList.toString())
                        true
                    }
                    R.id.reviews -> {
                        list.sortBy { it.rate }
                        list.reverse()
                        adapter.notifyDataSetChanged()
                        true
                    }

                    R.id.all -> {
                        obtainClinics()
                        true
                    }

                    else -> false
                }
            }
            try {
                val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                fieldMPopup.isAccessible = true
                val mPopup = fieldMPopup.get(popupMenu)
                mPopup.javaClass
                    .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(mPopup, true)
            } catch (e: Exception) {
                Log.e("Main", "Error showing menu icons.", e)
            } finally {
                popupMenu.show()
            }
        }

        return view
    }

    private fun obtainClinics(){
        dbRef.child("Clinics")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    list.clear()
                    snapshot.children.forEach { hijo: DataSnapshot?
                        ->
                        val pojo_clinic = hijo?.getValue(Clinic::class.java)
                        list.add(pojo_clinic!!)
                    }
                    recycler.adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }
            })
    }


}