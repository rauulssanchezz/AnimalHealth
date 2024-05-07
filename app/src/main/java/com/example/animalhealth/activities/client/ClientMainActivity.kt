package com.example.animalhealth.activities.client

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.animalhealth.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView

class ClientMainActivity : AppCompatActivity() {
   private lateinit var fusedLocationClient: FusedLocationProviderClient
   private lateinit var sharedPreferences: SharedPreferences
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
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Ubicación", "Permisos de ubicación otorgados")
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Aquí tienes la ubicación actual
                    Log.d("Ubicación", "Ubicación obtenida")
                        val latitud = location?.latitude
                        val longitud = location?.longitude
                        Log.d("Ubicación", "Latitud: $latitud, Longitud: $longitud")
                        // Usa la latitud y longitud aquí según sea necesario
                        sharedPreferences.edit().putString("latitud", latitud.toString()).apply()
                        sharedPreferences.edit().putString("longitud", longitud.toString()).apply()
                }
        } else {
            // Si los permisos de ubicación no están otorgados, solicítalos al usuario
            // (debes manejar esto según tu lógica de la aplicación)
            alertDialog(this,"Para poder usar la aplicación es necesario que acepte los permisos de ubicación","Permisos de ubicación")
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        alertDialog(this,"¿Esta seguro que desea salir de la aplicación?","Salir de AnimalHealth")
    }

    private fun alertDialog(context: Context,message:String,title: String) {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("Aceptar") { dialog, which ->
                // Acción cuando se hace clic en el botón Aceptar
                if (title=="Salir de AnimalHealth") {
                    finishAffinity()
                }else{
                    openAppSettings()
                }
            }
            setNegativeButton("Cancelar") { dialog, which ->
                // Acción cuando se hace clic en el botón Cancelar
                dialog.dismiss()
                if (title=="Permisos de ubicación") {
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
}