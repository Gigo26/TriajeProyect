package com.moviles.triaje.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.moviles.triaje.model.Pregunta
import com.moviles.triaje.model.Sintoma
import com.moviles.triaje.network.Callback
import com.moviles.triaje.network.FirestoreService

class SymptompsQuestionsViewModel : ViewModel() {

    private val firestoreService = FirestoreService()

    private val _listaPreguntas = MutableLiveData<List<Pregunta>>(emptyList())
    val listaPreguntas: LiveData<List<Pregunta>> get() = _listaPreguntas

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun cargarPreguntasPorSintomas(sintomas: List<Sintoma>) {
        if (sintomas.isEmpty()) {
            _listaPreguntas.value = emptyList()
            return
        }

        _isLoading.value = true
        val preguntasConsolidadas = mutableListOf<Pregunta>()
        var peticionesCompletadas = 0

        // Recorremos cada síntoma seleccionado para traer sus preguntas de su subcolección
        sintomas.forEach { sintoma ->
            firestoreService.obtenerPreguntasPorSintoma(sintoma.id, object : Callback<List<Pregunta>> {
                override fun onSuccess(result: List<Pregunta>?) {
                    if (result != null) {
                        preguntasConsolidadas.addAll(result)
                    }

                    peticionesCompletadas++
                    // Cuando todas las consultas asíncronas terminen, publicamos el resultado
                    if (peticionesCompletadas == sintomas.size) {
                        _listaPreguntas.value = preguntasConsolidadas
                        _isLoading.value = false
                    }
                }

                override fun onFailed(exception: Exception) {
                    peticionesCompletadas++
                    _error.value = "Error al cargar preguntas: ${exception.message}"
                    if (peticionesCompletadas == sintomas.size) {
                        _isLoading.value = false
                    }
                }
            })
        }
    }

    fun actualizarRespuestaPregunta(preguntaId: Int, opcionIndex: Int) {
        val listaActual = _listaPreguntas.value.orEmpty().map { pregunta ->
            if (pregunta.id == preguntaId) {
                pregunta.copy(selectedOptionIndex = opcionIndex)
            } else {
                pregunta
            }
        }
        _listaPreguntas.value = listaActual
    }

    fun verificarPreguntasCompletas(): Boolean {
        return _listaPreguntas.value.orEmpty().all { it.selectedOptionIndex != -1 }
    }
}