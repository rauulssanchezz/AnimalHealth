package com.example.animalhealth.clases

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    var id: String,
    var name: String,
    var email: String,
    var password: String,
    var type: String,
    var img: String = ""
):Parcelable