package com.example.animalhealth.clases

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Reviews(
    var id:String?="",
    var clinicId:String?="",
    var userId:String?="",
    var userName:String?="",
    var rate:Float?=0.0f,
    var userPhoto : String?="",
): Parcelable