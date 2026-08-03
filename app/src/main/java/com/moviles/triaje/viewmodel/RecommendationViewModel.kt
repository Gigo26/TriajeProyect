package com.moviles.triaje.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.moviles.triaje.model.Consulta
import com.moviles.triaje.model.Pregunta
import com.moviles.triaje.model.Recomendacion
import com.moviles.triaje.model.Sintoma
import com.moviles.triaje.network.Callback
import com.moviles.triaje.network.FirestoreService
import com.moviles.triaje.utils.ResultadoEvolutivo
import java.util.Date

class RecommendationViewModel : ViewModel() {

    private val firestoreService = FirestoreService()
    private val auth = FirebaseAuth.getInstance()

    private val _consultaFinal = MutableLiveData<Consulta>()
    val consultaFinal: LiveData<Consulta> get() = _consultaFinal

    // Control para evitar duplicidad de guardado
    private var isAlreadySaved = false

    // Nueva LiveData para que la UI obtenga la lista fácilmente
    private val _listaRecomendacionesUI = MutableLiveData<List<Recomendacion>>()
    val listaRecomendacionesUI: LiveData<List<Recomendacion>> get() = _listaRecomendacionesUI

    // ==============================================================
    // NUEVA FUNCIÓN: Habilita el guardado para una nueva consulta
    // ==============================================================
    fun prepararNuevoGuardado() {
        isAlreadySaved = false
    }

    /**
     * Consolida la consulta final usando los datos del Shared ViewModel y el resultado ya calculado.
     */
    fun consolidarConsulta(
        tipoPaciente: String,
        sintomas: List<Sintoma>,
        respuestasBasicas: List<Boolean>,
        uriImagen: String?,
        preguntasDinamicas: List<Pregunta>,
        resultadoEvolutivo: ResultadoEvolutivo
    ) {
        // 0. Guardar lista para la UI
        _listaRecomendacionesUI.value = resultadoEvolutivo.recomendaciones

        // 1. Mapear Respuestas Clínicas (Transformando las preguntas en estados médicos)
        val mapaRespuestas = mutableMapOf<String, String>()

        // Mapeo profesional en lugar de preguntas literales
        mapaRespuestas["Estado de Consciencia"] = if (respuestasBasicas.getOrElse(0) { true }) "Alerta / Consciente" else "Inconsciente"
        mapaRespuestas["Patrón Respiratorio"] = if (respuestasBasicas.getOrElse(1) { true }) "Normal" else "Dificultad Severa / Ausente"
        mapaRespuestas["Hemorragia Activa"] = if (respuestasBasicas.getOrElse(2) { false }) "Presente (Grave)" else "No presenta"

        // Mapear el resto de preguntas dinámicas
        preguntasDinamicas.forEach {
            mapaRespuestas[it.text] = it.options.getOrNull(it.selectedOptionIndex) ?: "N/A"
        }

        // 2. Mapear Recomendaciones por pasos para Firebase (Step -> {titulo, descripcion})
        val mapaRecomendaciones = resultadoEvolutivo.recomendaciones.associate { rec ->
            rec.step.toString() to mapOf(
                "titulo" to rec.titulo,
                "descripcion" to rec.recomendacion
            )
        }

        // 3. Formatear Tipo de Paciente de forma amigable
        val tipoFormateado = when(tipoPaciente) {
            "NINO" -> "Niño"
            "GESTANTE" -> "Gestante"
            "ADULTO_MAYOR" -> "Adulto Mayor"
            else -> "Adulto"
        }

        val consulta = Consulta(
            tipo_paciente = tipoFormateado,
            prioridad = resultadoEvolutivo.prioridad.nombre,
            sintomas = sintomas.map { it.sin_description },
            respuestas = mapaRespuestas,
            resultado_titulo = resultadoEvolutivo.diagnosticoProbable,
            resultado_descripcion = resultadoEvolutivo.explicacionRiesgo,
            url_imagen_evidencia = uriImagen,
            recomendaciones = mapaRecomendaciones,
            fecha_registro = Date()
        )

        _consultaFinal.value = consulta
    }

    /**
     * Persiste la consulta actual en la subcolección "historial" del usuario en Firestore.
     * AHORA DEVUELVE BOOLEAN (Éxito) y STRING (Mensaje de estado).
     */
    fun saveTriageToHistory(onComplete: (Boolean, String) -> Unit) {
        if (isAlreadySaved) {
            onComplete(true, "Ignorado: La consulta ya estaba guardada en esta sesión.")
            return
        }

        val currentConsulta = _consultaFinal.value
        val uidUsuario = auth.currentUser?.uid

        if (uidUsuario.isNullOrEmpty()) {
            onComplete(false, "El UID de Firebase Auth es nulo. El usuario no está logueado.")
            return
        }

        if (currentConsulta != null) {
            firestoreService.guardarConsultaEnHistorial(uidUsuario, currentConsulta, object : Callback<Boolean> {
                override fun onSuccess(result: Boolean?) {
                    isAlreadySaved = true
                    onComplete(true, "Historial guardado exitosamente en Firestore.")
                }

                override fun onFailed(exception: Exception) {
                    onComplete(false, "FirestoreService devolvió un error: ${exception.message}")
                }
            })
        } else {
            onComplete(false, "El objeto ConsultaFinal está vacío y no se puede guardar.")
        }
    }
}