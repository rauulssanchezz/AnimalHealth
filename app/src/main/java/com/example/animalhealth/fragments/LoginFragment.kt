package com.example.animalhealth.fragments

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.animalhealth.VetMainActivity
import com.example.animalhealth.R
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        if (FirebaseAuth.getInstance().currentUser != null) {
            val navController = findNavController()
            val newIntent = Intent(requireActivity(),VetMainActivity::class.java)
            startActivity(newIntent)
        }

        val buttonLogin=view.findViewById<TextView>(R.id.buttonLogin)
        val signInButton = view.findViewById<SignInButton>(R.id.buttonLoginGoogle)
        signInButton.setSize(SignInButton.SIZE_WIDE) // Puedes ajustar el tamaño del botón según tus necesidades

        val emailEditText = view.findViewById<TextView>(R.id.editTextEmail)
        val passwordEditText = view.findViewById<TextView>(R.id.editTextPassword)

        var email=""
        var password=""

        val textView = view.findViewById<TextView>(R.id.textViewRegisterOrLoginWith)
        textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        textView.setOnClickListener {
            val navController = findNavController()

            // Activa la acción de navegación para ir al Fragment principal
            navController.navigate(R.id.action_loginFragment_to_registerFragment)
        }

        buttonLogin.setOnClickListener {
            email=emailEditText.text.toString()
            password=passwordEditText.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
            }else {
                val auth = FirebaseAuth.getInstance()
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val newIntent = Intent(requireActivity(),VetMainActivity::class.java)
                        startActivity(newIntent)
                    } else {
                        // Si el inicio de sesión falla, muestra un mensaje de error
                        Toast.makeText(context, "Error al iniciar sesión", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        return view
    }
}