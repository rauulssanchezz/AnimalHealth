package com.example.animalhealth.clases

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class Utilities {

    companion object{
        suspend fun createUser(email:String,password:String,name:String,img:String,type:String){
            var dtb_ref= FirebaseDatabase.getInstance().reference
            val user=User(FirebaseAuth.getInstance().currentUser!!.uid,name, email, password,type,img)
            dtb_ref.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(user).await()
        }

        suspend fun savePhoto(image:Uri):String{
            lateinit var url_photo_firebase: Uri
            var sto_ref: StorageReference = FirebaseStorage.getInstance().reference
            url_photo_firebase = sto_ref.child("Users").child("photos").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .putFile(image).await().storage.downloadUrl.await()

            return url_photo_firebase.toString()

        }

        suspend fun obtainUser(dtb_ref: DatabaseReference):User{
            var user:User?=null
            try {
                Log.d("UserUid",FirebaseAuth.getInstance().currentUser!!.uid)
                val dataSnapshot = dtb_ref.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).get().await()
                Log.d("DataSnapShot",dataSnapshot.value.toString())
                user = dataSnapshot.getValue(User::class.java)
                Log.d("UserData",user.toString())
            } catch (e: Exception) {
                // Manejar excepción
            }
            return user!!
        }
    }

}