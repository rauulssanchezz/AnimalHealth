package com.example.animalhealth.clases

import java.util.Date

data class Booking(
    val id:String,
    val bookingReason:String,
    val date:Date,
    val time:String,
    val ownerId:String,
    val petId:String,
    val ownerPhoto:String
)