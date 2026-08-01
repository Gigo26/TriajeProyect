package com.moviles.triaje.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.moviles.triaje.model.Sintoma

class BasicQuestionsViewModel : ViewModel() {

    // Conservamos la lista de síntomas que arrastramos desde la pantalla anterior
    private val _sintomasRecuperados = MutableLiveData<List<Sintoma>>()
    val sintomasRecuperados: LiveData<List<Sintoma>> get() = _sintomasRecuperados

    // Estados de las respuestas (-1: No seleccionado, 0: Sí, 1: No)
    var respuestaConsciente: Int = -1
    var respuestaRespira: Int = -1
    var respuestaSangrado: Int = -1

    fun guardarSintomas(lista: List<Sintoma>) {
        _sintomasRecuperados.value = lista
    }

    /**
     * FLUJO 3: Evalúa si va directo al resultado con PRIORIDAD ROJA.
     * Retorna TRUE si está Inconsciente ("No"), No respira normalmente ("No") O tiene Sangrado ("Sí").
     */
    fun evaluarCriterioEmergencia(): Boolean {
        val estaInconsciente = (respuestaConsciente == 1) // Seleccionó "No"
        val noRespiraNormal = (respuestaRespira == 0)      // Seleccionó "Si"
        val tieneSangrado = (respuestaSangrado == 0)       // Seleccionó "Sí"

        return estaInconsciente || noRespiraNormal || tieneSangrado
    }

    /**
     * Determina el camino de los pacientes estables.
     * Retorna TRUE si al menos un síntoma seleccionado requiere captura fotográfica (Ej: Heridas, erupciones).
     * Retorna FALSE si todos los síntomas son puramente internos (Ej: Fiebre, dolor de garganta).
     */
    fun requiereAnalisisVisual(): Boolean {
        return _sintomasRecuperados.value.orEmpty().any { it.sin_requiere_imagen }
    }

    fun validarPreguntasCompletas(): Boolean {
        return respuestaConsciente != -1 && respuestaRespira != -1 && respuestaSangrado != -1
    }
}