package com.example.animalhealth.clases

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class Booking(
    val id:String = "",
    var bookingReason:String = "",
    var date:String = "",
    var time:String = "",
    var clinicId :String = "",
    var ownerId:String = "",
    var petId:String = "",
    var ownerPhoto:String = ""
):Parcelable