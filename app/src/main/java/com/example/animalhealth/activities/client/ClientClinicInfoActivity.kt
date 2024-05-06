package com.example.animalhealth.activities.client

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        var dbRef = FirebaseDatabase.getInstance().reference
        var clinic = intent.getParcelableExtra<Clinic>("clinic")

        val backButton = findViewById<ImageView>(R.id.backButton)

        val name = findViewById<TextView>(R.id.clinicName)
        val address = findViewById<TextView>(R.id.clinicAddress)
        val rate = findViewById<RatingBar>(R.id.clinicRate)
        val phone = findViewById<TextView>(R.id.clinicPhone)
        val reviewsButton = findViewById<TextView>(R.id.reviewButton)
        val reviewCardView = findViewById<CardView>(R.id.writeReviewCard)
        val saveReview = findViewById<TextView>(R.id.saveReview)
        var list = mutableListOf<Reviews>()
        var recycler = findViewById<RecyclerView>(R.id.reviewsRecyclerView)
        var adapter: ReviewsAdapter

        name.text = clinic?.name
        address.text = clinic?.location
        phone.text = clinic?.phone
        rate.rating = clinic?.rate!!

        var countReview = 0
        var mediaReviews = 0.0f

        list.clear()
        dbRef.child("Reviews")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { hijo: DataSnapshot? ->
                        val pojo_review = hijo?.getValue(Reviews::class.java)
                        list.add(pojo_review!!)
                        countReview++
                        mediaReviews += pojo_review.rate!!
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
        recycler.setHasFixedSize(true)
        rate.rating = mediaReviews

        mediaReviews /= countReview

        clinic?.rate = mediaReviews

        backButton.setOnClickListener {
            onBackPressed()
        }

        reviewsButton.setOnClickListener {
            reviewCardView.visibility = CardView.VISIBLE
            reviewsButton.visibility = AppCompatButton.GONE
            saveReview.visibility = AppCompatButton.VISIBLE
            val reviewRating = findViewById<RatingBar>(R.id.reviewRatingBar)
            var reviewRate = findViewById<RatingBar>(R.id.reviewRatingBar)
            var review = Reviews()
            var userImg = ""
            var userName = ""
            var id = ""
            reviewRate.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { ratingBar, rating, fromUser ->
                id = dbRef.push().key!!
                var sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE)
                userName = sharedPreferences.getString("Name","")!!
                userImg = sharedPreferences.getString("Img","")!!

            }

            saveReview.setOnClickListener {
                Log.d("Guardar", "Guardando valoración")
                if (reviewRate.rating == 0.0f) {
                    Log.d("Guardar", "No puedes guardar una valoración vacia")
                    Toast.makeText(
                        this,
                        "No puedes guardar una valoración vacia",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.d("Guardar", "Guardando valoración correctamente")
                    review = Reviews(
                        id,
                        clinic?.id,
                        FirebaseAuth.getInstance().uid,
                        userName,
                        reviewRating.rating,
                        userImg
                    )
                    reviewCardView.visibility = CardView.GONE
                    Toast.makeText(this, "Valoración guardada", Toast.LENGTH_SHORT).show()
                    GlobalScope.launch {
                        Utilities.saveReview(review, dbRef)
                    }
                    Log.d("Guardar", "Se ha guardado")
                    reviewsButton.visibility = AppCompatButton.VISIBLE
                    saveReview.visibility = AppCompatButton.GONE
                }
            }

        }
    }
}