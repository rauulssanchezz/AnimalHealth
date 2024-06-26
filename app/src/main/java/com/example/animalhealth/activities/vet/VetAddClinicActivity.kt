package com.example.animalhealth.activities.vet

import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
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

class VetAddClinicActivity : AppCompatActivity() {
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vet_add_clinic)

        dbRef = FirebaseDatabase.getInstance().reference
        photo = findViewById(R.id.addPhoto)
        job= Job()


        buttonSave = findViewById(R.id.buttonSave)
        nameEditText = findViewById(R.id.editTextName)
        streetEditText = findViewById(R.id.editTextStreet)
        postalCodeEditText = findViewById(R.id.editTextPostalCode)
        phoneEditText = findViewById(R.id.editTextPhone)

        buttonSave.setOnClickListener {
            if (nameEditText.text.toString().isEmpty() || streetEditText.text.toString()
                    .isEmpty() || postalCodeEditText.text.toString().isEmpty() || phoneEditText.text.toString().isEmpty()
            ) {
                nameEditText.setError("Campo obligatorio")
                streetEditText.setError("Campo obligatorio")
                postalCodeEditText.setError("Campo obligatorio")
                phoneEditText.setError("Campo obligatorio")
            } else {
                name = nameEditText.text.toString()
                phone = phoneEditText.text.toString()
                location = streetEditText.text.toString() + " " + postalCodeEditText.text.toString()
                val geocoder = Geocoder(applicationContext, Locale.getDefault())
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
                                    Clinic(clinicId!!, name, 0.0f, location, latitude, longitude,
                                        Firebase.auth.currentUser!!.uid, urlPhotoFb,phone)
                                Utilities.createClinic(clinic, dbRef)
                            }else{
                                val clinic =
                                    Clinic(clinicId!!, name, 0.0f, location, latitude, longitude,
                                        Firebase.auth.currentUser!!.uid,phone)
                                Utilities.createClinic(clinic, dbRef)
                            }


                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    applicationContext,
                                    "Clinica añadida",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(applicationContext, VetMainActivity::class.java)
                                startActivity(intent)
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
    }
    private val galeryAcces = registerForActivityResult(ActivityResultContracts.GetContent())
    { uri: Uri? ->
        if (uri != null) {
            urlPhoto = uri
            photo.setImageURI(uri)
        }
    }
}