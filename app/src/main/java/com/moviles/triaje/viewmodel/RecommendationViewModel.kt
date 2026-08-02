package com.moviles.triaje.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.moviles.triaje.model.Consulta
import com.moviles.triaje.model.DecisionContext
import com.moviles.triaje.utils.TriajeEvaluator

/**
 * ViewModel desacoplado de la lógica de evaluación.
 * Expone un estado inmutable a la vista.
 */
class RecommendationViewModel : ViewModel() {

    private val _consultaFinal = MutableLiveData<Consulta>()
    val consultaFinal: LiveData<Consulta> get() = _consultaFinal

    fun processTriage(context: DecisionContext) {
        // Delegamos la lógica pesada al Evaluator (Clean Architecture)
        val result = TriajeEvaluator.evaluate(context)
        _consultaFinal.value = result
    }

    fun saveTriageToHistory() {
        val currentConsulta = _consultaFinal.value ?: return
        // Aquí se llamaría al FirestoreService para persistir los datos
        // Por ahora simulamos éxito para el flujo de UI
    }
}
