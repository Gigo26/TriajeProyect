package com.moviles.triaje.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.moviles.triaje.model.DecisionContext
import com.moviles.triaje.model.Pregunta
import com.moviles.triaje.model.Sintoma
import com.moviles.triaje.utils.EvolutionaryTriageEngine
import com.moviles.triaje.utils.ResultadoEvolutivo

class SharedTriageViewModel : ViewModel() {

    // 1. Datos de Entrada (Estado del Triaje)
    private val _tipoPaciente = MutableLiveData<String>("ADULTO")
    val tipoPaciente: LiveData<String> get() = _tipoPaciente

    private val _respuestasBasicas = MutableLiveData<List<Boolean>>(listOf(true, true, false))
    val respuestasBasicas: LiveData<List<Boolean>> get() = _respuestasBasicas

    private val _sintomasSeleccionados = MutableLiveData<List<Sintoma>>(emptyList())
    val sintomasSeleccionados: LiveData<List<Sintoma>> get() = _sintomasSeleccionados

    private val _claseIA = MutableLiveData<String?>(null)
    val claseIA: LiveData<String?> get() = _claseIA

    private val _uriImagen = MutableLiveData<String?>(null)
    val uriImagen: LiveData<String?> get() = _uriImagen

    private val _preguntasDinamicas = MutableLiveData<List<Pregunta>>(emptyList())
    val preguntasDinamicas: LiveData<List<Pregunta>> get() = _preguntasDinamicas

    // 2. Resultado de la Optimización (Fuente de Verdad Única)
    private val _resultadoEvolutivo = MutableLiveData<ResultadoEvolutivo?>()
    val resultadoEvolutivo: LiveData<ResultadoEvolutivo?> get() = _resultadoEvolutivo

    // 3. Setters de Estado
    fun setTipoPaciente(tipo: String) {
        _tipoPaciente.value = tipo
    }

    fun setRespuestasBasicas(respuestas: List<Boolean>) {
        _respuestasBasicas.value = respuestas
    }

    fun setSintomas(lista: List<Sintoma>) {
        _sintomasSeleccionados.value = lista
    }

    fun setIAData(clase: String?, uri: String?) {
        _claseIA.value = clase
        _uriImagen.value = uri
    }

    fun setPreguntasDinamicas(lista: List<Pregunta>) {
        _preguntasDinamicas.value = lista
    }

    /**
     * Ejecuta el Motor Evolutivo una sola vez y guarda el resultado.
     */
    fun ejecutarEvaluacion() {
        val context = DecisionContext(
            tipoPaciente = _tipoPaciente.value ?: "ADULTO",
            respuestasBasicas = _respuestasBasicas.value ?: listOf(true, true, false),
            sintomas = _sintomasSeleccionados.value ?: emptyList(),
            claseIA = _claseIA.value,
            uriImagen = _uriImagen.value,
            preguntasDinamicas = _preguntasDinamicas.value ?: emptyList()
        )
        
        val resultado = EvolutionaryTriageEngine.evaluarTriajeEvolutivo(context)
        _resultadoEvolutivo.value = resultado
    }

    /**
     * Limpia todos los datos para iniciar una nueva consulta.
     */
    fun limpiarTodo() {
        _tipoPaciente.value = "ADULTO"
        _respuestasBasicas.value = listOf(true, true, false)
        _sintomasSeleccionados.value = emptyList()
        _claseIA.value = null
        _uriImagen.value = null
        _preguntasDinamicas.value = emptyList()
        _resultadoEvolutivo.value = null
    }
}
