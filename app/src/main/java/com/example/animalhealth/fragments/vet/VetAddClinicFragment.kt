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
import com.example.animalhealth.R
import com.example.animalhealth.clases.Clinic
import com.example.animalhealth.clases.Utilities
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
    private lateinit var buttonSave: Button

    private var name = ""
    private var location = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_vet_add_clinic, container, false)

        dbRef = FirebaseDatabase.getInstance().reference
        photo = view.findViewById(R.id.addPhoto)
        job=Job()

        buttonSave = view.findViewById(R.id.buttonSave)
        nameEditText = view.findViewById(R.id.editTextName)
        streetEditText = view.findViewById(R.id.editTextStreet)
        postalCodeEditText = view.findViewById(R.id.editTextPostalCode)

        buttonSave.setOnClickListener {
            if (nameEditText.text.toString().isEmpty() || streetEditText.text.toString()
                    .isEmpty()
            ) {
                nameEditText.setError("Campo obligatorio")
                streetEditText.setError("Campo obligatorio")
            } else {
                name = nameEditText.text.toString()
                location = streetEditText.text.toString() + " " + postalCodeEditText.text.toString()
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                try {
                    val addresses = geocoder.getFromLocationName(location, 1)
                    if (addresses!!.isNotEmpty()) {
                        val address = addresses[0]
                        val latitude = address.latitude
                        val longitude = address.longitude

                        val clinicId = dbRef.push().key

                        GlobalScope.launch {

                            if (urlPhoto!=null) {
                                val urlPhotoFb = Utilities.savePhoto(urlPhoto!!, "Clinics", clinicId!!)
                                val clinic =
                                    Clinic(clinicId!!, name, "0", location, latitude, longitude, urlPhotoFb)
                                Utilities.createClinic(clinic, dbRef)
                            }else{
                                val clinic =
                                    Clinic(clinicId!!, name, "0", location, latitude, longitude)
                                Utilities.createClinic(clinic, dbRef)
                            }


                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    requireContext(),
                                    "Clinica añadida",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                } catch (e: Exception) {
                    streetEditText.setError("Ubicación no encontrada")
                    postalCodeEditText.setError("Ubicación no encontrada")
                }
            }
        }

        photo.setOnClickListener {
            galeryAcces.launch("image/*")
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