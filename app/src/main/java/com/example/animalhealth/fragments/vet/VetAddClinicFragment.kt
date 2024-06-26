package com.example.animalhealth.fragments.vet

import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.animalhealth.R
import com.example.animalhealth.clases.Clinic
import com.example.animalhealth.clases.Utilities
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class VetAddClinicFragment : Fragment() {
    private lateinit var photo: ImageView
    private var urlPhoto: Uri? = null
    private lateinit var dbRef: DatabaseReference
    private lateinit var job: Job

    private lateinit var nameEditText: EditText
    private lateinit var streetEditText: EditText
    private lateinit var postalCodeEditText: EditText
    private lateinit var phoneEditText : EditText
    private lateinit var buttonSave: Button

    private lateinit var navController: NavController

    private var name = ""
    private var location = ""
    private var phone = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_vet_add_clinic, container, false)

        dbRef = FirebaseDatabase.getInstance().reference
        photo = view.findViewById(R.id.addPhoto)
        job=Job()

        navController = findNavController()

        buttonSave = view.findViewById(R.id.buttonSave)
        nameEditText = view.findViewById(R.id.editTextName)
        streetEditText = view.findViewById(R.id.editTextStreet)
        postalCodeEditText = view.findViewById(R.id.editTextPostalCode)
        phoneEditText = view.findViewById(R.id.editTextPhone)

        buttonSave.setOnClickListener {
            Utilities.animation(it, 0.95f, 1.0f, 100,Runnable {
                if (nameEditText.text.toString().isEmpty() || streetEditText.text.toString()
                        .isEmpty() || postalCodeEditText.text.toString()
                        .isEmpty() || phoneEditText.text.toString().isEmpty()
                ) {
                    nameEditText.setError("Campo obligatorio")
                    streetEditText.setError("Campo obligatorio")
                    postalCodeEditText.setError("Campo obligatorio")
                    phoneEditText.setError("Campo obligatorio")
                } else {
                    name = nameEditText.text.toString()
                    phone = phoneEditText.text.toString()
                    location =
                        streetEditText.text.toString() + " " + postalCodeEditText.text.toString()
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocationName(location, 1)
                        if (addresses!!.isNotEmpty()) {
                            val address = addresses[0]
                            val latitude = address.latitude
                            val longitude = address.longitude

                            val clinicId = dbRef.push().key

                            GlobalScope.launch {

                                if (urlPhoto != null) {
                                    val urlPhotoFb =
                                        Utilities.savePhoto(urlPhoto!!, "Clinics", clinicId!!)
                                    val clinic =
                                        Clinic(
                                            clinicId!!,
                                            name,
                                            0.0f,
                                            location,
                                            latitude,
                                            longitude,
                                            Firebase.auth.currentUser!!.uid,
                                            urlPhotoFb,
                                            phone,
                                            postalCodeEditText.text.toString()
                                        )
                                    Utilities.createClinic(clinic, dbRef)
                                } else {
                                    val clinic =
                                        Clinic(
                                            clinicId!!,
                                            name,
                                            0.0f,
                                            location,
                                            latitude,
                                            longitude,
                                            Firebase.auth.currentUser!!.uid,
                                            phone,
                                            postalCodeEditText.text.toString()
                                        )
                                    Utilities.createClinic(clinic, dbRef)
                                }


                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Clinica añadida",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate(R.id.vetAddClinicFragment_to_vetClinicFragment)
                                }
                            }
                        }

                    } catch (e: Exception) {
                        streetEditText.setError("Ubicación no encontrada")
                        postalCodeEditText.setError("Ubicación no encontrada")
                    }
                }
            })
        }

        photo.setOnClickListener {
            Utilities.animation(it, 0.95f, 1.0f, 100,Runnable {
                galeryAcces.launch("image/*")
            })
        }

        return view
    }
    private val galeryAcces = registerForActivityResult(ActivityResultContracts.GetContent())
    { uri: Uri? ->
        if (uri != null) {
            urlPhoto = uri
            photo.setImageURI(uri)
        }
    }
}