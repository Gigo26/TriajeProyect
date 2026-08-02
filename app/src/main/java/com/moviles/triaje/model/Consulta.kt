package com.moviles.triaje.model

import java.io.Serializable
import java.util.Date

/**
 * Modelo optimizado para persistencia en Firestore (Subcolección "historial")
 */
data class Consulta (
    val tipo_paciente: String = "",
    val prioridad: String = "", // "ROJO", "NARANJA", "VERDE", etc.
    val sintomas: List<String> = emptyList(),
    val respuestas: Map<String, String> = emptyMap(),
    val resultado_titulo: String = "",
    val resultado_descripcion: String = "",
    val url_imagen_evidencia: String? = null,
    val recomendaciones: Map<String, Map<String, Any>> = emptyMap(), // Step -> {titulo, descripcion}
    val fecha_registro: Date = Date()
) : Serializable
