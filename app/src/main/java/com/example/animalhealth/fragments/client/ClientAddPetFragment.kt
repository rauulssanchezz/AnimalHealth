package com.example.animalhealth.fragments.client

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
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
    private lateinit var typeSpinner: Spinner
    private lateinit var breedSpinner: Spinner
    private lateinit var ilnessEditText: TextInputEditText
    private lateinit var vacunesSpinner: Spinner
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
        typeSpinner = view.findViewById(R.id.spinnerType)
        breedSpinner = view.findViewById(R.id.spinnerBreed)
        ilnessEditText = view.findViewById(R.id.editTextIlness)
        vacunesSpinner = view.findViewById(R.id.spinnerVacunes)
        ageEditText = view.findViewById(R.id.editTextAge)
        weightEditText = view.findViewById(R.id.editTextWeight)
        savePet = view.findViewById(R.id.buttonSave)

        ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.species_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
            typeSpinner.adapter = adapter
        }

        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                type = typeSpinner.selectedItem.toString()
                breedSpinner()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                type = ""
            }
        }

            breedSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    breed = breedSpinner.selectedItem.toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    breed = ""
                }
            }

            ArrayAdapter.createFromResource(
                requireActivity(),
                R.array.vaccine_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears.
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner.
                vacunesSpinner.adapter = adapter
            }

            vacunesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    vacunes += vacunesSpinner.selectedItem.toString() + "\n"
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    vacunes = ""
                }
            }

            savePet.setOnClickListener {
                if (
                    !nameEditText.text.isNullOrBlank() && type != ""
                    && breed != "" && !ilnessEditText.text.isNullOrBlank()
                    && vacunes != "" && !ageEditText.text.isNullOrBlank()
                    && !weightEditText.text.isNullOrBlank()
                ) {

                    name = nameEditText.text.toString().trim().capitalize()
                    ilness = ilnessEditText.text.toString().trim().capitalize()
                    age = ageEditText.text.toString().trim().capitalize()
                    weight = weightEditText.text.toString().trim().capitalize()
                    owner = FirebaseAuth.getInstance().currentUser!!.uid.toString()

                    val generatedId: String? = db_ref.child("Pets").push().key

                    GlobalScope.launch {
                        val pet: Pet
                        if (url_photo != null) {
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
                        } else {
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

                        Utilities.createPet(pet, db_ref)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Mascota guardada con exito",
                                Toast.LENGTH_SHORT
                            ).show()

                            navController.navigate(R.id.action_clientAddPetFragment_to_clientPetFragment)
                        }

                    }

                } else {
                    Toast.makeText(requireContext(), "Faltan datos", Toast.LENGTH_SHORT).show()
                }

            }

            photo.setOnClickListener {
                galeryAcces.launch("image/*")
            }

            return view
        }

        private fun breedSpinner(){
            when (type) {
                "Perro" -> {
                    ArrayAdapter.createFromResource(
                        requireActivity(),
                        R.array.dog_breed_array,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        // Specify the layout to use when the list of choices appears.
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        // Apply the adapter to the spinner.
                        breedSpinner.adapter = adapter
                    }
                }
                "Gato" -> {
                    ArrayAdapter.createFromResource(
                        requireActivity(),
                        R.array.cat_breed_array,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        // Specify the layout to use when the list of choices appears.
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        // Apply the adapter to the spinner.
                        breedSpinner.adapter = adapter
                    }

                }
                "Pájaro" -> {
                    ArrayAdapter.createFromResource(
                        requireActivity(),
                        R.array.bird_breed_array,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        // Specify the layout to use when the list of choices appears.
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        // Apply the adapter to the spinner.
                        breedSpinner.adapter = adapter
                    }
                }
                "Otro" -> {
                    ArrayAdapter.createFromResource(
                        requireActivity(),
                        R.array.other_breed_array,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        // Specify the layout to use when the list of choices appears.
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        // Apply the adapter to the spinner.
                        breedSpinner.adapter = adapter
                    }
                }
            }
        }

        private val galeryAcces = registerForActivityResult(ActivityResultContracts.GetContent())
        { uri: Uri? ->
            if (uri != null) {
                url_photo = uri
                photo.setImageURI(uri)
            }
        }
    }