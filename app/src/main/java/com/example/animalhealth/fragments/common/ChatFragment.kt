package com.example.animalhealth.fragments.common

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.animalhealth.R
import com.example.animalhealth.clases.Chat
import com.google.firebase.database.*
import com.example.animalhealth.adapters.chatAdapter
import com.google.firebase.auth.FirebaseAuth

class ChatFragment : Fragment() {
private lateinit var adapter : chatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        var dbRef = FirebaseDatabase.getInstance().reference

        var chat_list = mutableListOf<Chat>()
        var recycler : RecyclerView
        recycler = view.findViewById(R.id.recyclerChat)

        dbRef.child("Users").child("ChatPublico").get().addOnSuccessListener {
            if (it.exists()) {
                for (i in it.children) {
                    val chat = i.getValue(Chat::class.java)
                    if (chat?.id != "") {
                        chat_list.add(chat!!)
                    }
                    Log.d("CHAT1", chat.toString())
                }
                recycler.adapter?.notifyDataSetChanged()
            }
        }

        dbRef.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Chats").get().addOnSuccessListener {
            if (it.exists()) {
                for (i in it.children) {
                    val chat = i.getValue(Chat::class.java)
                    if (chat?.id != "") {
                        chat_list.add(chat!!)
                    }
                    Log.d("CHAT1", chat.toString())
                }
                recycler.adapter?.notifyDataSetChanged()
            }
        }
        Log.d("CHAT", chat_list.toString())
        adapter = chatAdapter(chat_list)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(requireActivity())

        return view
    }
}

