package com.moviles.triaje.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.moviles.triaje.network.Callback
import com.moviles.triaje.network.FirestoreService

class InfoAppViewModel : ViewModel() {

    private val firestoreService = FirestoreService()

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String> get() = _versionApp

    // NUEVO: Variables para Términos y Políticas
    private val _terminosApp = MutableLiveData<String>()
    val terminosApp: LiveData<String> get() = _terminosApp

    private val _politicasApp = MutableLiveData<String>()
    val politicasApp: LiveData<String> get() = _politicasApp

    fun cargarDatosConfiguracion() {
        firestoreService.obtenerUtilidadesComunitarias(object : Callback<Map<String, Any>> {
            override fun onSuccess(result: Map<String, Any>?) {
                if (result != null) {
                    val version = result["version"]?.toString() ?: "1.0.0"
                    _versionApp.value = version

                    // NUEVO: Extraemos los textos de Firestore (asegúrate de que los nombres coincidan con tu BD)
                    val terminos = result["terminos_condiciones"]?.toString() ?: "Cargando términos..."
                    _terminosApp.value = terminos

                    val politicas = result["politica_privacidad"]?.toString() ?: "Cargando políticas..."
                    _politicasApp.value = politicas
                }
            }

            override fun onFailed(exception: Exception) {
                _versionApp.value = "1.0.0"
                _terminosApp.value = "No se pudieron cargar los términos."
                _politicasApp.value = "No se pudieron cargar las políticas."
            }
        })
    }
}