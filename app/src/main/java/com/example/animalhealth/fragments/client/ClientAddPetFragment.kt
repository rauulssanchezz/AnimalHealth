package com.example.animalhealth.fragments.client

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.animalhealth.R
import com.example.animalhealth.clases.Pet
import com.example.animalhealth.clases.Utilities
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClientAddPetFragment : Fragment() {
    private lateinit var photo: ImageView
    private var url_photo: Uri? = null
    private lateinit var job: Job
    private lateinit var db_ref: DatabaseReference
    private lateinit var navController: NavController
    private lateinit var nameEditText: TextInputEditText
    private lateinit var typeEditText: TextInputEditText
    private lateinit var breedEditText: TextInputEditText
    private lateinit var ilnessEditText: TextInputEditText
    private lateinit var vacunesEditText: TextInputEditText
    private lateinit var ageEditText: TextInputEditText
    private lateinit var weightEditText: TextInputEditText
    private lateinit var savePet: Button

    private var name = ""
    private var type = ""
    private var breed = ""
    private var ilness = ""
    private var vacunes = ""
    private var age = ""
    private var weight = ""
    private var owner = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_client_add_pet, container, false)

        navController = findNavController()

        job = Job()

        db_ref = FirebaseDatabase.getInstance().reference

        photo = view.findViewById(R.id.addPhoto)
        nameEditText = view.findViewById(R.id.editTextName)
        typeEditText = view.findViewById(R.id.editTextType)
        breedEditText = view.findViewById(R.id.editTextBreed)
        ilnessEditText = view.findViewById(R.id.editTextIlness)
        vacunesEditText = view.findViewById(R.id.editTextVacunes)
        ageEditText = view.findViewById(R.id.editTextAge)
        weightEditText = view.findViewById(R.id.editTextWeight)
        savePet = view.findViewById(R.id.buttonSave)

        savePet.setOnClickListener {
            if (
                !nameEditText.text.isNullOrBlank() && !typeEditText.text.isNullOrBlank()
                && !breedEditText.text.isNullOrBlank() && !ilnessEditText.text.isNullOrBlank()
                && !vacunesEditText.text.isNullOrBlank() && !ageEditText.text.isNullOrBlank()
                && !weightEditText.text.isNullOrBlank()
                ){

                name = nameEditText.text.toString().trim().capitalize()
                type = typeEditText.text.toString().trim().capitalize()
                breed = breedEditText.text.toString().trim().capitalize()
                ilness = ilnessEditText.text.toString().trim().capitalize()
                vacunes = vacunesEditText.text.toString().trim().capitalize()
                age = ageEditText.text.toString().trim().capitalize()
                weight = weightEditText.text.toString().trim().capitalize()
                owner = FirebaseAuth.getInstance().currentUser!!.uid.toString()

                val generatedId:String? = db_ref.child("Pets").push().key

                GlobalScope.launch {
                    val pet:Pet
                    if (url_photo!=null) {
                        val urlPhotoFirebase = Utilities.savePhoto(
                            url_photo!!,
                            "Pets",
                            FirebaseAuth.getInstance().currentUser!!.uid
                        )
                        pet = Pet(
                            generatedId!!,
                            name,
                            type,
                            breed,
                            age,
                            ilness,
                            vacunes,
                            weight,
                            owner,
                            urlPhotoFirebase
                        )
                    }else {
                        pet = Pet(
                            generatedId!!,
                            name,
                            type,
                            breed,
                            age,
                            ilness,
                            vacunes,
                            weight,
                            owner
                        )
                    }

                    Utilities.createPet(pet,db_ref)

                    withContext(Dispatchers.Main){
                        Toast.makeText(requireContext(),"Mascota guardada con exito",Toast.LENGTH_SHORT).show()
                    }

                }

            }else{
                Toast.makeText(requireContext(),"Faltan datos",Toast.LENGTH_SHORT).show()
            }

        }

        photo.setOnClickListener {
            galeryAcces
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