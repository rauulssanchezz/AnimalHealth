package com.example.animalhealth.activities.common
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.animalhealth.R
import com.example.animalhealth.activities.VetMainActivity
import com.example.animalhealth.activities.client.ClientMainActivity
import com.example.animalhealth.adapters.MensajeAdaptador
import com.example.animalhealth.clases.Chat
import com.example.animalhealth.clases.Mensaje
import com.example.animalhealth.clases.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MensajeActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var lista: MutableList<Mensaje>
    private lateinit var db_ref: DatabaseReference
    private lateinit var mensaje_enviado: EditText
    private lateinit var boton_enviar: Button
    private var userActual: User? = null
    private var last_pos: Int = 0
    private var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mensaje)
        db_ref = FirebaseDatabase.getInstance().reference
        userId = intent.getStringExtra("userId") ?: ""
        last_pos = intent.getIntExtra("LAST_POS", 100000)
        Log.d("LASTTT_POS_LLEGAMOS", last_pos.toString())

        lista = mutableListOf()
        mensaje_enviado = findViewById(R.id.texto_mensaje)
        boton_enviar = findViewById(R.id.boton_enviar)

        fetchCurrentUser()

        boton_enviar.setOnClickListener {
            last_pos = 1
            val mensaje = mensaje_enviado.text.toString().trim()

            if (mensaje.isNotEmpty()) {
                enviarMensaje(mensaje)
            } else {
                Toast.makeText(applicationContext, "Escribe algo", Toast.LENGTH_SHORT).show()
            }
        }

        recycler = findViewById(R.id.rview_mensajes)
        recycler.adapter = MensajeAdaptador(lista, last_pos)
        recycler.layoutManager = LinearLayoutManager(applicationContext)
        recycler.setHasFixedSize(true)
    }

    private fun enviarMensaje(mensaje: String) {
        if (userActual == null) {
            Toast.makeText(applicationContext, "User data not loaded yet", Toast.LENGTH_SHORT).show()
            return
        }

        val hoy: Calendar = Calendar.getInstance()
        val formateador = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val fecha_hora = formateador.format(hoy.time)

        if (userId.isEmpty()) {
            val id_mensaje = db_ref.child("Users").child("ChatPublico").child("Mensajes").push().key!!
            val nuevo_mensaje = Mensaje(
                id_mensaje,
                userActual?.id ?: "",
                userActual?.name ?: "",
                userId,
                userActual?.img ?: "",
                mensaje,
                fecha_hora
            )
            db_ref.child("Users").child("ChatPublico").child("Mensajes").child(id_mensaje).setValue(nuevo_mensaje)
        } else {
            lifecycleScope.launch {
                var chat = getChat(db_ref, userId)
                if (chat == null) {
                    var id_chat = userId
                    val user = getUser(userId)
                    var nuevo_chat = Chat(id_chat, userId, user!!.name,user.img)
                    db_ref.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Chats").child(nuevo_chat.id).setValue(nuevo_chat).await()

                    id_chat = FirebaseAuth.getInstance().currentUser!!.uid
                    nuevo_chat = Chat(id_chat, FirebaseAuth.getInstance().currentUser!!.uid, userActual!!.name,userActual!!.img)
                    db_ref.child("Users").child(userId).child("Chats").child(nuevo_chat.id).setValue(nuevo_chat).await()
                    chat = getChat(db_ref, userId)
                }
                val id_mensaje = db_ref.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Chats").child(chat!!.id).child("Mensajes").push().key!!
                val nuevo_mensaje = Mensaje(
                    id_mensaje,
                    userActual?.id ?: "",
                    userActual?.name ?: "",
                    userId,
                    userActual?.img ?: "",
                    mensaje,
                    fecha_hora
                )
                db_ref.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Chats").child(userId).child("Mensajes").child(id_mensaje).setValue(nuevo_mensaje)
                db_ref.child("Users").child(userId).child("Chats").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Mensajes").child(id_mensaje).setValue(nuevo_mensaje)
            }
        }
        mensaje_enviado.setText("")
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        finish()
        val actividad: Intent
        if (userActual?.type == "Veterinario") {
            actividad = Intent(applicationContext, VetMainActivity::class.java)
        } else {
            actividad = Intent(applicationContext, ClientMainActivity::class.java)
        }
        last_pos = lista.size
        actividad.putExtra("LAST_POS", last_pos)
        Log.d("LASTTT_POS_ATRAS", last_pos.toString())
        startActivity(actividad)
    }

    private fun fetchCurrentUser() {
        FirebaseAuth.getInstance().currentUser?.let { currentUser ->
            db_ref.child("Users").child(currentUser.uid).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.getValue(User::class.java)?.let { user ->
                        userActual = user
                        setupMessageListener()
                    } ?: run {
                        Log.e("MensajeActivity", "User data is null")
                        finish()
                    }
                } else {
                    Log.e("MensajeActivity", "Error fetching user data", task.exception)
                    finish()
                }
            }
        } ?: run {
            Log.e("MensajeActivity", "No user is logged in")
            finish()
        }
    }

    private fun setupMessageListener() {
        val messagesRef = if (userId.isEmpty()) {
            db_ref.child("Users").child("ChatPublico").child("Mensajes")
        } else {
            db_ref.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Chats").child(userId).child("Mensajes")
        }

        messagesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val pojo_mensaje = snapshot.getValue(Mensaje::class.java)
                        if (pojo_mensaje != null && userActual != null) {
                            if (pojo_mensaje.id_receptor == pojo_mensaje.id_emisor) {
                                pojo_mensaje.imagen_emisor = userActual!!.img
                            } else {
                                val user = getUser(pojo_mensaje.id_emisor!!)
                                pojo_mensaje.imagen_emisor = user?.img
                            }
                            withContext(Dispatchers.Main) {
                                lista.add(pojo_mensaje)
                                lista.sortBy { it.fecha_hora }
                                recycler.adapter!!.notifyDataSetChanged()
                                if (last_pos < lista.size && last_pos != 1 && last_pos != 100000) {
                                    recycler.scrollToPosition(last_pos)
                                } else {
                                    recycler.scrollToPosition(lista.size - 1)
                                }
                            }
                        } else {
                            Log.e("MensajeActivity", "Mensaje is null or userActual is null for snapshot: ${snapshot.key}")
                        }
                    } catch (e: DatabaseException) {
                        Log.e("MensajeActivity", "Error converting message", e)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("MensajeActivity", "Error: ${error.message}")
            }
        })
    }

    private suspend fun getUser(userId: String): User? {
        return suspendCancellableCoroutine { continuation ->
            val userRef = db_ref.child("Users").child(userId)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
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

    private suspend fun getChat(db_ref: DatabaseReference, id: String): Chat? {
        return suspendCancellableCoroutine { continuation ->
            val chatsRef = db_ref.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Chats").child(id)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chat = snapshot.getValue(Chat::class.java)
                    continuation.resume(chat)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(error.toException())
                }
            }
            chatsRef.addListenerForSingleValueEvent(listener)

            continuation.invokeOnCancellation {
                chatsRef.removeEventListener(listener)
            }
        }
    }
}




