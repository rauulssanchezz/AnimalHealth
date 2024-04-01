package com.example.animalhealth.clases

import java.util.Date

data class Booking(
    val id:String,
    val ownerName:String,
    val bookingReason:String,
    val petName:String,
    val petBreed:String,
    val date:Date,
    val time:String,
    val ownerPhoto:String
)