package com.example.animalhealth.clases

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Mensaje(
    val id_mensaje: String? = null,
    val id_emisor: String? = null,
    var nombre_emisor: String? = null,
    var id_receptor: String? = null,
    var imagen_emisor: String? = null,
    val contenido: String? = null,
    val fecha_hora: String? = null,
) : Parcelable