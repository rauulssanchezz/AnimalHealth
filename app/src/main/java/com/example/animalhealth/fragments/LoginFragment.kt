package com.example.animalhealth.fragments

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.animalhealth.R
import com.google.android.gms.common.SignInButton

class LoginFragment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        val signInButton = view.findViewById<SignInButton>(R.id.buttonLoginGoogle)
        signInButton.setSize(SignInButton.SIZE_WIDE) // Puedes ajustar el tamaño del botón según tus necesidades

        val textView = view.findViewById<TextView>(R.id.textViewRegisterOrLoginWith)
        textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        textView.setOnClickListener {
            val navController = findNavController()

            // Activa la acción de navegación para ir al Fragment principal
            navController.navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return view
    }
}