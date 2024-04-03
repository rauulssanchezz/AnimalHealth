package com.example.animalhealth.clases

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class Booking(
    val id:String,
    val bookingReason:String,
    val date:Date,
    val time:String,
    val ownerId:String,
    val petId:String,
    val ownerPhoto:String
):Parcelable