package com.example.animalhealth.clases

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Pet (
    val id:String,
    var name:String,
    var type:String,
    var breed:String,
    var age:String,
    var ilness:String,
    var vacunes:String,
    var weight:String,
    var ownerId:String,
    var photo:String = ""
):Parcelable