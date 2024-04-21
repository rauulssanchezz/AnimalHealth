package com.example.animalhealth.fragments.client

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.animalhealth.R
import com.example.animalhealth.adapters.ClinicAdapter
import com.example.animalhealth.clases.Clinic
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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_client_clinics, container, false)

        dbRef = FirebaseDatabase.getInstance().reference
        list = mutableListOf<Clinic>()
        recycler = view.findViewById(R.id.clinicRecyclerView)

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

        val navController = findNavController()
        adapter = ClinicAdapter(list,navController)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(requireActivity())

        return view
    }
}