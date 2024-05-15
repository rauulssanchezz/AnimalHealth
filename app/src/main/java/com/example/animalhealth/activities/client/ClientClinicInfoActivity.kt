package com.example.animalhealth.activities.client

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.animalhealth.R
import com.example.animalhealth.adapters.ClinicAdapter
import com.example.animalhealth.adapters.ReviewsAdapter
import com.example.animalhealth.clases.Clinic
import com.example.animalhealth.clases.Reviews
import com.example.animalhealth.clases.Utilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ClientClinicInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_clinic_info)

        val dbRef = FirebaseDatabase.getInstance().reference
        val clinic = intent.getParcelableExtra<Clinic>("clinic")

        val backButton = findViewById<ImageView>(R.id.backButton)
        val name = findViewById<TextView>(R.id.clinicName)
        val address = findViewById<TextView>(R.id.clinicAddress)
        val rate = findViewById<RatingBar>(R.id.clinicRate)
        val phone = findViewById<TextView>(R.id.clinicPhone)
        val photo = findViewById<ImageView>(R.id.clinicImage)
        val reviewsButton = findViewById<TextView>(R.id.reviewButton)
        val reviewCardView = findViewById<CardView>(R.id.writeReviewCard)
        val saveReview = findViewById<TextView>(R.id.saveReview)
        val list = mutableListOf<Reviews>()
        val recycler = findViewById<RecyclerView>(R.id.reviewsRecyclerView)
        val sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE)
        val userName = sharedPreferences.getString("Name", "")!!
        val userImg = sharedPreferences.getString("Img", "")!!
        val userId = FirebaseAuth.getInstance().uid!!

        var adapter: ReviewsAdapter

        val URL: String? = when (clinic!!.photo) {
            "" -> null
            else -> clinic.photo
        }

        Glide.with(applicationContext).load(URL).apply(Utilities.glideOptions(applicationContext))
            .transition(Utilities.transition).into(photo)

        name.text = clinic.name
        address.text = clinic.location
        phone.text = clinic.phone

        dbRef.child("Reviews").child(clinic.id!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var countReview = 0
                    var mediaReviews = 0.0f
                    list.clear()
                    snapshot.children.forEach { hijo: DataSnapshot? ->
                        val pojo_review = hijo?.getValue(Reviews::class.java)
                        list.add(pojo_review!!)
                        Log.d("Review", pojo_review.rate.toString())
                        countReview++
                        mediaReviews += pojo_review.rate!!
                        Log.d("Review double", pojo_review.rate!!.toDouble().toString())
                        Log.d("Media", mediaReviews.toString())
                    }
                    if (countReview != 0) {
                        mediaReviews /= countReview
                        clinic.rate = mediaReviews
                        GlobalScope.launch {
                            Utilities.createClinic(clinic, dbRef)
                        }
                    }
                    recycler.adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }
            })

        adapter = ReviewsAdapter(list)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(applicationContext)

        rate.rating = clinic.rate
        Log.d("ClinicRate", rate.rating.toString())

        backButton.setOnClickListener {
            onBackPressed()
        }

        reviewsButton.setOnClickListener {
            reviewCardView.visibility = View.VISIBLE
            reviewsButton.visibility = View.GONE
            saveReview.visibility = View.VISIBLE
            val reviewRating = findViewById<RatingBar>(R.id.reviewRatingBar)

            saveReview.setOnClickListener {
                Log.d("Guardar", "Guardando valoración")
                if (reviewRating.rating == 0.0f) {
                    Log.d("Guardar", "No puedes guardar una valoración vacía")
                    Toast.makeText(
                        this,
                        "No puedes guardar una valoración vacía",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    dbRef.child("Reviews").child(clinic.id!!)
                        .orderByChild("userId")
                        .equalTo(userId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var reviewId = dbRef.push().key!!
                                if (snapshot.exists()) {
                                    for (reviewSnapshot in snapshot.children) {
                                        reviewId = reviewSnapshot.key!!
                                    }
                                }

                                val review = Reviews(
                                    reviewId,
                                    clinic.id,
                                    userId,
                                    userName,
                                    reviewRating.rating,
                                    userImg
                                )

                                dbRef.child("Reviews").child(clinic.id!!).child(reviewId).setValue(review)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            reviewCardView.visibility = View.GONE
                                            Toast.makeText(this@ClientClinicInfoActivity, "Valoración guardada", Toast.LENGTH_SHORT).show()
                                            reviewsButton.visibility = View.VISIBLE
                                            saveReview.visibility = View.GONE
                                        } else {
                                            Toast.makeText(this@ClientClinicInfoActivity, "Error al guardar la valoración", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("Firebase", "Error al verificar las reseñas existentes", error.toException())
                            }
                        })
                }
            }
        }
    }
}