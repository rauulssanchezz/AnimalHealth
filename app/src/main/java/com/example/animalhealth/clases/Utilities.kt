package com.example.animalhealth.clases

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.animalhealth.R
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Utilities {

    companion object{
        suspend fun createUser(email:String,password:String,name:String,img:String,type:String,fav:String){
            val dtb_ref= FirebaseDatabase.getInstance().reference
            val user=User(FirebaseAuth.getInstance().currentUser!!.uid,name, email, password,type,img,fav)
            dtb_ref.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(user).await()
        }

        suspend fun createPet(pet:Pet,db_ref:DatabaseReference){
            db_ref.child("Pets").child(pet.ownerId).child(pet.id).setValue(pet).await()
        }

        suspend fun createClinic(clinic:Clinic,db_ref:DatabaseReference){
            db_ref.child("Clinics").child(clinic.id).setValue(clinic).await()
        }

        fun calcularDistancia(latitud1: Double, longitud1: Double, latitud2: Double, longitud2: Double): Double {
            val radioTierra = 6371 // Radio de la Tierra en kilómetros
            val deltaLatitud = Math.toRadians(latitud2 - latitud1)
            val deltaLongitud = Math.toRadians(longitud2 - longitud1)
            val a = sin(deltaLatitud / 2) * sin(deltaLatitud / 2) +
                    cos(Math.toRadians(latitud1)) * cos(Math.toRadians(latitud2)) *
                    sin(deltaLongitud / 2) * sin(deltaLongitud / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return radioTierra * c
        }

        suspend fun savePhoto(image:Uri,root:String,id:String):String{
            lateinit var url_photo_firebase: Uri
            var sto_ref: StorageReference = FirebaseStorage.getInstance().reference
            url_photo_firebase = sto_ref.child(root).child("photos").child(id)
                .putFile(image).await().storage.downloadUrl.await()

            return url_photo_firebase.toString()

        }

        suspend fun savePetPhoto(image:Uri,root:String,Ownerid:String,id: String):String{
            lateinit var url_photo_firebase: Uri
            var sto_ref: StorageReference = FirebaseStorage.getInstance().reference
            url_photo_firebase = sto_ref.child(root).child("photos").child(Ownerid).child(id)
                .putFile(image).await().storage.downloadUrl.await()

            return url_photo_firebase.toString()

        }

        suspend fun deletePhotos(root: String,id: String){
            var sto_ref: StorageReference = FirebaseStorage.getInstance().reference
            sto_ref.child(root).child("photos").child(id).delete().await()
        }

        suspend fun deletePets(ownerId:String,db_ref:DatabaseReference){
            deletePhotos("Pets",ownerId)
            db_ref.child("Pets").child(ownerId).removeValue().await()
        }

        suspend fun deleteUser(userId:String,db_ref:DatabaseReference){
            deletePhotos("Users",userId)
            deletePets(userId,db_ref)
            db_ref.child("Users").child(userId).removeValue().await()
        }

        suspend fun obtainUser(db_ref: DatabaseReference): User? {
            return suspendCancellableCoroutine { continuation ->
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }

                val userRef = db_ref.child("Users").child(currentUser.uid)

                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)
                        Log.d("DatosUsuario", user.toString())
                        continuation.resume(user)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                }

                userRef.addListenerForSingleValueEvent(listener)

                continuation.invokeOnCancellation {
                    userRef.removeEventListener(listener)
                }
            }
        }

        suspend fun saveBooking(booking:Booking,db_ref:DatabaseReference){
            db_ref.child("Bookings").child(booking.id).setValue(booking).await()
        }

        suspend fun saveReview(reviews: Reviews,db_ref:DatabaseReference){
            db_ref.child("Reviews").child(reviews.clinicId!!).child(reviews.id!!).setValue(reviews).await()
        }

        fun load_animation(contex: Context): CircularProgressDrawable {
            val animation = CircularProgressDrawable(contex)
            animation.strokeWidth = 5f
            animation.centerRadius = 30f
            animation.start()
            return animation
        }

        val transition = DrawableTransitionOptions.withCrossFade(500)
        fun glideOptions(contex: Context): RequestOptions {
            val options = RequestOptions().placeholder(load_animation(contex))
                .fallback(R.drawable.logo_animal_health)
                .error(R.drawable.baseline_error_outline_24)
            return options
        }
    }



}