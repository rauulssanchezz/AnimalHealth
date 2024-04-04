package com.example.animalhealth.fragments.vet

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import com.example.animalhealth.R
import kotlinx.coroutines.Job

class VetAddClinicFragment : Fragment() {
    private lateinit var photo: ImageView
    private var url_photo: Uri? = null
    private lateinit var job: Job

    private lateinit var nameEditText: EditText
    private lateinit var locationEditText: EditText
    private lateinit var buttonSave: Button

    private var name = ""
    private var location = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_vet_add_clinic, container, false)

        photo = view.findViewById(R.id.addPhoto)
        job=Job()

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