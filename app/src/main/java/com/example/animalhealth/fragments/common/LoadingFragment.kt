package com.example.animalhealth.fragments.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.animalhealth.R
import com.example.animalhealth.activities.client.ClientMainActivity
import com.example.animalhealth.activities.VetMainActivity
import com.example.animalhealth.activities.vet.VetAddClinicActivity
import com.example.animalhealth.clases.Clinic
import com.example.animalhealth.clases.User
import com.example.animalhealth.clases.Utilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LoadingFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_loading, container, false)

        var db_ref = FirebaseDatabase.getInstance().reference
        var user: User?=null
        var clinic : Clinic?=null
        val sharedPreferences = requireContext().getSharedPreferences("sharedPreferences",
            Context.MODE_PRIVATE
        )
        CoroutineScope(Dispatchers.IO).launch {
            user = Utilities.obtainUser(db_ref)
            clinic = getClinicForUser(db_ref,user)
            with(sharedPreferences.edit()){
                putString("Name",user?.name)
                putString("Email",user?.email)
                putString("Password",user?.password)
                putString("Type",user?.type)
                putString("Img",user?.img)
                apply()
            }
            withContext(Dispatchers.Main) {
                if (user?.type == ( "Veterinario")) {
                    if (clinic != null) {
                        val intent = Intent(requireContext(), VetMainActivity::class.java)
                        startActivity(intent)
                    }else{
                        val intent = Intent(requireContext(), VetAddClinicActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    val intent = Intent(requireContext(), ClientMainActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        return view
    }
    private suspend fun getClinicForUser(db_ref: DatabaseReference, user: User?): Clinic? {
        return suspendCancellableCoroutine { continuation ->
            val clinicsRef = db_ref.child("Clinics")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var clinic: Clinic? = null
                    snapshot.children.forEach { hijo: DataSnapshot? ->
                        val pojo_clinic = hijo?.getValue(Clinic::class.java)
                        if (pojo_clinic?.vetId == user?.id) {
                            clinic = pojo_clinic
                        }
                    }
                    continuation.resume(clinic)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(error.toException())
                }
            }
            clinicsRef.addListenerForSingleValueEvent(listener)

            continuation.invokeOnCancellation {
                clinicsRef.removeEventListener(listener)
            }
        }
    }

}