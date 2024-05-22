package com.example.animalhealth.clases

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Booking(
    val id:String = "",
    var bookingReason:String = "",
    var date:String = "", // Cambiar a formato "YYYY-MM-DD"
    var startHour:String = "",
    var clinicId :String = "",
    var ownerId:String = "",
    var petId:String = "",
    var ownerPhoto:String = ""
) : Parcelable
