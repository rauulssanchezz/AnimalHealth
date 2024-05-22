package com.example.animalhealth.fragments.client

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.animalhealth.R
import com.example.animalhealth.adapters.PetsAdapter
import com.example.animalhealth.clases.Clinic
import com.example.animalhealth.clases.Pet
import com.example.animalhealth.clases.Utilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ClientPetFragment : Fragment() {
    private lateinit var dbRef : DatabaseReference
    private lateinit var list : MutableList<Pet>
    private lateinit var recycler : RecyclerView
    private lateinit var adapter : PetsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_client_pet, container, false)
        val navController=findNavController()
        val addPet = view?.findViewById<CardView>(R.id.addPet)
        dbRef = FirebaseDatabase.getInstance().reference
        list = mutableListOf<Pet>()
        recycler = view.findViewById(R.id.petRecyclerView)

        dbRef.child("Pets").child(FirebaseAuth.getInstance().uid!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                snapshot.children.forEach { hijo: DataSnapshot?
                    ->
                    val pojo_pet = hijo?.getValue(Pet::class.java)
                    if (pojo_pet?.ownerId == FirebaseAuth.getInstance().currentUser?.uid) {
                        list.add(pojo_pet!!)
                    }
                }
                recycler.adapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                println(error.message)
            }
        })

        adapter = PetsAdapter(list)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(requireActivity())

        Log.d("Boton","listo")
        addPet!!.setOnClickListener {
            Utilities.animation(it, 0.95f, 1.0f, 100,Runnable {
                Log.d("Boton", "pulsado")
                navController.navigate(R.id.action_clientPetFragment_to_clientAddPetFragment)
            })
        }
        return view
    }
}