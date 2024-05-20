package com.example.animalhealth.activities.common

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.animalhealth.R
import com.example.animalhealth.activities.VetMainActivity
import com.example.animalhealth.activities.client.ClientMainActivity
import com.example.animalhealth.adapters.MensajeAdaptador
import com.example.animalhealth.clases.Chat
import com.example.animalhealth.clases.Mensaje
import com.example.animalhealth.clases.User
import com.example.animalhealth.clases.Utilities
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.snapshots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.CountDownLatch

class MensajeActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var lista: MutableList<Mensaje>
    private lateinit var db_ref: DatabaseReference
    private lateinit var mensaje_enviado: EditText
    private lateinit var boton_enviar: Button
    private lateinit var user_actual: User
    private var last_pos: Int = 0
    private var chat : Chat? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mensaje)
        CoroutineScope(Dispatchers.Main).launch {
            db_ref = Firebase.database.reference
            user_actual = Utilities.obtainUser(db_ref)!!
            last_pos = intent.getIntExtra("LAST_POS", 100000)
            Log.d("LASTTT_POS_LLEGAMOS", last_pos.toString())
            lista = mutableListOf()
            mensaje_enviado = findViewById(R.id.texto_mensaje)
            boton_enviar = findViewById(R.id.boton_enviar)
            var userId = intent.getStringExtra("userId")

            boton_enviar.setOnClickListener {
                last_pos = 1
                val mensaje = mensaje_enviado.text.toString().trim()

                if (mensaje.trim() != "") {
                    val hoy: Calendar = Calendar.getInstance()
                    val formateador: SimpleDateFormat = SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                    val fecha_hora = formateador.format(hoy.getTime());

                    val id_mensaje = db_ref.child("chat").child("mensajes").push().key!!
                    val nuevo_mensaje = Mensaje(
                        id_mensaje,
                        user_actual.id,
                        userId,
                        user_actual.img,
                        mensaje,
                        fecha_hora
                    )
                    db_ref.child("Users").child(user_actual.id).child("Chats").child(userId!!)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                chat = snapshot.getValue(Chat::class.java)
                                if (chat == null) {
                                    var id = db_ref.child("Users").child(user_actual.id).child("Chats").push().key
                                    chat = Chat(id!!,userId, user_actual.img, user_actual.name)
                                    db_ref.child("Users").child(userId!!).child("Chats").setValue(chat)
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {
                                println(error.message)
                            }
                        })

                  mensaje_enviado.setText("")
                } else {
                    Toast.makeText(applicationContext, "Escribe algo", Toast.LENGTH_SHORT).show()
                }
            }



            db_ref.child("chat").child("mensajes")
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        GlobalScope.launch(Dispatchers.IO) {
                            val pojo_mensaje = snapshot.getValue(Mensaje::class.java)
                            pojo_mensaje!!.id_receptor = user_actual.id
                            if (pojo_mensaje.id_receptor == pojo_mensaje.id_emisor) {
                                pojo_mensaje.imagen_emisor = user_actual.img
                            } else {

                                var semaforo = CountDownLatch(1)


                                db_ref.child("liga").child("clubs").child(pojo_mensaje.id_emisor!!)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val user = snapshot.getValue(User::class.java)
                                            pojo_mensaje.imagen_emisor = user!!.img
                                            semaforo.countDown()
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            println(error.message)
                                        }
                                    })
                                semaforo.await()
                            }

                            runOnUiThread {
                                lista.add(pojo_mensaje)
                                lista.sortBy { it.fecha_hora }
                                recycler.adapter!!.notifyDataSetChanged()
                                if (last_pos < lista.size && last_pos != 1 && last_pos != 100000) {
                                    recycler.scrollToPosition((last_pos))
                                } else {
                                    recycler.scrollToPosition((lista.size - 1))
                                }


                            }
                        }

                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {

                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {

                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })



            recycler = findViewById(R.id.rview_mensajes)
            recycler.adapter = MensajeAdaptador(lista, last_pos)
            recycler.layoutManager = LinearLayoutManager(applicationContext)
            recycler.setHasFixedSize(true)


        }
    }
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        finish()
        val actividad : Intent
        if (user_actual.type == "Veterinario") {
            actividad = Intent(applicationContext, VetMainActivity::class.java)
        }else{
            actividad = Intent(applicationContext, ClientMainActivity::class.java)

        }
        last_pos = lista.size
        actividad.putExtra("LAST_POS", last_pos)
        Log.d("LASTTT_POS_ATRAS",last_pos.toString())
        startActivity (actividad)
    }
}