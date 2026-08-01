package com.moviles.triaje.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.moviles.triaje.model.Hospital
import com.moviles.triaje.network.Callback
import com.moviles.triaje.network.FirestoreService

class HospitalesViewModel : ViewModel() {

    private val firestoreService = FirestoreService()

    private val _listaHospitales = MutableLiveData<List<Hospital>>(emptyList())
    val listaHospitales: LiveData<List<Hospital>> get() = _listaHospitales

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun cargarHospitales() {
        _isLoading.value = true
        firestoreService.obtenerHospitales(object : Callback<List<Hospital>> {
            override fun onSuccess(result: List<Hospital>?) {
                _isLoading.value = false
                if (!result.isNullOrEmpty()) {
                    _listaHospitales.value = result!!
                } else {
                    // Datos de ejemplo predeterminados iguales a los de la imagen
                    _listaHospitales.value = obtenerHospitalesEjemplo()
                }
            }

            override fun onFailed(exception: Exception) {
                _isLoading.value = false
                _error.value = "Error al cargar hospitales: ${exception.message}"
                // Cargar fallback en caso de error de conexión/permisos iniciales
                _listaHospitales.value = obtenerHospitalesEjemplo()
            }
        })
    }

    private fun obtenerHospitalesEjemplo(): List<Hospital> {
        return listOf(
            Hospital(
                id = "1",
                nombre = "Hospital Regional",
                distancia = "2.3 KM",
                direccion = "Av. Principal 123",
                tiempoEstimado = "30 min",
                telefono = "014567890"
            ),
            Hospital(
                id = "2",
                nombre = "Centro de Salud\nSan Pedro",
                distancia = "1.1 KM",
                direccion = "Jr. Los Pinos 456",
                tiempoEstimado = "20 min",
                telefono = "019876543"
            ),
            Hospital(
                id = "3",
                nombre = "Puesto de Salud\nVilla Esperanza",
                distancia = "2.8 KM",
                direccion = "Av. Principal 123",
                tiempoEstimado = "25 min",
                telefono = "106"
            )
        )
    }
}
