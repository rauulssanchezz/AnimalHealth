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
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat.recreate
import com.bumptech.glide.Glide
import com.example.animalhealth.R
import com.example.animalhealth.activities.common.LoginActivity
import com.example.animalhealth.clases.Utilities
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserFragment : Fragment() {
    private lateinit var photo : ImageView
    private var url_photo: Uri? = null
    var url_img:String=""
    private lateinit var sharedPreferences: android.content.SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        sharedPreferences = requireActivity().getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        photo = view.findViewById<ImageView>(R.id.userPhoto)
        val userNameEditText = view.findViewById<TextInputEditText>(R.id.userNameEditText)
        val userEmailEditText = view.findViewById<TextInputEditText>(R.id.userEmailEditText)
        val userTypeEditText = view.findViewById<TextInputEditText>(R.id.userTypeEditText)
        val userPasswordEditText = view.findViewById<TextInputEditText>(R.id.userPasswordEditText)
        val saveChangesButton = view.findViewById<AppCompatButton>(R.id.saveChangesButton)
        val logOutButton = view.findViewById<Button>(R.id.logOutButton)
        val settingsButton = view.findViewById<ImageView>(R.id.settingsButton)

        url_img = sharedPreferences.getString("Img", "").toString()
        var beforeUrl_img = url_img.toString()
        var beforeName = sharedPreferences.getString("Name", "")
        var email = sharedPreferences.getString("Email", "")
        var type = sharedPreferences.getString("Type", "")
        var password = sharedPreferences.getString("Password", "")
        var favClinics = sharedPreferences.getString("FavClinics", "")
        var name = beforeName

        val user = FirebaseAuth.getInstance().currentUser

        settingsButton.setOnClickListener {
            Utilities.animation(it, 0.95f, 1.0f, 100,Runnable {
                Log.d("SETTINGS", "SETTINGS")
                showThemeMenu(it)
            })
        }

        Glide.with(requireContext()).load(beforeUrl_img).apply(Utilities.glideOptions(requireContext())).transition(Utilities.transition).into(photo)

        photo.setOnClickListener {
            Utilities.animation(it, 0.95f, 1.0f, 100,Runnable {
                galeryAcces.launch("image/*")
            })
        }

        Log.d("URL", url_photo.toString())

        userNameEditText.setText(beforeName)
        userEmailEditText.setText(email)
        userTypeEditText.setText(type)
        userPasswordEditText.setText(password)

        saveChangesButton.setOnClickListener {
            Utilities.animation(it, 0.95f, 1.0f, 100,Runnable {
                name = userNameEditText.text.toString()
                if (name == "") {
                    Toast.makeText(
                        requireContext(),
                        "El nombre no puede estar vacío",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {

                    GlobalScope.launch {
                        var url_photo_firebase = String()
                        if (beforeName == name && url_photo == null) {
                            GlobalScope.launch(Main) {
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
                                Utilities.savePhoto(url_photo!!, "Users", user!!.uid)
                        }

                        Utilities.createUser(
                            userEmailEditText.text.toString(),
                            userPasswordEditText.text.toString(),
                            userNameEditText.text.toString(),
                            url_photo_firebase,
                            userTypeEditText.text.toString(),
                            favClinics!!
                        )

                        GlobalScope.launch(Dispatchers.Main) {
                            with(sharedPreferences.edit()) {
                                putString("Img", url_photo_firebase.toString())
                                putString("Name", userNameEditText.text.toString())
                                apply()
                            }
                            Toast.makeText(
                                requireContext(),
                                "Cambios guardados",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
                }
            })
        }

        logOutButton.setOnClickListener {
            Utilities.animation(it, 0.95f, 1.0f, 100,Runnable {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
                val newIntent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(newIntent)
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
    private fun showThemeMenu(anchorView: View) {
        val popupMenu = PopupMenu(requireContext(), anchorView)
        popupMenu.menuInflater.inflate(R.menu.theme, popupMenu.menu)

        val currentNightMode = AppCompatDelegate.getDefaultNightMode()
        if (currentNightMode == AppCompatDelegate.MODE_NIGHT_NO) {
            popupMenu.menu.findItem(R.id.nightMode)?.isVisible = true
        } else {
            popupMenu.menu.findItem(R.id.dayMode)?.isVisible = true
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.dayMode -> {
                    // Cambiar al modo de día
                    sharedPreferences.edit().putInt("Theme", AppCompatDelegate.MODE_NIGHT_NO).apply()
                    recreate(requireActivity()) // Reinicia la actividad para aplicar el cambio de tema
                    true
                }
                R.id.nightMode -> {
                    // Cambiar al modo de noche
                    sharedPreferences.edit().putInt("Theme", AppCompatDelegate.MODE_NIGHT_YES).apply()
                    recreate(requireActivity()) // Reinicia la actividad para aplicar el cambio de tema
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}