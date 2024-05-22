package com.example.animalhealth.activities.client

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.animalhealth.R
import com.example.animalhealth.clases.Chat
import com.example.animalhealth.clases.Estado
import com.example.animalhealth.clases.Mensaje
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.atomic.AtomicInteger

class ClientMainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sharedPreferences: SharedPreferences
    private val REQUEST_CHECK_SETTINGS = 100
    private var androidId: String = ""
    private lateinit var generator: AtomicInteger
    private lateinit var db_ref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE)
        val darkMode = sharedPreferences.getInt("Theme", AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setDefaultNightMode(darkMode)
        setContentView(R.layout.activity_client_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.client_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.client_bottom_navigation_view)
        bottomNavigationView.setupWithNavController(navController)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        obtenerUbicacion()
        createNotificationChannel()
        androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        db_ref = FirebaseDatabase.getInstance().reference
        generator = AtomicInteger(0)

        db_ref.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Chats")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val pojo = snapshot.getValue(Chat::class.java)
                    if (pojo != null) {
                        val notState = pojo.not_state
                        val chatId = pojo.id

                        if (notState != null && chatId != null) {
                            if (!notState.equals(androidId) && notState.equals(Estado.CREADO)) {
                                db_ref.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("Chats").child(chatId)
                                    .child("not_state").setValue(Estado.NOTIFICADO)
                                generateNotification(
                                    generator.incrementAndGet(),
                                    pojo,
                                    "Tienes un nuevo mensaje!",
                                    "Nuevos mensajes",
                                    this@ClientMainActivity::class.java
                                )
                            }
                        } else {
                            Log.e("ClientMainActivity", "notState or chatId is null: notState=$notState, chatId=$chatId")
                        }
                    } else {
                        Log.e("ClientMainActivity", "Chat object is null")
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    // Proporcione una implementación vacía para evitar NotImplementedError
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // Proporcione una implementación vacía para evitar NotImplementedError
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // Proporcione una implementación vacía para evitar NotImplementedError
                }

                override fun onCancelled(error: DatabaseError) {
                    // Proporcione una implementación vacía para evitar NotImplementedError
                }
            })
    }

    private fun obtenerUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            checkLocationSettings()
        } else {
            // Solicita permisos de ubicación si no están otorgados
            alertDialog(this, "Para poder usar la aplicación es necesario que acepte los permisos de ubicación", "Permisos de ubicación")
        }
    }

    private fun checkLocationSettings() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                return@addOnSuccessListener
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                val latitud = location?.latitude
                val longitud = location?.longitude
                if (latitud != null && longitud != null) {
                    sharedPreferences.edit().putString("latitud", latitud.toString()).apply()
                    sharedPreferences.edit().putString("longitud", longitud.toString()).apply()
                }
            }
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(this@ClientMainActivity, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Error al intentar solucionar la configuración de ubicación
                }
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        alertDialog(this, "¿Está seguro que desea salir de la aplicación?", "Salir de AnimalHealth")
    }

    private fun alertDialog(context: Context, message: String, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("Aceptar") { dialog, which ->
                when (title) {
                    "Salir de AnimalHealth" -> finishAffinity()
                    "Permisos de ubicación" -> openAppSettings()
                    "Error de ubicación" -> recreate()
                }
            }
            setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
                if (title == "Permisos de ubicación" || title == "Error de ubicación") {
                    finishAffinity()
                }
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            when (resultCode) {
                RESULT_OK -> obtenerUbicacion()
                RESULT_CANCELED -> alertDialog(this, "Para usar la aplicación es necesario activar la ubicación", "Error de ubicación")
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "basic_channel"
            val id = "test_channel"
            val descriptionText = "basic_notification"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(id, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun generateNotification(id: Int, pojo: Parcelable?, title: String, body: String, activity: Class<*>) {
        val intent = Intent(this, activity).apply {
            putExtra("chats", pojo)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, "test_channel")
            .setSmallIcon(R.drawable.logo_animal_health)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(id, builder.build())
        }
    }
}
