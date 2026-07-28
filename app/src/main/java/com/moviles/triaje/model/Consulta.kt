package com.moviles.triaje.model

import java.io.Serializable
import java.util.Date

data class Consulta (
    val con_tipo_paciente: String = "",
    val con_sintomas_seleccionados: List<String> = emptyList(),
    val con_respuestas_basicas: Map<String, Any> = emptyMap(),
    val con_url_imagen_evidencia: String? = null,
    val con_cuestionario_avanzado: Map<String, String> = emptyMap(),
    val con_resultado: Resultado = Resultado(),
    val con_recomendaciones: List<Recomendacion> = emptyList(),
    val con_fecha_registro: Date = Date()
) : Serializable