package com.example.animalhealth.fragments.vet

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.animalhealth.R
import com.example.animalhealth.clases.Clinic
import com.example.animalhealth.clases.Utilities
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class VetClinicFragment : Fragment() {
    private lateinit var photo: ImageView
    private var url_photo: Uri? = null
    private var beforeUrl_img: String = ""
    var url_img:String=""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_vet_clinic, container, false)

        val dbRef = Firebase.database.reference
        var clinic: Clinic? = null
        photo = view.findViewById<ImageView>(R.id.addPhoto)
        val nameEdit = view.findViewById<TextInputEditText>(R.id.editTextName)
        var addressEdit = view.findViewById<TextInputEditText>(R.id.editTextStreet)
        var phoneEdit = view.findViewById<TextInputEditText>(R.id.editTextPhone)
        var postalCodeEdit = view.findViewById<TextInputEditText>(R.id.editTextPostalCode)
        val buttonSave = view.findViewById<AppCompatButton>(R.id.buttonSave)
        val ratingBar = view.findViewById<RatingBar>(R.id.clinicRate)

        var name = ""
        var location = ""
        var phone = ""
        var postalCode = ""

        dbRef.child("Clinics")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { hijo: DataSnapshot?
                        ->
                        val pojo_clinic = hijo?.getValue(Clinic::class.java)
                        if (pojo_clinic?.vetId == FirebaseAuth.getInstance().currentUser!!.uid) {
                            clinic = pojo_clinic
                            // Actualizar la interfaz de usuario aquí dentro de onDataChange
                            beforeUrl_img = clinic?.photo.toString()
                            name = clinic?.name.toString()
                            location = clinic?.location.toString()
                            phone = clinic?.phone.toString()
                            postalCode = clinic?.postalCode.toString()
                            url_img = beforeUrl_img

                            nameEdit.setText(name)
                            addressEdit.setText(location)
                            phoneEdit.setText(phone)
                            postalCodeEdit.setText(postalCode)
                            ratingBar.rating = clinic?.rate!!

                            Glide.with(requireActivity())
                                .load(beforeUrl_img)
                                .apply(Utilities.glideOptions(requireContext()))
                                .transition(Utilities.transition)
                                .into(photo)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }
            })

        buttonSave.setOnClickListener {
            Utilities.animation(it, 0.95f, 1.0f, 100,Runnable {
                GlobalScope.launch {
                    var url_photo_firebase = String()
                    if (nameEdit.text.toString() == name && url_photo == null && addressEdit.text.toString() == location && phoneEdit.text.toString() == phone && postalCodeEdit.text.toString() == postalCode) {
                        GlobalScope.launch(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "No se han realizado cambios",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return@launch
                    }
                    if (url_photo == null) {
                        url_photo_firebase = url_img
                    } else {
                        url_photo_firebase =
                            Utilities.savePhoto(url_photo!!, "Clinics", clinic!!.id)
                    }

                    clinic?.name = nameEdit.text.toString()
                    clinic?.photo = url_photo_firebase
                    clinic?.phone = phoneEdit.text.toString()

                    Utilities.createClinic(clinic!!, dbRef)

                    GlobalScope.launch(Dispatchers.Main) {

                        Toast.makeText(requireContext(), "Cambios guardados", Toast.LENGTH_SHORT)
                            .show()
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
            url_photo = uri
            photo.setImageURI(uri)
        }
    }

}