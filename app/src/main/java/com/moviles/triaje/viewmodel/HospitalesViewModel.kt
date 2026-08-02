package com.moviles.triaje.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.moviles.triaje.model.Hospital
import com.moviles.triaje.network.Callback
import com.moviles.triaje.network.FirestoreService

/**
 * ViewModel que maneja la lógica de Hospitales basándose exclusivamente en Firebase Firestore.
 * Calcula distancias en tiempo real comparando el GPS contra los datos de la BD.
 */
class HospitalesViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreService = FirestoreService()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _listaHospitales = MutableLiveData<List<Hospital>>()
    val listaHospitales: LiveData<List<Hospital>> get() = _listaHospitales

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    @SuppressLint("MissingPermission")
    fun cargarHospitalesCercanos() {
        _isLoading.value = true
        Log.d("HOSPITAL_DEBUG", "Obteniendo ubicación GPS...")

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                val miLoc = location ?: return@addOnSuccessListener run {
                    fusedLocationClient.lastLocation.addOnSuccessListener { last ->
                        if (last != null) buscarEnFirestore(LatLng(last.latitude, last.longitude))
                        else {
                            _error.value = "GPS no disponible"
                            _isLoading.value = false
                        }
                    }
                }
                buscarEnFirestore(LatLng(miLoc.latitude, miLoc.longitude))
            }
            .addOnFailureListener {
                Log.e("HOSPITAL_DEBUG", "Error GPS", it)
                _error.value = "Error al acceder al GPS"
                _isLoading.value = false
            }
    }

    private fun buscarEnFirestore(miUbicacion: LatLng) {
        Log.d("HOSPITAL_DEBUG", "Buscando hospitales en tu base de datos...")
        firestoreService.obtenerHospitales(object : Callback<List<Hospital>> {
            override fun onSuccess(result: List<Hospital>?) {
                val listaCercanos = mutableListOf<Hospital>()
                
                result?.forEach { hosp ->
                    // Validamos que el hospital tenga coordenadas válidas en la BD
                    if (hosp.hos_latitud != 0.0 && hosp.hos_longitud != 0.0) {
                        val dist = calcularDistancia(miUbicacion, LatLng(hosp.hos_latitud, hosp.hos_longitud))
                        
                        // Log para depuración manual
                        Log.d("HOSPITAL_DEBUG", "Hospital: ${hosp.hos_name} | Distancia: $dist KM")

                        // Mostramos hospitales en un rango de 20km (ajustable)
                        if (dist <= 20.0) {
                            hosp.distance = dist
                            hosp.duration = (dist * 2.5).toInt() // Estimación: 2.5 min por KM
                            listaCercanos.add(hosp)
                        }
                    }
                }

                _listaHospitales.postValue(listaCercanos.sortedBy { it.distance })
                _isLoading.postValue(false)
                
                if (listaCercanos.isEmpty()) {
                    Log.w("HOSPITAL_DEBUG", "No se encontraron hospitales a menos de 20km en la BD")
                }
            }

            override fun onFailed(exception: Exception) {
                Log.e("HOSPITAL_DEBUG", "Error al leer Firestore", exception)
                _error.postValue("Error al conectar con la base de datos")
                _isLoading.postValue(false)
            }
        })
    }

    private fun calcularDistancia(p1: LatLng, p2: LatLng): Double {
        val results = FloatArray(1)
        Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, results)
        return (results[0] / 1000).toDouble() // Metros a KM
    }

    fun toggleFavorito(hospitalName: String, esFavorito: Boolean) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestoreService.toggleHospitalFavorito(uid, hospitalName, esFavorito, object : Callback<Boolean> {
            override fun onSuccess(result: Boolean?) {
                // Éxito al actualizar favoritos
            }
            override fun onFailed(exception: Exception) {
                _error.value = "Error al actualizar favorito: ${exception.message}"
            }
        })
    }
}
