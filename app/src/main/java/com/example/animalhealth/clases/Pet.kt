package com.example.animalhealth.clases

data class Pet (
    val id:String,
    val name:String,
    val breed:String,
    val age:Int,
    val ilness:Array<String>,
    val weight:Double,
    val ownerId:String,
    val photo:String
)