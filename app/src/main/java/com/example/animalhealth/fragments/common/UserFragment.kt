package com.example.animalhealth.fragments.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.animalhealth.R
import com.example.animalhealth.activities.LoginActivity
import com.example.animalhealth.clases.User
import com.example.animalhealth.clases.Utilities
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserFragment : Fragment() {
    private lateinit var photo : ImageView
    private var url_photo: Uri? = null
    var url_img:String=""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        val sharedPreferences = requireActivity().getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        photo = view.findViewById<ImageView>(R.id.userPhoto)
        val userNameEditText = view.findViewById<TextInputEditText>(R.id.userNameEditText)
        val userEmailEditText = view.findViewById<TextInputEditText>(R.id.userEmailEditText)
        val userTypeEditText = view.findViewById<TextInputEditText>(R.id.userTypeEditText)
        val userPasswordEditText = view.findViewById<TextInputEditText>(R.id.userPasswordEditText)
        val saveChangesButton = view.findViewById<AppCompatButton>(R.id.saveChangesButton)
        val logOutButton = view.findViewById<Button>(R.id.logOutButton)

        url_img = sharedPreferences.getString("Img", "").toString()
        var name = sharedPreferences.getString("Name", "")
        var email = sharedPreferences.getString("Email", "")
        var type = sharedPreferences.getString("Type", "")
        var password = sharedPreferences.getString("Password", "")
        var favClinics = sharedPreferences.getString("FavClinics", "")

        val user = FirebaseAuth.getInstance().currentUser

        GlobalScope.launch {
            url_photo = Utilities.getPhoto("Users", user!!.uid)
        }

        photo.setOnClickListener {
            galeryAcces.launch("image/*")
        }

        Log.d("URL", url_photo.toString())

        val URL:String? = when (url_img){
            ""->null
            else->url_img
        }

        Glide.with(requireContext()).load(URL).apply(Utilities.glideOptions(requireContext())).transition(Utilities.transition).into(photo)

        userNameEditText.setText(name)
        userEmailEditText.setText(email)
        userTypeEditText.setText(type)
        userPasswordEditText.setText(password)

        saveChangesButton.setOnClickListener {
            with(sharedPreferences.edit()) {
                putString("Img", url_photo.toString())
                putString("Name", userNameEditText.text.toString())
                putString("Email", userEmailEditText.text.toString())
                putString("Type", userTypeEditText.text.toString())
                putString("Password", userPasswordEditText.text.toString())
                putString("FavClinics", favClinics)
                apply()
            }
            user?.updatePassword(userPasswordEditText.text.toString())?.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("UserFragment", "Password updated")
                } else {
                    Log.d("UserFragment", "Password not updated")
                }
            }
            user?.updateEmail(userEmailEditText.text.toString())?.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("UserFragment", "Email updated")
                } else {
                    Log.d("UserFragment", "Email not updated")
                }
            }

            GlobalScope.launch {
                url_img = Utilities.savePhoto(url_photo!!,"Users",FirebaseAuth.getInstance().currentUser!!.uid)

                Utilities.createUser(
                    userEmailEditText.text.toString(),
                    userPasswordEditText.text.toString(),
                    userNameEditText.text.toString(),
                    url_img,
                    userTypeEditText.text.toString(),
                    favClinics!!
                )
            }
            FirebaseAuth.getInstance().updateCurrentUser(user!!)
            Toast.makeText(requireContext(), "Cambios guardados", Toast.LENGTH_SHORT).show()

        }

        logOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
            val newIntent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(newIntent)
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