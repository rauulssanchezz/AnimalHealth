package com.example.animalhealth.fragments.client

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
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
    private lateinit var ageEditText: TextInputEditText
    private lateinit var weightEditText: TextInputEditText
    private lateinit var savePet: Button
    private lateinit var back : ImageView

    // Variables para los CheckBoxes
    private lateinit var vaccineCheckBoxes: List<CheckBox>

    private var name = ""
    private var type = ""
    private var breed = ""
    private var ilness = ""
    private var vaccines = ""
    private var age = ""
    private var weight = ""
    private var owner = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_client_add_pet, container, false)

        navController = requireParentFragment().findNavController()
        job = Job()
        db_ref = FirebaseDatabase.getInstance().reference

        var pulsado = false

        photo = view.findViewById(R.id.addPhoto)
        nameEditText = view.findViewById(R.id.editTextName)
        typeSpinner = view.findViewById(R.id.spinnerType)
        breedSpinner = view.findViewById(R.id.spinnerBreed)
        ilnessEditText = view.findViewById(R.id.editTextIlness)
        ageEditText = view.findViewById(R.id.editTextAge)
        weightEditText = view.findViewById(R.id.editTextWeight)
        savePet = view.findViewById(R.id.buttonSave)
        back = view.findViewById(R.id.petBackButton)

        back.setOnClickListener {
            Utilities.animation(it, 0.95f, 1.0f, 100,Runnable {
                navController.navigate(R.id.action_clientAddPetFragment_to_clientPetFragment)
            })
        }

        // Inicializar los CheckBoxes de vacunas
        vaccineCheckBoxes = listOf(
            view.findViewById(R.id.checkBoxVacuna1),
            view.findViewById(R.id.checkBoxVacuna2),
            view.findViewById(R.id.checkBoxVacuna3)
            // Agrega más CheckBoxes según sea necesario
        )

        ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.species_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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

        savePet.setOnClickListener {
            Utilities.animation(it, 0.95f, 1.0f, 100,Runnable {
                name = nameEditText.text.toString().trim().capitalize()
                ilness = ilnessEditText.text.toString().trim().capitalize()
                age = ageEditText.text.toString().trim().capitalize()
                weight = weightEditText.text.toString().trim().capitalize()
                owner = FirebaseAuth.getInstance().currentUser!!.uid.toString()

                // Obtener vacunas seleccionadas
                vaccines = vaccineCheckBoxes.filter { it.isChecked }
                    .joinToString(separator = "\n") { it.text.toString() }

                if (vaccines.isBlank()) {
                    vaccines = "No tiene vacunas"
                }

                if (
                    name.isNotEmpty() &&
                    type.isNotEmpty() &&
                    breed.isNotEmpty() &&
                    age.isNotEmpty() &&
                    ilness.isNotEmpty() &&
                    weight.isNotEmpty() &&
                    !pulsado
                ) {

                    pulsado = true
                    val generatedId: String? = db_ref.child("Pets").push().key

                    GlobalScope.launch {
                        val pet: Pet
                        if (url_photo != null) {
                            val urlPhotoFirebase = Utilities.savePetPhoto(
                                url_photo!!,
                                "Pets",
                                FirebaseAuth.getInstance().currentUser!!.uid,
                                generatedId!!
                            )
                            pet = Pet(
                                generatedId!!,
                                name,
                                type,
                                breed,
                                age,
                                ilness,
                                vaccines,
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
                                vaccines,
                                weight,
                                owner
                            )
                        }

                        Utilities.createPet(pet, db_ref)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Mascota guardada con éxito",
                                Toast.LENGTH_SHORT
                            ).show()

                            navController.navigate(R.id.action_clientAddPetFragment_to_clientPetFragment)
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Faltan datos", Toast.LENGTH_SHORT).show()
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

    private fun breedSpinner() {
        when (type) {
            "Perro" -> {
                ArrayAdapter.createFromResource(
                    requireActivity(),
                    R.array.dog_breed_array,
                    android.R.layout.simple_spinner_item
                ).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    breedSpinner.adapter = adapter
                }
            }
            "Gato" -> {
                ArrayAdapter.createFromResource(
                    requireActivity(),
                    R.array.cat_breed_array,
                    android.R.layout.simple_spinner_item
                ).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    breedSpinner.adapter = adapter
                }
            }
            "Pájaro" -> {
                ArrayAdapter.createFromResource(
                    requireActivity(),
                    R.array.bird_breed_array,
                    android.R.layout.simple_spinner_item
                ).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    breedSpinner.adapter = adapter
                }
            }
            "Otro" -> {
                ArrayAdapter.createFromResource(
                    requireActivity(),
                    R.array.other_breed_array,
                    android.R.layout.simple_spinner_item
                ).also { adapter ->  adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    breedSpinner.adapter = adapter
                }
            }
        }
    }

    private val galeryAcces = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            url_photo = uri
            photo.setImageURI(uri)
        }
    }
}