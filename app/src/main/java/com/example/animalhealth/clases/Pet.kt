package com.example.animalhealth.clases

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Pet (
    val id:String,
    val name:String,
    val type:String,
    val breed:String,
    val age:Int,
    val ilness:String,
    val vacunes:String,
    val weight:Double,
    val ownerId:String,
    val photo:String
):Parcelable