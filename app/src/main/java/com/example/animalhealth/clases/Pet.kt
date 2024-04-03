package com.example.animalhealth.clases

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Pet (
    val id:String,
    val name:String,
    val type:String,
    val breed:String,
    val age:String,
    val ilness:String,
    val vacunes:String,
    val weight:String,
    val ownerId:String,
    val photo:String = ""
):Parcelable