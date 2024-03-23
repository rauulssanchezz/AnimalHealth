package com.example.animalhealth.clases

data class User(
    var id: String,
    var name: String,
    var email: String,
    var password: String,
    var type: String,
    var img: String = ""
)