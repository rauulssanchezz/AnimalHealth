package com.example.animalhealth.clases

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Chat(
    val id:String = "",
    var idDestinatario:String = "",
    var nombreDestinatario:String = "",
    var fotoDestinatario:String = "",
): Parcelable