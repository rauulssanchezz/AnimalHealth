package com.example.animalhealth.fragments.common

import android.graphics.Paint
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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.animalhealth.R
import com.example.animalhealth.clases.Utilities
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var photo: ImageView
    private var url_photo: Uri? = null
    private var email:String=""
    private var password:String=""
    private var nombre:String=""
    private var type:String=""
    private lateinit var emailEdit: TextInputEditText
    private lateinit var passwordEdit: TextInputEditText
    private lateinit var confirmPasswordEdit: TextInputEditText
    private lateinit var nameEdit: TextInputEditText
    private lateinit var register: Button
    private lateinit var job: Job
    private lateinit var navController: NavController
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        auth= FirebaseAuth.getInstance()
        photo = view.findViewById<ImageView>(R.id.addPhoto)
        emailEdit = view.findViewById<TextInputEditText>(R.id.editTextEmail)
        passwordEdit = view.findViewById<TextInputEditText>(R.id.editTextPassword)
        confirmPasswordEdit = view.findViewById<TextInputEditText>(R.id.editTextConfirmPassword)
        nameEdit = view.findViewById<TextInputEditText>(R.id.editTextName)
        register = view.findViewById<Button>(R.id.buttonRegister)
        job= Job()
        navController = findNavController()

        val textView = view.findViewById<TextView>(R.id.textViewRegisterOrLoginWith)
        textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        textView.setOnClickListener {
            navController.navigate(R.id.action_registerFragment_to_loginFragment)
        }

        val spinner: Spinner = view.findViewById(R.id.type_spinner)
// Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.types_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener= object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                type= spinner.selectedItem.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                type=""
            }
        }

        photo.setOnClickListener {
            galeryAcces.launch("image/*")
        }

        register()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel() // Cancel all coroutines when the view is destroyed
    }

    private fun register(){
        register.setOnClickListener {
            if (
                emailEdit.text.toString().isNotEmpty()
                && passwordEdit.text.toString().isNotEmpty()
                && confirmPasswordEdit.text.toString().isNotEmpty()
                && nameEdit.text.toString().isNotEmpty()
                && type!=""
            ) {
                if (!emailEdit.text.toString().contains("@gmail.com")){
                    emailEdit.error = "Correo no válido"
                    // Correo no válido
                }else {
                    if (passwordEdit.text!!.length < 6) {
                        passwordEdit.error = "La contraseña debe tener al menos 6 caracteres"
                        // La contraseña debe tener al menos 6 caracteres
                    } else {

                        if (passwordEdit.text.toString() == confirmPasswordEdit.text.toString()) {
                            email = emailEdit.text.toString()
                            password = passwordEdit.text.toString()
                            nombre = nameEdit.text.toString()
                            // Aquí se debe registrar el usuario
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(requireActivity()) { task ->
                                    if (task.isSuccessful) {
                                        GlobalScope.launch {
                                            if (url_photo != null) {
                                                val url_img = Utilities.savePhoto(url_photo!!)
                                                Utilities.createUser(
                                                    email,
                                                    password,
                                                    nombre,
                                                    url_img,
                                                    type
                                                )
                                            } else {
                                                Utilities.createUser(
                                                    email,
                                                    password,
                                                    nombre,
                                                    "",
                                                    type
                                                )
                                            }
                                        }
                                        Toast.makeText(
                                            requireActivity(),
                                            "Usuario registrado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate(R.id.action_registerFragment_to_loginFragment)
                                    } else {
                                        Toast.makeText(
                                            requireActivity(),
                                            "Error en el registro",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } else {
                            passwordEdit.error = "Las contraseñas no coinciden"
                            confirmPasswordEdit.error = "Las contraseñas no coinciden"
                            // Las contraseñas no coinciden
                        }
                    }
                }
            } else {
                if (emailEdit.text.toString().isEmpty()) {
                    emailEdit.error = "Campo obligatorio"
                    // Campo obligatorio
                }
                if (passwordEdit.text.toString().isEmpty()) {
                    passwordEdit.error = "Campo obligatorio"
                    // Campo obligatorio
                }
                if (confirmPasswordEdit.text.toString().isEmpty()) {
                    confirmPasswordEdit.error = "Campo obligatorio"
                    // Campo obligatorio
                }
                if (nameEdit.text.toString().isEmpty()) {
                    nameEdit.error = "Campo obligatorio"
                    // Campo obligatorio
                }
                if (type == "") {
                    Toast.makeText(
                        requireActivity(),
                        "Seleccione un tipo de usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Seleccione un tipo de usuario
                }
                // Todos los campos son obligatorios
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