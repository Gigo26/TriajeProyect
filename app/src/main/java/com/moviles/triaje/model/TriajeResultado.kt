package com.moviles.triaje.model

import java.io.Serializable

data class TriajeResultado(
    val prioridad: Prioridad,
    val posibleDiagnostico: String,
    val recomendaciones: List<Recomendacion>
) : Serializable