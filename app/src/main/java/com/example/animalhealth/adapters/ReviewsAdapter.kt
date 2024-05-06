package com.example.animalhealth.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.animalhealth.R
import com.example.animalhealth.clases.Reviews
import com.example.animalhealth.clases.Utilities

class ReviewsAdapter(private val reviews_list:MutableList<Reviews>): RecyclerView.Adapter<ReviewsAdapter.ReviewsViewHolder>() {

    private lateinit var context: Context
    private var filter_list = reviews_list


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsViewHolder {
        val item_view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        context = parent.context
        return ReviewsViewHolder(item_view)
    }

    override fun getItemCount(): Int = filter_list.size

    class ReviewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photo: ImageView = itemView.findViewById(R.id.userReviewPhoto)
        val name: TextView = itemView.findViewById(R.id.itemReviewUserName)
        val ratingBar: RatingBar = itemView.findViewById(R.id.rateReview)
    }


    override fun onBindViewHolder(holder: ReviewsViewHolder, position: Int) {
        val actual_item = filter_list[position]
        holder.name.text = actual_item.userName
        holder.ratingBar.rating = actual_item.rate?: 0.0f

        val URL: String? = when (actual_item.userPhoto) {
            "" -> null
            else -> actual_item.userPhoto
        }

        Glide.with(context).load(URL).apply(Utilities.glideOptions(context))
            .transition(Utilities.transition).into(holder.photo)
    }
}
