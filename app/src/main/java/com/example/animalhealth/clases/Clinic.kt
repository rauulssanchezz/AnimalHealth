package com.example.animalhealth.clases

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Clinic (
    val id : String = "",
    var name : String = "",
    var rate : String = "",
    var location : String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var photo : String = ""
):Parcelable