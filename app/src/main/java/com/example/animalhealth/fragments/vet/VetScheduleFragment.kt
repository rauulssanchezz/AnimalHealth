package com.example.animalhealth.fragments.vet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import com.example.animalhealth.R
import com.example.animalhealth.clases.Schedule
import com.example.animalhealth.clases.Utilities
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class VetScheduleFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_vet_schedule, container, false)

        val dbReference = Firebase.database.reference
        val monday = view.findViewById<CheckBox>(R.id.mondayCheckBox)
        val tuesday = view.findViewById<CheckBox>(R.id.tuesdayCheckBox)
        val wednesday = view.findViewById<CheckBox>(R.id.wednesdayCheckBox)
        val thursday = view.findViewById<CheckBox>(R.id.thursdayCheckBox)
        val friday = view.findViewById<CheckBox>(R.id.fridayCheckBox)
        val saturday = view.findViewById<CheckBox>(R.id.saturdayCheckBox)
        val sunday = view.findViewById<CheckBox>(R.id.sundayCheckBox)
        val horasDelDia = (0..23).map { it.toString().padStart(2, '0') }
        val startTimespinner: Spinner = view.findViewById(R.id.startTimeSpinner)
        val endTimespinner: Spinner = view.findViewById(R.id.endTimeSpinner)
        val timeSlotEditText = view.findViewById<EditText>(R.id.timeSlotEditText)
        var days = mutableListOf<String>()
        var hourStart = ""
        var hourEnd = ""
        var timeSlot = ""
        var timeSlotList = mutableListOf<String>()

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, horasDelDia)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        startTimespinner.adapter = adapter
        endTimespinner.adapter = adapter

        startTimespinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                hourStart = parent?.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No se necesita hacer nada
            }
        }

        endTimespinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                hourEnd = parent?.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No se necesita hacer nada
            }
        }

        if (timeSlotEditText.text.isNotEmpty()) {
            timeSlot = timeSlotEditText.text.toString()
        }

        if (hourStart.isNotEmpty() && hourEnd.isNotEmpty() && timeSlot.isNotEmpty()) {
            timeSlotList = repartirCitas(hourStart.toInt() * 60, hourEnd.toInt() * 60, timeSlot.toInt()*60)
        }

        monday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                days.add("Monday")
            } else {
                days.remove("Monday")
            }
        }

        tuesday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                days.add("Tuesday")
            } else {
                days.remove("Tuesday")
            }
        }

        wednesday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                days.add("Wednesday")
            } else {
                days.remove("Wednesday")
            }
        }

        thursday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                days.add("Thursday")
            } else {
                days.remove("Thursday")
            }
        }

        friday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                days.add("Friday")
            } else {
                days.remove("Friday")
            }
        }

        saturday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                days.add("Saturday")
            } else {
                days.remove("Saturday")
            }
        }

        sunday.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                days.add("Sunday")
            } else {
                days.remove("Sunday")
            }
        }

        var sharedPreferences = requireActivity().getSharedPreferences("sharedPreferences", 0)
        var clinicId = sharedPreferences.getString("clinicId", "")
        var schedule = Schedule(dbReference.push().key.toString(),days, clinicId!!, hourStart, hourEnd, timeSlotList)

        GlobalScope.launch {
            Utilities.saveSchedule(schedule, dbReference)
        }

        return view
    }

    fun repartirCitas(horaInicio: Int, horaFin: Int, duracionCita: Int): MutableList<String> {
        val citas = mutableListOf<String>()
        var horaActual = horaInicio

        while (horaActual + duracionCita <= horaFin) {
            val horaInicioCita = formatarHora(horaActual)
            val horaFinCita = formatarHora(horaActual + duracionCita)
            val intervalo = "$horaInicioCita - $horaFinCita"
            citas.add(intervalo)
            horaActual += duracionCita
        }

        return citas
    }

    fun formatarHora(hora: Int): String {
        val horas = hora / 60
        val minutos = hora % 60
        return String.format("%02d:%02d", horas, minutos)
    }

}