package com.moviles.triaje.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.moviles.triaje.network.Callback
import com.moviles.triaje.network.FirestoreService
import com.moviles.triaje.utils.PreferenceManager
import com.moviles.triaje.utils.TranslationManager

class InfoAppViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreService = FirestoreService()
    private val prefManager = PreferenceManager(application)

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String> get() = _versionApp

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

                    val isEnglish = prefManager.language == "en"

                    // Lógica para elegir el campo en español o inglés de Firestore
                    val terminosKey = if (isEnglish) "terminos_condiciones_en" else "terminos_condiciones"
                    val politicasKey = if (isEnglish) "politica_privacidad_en" else "politica_privacidad"

                    val terminos = result[terminosKey]?.toString() 
                        ?: result["terminos_condiciones"]?.toString() 
                        ?: "..."
                    
                    val politicas = result[politicasKey]?.toString() 
                        ?: result["politica_privacidad"]?.toString() 
                        ?: "..."

                    // Si es inglés y el campo descargado está en español (porque no existía el campo _en), traducimos
                    if (isEnglish && result[terminosKey] == null) {
                        traducirPorParrafos(terminos) { translated ->
                            _terminosApp.value = translated
                        }
                    } else {
                        _terminosApp.value = terminos
                    }

                    if (isEnglish && result[politicasKey] == null) {
                        traducirPorParrafos(politicas) { translated ->
                            _politicasApp.value = translated
                        }
                    } else {
                        _politicasApp.value = politicas
                    }
                }
            }

            override fun onFailed(exception: Exception) {
                _versionApp.value = "1.0.0"
                _terminosApp.value = "Error"
                _politicasApp.value = "Error"
            }
        })
    }

    /**
     * Divide el texto en párrafos (usando \n, \\n o //n) ANTES de traducir.
     * Esto evita que el traductor rompa la estructura del documento.
     */
    private fun traducirPorParrafos(textoCrudo: String, onFinalizado: (String) -> Unit) {
        // 1. Estandarizamos los saltos de línea para poder separar
        val parrafos = textoCrudo
            .replace("\\\\n", "\n")
            .replace("\\n", "\n")
            .replace("//n", "\n")
            .split("\n")
            .filter { it.isNotBlank() }

        if (parrafos.isEmpty()) {
            onFinalizado(textoCrudo)
            return
        }

        // Usamos un array con tamaño fijo para garantizar el orden de los párrafos
        val resultadosTraducidos = arrayOfNulls<String>(parrafos.size)
        var peticionesCompletadas = 0

        parrafos.forEachIndexed { index, parrafo ->
            TranslationManager.translate(parrafo) { trad ->
                resultadosTraducidos[index] = trad 
                peticionesCompletadas++

                if (peticionesCompletadas == parrafos.size) {
                    // Unimos todo con saltos de línea reales al final (filtrando nulos por seguridad)
                    onFinalizado(resultadosTraducidos.filterNotNull().joinToString("\n\n"))
                }
            }
        }
    }
}
