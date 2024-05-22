package com.example.animalhealth.adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.animalhealth.R
import com.example.animalhealth.activities.common.MensajeActivity
import com.example.animalhealth.clases.Chat
import com.example.animalhealth.clases.Utilities
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class chatAdapter(private val chat_list:MutableList<Chat>): RecyclerView.Adapter<chatAdapter.ChatViewHolder>(),
    Filterable {

    private lateinit var context: Context
    private var filter_list = chat_list
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dbRef: DatabaseReference


    private lateinit var navController: NavController


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val item_view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        context = parent.context
        sharedPreferences = context.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        navController = Navigation.findNavController(parent)
        dbRef = FirebaseDatabase.getInstance().reference
        return ChatViewHolder(item_view)
    }

    override fun getItemCount(): Int = filter_list.size

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.itemUserName)
        val photo: ImageView = itemView.findViewById(R.id.itemUserPhoto)

    }


    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val actual_item = filter_list[position]

        // Establecer valores iniciales o por defecto
        holder.name.text = actual_item.nombreDestinatario

        holder.itemView.setOnClickListener {
            Utilities.animation(it, 0.95f, 1.0f, 100,Runnable {
                val newIntent = Intent(context, MensajeActivity::class.java)
                newIntent.putExtra("userId", actual_item.idDestinatario)
                context.startActivity(newIntent)
            })
        }

        // Cargar la imagen del dueño usando Glide
        val photoUrl =
            if (actual_item.fotoDestinatario.isNullOrEmpty()) null else actual_item.fotoDestinatario
        Glide.with(context)
            .load(photoUrl)
            .apply(Utilities.glideOptions(context))
            .transition(Utilities.transition)
            .into(holder.photo)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                filterResults.values = filter_list
                return filterResults
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                notifyDataSetChanged()
            }
        }
    }
}