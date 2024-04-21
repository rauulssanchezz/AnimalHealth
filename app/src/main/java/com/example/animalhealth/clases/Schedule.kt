package com.example.animalhealth.clases

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Schedule (
    var id: String = "",
    var days : MutableList<String> = mutableListOf(""),
    var clinicId: String = "",
    var hourStart : String = "",
    var hourEnd : String = "",
    var timeSlot : MutableList<String> = mutableListOf("")
): Parcelable