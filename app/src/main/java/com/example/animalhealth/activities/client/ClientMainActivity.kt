package com.example.animalhealth.activities.client

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.animalhealth.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView

class ClientMainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sharedPreferences: SharedPreferences
    private val REQUEST_CHECK_SETTINGS = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_main)

        val navHostFragment=
            supportFragmentManager.findFragmentById(R.id.client_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.client_bottom_navigation_view)
        bottomNavigationView.setupWithNavController(navController)

        sharedPreferences = getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        Log.d("Ubicación", "Obteniendo ubicación")
        obtenerUbicacion()
    }


    private fun obtenerUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Ubicación", "Permisos de ubicación otorgados")
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

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            // Configuración de ubicación satisfecha, obtener la última ubicación conocida
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@addOnSuccessListener
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Aquí tienes la ubicación actual
                    Log.d("Ubicación", "Ubicación obtenida")
                    val latitud = location?.latitude
                    val longitud = location?.longitude
                    Log.d("Ubicación", "Latitud: $latitud, Longitud: $longitud")
                    // Usa la latitud y longitud aquí según sea necesario
                    if (latitud != null && longitud != null) {
                        sharedPreferences.edit().putString("latitud", latitud.toString()).apply()
                        sharedPreferences.edit().putString("longitud", longitud.toString()).apply()
                    }
                }
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // La configuración de ubicación no está satisfecha. Pero esto puede ser corregido
                // mostrando un diálogo al usuario.
                try {
                    exception.startResolutionForResult(
                        this@ClientMainActivity,
                        REQUEST_CHECK_SETTINGS
                    )
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
                // Acción cuando se hace clic en el botón Aceptar
                when (title) {
                    "Salir de AnimalHealth" -> finishAffinity()
                    "Permisos de ubicación" -> openAppSettings()
                    "Error de ubicación" -> recreate()
                }
            }
            setNegativeButton("Cancelar") { dialog, which ->
                // Acción cuando se hace clic en el botón Cancelar
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
        // Abre la configuración de la aplicación para que el usuario pueda activar los permisos
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            when (resultCode) {
                RESULT_OK -> {
                    // El usuario ha activado la configuración de ubicación
                    obtenerUbicacion()
                }
                RESULT_CANCELED -> {
                    // El usuario ha rechazado la activación de la configuración de ubicación
                    alertDialog(this, "Para usar la aplicación es necesario activar la ubicación", "Error de ubicación")
                }
            }
        }
    }
}