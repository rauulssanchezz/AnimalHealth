package com.example.animalhealth.clases

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Clinic (
    val id : String = "",
    var name : String = "",
    var rate : Float = 0.0f,
    var location : String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var vetId : String = "",
    var photo : String = "",
    var phone : String = "",
):Parcelable